package chat.cabal.mobile.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import chat.cabal.database.CabalDatabase
import chat.cabal.mobile.core.KeyStoreManager
import chat.cabal.mobile.core.QuickJsEngine
import chat.cabal.mobile.core.SyncEngine
import chat.cabal.mobile.ui.viewmodel.ChatViewModel
import chat.cabal.mobile.ui.viewmodel.MainViewModel
import chat.cabal.network.TcpTransport
import chat.cabal.protocol.CableCore
import chat.cabal.protocol.Crypto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single<SqlDriver> {
        AndroidSqliteDriver(CabalDatabase.Schema, androidContext(), "cabal.db") 
    }
    single { CabalDatabase(get()) }
    
    single { KeyStoreManager() }
    
    single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
    
    single { TcpTransport(get()) }
    
    single { QuickJsEngine(androidContext(), get()) }
    
    single { SyncEngine(get(), get(), get(), get(), get()) }
}

val viewModelModule = module {
    viewModel { ChatViewModel(get(), get()) }
    viewModel { MainViewModel(get()) }
}
