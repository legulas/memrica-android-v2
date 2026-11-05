package app.enjoylearning504.core.domain.sync

import app.enjoylearning504.core.domain.repository.AuthRepository
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map

/** Runs progress sync in the background, whenever the network allows. */
interface ProgressSyncScheduler {
    /** Sync as soon as there is a connection. */
    fun syncNow()

    /** Sync a little later; a burst of changes (a learning session, a quiz) ends in one sync. */
    fun syncAfterChanges()

    /** A periodic sync as a safety net, scheduled once. */
    fun schedulePeriodic()
}

/**
 * Starts syncing for the life of the app: on launch and whenever a registered account signs in.
 * Guests never sync; their progress is merged into the account they sign up or sign in with.
 */
class KeepProgressSyncedUseCase(
    private val auth: AuthRepository,
    private val scheduler: ProgressSyncScheduler,
) {
    suspend operator fun invoke() {
        scheduler.schedulePeriodic()
        auth.session
            .map { session -> session?.takeUnless { it.isGuest }?.userId }
            .distinctUntilChanged()
            .filterNotNull()
            .collect { scheduler.syncNow() }
    }
}
