package chat.cabal.mobile.network

import chat.cabal.network.PeerDiscovery
import chat.cabal.network.PeerInfo

class CompositeDiscovery(private val methods: List<PeerDiscovery>) : PeerDiscovery {
    override fun startDiscovery(cabalKey: String, onPeerFound: (PeerInfo) -> Unit) {
        val seenPeers = mutableSetOf<String>()
        methods.forEach { method ->
            method.startDiscovery(cabalKey) { peer ->
                val identifier = "${peer.address}:${peer.port}"
                if (identifier !in seenPeers) {
                    seenPeers.add(identifier)
                    onPeerFound(peer)
                }
            }
        }
    }

    override fun stopDiscovery() {
        methods.forEach { it.stopDiscovery() }
    }

    override fun announce(cabalKey: String, port: Int) {
        methods.forEach { it.announce(cabalKey, port) }
    }
}
