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
        // Essential for physical devices: allow the phone to receive multicast packets
        try {
            multicastLock = wifiManager.createMulticastLock("cabal_nsd_lock").apply {
                setReferenceCounted(true)
                acquire()
            }
            Log.d("NsdDiscovery", "Multicast lock acquired")
        } catch (e: Exception) {
            Log.e("NsdDiscovery", "Failed to acquire multicast lock", e)
        }

        discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(regType: String) {
                Log.d("NsdDiscovery", "Discovery started")
            }
            override fun onServiceFound(service: NsdServiceInfo) {
                if (service.serviceType.contains("cabal")) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                        // Android 15+ modern way
                        nsdManager.registerServiceInfoCallback(service, context.mainExecutor, object : NsdManager.ServiceInfoCallback {
                            override fun onServiceInfoCallbackRegistered() {}
                            override fun onServiceInfoCallbackRegistrationFailed(errorCode: Int) {}
                            override fun onServiceInfoCallbackUnregistered() {}
                            override fun onServiceLost() {}
                            override fun onServiceUpdated(serviceInfo: NsdServiceInfo) {
                                val hostAddress = serviceInfo.hostAddresses.firstOrNull()?.hostAddress
                                Log.d("NsdDiscovery", "Service updated/resolved: ${serviceInfo.serviceName} at $hostAddress:${serviceInfo.port}")
                                onPeerFound(
                                    PeerInfo(
                                        address = hostAddress ?: "",
                                        port = serviceInfo.port,
                                        cabalKey = cabalKey
                                    )
                                )
                                // Unregister after finding to match resolve behavior
                                try { nsdManager.unregisterServiceInfoCallback(this) } catch (_: Exception) {}
                            }
                        })
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        // API 34 specific resolve (avoiding the deprecated one without executor if possible)
                        @Suppress("DEPRECATION")
                        nsdManager.resolveService(service, context.mainExecutor, object : NsdManager.ResolveListener {
                            override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                                Log.e("NsdDiscovery", "Resolve failed: $errorCode")
                            }
                            override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                                val hostAddress = serviceInfo.hostAddresses.firstOrNull()?.hostAddress
                                Log.d("NsdDiscovery", "Service resolved: ${serviceInfo.serviceName} at $hostAddress:${serviceInfo.port}")
                                onPeerFound(
                                    PeerInfo(
                                        address = hostAddress ?: "",
                                        port = serviceInfo.port,
                                        cabalKey = cabalKey
                                    )
                                )
                            }
                        })
                    } else {
                        // Fallback for older APIs
                        @Suppress("DEPRECATION")
                        nsdManager.resolveService(service, object : NsdManager.ResolveListener {
                            override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                                Log.e("NsdDiscovery", "Resolve failed: $errorCode")
                            }
                            override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                                val hostAddress = serviceInfo.host?.hostAddress
                                Log.d("NsdDiscovery", "Service resolved: ${serviceInfo.serviceName} at $hostAddress:${serviceInfo.port}")
                                onPeerFound(
                                    PeerInfo(
                                        address = hostAddress ?: "",
                                        port = serviceInfo.port,
                                        cabalKey = cabalKey
                                    )
                                )
                            }
                        })
                    }
                }
            }
            override fun onServiceLost(service: NsdServiceInfo) {
                Log.d("NsdDiscovery", "Service lost: ${service.serviceName}")
            }
            override fun onDiscoveryStopped(regType: String) {
                Log.d("NsdDiscovery", "Discovery stopped")
            }
            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e("NsdDiscovery", "Start discovery failed: $errorCode")
            }
            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e("NsdDiscovery", "Stop discovery failed: $errorCode")
            }
        }
        nsdManager.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
    }

    override fun stopDiscovery() {
        discoveryListener?.let { 
            try {
                nsdManager.stopServiceDiscovery(it)
            } catch (e: Exception) {
                Log.e("NsdDiscovery", "Failed to stop discovery: ${e.message}")
            }
        }
        discoveryListener = null
        
        try {
            multicastLock?.let {
                if (it.isHeld) it.release()
            }
            Log.d("NsdDiscovery", "Multicast lock released")
        } catch (e: Exception) {
            Log.e("NsdDiscovery", "Error releasing multicast lock", e)
        }
    }

    override fun announce(cabalKey: String, port: Int) {
        // Use a short model name and a random part to ensure service name uniqueness
        val deviceId = (Build.MODEL.take(5) + "-" + (100..999).random()).filter { it.isLetterOrDigit() || it == '-' }
        val serviceInfo = NsdServiceInfo().apply {
            serviceName = "Cabal-$cabalKey-$deviceId"
            serviceType = this@NsdDiscovery.serviceType
            setPort(port)
        }
        nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, object : NsdManager.RegistrationListener {
            override fun onServiceRegistered(serviceInfo: NsdServiceInfo) {
                Log.d("NsdDiscovery", "Service registered: ${serviceInfo.serviceName}")
            }
            override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                Log.e("NsdDiscovery", "Registration failed: $errorCode")
            }
            override fun onServiceUnregistered(serviceInfo: NsdServiceInfo) {
                Log.d("NsdDiscovery", "Service unregistered")
            }
            override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                Log.e("NsdDiscovery", "Unregistration failed: $errorCode")
            }
        })
    }
}
