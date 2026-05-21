package chat.cabal.mobile

import android.app.Application
import chat.cabal.mobile.di.appModule
import chat.cabal.mobile.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class CabalApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@CabalApplication)
            modules(appModule, viewModelModule)
        }
    }
}
