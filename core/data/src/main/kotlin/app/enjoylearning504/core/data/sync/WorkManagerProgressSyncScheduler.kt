package app.enjoylearning504.core.data.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import app.enjoylearning504.core.domain.sync.ProgressSyncScheduler
import java.util.concurrent.TimeUnit

class WorkManagerProgressSyncScheduler(context: Context) : ProgressSyncScheduler {
    private val workManager = WorkManager.getInstance(context)
    private val online = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

    override fun syncNow() = enqueue(delaySeconds = 0)

    // Each change restarts the delay, so a learning session or quiz syncs once, after it ends
    override fun syncAfterChanges() = enqueue(delaySeconds = CHANGES_DELAY_SECONDS)

    override fun schedulePeriodic() {
        val request = PeriodicWorkRequestBuilder<ProgressSyncWorker>(PERIOD_HOURS, TimeUnit.HOURS)
            .setConstraints(online)
            .build()
        workManager.enqueueUniquePeriodicWork(PERIODIC_WORK, ExistingPeriodicWorkPolicy.KEEP, request)
    }

    private fun enqueue(delaySeconds: Long) {
        val request = OneTimeWorkRequestBuilder<ProgressSyncWorker>()
            .setConstraints(online)
            .setInitialDelay(delaySeconds, TimeUnit.SECONDS)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, BACKOFF_SECONDS, TimeUnit.SECONDS)
            .build()
        workManager.enqueueUniqueWork(ONE_TIME_WORK, ExistingWorkPolicy.REPLACE, request)
    }

    private companion object {
        const val ONE_TIME_WORK = "progress-sync"
        const val PERIODIC_WORK = "progress-sync-periodic"
        const val CHANGES_DELAY_SECONDS = 30L
        const val BACKOFF_SECONDS = 30L
        const val PERIOD_HOURS = 12L
    }
}
