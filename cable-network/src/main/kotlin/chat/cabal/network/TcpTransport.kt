package chat.cabal.network

import android.util.Log
import chat.cabal.protocol.Varint
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap

class TcpTransport(
    private val scope: CoroutineScope,
    private val port: Int = 13330
) {
    private val selectorManager = SelectorManager(Dispatchers.IO)
    private var serverSocket: ServerSocket? = null
    
    // Active connections: RemoteAddress -> Socket
    private val activeConnections = ConcurrentHashMap<String, Socket>()
    
    private val _connectionCount = MutableStateFlow(0)
    val connectionCount = _connectionCount.asStateFlow()
    
    private val _newConnections = MutableSharedFlow<String>()
    val newConnections = _newConnections.asSharedFlow()
    
    private val _messages = MutableSharedFlow<Pair<String, ByteArray>>()
    val messages = _messages.asSharedFlow()

    fun start() {
        scope.launch(Dispatchers.IO) {
            var currentPort = port
            var bound = false
            while (!bound && currentPort < port + 10) {
                try {
                    Log.d("TcpTransport", "Trying to bind to port $currentPort...")
                    serverSocket = aSocket(selectorManager).tcp().bind(hostname = "0.0.0.0", port = currentPort)
                    bound = true
                    Log.i("TcpTransport", "SERVER ONLINE: Listening on port $currentPort")
                } catch (_: Exception) {
                    Log.w("TcpTransport", "Port $currentPort busy, trying next...")
                    currentPort++
                }
            }

            if (!bound) {
                Log.e("TcpTransport", "FATAL: Could not bind to any port in range $port-${port+10}")
                return@launch
            }
            
            try {
                while (isActive) {
                    val socket = serverSocket?.accept() ?: break
                    // Normalize remote ID to avoid duplicates from bridge
                    val remoteAddress = socket.remoteAddress.toString().substringBeforeLast(":")
                    if (activeConnections.containsKey(remoteAddress)) {
                        socket.close()
                        continue
                    }
                    
                    Log.i("TcpTransport", "!!! INCOMING CONNECTION FROM: $remoteAddress")
                    activeConnections[remoteAddress] = socket
                    _connectionCount.value = activeConnections.size
                    _newConnections.emit(remoteAddress)
                    launch { handleConnection(socket, remoteAddress) }
                }
            } catch (e: Exception) {
                Log.e("TcpTransport", "Server accept loop failed", e)
            }
        }
    }

    suspend fun connectToPeer(address: String, peerPort: Int): Boolean = withContext(Dispatchers.IO) {
        val remoteId = address // Use IP only as ID to avoid bridge duplicates
        if (activeConnections.containsKey(remoteId)) return@withContext true
        
        Log.d("TcpTransport", "Attempting to connect to $address:$peerPort")
        var success = tryConnect(address, peerPort, remoteId)
        
        // Emulator fallback: try the host bridge (10.0.2.2)
        if (!success && address.startsWith("10.0.2.") && address != "10.0.2.2") {
            Log.d("TcpTransport", "Connection to $address failed, trying emulator host bridge 10.0.2.2")
            success = tryConnect("10.0.2.2", peerPort, "10.0.2.2")
        }
        
        success
    }

    private suspend fun tryConnect(address: String, port: Int, remoteId: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d("TcpTransport", "Opening socket to $address:$port...")
            withTimeout(3000) {
                val socket = aSocket(selectorManager).tcp().connect(address, port)
                activeConnections[remoteId] = socket
                _connectionCount.value = activeConnections.size
                _newConnections.emit(remoteId)
                scope.launch { handleConnection(socket, remoteId) }
                Log.i("TcpTransport", "SUCCESS: Connected to $remoteId")
            }
            true
        } catch (e: Exception) {
            Log.w("TcpTransport", "FAIL: Connection to $address:$port - ${e.javaClass.simpleName}: ${e.message}")
            false
        }
    }

    private suspend fun handleConnection(socket: Socket, remoteId: String) {
        val receiveChannel = socket.openReadChannel()
        try {
            while (!receiveChannel.isClosedForRead) {
                val length = readVarint(receiveChannel)
                if (length <= 0) break
                val packet = ByteArray(length.toInt())
                withContext(Dispatchers.IO) {
                    receiveChannel.readFully(packet)
                }
                _messages.emit(remoteId to packet)
            }
        } catch (_: Exception) {
            println("Connection to $remoteId lost")
        } finally {
            activeConnections.remove(remoteId)
            _connectionCount.value = activeConnections.size
            socket.close()
        }
    }

    suspend fun broadcast(data: ByteArray) {
        activeConnections.values.forEach { socket ->
            try {
                send(socket, data)
            } catch (_: Exception) {
                // ignore
            }
        }
    }

    suspend fun sendToPeer(remoteId: String, data: ByteArray) {
        activeConnections[remoteId]?.let { socket ->
            try {
                send(socket, data)
            } catch (_: Exception) {
                // ignore
            }
        }
    }

    private suspend fun send(socket: Socket, data: ByteArray) {
        val sendChannel = socket.openWriteChannel(autoFlush = true)
        val len = Varint.encode(data.size.toLong())
        sendChannel.writeFully(len)
        sendChannel.writeFully(data)
    }

    private suspend fun readVarint(channel: ByteReadChannel): Long {
        var result = 0L
        var shift = 0
        while (true) {
            val b = try { channel.readByte().toInt() } catch (e: Exception) { return -1 }
            result = result or ((b and 0x7F).toLong() shl shift)
            if (b and 0x80 == 0) break
            shift += 7
            if (shift > 63) throw IllegalArgumentException("Varint too long")
        }
        return result
    }
    
    @Suppress("unused")
    fun stop() {
        try {
            serverSocket?.close()
        } catch (_: Exception) {}
        activeConnections.values.forEach { 
            try {
                it.close()
            } catch (_: Exception) {}
        }
        activeConnections.clear()
        selectorManager.close()
    }
}
