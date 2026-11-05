package app.enjoylearning504.core.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import app.enjoylearning504.core.domain.repository.ProgressSyncRepository
import app.enjoylearning504.core.model.AppError
import app.enjoylearning504.core.model.Outcome

class ProgressSyncWorker(
    context: Context,
    params: WorkerParameters,
    private val repository: ProgressSyncRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = when (val outcome = repository.sync()) {
        is Outcome.Success -> Result.success()
        // Worth another try later; a rejected request would only be rejected again
        is Outcome.Failure -> when (outcome.error) {
            AppError.NoConnection, AppError.RateLimited, is AppError.Unexpected -> Result.retry()
            else -> Result.failure()
        }
    }
}
