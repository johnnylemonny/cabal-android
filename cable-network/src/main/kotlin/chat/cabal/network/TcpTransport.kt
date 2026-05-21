package chat.cabal.network

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
    private val port: Int = 13333
) {
    private val selectorManager = SelectorManager(Dispatchers.IO)
    private var serverSocket: ServerSocket? = null
    
    // Active connections: RemoteAddress -> Socket
    private val activeConnections = ConcurrentHashMap<String, Socket>()
    
    private val _connectionCount = MutableStateFlow(0)
    val connectionCount = _connectionCount.asStateFlow()
    
    private val _messages = MutableSharedFlow<Pair<String, ByteArray>>()
    val messages = _messages.asSharedFlow()

    fun start() {
        scope.launch(Dispatchers.IO) {
            try {
                serverSocket = aSocket(selectorManager).tcp().bind(port = port)
                println("Server started on port $port")
                while (isActive) {
                    val socket = serverSocket?.accept() ?: break
                    val remoteAddress = socket.remoteAddress.toString()
                    activeConnections[remoteAddress] = socket
                    _connectionCount.value = activeConnections.size
                    launch { handleConnection(socket, remoteAddress) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun connectToPeer(address: String, peerPort: Int) {
        val remoteId = "$address:$peerPort"
        if (activeConnections.containsKey(remoteId)) return
        
        try {
            val socket = aSocket(selectorManager).tcp().connect(address, peerPort)
            activeConnections[remoteId] = socket
            _connectionCount.value = activeConnections.size
            scope.launch { handleConnection(socket, remoteId) }
            println("Connected to peer: $remoteId")
        } catch (e: Exception) {
            println("Failed to connect to $remoteId: ${e.message}")
        }
    }

    private suspend fun handleConnection(socket: Socket, remoteId: String) {
        val receiveChannel = socket.openReadChannel()
        try {
            while (!receiveChannel.isClosedForRead) {
                val length = readVarint(receiveChannel)
                if (length <= 0) break
                val packet = ByteArray(length.toInt())
                receiveChannel.readFully(packet)
                _messages.emit(remoteId to packet)
            }
        } catch (e: Exception) {
            println("Connection to $remoteId lost: ${e.message}")
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
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun sendToPeer(remoteId: String, data: ByteArray) {
        activeConnections[remoteId]?.let { socket ->
            try {
                send(socket, data)
            } catch (e: Exception) {
                e.printStackTrace()
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
    
    fun stop() {
        serverSocket?.close()
        activeConnections.values.forEach { it.close() }
        activeConnections.clear()
        selectorManager.close()
    }
}
