package app.enjoylearning504.core.data.sync

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import app.enjoylearning504.core.domain.repository.ProgressSyncRepository

/** Gives WorkManager's workers their dependencies; the app installs it in its WorkManager configuration. */
class SyncWorkerFactory(private val repository: () -> ProgressSyncRepository) : WorkerFactory() {

    override fun createWorker(appContext: Context, workerClassName: String, workerParameters: WorkerParameters): ListenableWorker? =
        when (workerClassName) {
            ProgressSyncWorker::class.java.name -> ProgressSyncWorker(appContext, workerParameters, repository())
            else -> null // WorkManager falls back to its default factory
        }
}
