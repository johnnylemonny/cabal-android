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

    /**
     * Initial sync with a new peer.
     */
    fun onPeerConnected(remoteId: String) {
        scope.launch {
            // Request messages from the last 24 hours in the "general" channel
            val now = System.currentTimeMillis() / 1000
            val oneDayAgo = now - (24 * 60 * 60)
            
            val request = TimeRangeRequest(
                reqId = Crypto.randomBytes(4),
                ttl = 1,
                channel = "general",
                timeStart = oneDayAgo,
                timeEnd = 0, // 0 means "up to now"
                limit = 100
            )
            
            transport.sendToPeer(remoteId, request.serialize())
            Log.d("SyncEngine", "Requested history from $remoteId")
        }
    }

    private fun handleMessage(remoteId: String, data: ByteArray) {
        if (data.isEmpty()) return
        
        try {
            // Cable protocol logic:
            // If the first byte is part of a varint representing a message type (0-8), parse as message.
            // If data is long enough and looks like a post (starts with 32 bytes of pubkey), parse as post.
            
            val typeByte = data[0].toInt()
            Log.d("SyncEngine", "Received packet from $remoteId, first byte: $typeByte, size: ${data.size}")

            if (typeByte in 0..8 && data.size < 100) { // Messages are usually small
                val message = CableParser.parseMessage(data)
                Log.i("SyncEngine", "Parsed Message type: ${message.javaClass.simpleName}")
                
                when (message) {
                    is PostResponse -> {
                        Log.d("SyncEngine", "Received ${message.posts.size} posts in response from $remoteId")
                        message.posts.forEach { handlePost(it) }
                    }
                    is PostRequest -> {
                        scope.launch {
                            val posts = database.cabalQueries.getMessagesByHashes(message.hashes).executeAsList()
                            if (posts.isNotEmpty()) {
                                val response = PostResponse(message.reqId, posts.map { it.rawPost })
                                transport.sendToPeer(remoteId, response.serialize())
                            }
                        }
                    }
                    is TimeRangeRequest -> {
                        scope.launch {
                            val posts = database.cabalQueries.getMessagesInRange(
                                channel = message.channel,
                                timeStart = message.timeStart,
                                timeEnd = message.timeEnd,
                                limit = message.limit.toLong()
                            ).executeAsList()
                            if (posts.isNotEmpty()) {
                                val response = PostResponse(message.reqId, posts.map { it.rawPost })
                                transport.sendToPeer(remoteId, response.serialize())
                            }
                        }
                    }
                    else -> Log.w("SyncEngine", "Unhandled message type: ${message.javaClass.simpleName}")
                }
            } else {
                // Treat as raw Post (gossip/broadcast)
                handlePost(data)
            }
        } catch (_: Exception) {
            // ignore
        }
    }

    private fun handlePost(data: ByteArray) {
        try {
            val post = CableParser.parsePost(data)
            if (post is TextPost) {
                // E2EE: Decrypt text. If it fails, it might be plain text or wrong key.
                val displayText = try {
                    cableCore.decryptText(post.text)
                } catch (_: Exception) {
                    post.text // Fallback to raw text if decryption fails
                }
                
                database.cabalQueries.insertMessage(
                    hash = post.hash(),
                    publicKey = post.publicKey,
                    channel = post.channel,
                    timestamp = post.timestamp,
                    text = displayText,
                    rawPost = data,
                    status = 1L // Received from network
                )
                Log.i("SyncEngine", "Stored new TextPost from ${post.publicKey.toHex().take(8)}")
            }
        } catch (e: Exception) {
            Log.e("SyncEngine", "Failed to parse/store post: ${e.message}")
        }
    }
    
    fun broadcastPost(post: CablePost) {
        scope.launch {
            transport.broadcast(post.serialize())
        }
    }
}
