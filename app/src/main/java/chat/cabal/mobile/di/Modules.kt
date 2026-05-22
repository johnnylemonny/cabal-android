package chat.cabal.mobile.di

import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import chat.cabal.database.CabalDatabase
import chat.cabal.mobile.core.KeyStoreManager
import chat.cabal.mobile.core.SyncEngine
import chat.cabal.mobile.ui.viewmodel.ChatViewModel
import chat.cabal.mobile.ui.viewmodel.MainViewModel
import chat.cabal.network.TcpTransport
import chat.cabal.protocol.CableCore
import chat.cabal.protocol.Crypto
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { 
        AndroidSqliteDriver(CabalDatabase.Schema, androidContext(), "cabal.db") 
    }
    single { CabalDatabase(get()) }
    
    single { KeyStoreManager() }
    
    single { 
        val ksm: KeyStoreManager = get()
        val kp = ksm.getOrCreateKeyPair()
        val publicKey = kp.public.encoded.takeLast(32).toByteArray()
        val cabalSecret = Crypto.blake2b("default".toByteArray())
        CableCore(publicKey, kp.private, cabalSecret)
    }
    
    single { TcpTransport(get()) }
    
    single { SyncEngine(get(), get(), get(), get()) }
}

val viewModelModule = module {
    viewModel { ChatViewModel(get(), get(), get()) }
    viewModel { MainViewModel(get()) }
}
