package chat.cabal.mobile.network

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import chat.cabal.network.PeerDiscovery
import chat.cabal.network.PeerInfo
import kotlinx.coroutines.*
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketException
import kotlin.time.Duration.Companion.seconds

class UdpDiscovery(
    private val context: Context,
    private val scope: CoroutineScope,
    private val udpPort: Int = 13334
) : PeerDiscovery {
    private var socket: DatagramSocket? = null
    private var discoveryJob: Job? = null
    private var announceJob: Job? = null
    private val TAG = "UdpDiscovery"
    private var multicastLock: WifiManager.MulticastLock? = null

    override fun startDiscovery(cabalKey: String, onPeerFound: (PeerInfo) -> Unit) {
        stopDiscovery()
        
        // Acquire multicast lock to receive UDP broadcasts
        try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            multicastLock = wifiManager.createMulticastLock("cabal_udp_lock").apply {
                setReferenceCounted(true)
                acquire()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Could not acquire multicast lock", e)
        }

        discoveryJob = scope.launch(Dispatchers.IO) {
            try {
                socket = DatagramSocket(udpPort).apply {
                    broadcast = true
                    reuseAddress = true
                }
                Log.d(TAG, "UDP listener started on port $udpPort")
                
                val buffer = ByteArray(1024)
                while (isActive) {
                    val packet = DatagramPacket(buffer, buffer.size)
                    socket?.receive(packet)
                    
                    val message = String(packet.data, 0, packet.length)
                    val parts = message.split("|")
                    
                    // Format: CABAL|key|port
                    if (parts.size == 3 && parts[0] == "CABAL" && parts[1] == cabalKey) {
                        val peerPort = parts[2].toIntOrNull() ?: 13330
                        val peerAddress = packet.address.hostAddress
                        
                        if (peerAddress != null) {
                            Log.d(TAG, "Found peer via UDP: $peerAddress:$peerPort")
                            onPeerFound(PeerInfo(peerAddress, peerPort, cabalKey))
                        }
                    }
                }
            } catch (e: Exception) {
                if (e !is SocketException) {
                    Log.e(TAG, "Discovery error", e)
                }
            }
        }
    }

    override fun announce(cabalKey: String, port: Int) {
        announceJob?.cancel()
        announceJob = scope.launch(Dispatchers.IO) {
            val message = "CABAL|$cabalKey|$port".toByteArray()
            val broadcastAddress = InetAddress.getByName("255.255.255.255")
            val emulatorAddress = InetAddress.getByName("10.0.2.2") // Special address for Android Emulators
            
            while (isActive) {
                try {
                    val sendSocket = DatagramSocket()
                    sendSocket.broadcast = true
                    
                    // Standard broadcast
                    val packet = DatagramPacket(message, message.size, broadcastAddress, udpPort)
                    sendSocket.send(packet)
                    
                    // Special ping for other emulators (loopback to host which might route to other emulators)
                    val emuPacket = DatagramPacket(message, message.size, emulatorAddress, udpPort)
                    sendSocket.send(emuPacket)
                    
                    sendSocket.close()
                    Log.v(TAG, "Broadcast announcement sent (Standard + Emulator Link)")
                } catch (e: Exception) {
                    Log.e(TAG, "Announce error", e)
                }
                delay(5.seconds) // Announce every 5 seconds
            }
        }
    }

    override fun stopDiscovery() {
        discoveryJob?.cancel()
        announceJob?.cancel()
        socket?.close()
        socket = null
        
        try {
            multicastLock?.let {
                if (it.isHeld) it.release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing multicast lock", e)
        }
        multicastLock = null
    }
}
