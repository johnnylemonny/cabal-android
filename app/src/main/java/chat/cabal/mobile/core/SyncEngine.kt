package chat.cabal.mobile.core

import android.util.Log
import chat.cabal.database.CabalDatabase
import chat.cabal.network.TcpTransport
import chat.cabal.protocol.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.nio.ByteBuffer

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
            Log.i("SyncEngine", "Handshaking with $remoteId (Requesting History)")
            transport.sendToPeer(remoteId, request.serialize())
        }
    }

    private fun handleMessage(remoteId: String, data: ByteArray) {
        if (data.isEmpty()) return
        
        // CableMessage: [Type (Varint), CircuitID (4 bytes 0x00), ReqID (4 bytes), ...]
        // CablePost: [PublicKey (32 bytes), Signature (64 bytes), Payload]
        
        var isMessage = false
        try {
            val buffer = ByteBuffer.wrap(data)
            val type = Varint.decode(buffer).toInt()
            if (type in 0..8 && buffer.remaining() >= 8) {
                // Check if next 4 bytes are zero (CircuitID)
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
                        scope.launch {
                            val results = database.cabalQueries.getMessagesByHashes(message.hashes).executeAsList()
                            if (results.isNotEmpty()) {
                                Log.i("SyncEngine", "Responding to PostRequest with ${results.size} items")
                                val response = PostResponse(message.reqId, results.map { it.rawPost })
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
                                Log.i("SyncEngine", "Responding to TimeRangeRequest with ${results.size} items")
                                val response = PostResponse(message.reqId, results.map { it.rawPost })
                                transport.sendToPeer(remoteId, response.serialize())
                            }
                        }
                    }
                    else -> Log.v("SyncEngine", "Ignored message type: ${message.javaClass.simpleName}")
                }
            } else {
                Log.d("SyncEngine", "Treating as direct CABLE_POST from $remoteId")
                handlePost(data)
            }
        } catch (e: Exception) {
            Log.e("SyncEngine", "Failed to handle data from $remoteId: ${e.message}")
        }
    }

    private fun handlePost(data: ByteArray) {
        try {
            val post = CableParser.parsePost(data)
            if (post is TextPost) {
                val displayText = try { cableCore.decryptText(post.text) } catch (_: Exception) { post.text }
                Log.i("SyncEngine", "Storing TextPost from ${post.publicKey.toHex().take(8)}")
                database.cabalQueries.insertMessage(
                    hash = post.hash(),
                    publicKey = post.publicKey,
                    channel = post.channel,
                    timestamp = post.timestamp,
                    text = displayText,
                    rawPost = data,
                    status = 1L // Received
                )
            }
        } catch (e: Exception) {
            Log.e("SyncEngine", "Could not parse/store post: ${e.message}")
        }
    }
    
    fun broadcastPost(post: CablePost) {
        val bytes = post.serialize()
        Log.i("SyncEngine", "Broadcasting TextPost (${bytes.size} bytes)")
        scope.launch { transport.broadcast(bytes) }
    }
}
