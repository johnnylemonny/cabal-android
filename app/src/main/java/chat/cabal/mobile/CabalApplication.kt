package chat.cabal.mobile

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import chat.cabal.mobile.core.MessageTtlWorker
import chat.cabal.mobile.di.appModule
import chat.cabal.mobile.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import java.util.concurrent.TimeUnit

class CabalApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@CabalApplication)
            modules(appModule, viewModelModule)
        }

        setupPeriodicWorkers()
    }

    private fun setupPeriodicWorkers() {
        try {
            val ttlWorkRequest = PeriodicWorkRequestBuilder<MessageTtlWorker>(
                repeatInterval = 1,
                repeatIntervalTimeUnit = TimeUnit.HOURS
            ).build()

            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "CabalMessageTtlCleanup",
                ExistingPeriodicWorkPolicy.KEEP,
                ttlWorkRequest
            )
        } catch (_: Exception) {}
    }
}
