package chat.cabal.mobile.core

import android.util.Log
import chat.cabal.database.CabalDatabase
import chat.cabal.network.TcpTransport
import chat.cabal.protocol.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class SyncEngine(
    private val scope: CoroutineScope,
    private val database: CabalDatabase,
    private val transport: TcpTransport,
    private val cableCore: CableCore
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
        scope.launch {
            val now = System.currentTimeMillis() / 1000
            val request = TimeRangeRequest(
                reqId = Crypto.randomBytes(4),
                ttl = 1,
                channel = "general",
                timeStart = now - 86400,
                timeEnd = 0,
                limit = 100
            )
            transport.sendToPeer(remoteId, request.serialize())
        }
    }

    private fun handleMessage(remoteId: String, data: ByteArray) {
        if (data.isEmpty()) return
        try {
            // Heuristic to distinguish between CableMessage and CablePost
            // CableMessage: [msgType (Varint), CircuitID (4 bytes), reqId (4 bytes), ...]
            // CablePost: [publicKey (32 bytes), signature (64 bytes), payload]
            
            val isMessage = if (data.size >= 5) {
                // If bytes 1..4 are zero, it's likely a CableMessage (our implementation uses 0 for CircuitID)
                data[1].toInt() == 0 && data[2].toInt() == 0 && data[3].toInt() == 0 && data[4].toInt() == 0
            } else {
                // Too small for a post, must be a message or fragment
                true
            }

            if (isMessage) {
                val message = CableParser.parseMessage(data)
                Log.d("SyncEngine", "Received message: ${message.javaClass.simpleName} from $remoteId")
                when (message) {
                    is PostResponse -> {
                        Log.d("SyncEngine", "PostResponse contains ${message.posts.size} posts")
                        message.posts.forEach { handlePost(it) }
                    }
                    is PostRequest -> {
                        scope.launch {
                            val results = database.cabalQueries.getMessagesByHashes(message.hashes).executeAsList()
                            if (results.isNotEmpty()) {
                                val rawPosts = results.map { it.rawPost }
                                val response = PostResponse(message.reqId, rawPosts)
                                transport.sendToPeer(remoteId, response.serialize())
                            }
                        }
                    }
                    is TimeRangeRequest -> {
                        scope.launch {
                            val results = database.cabalQueries.getMessagesInRange(
                                channel = message.channel,
                                timeStart = message.timeStart,
                                timeEnd = message.timeEnd,
                                limit = message.limit.toLong()
                            ).executeAsList()
                            if (results.isNotEmpty()) {
                                val rawPosts = results.map { it.rawPost }
                                val response = PostResponse(message.reqId, rawPosts)
                                transport.sendToPeer(remoteId, response.serialize())
                            }
                        }
                    }
                    else -> Log.d("SyncEngine", "Unhandled message type from $remoteId")
                }
            } else {
                handlePost(data)
            }
        } catch (e: Exception) {
            Log.e("SyncEngine", "Failed to parse data from $remoteId: ${e.message}")
        }
    }

    private fun handlePost(data: ByteArray) {
        try {
            val post = CableParser.parsePost(data)
            Log.d("SyncEngine", "Handling post: ${post.javaClass.simpleName} (hash: ${post.hash().toHex().take(8)})")
            if (post is TextPost) {
                val displayText = try { cableCore.decryptText(post.text) } catch (_: Exception) { post.text }
                database.cabalQueries.insertMessage(
                    hash = post.hash(),
                    publicKey = post.publicKey,
                    channel = post.channel,
                    timestamp = post.timestamp,
                    text = displayText,
                    rawPost = data,
                    status = 1L
                )
            }
        } catch (e: Exception) {
            Log.e("SyncEngine", "Failed to parse post: ${e.message}")
        }
    }
    
    fun broadcastPost(post: CablePost) {
        scope.launch { transport.broadcast(post.serialize()) }
    }
}
