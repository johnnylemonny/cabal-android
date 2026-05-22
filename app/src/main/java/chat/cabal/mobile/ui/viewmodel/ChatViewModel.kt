package chat.cabal.mobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import chat.cabal.database.CabalDatabase
import chat.cabal.database.Message
import chat.cabal.mobile.core.SyncEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(
    private val database: CabalDatabase,
    private val syncEngine: SyncEngine
) : ViewModel() {
    val messages: StateFlow<List<Message>> = database.cabalQueries
        .getMessagesByChannel("general")
        .asFlow()
        .mapToList(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun sendMessage(text: String) {
        viewModelScope.launch(Dispatchers.IO) {
            syncEngine.postText("general", text)
        }
    }
}
