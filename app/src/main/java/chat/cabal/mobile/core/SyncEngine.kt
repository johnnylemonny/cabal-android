package chat.cabal.mobile.core

import android.util.Log
import chat.cabal.database.CabalDatabase
import chat.cabal.network.TcpTransport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class JsChatMessage(
    val channel: String,
    val publicKey: String,
    val hash: String,
    val post: JsPost
)

@Serializable
data class JsPost(
    val text: String? = null,
    val timestamp: Long,
    val postType: Int
)

class SyncEngine(
    private val scope: CoroutineScope,
    private val database: CabalDatabase,
    private val transport: TcpTransport,
    private val quickJs: QuickJsEngine,
    private val keyStoreManager: KeyStoreManager
) {
    init {
        quickJs.setBridges(
            network = object : QuickJsEngine.NetworkBridge {
                override fun broadcast(dataHex: String) {
                    scope.launch {
                        transport.broadcast(dataHex.decodeHex())
                    }
                }
            },
            storage = object : QuickJsEngine.StorageBridge {
                override fun get(keyHex: String): String? {
                    return try {
                        database.cabalQueries.getKV(keyHex).executeAsOneOrNull()
                    } catch (e: Exception) {
                        null
                    }
                }

                override fun put(keyHex: String, valueHex: String) {
                    try {
                        database.cabalQueries.putKV(keyHex, valueHex)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            },
            ui = object : QuickJsEngine.UIBridge {
                override fun onChatMessage(json: String) {
                    try {
                        val msg = Json { ignoreUnknownKeys = true }.decodeFromString<JsChatMessage>(json)
                        if (msg.post.postType == 0) { // TEXT_POST
                            database.cabalQueries.insertMessage(
                                hash = msg.hash.decodeHex(),
                                publicKey = msg.publicKey.decodeHex(),
                                channel = msg.channel,
                                timestamp = msg.post.timestamp,
                                text = msg.post.text ?: "",
                                rawPost = ByteArray(0), // We don't store raw bytes in this bridge for now
                                status = 1L
                            )
                        }
                    } catch (e: Exception) {
                        Log.e("SyncEngine", "Failed to parse JS message: $json")
                        e.printStackTrace()
                    }
                }
            }
        )

        // Initialize Cable in JS
        val kp = keyStoreManager.getOrCreateKeyPair()
        val pub = kp.public.encoded.takeLast(32).toByteArray().toHex()
        // Note: Formatting secret key for JS (QuickJS doesn't have Ed25519 native, so we pass it)
        // In a real app, this should be the full 64-byte secret key (seed + pub)
        val sec = kp.private.encoded.takeLast(64).toByteArray().toHex()
        quickJs.initCable(pub, sec)

        // Listen for network messages
        transport.messages.onEach { (remoteId, data) ->
            quickJs.handleIncomingData(data.toHex())
        }.launchIn(scope)
    }

    fun postText(channel: String, text: String) {
        quickJs.postText(channel, text)
    }

    /**
     * Initial sync with a new peer.
     * Logic is now handled in JS when it receives "new-peer" event if we trigger it.
     */
    fun onPeerConnected(remoteId: String) {
        Log.d("SyncEngine", "Peer connected: $remoteId")
        // We could notify JS here if needed, but CableCore usually handles swarm events.
        // For now, JS handles the protocol responses.
    }
}
