package chat.cabal.mobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import chat.cabal.database.Cabal
import chat.cabal.database.CabalDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(private val database: CabalDatabase) : ViewModel() {
    val cabals: StateFlow<List<Cabal>> = database.cabalQueries
        .getAllCabals()
        .asFlow()
        .mapToList(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addCabal(key: String, name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            database.cabalQueries.insertCabal(key, name)
        }
    }
}
