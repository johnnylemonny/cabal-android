package chat.cabal.mobile.core

import android.content.Context
import app.cash.zipline.EngineApi
import app.cash.zipline.Zipline
import app.cash.zipline.ZiplineService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import java.io.Closeable

@OptIn(EngineApi::class)
class QuickJsEngine(
    private val context: Context,
    scope: CoroutineScope
) : Closeable {
    private val zipline = Zipline.create(scope.coroutineContext[CoroutineDispatcher]!!)

    interface NetworkBridge : ZiplineService {
        fun broadcast(dataHex: String)
    }

    interface StorageBridge : ZiplineService {
        fun get(keyHex: String): String?
        fun put(keyHex: String, valueHex: String)
    }

    interface UIBridge : ZiplineService {
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
        val networkProxy = object : NetworkBridge {
            override fun broadcast(dataHex: String) {
                networkBridge?.broadcast(dataHex)
            }
            override fun close() {}
        }
        val storageProxy = object : StorageBridge {
            override fun get(keyHex: String): String? = storageBridge?.get(keyHex)
            override fun put(keyHex: String, valueHex: String) {
                storageBridge?.put(keyHex, valueHex)
            }
            override fun close() {}
        }
        val uiProxy = object : UIBridge {
            override fun onChatMessage(json: String) {
                uiBridge?.onChatMessage(json)
            }
            override fun close() {}
        }

        zipline.bind<NetworkBridge>("kotlinNetworkInternal", networkProxy)
        zipline.bind<StorageBridge>("kotlinStorageInternal", storageProxy)
        zipline.bind<UIBridge>("kotlinUIInternal", uiProxy)
    }

    private fun loadBundle() {
        try {
            val bundle = context.assets.open("cable-protocol.bundle.js").bufferedReader().use { it.readText() }
            val shim = """
                var console = { log: function() {}, error: function() {}, warn: function() {} };
                var TextEncoder = function() {
                    this.encode = function(s) {
                        var a = new Uint8Array(s.length);
                        for(var i=0; i<s.length; i++) a[i]=s.charCodeAt(i);
                        return a;
                    };
                };
                var TextDecoder = function() {
                    this.decode = function(a) {
                        return String.fromCharCode.apply(null, a);
                    };
                };

                var kotlinNetwork = {
                    broadcast: function(data) {
                        try {
                            if (typeof zipline !== 'undefined') zipline.take('kotlinNetworkInternal').broadcast(data);
                        } catch(e) {}
                    }
                };
                var kotlinStorage = {
                    get: function(key) {
                        try {
                            if (typeof zipline !== 'undefined') return zipline.take('kotlinStorageInternal').get(key);
                        } catch(e) {}
                        return null;
                    },
                    put: function(key, value) {
                        try {
                            if (typeof zipline !== 'undefined') zipline.take('kotlinStorageInternal').put(key, value);
                        } catch(e) {}
                    }
                };
                var kotlinUI = {
                    onChatMessage: function(json) {
                        try {
                            if (typeof zipline !== 'undefined') zipline.take('kotlinUIInternal').onChatMessage(json);
                        } catch(e) {}
                    }
                };
            """.trimIndent()
            zipline.quickJs.evaluate(shim)
            zipline.quickJs.evaluate(bundle)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun initCable(publicKey: String, secretKey: String) {
        try {
            zipline.quickJs.evaluate("if (typeof initCable !== 'undefined') initCable('$publicKey', '$secretKey')")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun handleIncomingData(dataHex: String) {
        try {
            zipline.quickJs.evaluate("if (typeof handleIncomingData !== 'undefined') handleIncomingData('$dataHex')")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun postText(channel: String, text: String) {
        try {
            zipline.quickJs.evaluate("if (typeof postText !== 'undefined') postText('$channel', '$text')")
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
        zipline.close()
    }
}
