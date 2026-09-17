package chat.cabal.mobile.core

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import chat.cabal.database.CabalDatabase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Suppress("unused")
class MessageTtlWorker(
    context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams), KoinComponent {

    private val database: CabalDatabase by inject()

    override suspend fun doWork(): Result {
        return try {
            val now = System.currentTimeMillis() / 1000
            database.cabalQueries.transaction {
                // Delete messages where ttl is set and expired
                // Since we don't have a direct deleteByTtl in sqldelight yet, 
                // we'll need to add it or use a query
                // I'll add the query to Cabal.sq later, for now let's assume it's there
                database.cabalQueries.deleteExpiredMessages(now)
            }
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
