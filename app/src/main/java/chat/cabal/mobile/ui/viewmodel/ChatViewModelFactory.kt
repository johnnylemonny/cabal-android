package chat.cabal.mobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import chat.cabal.database.CabalDatabase
import chat.cabal.mobile.core.SyncEngine
import chat.cabal.protocol.CableCore

class ChatViewModelFactory(
    private val database: CabalDatabase,
    private val cableCore: CableCore,
    private val syncEngine: SyncEngine
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(database, cableCore, syncEngine) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
