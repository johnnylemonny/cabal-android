package chat.cabal.mobile.core

import chat.cabal.database.CabalDatabase
import chat.cabal.database.Message
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
            val typeByte = data[0].toInt()
            if (typeByte in 0..8 && data.size < 100) {
                val message = CableParser.parseMessage(data)
                when (message) {
                    is PostResponse -> message.posts.forEach { handlePost(it) }
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
                    else -> {}
                }
            } else {
                handlePost(data)
            }
        } catch (_: Exception) {}
    }

    private fun handlePost(data: ByteArray) {
        try {
            val post = CableParser.parsePost(data)
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
        } catch (_: Exception) {}
    }
    
    fun broadcastPost(post: CablePost) {
        scope.launch { transport.broadcast(post.serialize()) }
    }
}
