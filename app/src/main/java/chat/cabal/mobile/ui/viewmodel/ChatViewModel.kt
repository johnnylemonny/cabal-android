package chat.cabal.mobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import chat.cabal.database.CabalDatabase
import chat.cabal.database.Message
import chat.cabal.mobile.core.SyncEngine
import chat.cabal.protocol.CableCore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(
    private val database: CabalDatabase,
    private val cableCore: CableCore,
    private val syncEngine: SyncEngine
) : ViewModel() {
    val messages: StateFlow<List<Message>> = database.cabalQueries
        .getMessagesByChannel("general")
        .asFlow()
        .mapToList(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        android.util.Log.d("ChatViewModel", "Sending message: $text")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val post = cableCore.createTextPost("general", text)
                val rawPost = post.serialize()
                val hash = post.hash()
                
                database.cabalQueries.insertMessage(
                    hash = hash,
                    publicKey = post.publicKey,
                    channel = post.channel,
                    timestamp = post.timestamp,
                    text = text,
                    rawPost = rawPost,
                    status = 0L // Sending
                )
                
                syncEngine.broadcastPost(post)
                database.cabalQueries.updateMessageStatus(1L, hash)
            } catch (e: Exception) {
                android.util.Log.e("ChatViewModel", "Failed to send message", e)
            }
        }
    }
}
