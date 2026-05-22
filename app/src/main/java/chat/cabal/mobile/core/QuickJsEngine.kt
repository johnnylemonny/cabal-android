package chat.cabal.mobile.core

import android.content.Context
import app.cash.quickjs.QuickJs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.Closeable

class QuickJsEngine(
    private val context: Context,
    private val scope: CoroutineScope
) : Closeable {
    private val quickJs = QuickJs.create()

    interface NetworkBridge {
        fun broadcast(dataHex: String)
    }

    interface StorageBridge {
        fun get(keyHex: String): String?
        fun put(keyHex: String, valueHex: String)
    }

    interface UIBridge {
        fun onChatMessage(json: String)
    }

    private var networkBridge: NetworkBridge? = null
    private var storageBridge: StorageBridge? = null
    private var uiBridge: UIBridge? = null

    init {
        setupBridges()
        loadBundle()
    }

    private fun setupBridges() {
        // Expose bridges to JS
        // Note: QuickJS-Android allows setting objects that implement interfaces
        quickJs.set("kotlinNetwork", NetworkBridge::class.java, object : NetworkBridge {
            override fun broadcast(dataHex: String) {
                networkBridge?.broadcast(dataHex)
            }
        })
        quickJs.set("kotlinStorage", StorageBridge::class.java, object : StorageBridge {
            override fun get(keyHex: String): String? = storageBridge?.get(keyHex)
            override fun put(keyHex: String, valueHex: String) {
                storageBridge?.put(keyHex, valueHex)
            }
        })
        quickJs.set("kotlinUI", UIBridge::class.java, object : UIBridge {
            override fun onChatMessage(json: String) {
                uiBridge?.onChatMessage(json)
            }
        })
    }

    private fun loadBundle() {
        try {
            val bundle = context.assets.open("cable-protocol.bundle.js").bufferedReader().use { it.readText() }
            quickJs.evaluate(bundle)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun initCable(publicKey: String, secretKey: String) {
        try {
            quickJs.evaluate("initCable('$publicKey', '$secretKey')")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun handleIncomingData(dataHex: String) {
        try {
            quickJs.evaluate("handleIncomingData('$dataHex')")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun postText(channel: String, text: String) {
        try {
            quickJs.evaluate("postText('$channel', '$text')")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setBridges(network: NetworkBridge, storage: StorageBridge, ui: UIBridge) {
        this.networkBridge = network
        this.storageBridge = storage
        this.uiBridge = ui
    }

    override fun close() {
        quickJs.close()
    }
}
