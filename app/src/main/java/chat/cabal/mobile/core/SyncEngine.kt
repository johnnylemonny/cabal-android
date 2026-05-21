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
        try {
            // Try parsing as Message first
            val message = try { CableParser.parseMessage(data) } catch (e: Exception) { null }
            
            if (message != null) {
                when (message) {
                    is PostResponse -> {
                        Log.d("SyncEngine", "Received ${message.posts.size} posts from $remoteId")
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
                                timestamp = message.timeStart,
                                timestamp_ = message.timeEnd,
                                _0 = message.timeEnd,
                                limit = message.limit.toLong()
                            ).executeAsList()
                            if (posts.isNotEmpty()) {
                                val response = PostResponse(message.reqId, posts.map { it.rawPost })
                                transport.sendToPeer(remoteId, response.serialize())
                            }
                        }
                    }
                    else -> {}
                }
            } else {
                // Try parsing as a raw Post
                try {
                    handlePost(data)
                } catch (e: Exception) {
                    Log.e("SyncEngine", "Failed to parse data as message or post")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun handlePost(data: ByteArray) {
        val post = CableParser.parsePost(data)
        if (post is TextPost) {
            // E2EE: Decrypt text before storing for local UI
            val decryptedText = cableCore.decryptText(post.text)
            
            database.cabalQueries.insertMessage(
                hash = post.hash(),
                publicKey = post.publicKey,
                channel = post.channel,
                timestamp = post.timestamp,
                text = decryptedText,
                rawPost = data,
                status = 1L // Received from network
            )
        }
    }
    
    fun broadcastPost(post: CablePost) {
        scope.launch {
            transport.broadcast(post.serialize())
        }
    }
}
