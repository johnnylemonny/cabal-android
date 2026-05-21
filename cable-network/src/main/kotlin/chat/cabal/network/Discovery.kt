package chat.cabal.network

interface PeerDiscovery {
    fun startDiscovery(cabalKey: String, onPeerFound: (PeerInfo) -> Unit)
    fun stopDiscovery()
    fun announce(cabalKey: String, port: Int)
}

data class PeerInfo(
    val address: String,
    val port: Int,
    val cabalKey: String
)
