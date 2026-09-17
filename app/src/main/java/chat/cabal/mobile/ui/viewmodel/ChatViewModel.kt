package chat.cabal.mobile.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import chat.cabal.database.CabalDatabase
import chat.cabal.database.Message
import chat.cabal.database.Peer
import chat.cabal.mobile.core.toHex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(
    private val database: CabalDatabase,
    private val cableCore: chat.cabal.protocol.CableCore,
    private val syncEngine: chat.cabal.mobile.core.SyncEngine
) : ViewModel() {
    private val _currentChannel = MutableStateFlow("general")

    private val _replyTo = mutableStateOf<Message?>(null)
    val replyTo: State<Message?> = _replyTo

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val messages: StateFlow<List<Message>> = _currentChannel
        .flatMapLatest { channel ->
            database.cabalQueries
                .getMessagesByChannel(channel)
                .asFlow()
                .mapToList(Dispatchers.IO)
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val peers: StateFlow<List<Peer>> = database.cabalQueries
        .getAllPeers()
        .asFlow()
        .mapToList(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun setChannel(channel: String) {
        _currentChannel.value = channel
    }

    fun setReplyTo(message: Message?) {
        _replyTo.value = message
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val channel = _currentChannel.value
                val links = _replyTo.value?.hash?.let { listOf(it) } ?: emptyList()
                val post = cableCore.createTextPost(channel, text, links)
                val rawPost = post.serialize()
                val hash = post.hash()
                
                database.cabalQueries.insertMessage(
                    hash = hash,
                    publicKey = post.publicKey,
                    channel = post.channel,
                    timestamp = post.timestamp,
                    text = text,
                    rawPost = rawPost,
                    status = 0L,
                    parentHash = _replyTo.value?.hash,
                    isEdited = 0L,
                    isDeleted = 0L,
                    ttl = 0L
                )
                
                syncEngine.broadcastPost(post)
                database.cabalQueries.updateMessageStatus(1L, hash)
                _replyTo.value = null
            } catch (e: Exception) {
                android.util.Log.e("ChatViewModel", "Failed to send message", e)
            }
        }
    }

    fun updateProfile(name: String, status: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val info = mapOf("name" to name, "status" to status)
            val post = cableCore.createInfoPost(info)
            syncEngine.broadcastPost(post)
            
            database.cabalQueries.insertOrUpdatePeer(
                publicKey = post.publicKey,
                name = name,
                status = status,
                lastSeen = post.timestamp,
                isIgnored = 0L,
                isVerified = 0L,
                role = 2L
            )
        }
    }
}
