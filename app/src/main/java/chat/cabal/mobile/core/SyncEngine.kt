package chat.cabal.mobile.core

import android.util.Log
import chat.cabal.database.CabalDatabase
import chat.cabal.network.TcpTransport
import chat.cabal.protocol.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.nio.ByteBuffer

enum class ReplicationPolicy(val windowSize: Long, val limit: Int) {
    JOINED(86400 * 7, 500), // 7 days, 500 messages
    UNJOINED(86400, 50)     // 1 day, 50 messages
}

class SyncEngine(
    private val scope: CoroutineScope,
    private val database: CabalDatabase,
    private val transport: TcpTransport,
    private val cableCore: CableCore,
) {
    init {
        transport.messages.onEach { (remoteId, data) ->
            handleMessage(remoteId, data)
        }.launchIn(scope)

        transport.newConnections.onEach { remoteId ->
            onPeerConnected(remoteId)
        }.launchIn(scope)
    }

    fun onPeerConnected(remoteId: String) {
        scope.launch(Dispatchers.IO) {
            val allChannels = database.cabalQueries.getAllChannels().executeAsList()
            Log.i("SyncEngine", "Peer connected: $remoteId. Requesting history for ${allChannels.size} channels.")
            
            if (allChannels.isEmpty()) {
                // If no channels known, at least request 'general' with UNJOINED policy
                requestHistory(remoteId, "general", ReplicationPolicy.UNJOINED)
            } else {
                allChannels.forEach { channel ->
                    val policy = if (channel.isJoined == 1L) ReplicationPolicy.JOINED else ReplicationPolicy.UNJOINED
                    requestHistory(remoteId, channel.name, policy)
                }
            }
            
            // Also request channel list
            val channelListReq = ChannelListRequest(
                reqId = Crypto.randomBytes(4),
                ttl = 1,
                offset = 0,
                limit = 100
            )
            transport.sendToPeer(remoteId, channelListReq.serialize())
        }
    }

    private suspend fun requestHistory(remoteId: String, channel: String, policy: ReplicationPolicy) {
        if (policy.limit <= 0) return
        val now = System.currentTimeMillis() / 1000
        val request = TimeRangeRequest(
            reqId = Crypto.randomBytes(4),
            ttl = 1,
            channel = channel,
            timeStart = now - policy.windowSize,
            timeEnd = 0,
            limit = policy.limit
        )
        transport.sendToPeer(remoteId, request.serialize())
    }

    private fun handleMessage(remoteId: String, data: ByteArray) {
        if (data.isEmpty()) return
        
        var isMessage = false
        try {
            val buffer = ByteBuffer.wrap(data)
            val type = Varint.decode(buffer).toInt()
            if ((type in 0..8) && (buffer.remaining() >= 8)) {
                val c1 = buffer.get().toInt()
                val c2 = buffer.get().toInt()
                val c3 = buffer.get().toInt()
                val c4 = buffer.get().toInt()
                if (c1 == 0 && c2 == 0 && c3 == 0 && c4 == 0) {
                    isMessage = true
                }
            }
        } catch (_: Exception) {}

        try {
            if (isMessage) {
                val message = CableParser.parseMessage(data)
                Log.d("SyncEngine", "Parsed CABLE_MESSAGE: ${message.javaClass.simpleName} from $remoteId")
                when (message) {
                    is PostResponse -> {
                        Log.i("SyncEngine", "Received PostResponse from $remoteId with ${message.posts.size} items")
                        message.posts.forEach { handlePost(it) }
                    }
                    is PostRequest -> {
                        scope.launch(Dispatchers.IO) {
                            val results = database.cabalQueries.getMessagesByHashes(message.hashes).executeAsList()
                            if (results.isNotEmpty()) {
                                val response = PostResponse(message.reqId, results.map { it.rawPost })
                                transport.sendToPeer(remoteId, response.serialize())
                            }
                        }
                    }
                    is TimeRangeRequest -> {
                        scope.launch(Dispatchers.IO) {
                            val results = database.cabalQueries.getMessagesInRange(
                                channel = message.channel,
                                timeStart = message.timeStart,
                                timeEnd = message.timeEnd,
                                limit = message.limit.toLong()
                            ).executeAsList()
                            if (results.isNotEmpty()) {
                                val response = PostResponse(message.reqId, results.map { it.rawPost })
                                transport.sendToPeer(remoteId, response.serialize())
                            }
                        }
                    }
                    is ChannelListRequest -> {
                        // We don't have a ChannelListResponse model yet, but the protocol supports it
                        // For now, let's just ignore or implement it if needed
                    }
                    else -> Log.v("SyncEngine", "Ignored message type: ${message.javaClass.simpleName}")
                }
            } else {
                handlePost(data)
            }
        } catch (e: Exception) {
            Log.e("SyncEngine", "Failed to handle data from $remoteId: ${e.message}")
        }
    }

    private fun handlePost(data: ByteArray) {
        try {
            when (val post = CableParser.parsePost(data)) {
                is TextPost -> {
                    val displayText = try { cableCore.decryptText(post.text) } catch (_: Exception) { post.text }
                    database.cabalQueries.insertMessage(
                        hash = post.hash(),
                        publicKey = post.publicKey,
                        channel = post.channel,
                        timestamp = post.timestamp,
                        text = displayText,
                        rawPost = data,
                        status = 1L, // Received
                        parentHash = post.links.firstOrNull(), // Simplified reply logic
                        isEdited = 0L,
                        isDeleted = 0L,
                        ttl = 0L
                    )
                }
                is InfoPost -> {
                    val name = post.info["name"]
                    val status = post.info["status"]
                    database.cabalQueries.insertOrUpdatePeer(
                        publicKey = post.publicKey,
                        name = name,
                        status = status,
                        lastSeen = post.timestamp,
                        isIgnored = 0L,
                        isVerified = 0L,
                        role = 2L // Default User
                    )
                }
                is DeletePost -> {
                    post.links.forEach { hash ->
                        database.cabalQueries.markMessageDeleted(hash)
                    }
                }
                is TopicPost -> {
                    database.cabalQueries.updateChannelTopic(post.topic, post.channel)
                }
                is JoinPost -> {
                    database.cabalQueries.insertChannel(post.channel, null, 0L)
                }
            }
        } catch (e: Exception) {
            Log.e("SyncEngine", "Could not parse/store post: ${e.message}")
        }
    }
    
    fun broadcastPost(post: CablePost) {
        val bytes = post.serialize()
        scope.launch(Dispatchers.IO) { transport.broadcast(bytes) }
    }
}
