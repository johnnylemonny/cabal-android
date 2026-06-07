package chat.cabal.network

import android.util.Log
import chat.cabal.protocol.Varint
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration.Companion.seconds
import java.util.concurrent.ConcurrentHashMap
import java.net.NetworkInterface

class TcpTransport(
    private val scope: CoroutineScope,
    private val port: Int = 13330
) {
    private val selectorManager = SelectorManager(Dispatchers.IO)
    private var serverSocket: ServerSocket? = null
    
    private val activeConnections = ConcurrentHashMap<String, Socket>()
    private val writeChannels = ConcurrentHashMap<String, ByteWriteChannel>()
    private val writeMutexes = ConcurrentHashMap<String, Mutex>()
    
    private val _connectionCount = MutableStateFlow(0)
    val connectionCount = _connectionCount.asStateFlow()
    
    private val _newConnections = MutableSharedFlow<String>(replay = 5)
    val newConnections = _newConnections.asSharedFlow()
    
    private val _messages = MutableSharedFlow<Pair<String, ByteArray>>(replay = 10)
    val messages = _messages.asSharedFlow()

    private val knownRelays = mutableSetOf<String>()

    private var myIPs = setOf<String>()

    init {
        scope.launch(Dispatchers.IO) {
            myIPs = try {
                withContext(Dispatchers.IO) {
                    NetworkInterface.getNetworkInterfaces().asSequence()
                        .flatMap { it.inetAddresses.asSequence() }
                        .map { it.hostAddress?.removePrefix("/") ?: "" }
                        .filter { it.isNotEmpty() }
                        .toSet()
                }
            } catch (_: Exception) {
                emptySet()
            }
            Log.d("TcpTransport", "Local IPs: $myIPs")
        }
        
        // Background job to keep relay connections alive
        scope.launch(Dispatchers.IO) {
            while (isActive) {
                knownRelays.forEach { address ->
                    if (!activeConnections.containsKey(address)) {
                        Log.d("TcpTransport", "Attempting to reconnect to relay: $address")
                        connectToPeer(address, port)
                    }
                }
                delay(30.seconds) // Every 30 seconds
            }
        }
    }

    fun addRelay(address: String) {
        knownRelays.add(address)
        scope.launch(Dispatchers.IO) { connectToPeer(address, port) }
    }

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
                    val remoteAddress = normalizeAddress(socket.remoteAddress.toString())
                    
                    if (myIPs.contains(remoteAddress) || remoteAddress == "127.0.0.1" || remoteAddress == "localhost") {
                        Log.v("TcpTransport", "Ignoring connection from self: $remoteAddress")
                        socket.close()
                        continue
                    }

                    if (activeConnections.containsKey(remoteAddress)) {
                        socket.close()
                        continue
                    }
                    
                    Log.i("TcpTransport", "Accepted connection from $remoteAddress")
                    registerConnection(socket, remoteAddress)
                }
            } catch (_: Exception) {}
        }
    }

    private fun normalizeAddress(address: String): String {
        val clean = address.removePrefix("/").substringBeforeLast(":")
        if (clean == "127.0.0.1" || clean == "localhost") return "10.0.2.2"
        return clean
    }

    private fun updateStats() {
        _connectionCount.value = activeConnections.size
    }

    private fun registerConnection(socket: Socket, remoteId: String) {
        activeConnections[remoteId] = socket
        writeChannels[remoteId] = socket.openWriteChannel(autoFlush = true)
        writeMutexes[remoteId] = Mutex()
        updateStats()
        scope.launch { _newConnections.emit(remoteId) }
        scope.launch { handleConnection(socket, remoteId) }
    }

    suspend fun connectToPeer(address: String, peerPort: Int): Boolean = withContext(Dispatchers.IO) {
        val normalized = normalizeAddress(address)
        if (myIPs.contains(normalized) || normalized == "127.0.0.1") return@withContext false
        if (activeConnections.containsKey(normalized)) return@withContext true
        
        var success = tryConnect(address, peerPort, normalized)
        if (!success && normalized.startsWith("10.0.2.") && normalized != "10.0.2.2") {
            success = tryConnect("10.0.2.2", peerPort, "10.0.2.2")
        }
        success
    }

    private suspend fun tryConnect(address: String, port: Int, remoteId: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            withTimeout(3.seconds) {
                val socket = aSocket(selectorManager).tcp().connect(address, port)
                if (activeConnections.containsKey(remoteId)) {
                    socket.close()
                    return@withTimeout true
                }
                Log.i("TcpTransport", "Connected to $remoteId")
                registerConnection(socket, remoteId)
                true
            }
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
                withContext(Dispatchers.IO) {
                    receiveChannel.readFully(packet)
                }
                _messages.emit(remoteId to packet)
            }
        } catch (_: Exception) {
        } finally {
            activeConnections.remove(remoteId)
            writeChannels.remove(remoteId)
            writeMutexes.remove(remoteId)
            updateStats()
            socket.close()
            Log.d("TcpTransport", "Connection to $remoteId closed")
        }
    }

    suspend fun broadcast(data: ByteArray) {
        activeConnections.keys.forEach { id ->
            sendToPeer(id, data)
        }
    }

    suspend fun sendToPeer(remoteId: String, data: ByteArray) {
        val channel = writeChannels[remoteId]
        val mutex = writeMutexes[remoteId]
        if (channel != null && mutex != null) {
            try {
                mutex.withLock {
                    channel.writeFully(Varint.encode(data.size.toLong()))
                    channel.writeFully(data)
                }
            } catch (_: Exception) {}
        }
    }

    private suspend fun readVarint(channel: ByteReadChannel): Long {
        var result = 0L
        var shift = 0
        while (true) {
            val b = try { channel.readByte().toInt() } catch (_: Exception) { return -1 }
            result = result or ((b.toLong() and 0x7F) shl shift)
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
        writeChannels.clear()
        writeMutexes.clear()
        selectorManager.close()
    }
}
