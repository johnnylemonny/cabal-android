package chat.cabal.mobile.network

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import chat.cabal.network.PeerDiscovery
import chat.cabal.network.PeerInfo

class NsdDiscovery(private val context: Context) : PeerDiscovery {
    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    private val serviceType = "_cabal._tcp"
    
    private var discoveryListener: NsdManager.DiscoveryListener? = null

    override fun startDiscovery(cabalKey: String, onPeerFound: (PeerInfo) -> Unit) {
        discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(regType: String) {
                Log.d("NsdDiscovery", "Discovery started")
            }
            override fun onServiceFound(service: NsdServiceInfo) {
                // Check if it's our service type
                if (service.serviceType.contains("cabal")) {
                    nsdManager.resolveService(service, object : NsdManager.ResolveListener {
                        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                            Log.e("NsdDiscovery", "Resolve failed: $errorCode")
                        }
                        override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                            Log.d("NsdDiscovery", "Service resolved: ${serviceInfo.host.hostAddress}:${serviceInfo.port}")
                            onPeerFound(
                                PeerInfo(
                                    address = serviceInfo.host.hostAddress ?: "",
                                    port = serviceInfo.port,
                                    cabalKey = cabalKey // In a real scenario, extract from service name
                                )
                            )
                        }
                    })
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
    }

    override fun announce(cabalKey: String, port: Int) {
        val serviceInfo = NsdServiceInfo().apply {
            serviceName = "Cabal-$cabalKey"
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
