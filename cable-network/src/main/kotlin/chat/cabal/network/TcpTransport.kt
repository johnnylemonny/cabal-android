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
                    serverSocket = aSocket(selectorManager).tcp().bind("0.0.0.0", currentPort)
                    bound = true
                    Log.i("TcpTransport", "SERVER ONLINE: Listening on port $currentPort")
                } catch (_: Exception) {
                    currentPort++
                }
            }

            if (!bound) return@launch
            
            try {
                while (isActive) {
                    val socket = serverSocket?.accept() ?: break
                    val rawAddress = socket.remoteAddress.toString()
                    val remoteAddress = normalizeAddress(rawAddress)
                    
                    if (activeConnections.containsKey(remoteAddress)) {
                        Log.d("TcpTransport", "Closing duplicate incoming connection from $remoteAddress")
                        socket.close()
                        continue
                    }
                    
                    Log.i("TcpTransport", "Incoming connection from $remoteAddress")
                    activeConnections[remoteAddress] = socket
                    _connectionCount.value = activeConnections.size
                    _newConnections.emit(remoteAddress)
                    launch { handleConnection(socket, remoteAddress) }
                }
            } catch (_: Exception) {}
        }
    }

    private fun normalizeAddress(address: String): String {
        return address.removePrefix("/").substringBeforeLast(":")
    }

    suspend fun connectToPeer(address: String, peerPort: Int): Boolean = withContext(Dispatchers.IO) {
        val normalized = normalizeAddress(address)
        if (activeConnections.containsKey(normalized)) return@withContext true
        
        var success = tryConnect(address, peerPort, normalized)
        // Emulator fallback: try the host bridge (10.0.2.2)
        if (!success && address.startsWith("10.0.2.") && normalized != "10.0.2.2") {
            success = tryConnect("10.0.2.2", peerPort, "10.0.2.2")
        }
        success
    }

    private suspend fun tryConnect(address: String, port: Int, remoteId: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            withTimeout(3000) {
                val socket = aSocket(selectorManager).tcp().connect(address, port)
                activeConnections[remoteId] = socket
                _connectionCount.value = activeConnections.size
                _newConnections.emit(remoteId)
                scope.launch { handleConnection(socket, remoteId) }
            }
            true
        } catch (_: Exception) {
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
                // Ensure we don't block the selector thread
                withContext(Dispatchers.IO) {
                    receiveChannel.readFully(packet)
                }
                _messages.emit(remoteId to packet)
            }
        } catch (_: Exception) {
        } finally {
            activeConnections.remove(remoteId)
            _connectionCount.value = activeConnections.size
            socket.close()
        }
    }

    suspend fun broadcast(data: ByteArray) {
        activeConnections.values.forEach { socket ->
            try { send(socket, data) } catch (_: Exception) {}
        }
    }

    suspend fun sendToPeer(remoteId: String, data: ByteArray) {
        activeConnections[remoteId]?.let { try { send(it, data) } catch (_: Exception) {} }
    }

    private suspend fun send(socket: Socket, data: ByteArray) {
        val sendChannel = socket.openWriteChannel(autoFlush = true)
        sendChannel.writeFully(Varint.encode(data.size.toLong()))
        sendChannel.writeFully(data)
    }

    private suspend fun readVarint(channel: ByteReadChannel): Long {
        var result = 0L
        var shift = 0
        while (true) {
            val b = try { channel.readByte().toInt() } catch (_: Exception) { return -1 }
            result = result or ((b and 0x7F).toLong() shl shift)
            if (b and 0x80 == 0) break
            shift += 7
        }
        return result
    }
    
    @Suppress("unused")
    fun stop() {
        try { serverSocket?.close() } catch (_: Exception) {}
        activeConnections.values.forEach { try { it.close() } catch (_: Exception) {} }
        activeConnections.clear()
        selectorManager.close()
    }
}
