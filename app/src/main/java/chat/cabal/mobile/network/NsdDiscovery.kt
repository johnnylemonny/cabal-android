package chat.cabal.mobile.network

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log
import chat.cabal.network.PeerDiscovery
import chat.cabal.network.PeerInfo

class NsdDiscovery(private val context: Context) : PeerDiscovery {
    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val serviceType = "_cabal._tcp"
    
    private var discoveryListener: NsdManager.DiscoveryListener? = null
    private var multicastLock: WifiManager.MulticastLock? = null

    override fun startDiscovery(cabalKey: String, onPeerFound: (PeerInfo) -> Unit) {
        try {
            multicastLock = wifiManager.createMulticastLock("cabal_nsd_lock").apply {
                setReferenceCounted(true)
                acquire()
            }
        } catch (e: Exception) {
            Log.e("NsdDiscovery", "Failed to acquire multicast lock", e)
        }

        discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(regType: String) {}
            override fun onServiceFound(service: NsdServiceInfo) {
                if (service.serviceType.contains("cabal")) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                        nsdManager.registerServiceInfoCallback(service, context.mainExecutor, object : NsdManager.ServiceInfoCallback {
                            override fun onServiceInfoCallbackRegistered() {}
                            override fun onServiceInfoCallbackRegistrationFailed(errorCode: Int) {}
                            override fun onServiceInfoCallbackUnregistered() {}
                            override fun onServiceLost() {}
                            override fun onServiceUpdated(serviceInfo: NsdServiceInfo) {
                                val hostAddress = serviceInfo.hostAddresses.firstOrNull()?.hostAddress
                                if (hostAddress != null) {
                                    onPeerFound(PeerInfo(hostAddress, serviceInfo.port, cabalKey))
                                }
                                try { nsdManager.unregisterServiceInfoCallback(this) } catch (_: Exception) {}
                            }
                        })
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        nsdManager.resolveService(service, context.mainExecutor, object : NsdManager.ResolveListener {
                            override fun onResolveFailed(si: NsdServiceInfo, errorCode: Int) {}
                            override fun onServiceResolved(si: NsdServiceInfo) {
                                val hostAddress = si.hostAddresses.firstOrNull()?.hostAddress
                                if (hostAddress != null) {
                                    onPeerFound(PeerInfo(hostAddress, si.port, cabalKey))
                                }
                            }
                        })
                    } else {
                        @Suppress("DEPRECATION")
                        nsdManager.resolveService(service, object : NsdManager.ResolveListener {
                            override fun onResolveFailed(si: NsdServiceInfo, errorCode: Int) {}
                            override fun onServiceResolved(si: NsdServiceInfo) {
                                val hostAddress = si.host?.hostAddress
                                if (hostAddress != null) {
                                    onPeerFound(PeerInfo(hostAddress, si.port, cabalKey))
                                }
                            }
                        })
                    }
                }
            }
            override fun onServiceLost(service: NsdServiceInfo) {}
            override fun onDiscoveryStopped(regType: String) {}
            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {}
            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {}
        }
        nsdManager.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
    }

    override fun stopDiscovery() {
        discoveryListener?.let { try { nsdManager.stopServiceDiscovery(it) } catch (_: Exception) {} }
        discoveryListener = null
        try { multicastLock?.let { if (it.isHeld) it.release() } } catch (_: Exception) {}
    }

    override fun announce(cabalKey: String, port: Int) {
        val deviceId = (Build.MODEL.take(5) + "-" + (100..999).random()).filter { it.isLetterOrDigit() || it == '-' }
        val serviceInfo = NsdServiceInfo().apply {
            serviceName = "Cabal-$cabalKey-$deviceId"
            serviceType = this@NsdDiscovery.serviceType
            setPort(port)
        }
        nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, object : NsdManager.RegistrationListener {
            override fun onServiceRegistered(si: NsdServiceInfo) {}
            override fun onRegistrationFailed(si: NsdServiceInfo, err: Int) {}
            override fun onServiceUnregistered(si: NsdServiceInfo) {}
            override fun onUnregistrationFailed(si: NsdServiceInfo, err: Int) {}
        })
    }
}
