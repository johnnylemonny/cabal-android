package chat.cabal.mobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import chat.cabal.database.CabalDatabase
import chat.cabal.database.Message
import chat.cabal.mobile.core.toHex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(
    private val database: CabalDatabase,
    private val cableCore: chat.cabal.protocol.CableCore,
    private val syncEngine: chat.cabal.mobile.core.SyncEngine
) : ViewModel() {
    val messages: StateFlow<List<Message>> = database.cabalQueries
        .getMessagesByChannel("general")
        .asFlow()
        .mapToList(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                android.util.Log.d("ChatViewModel", "Sending message: $text")
                val post = cableCore.createTextPost("general", text)
                val rawPost = post.serialize()
                val hash = post.hash()
                
                // Save locally
                database.cabalQueries.insertMessage(
                    hash = hash,
                    publicKey = post.publicKey,
                    channel = post.channel,
                    timestamp = post.timestamp,
                    text = text, // Local display uses original text
                    rawPost = rawPost,
                    status = 0L // Sending
                )
                
                // Broadcast to peers
                syncEngine.broadcastPost(post)
                
                // Mark as sent to network
                database.cabalQueries.updateMessageStatus(1L, hash)
                android.util.Log.d("ChatViewModel", "Message sent and saved: ${hash.toHex()}")
            } catch (e: Exception) {
                android.util.Log.e("ChatViewModel", "Failed to send message", e)
            }
        }
    }
}
