package app.enjoylearning504.core.data.sync

import app.enjoylearning504.core.data.repository.toOutcome
import app.enjoylearning504.core.database.user.ClassStatsEntity
import app.enjoylearning504.core.database.user.UserDao
import app.enjoylearning504.core.database.user.WordProgressEntity
import app.enjoylearning504.core.datastore.AppPreferences
import app.enjoylearning504.core.datastore.SyncState
import app.enjoylearning504.core.datastore.TokenStorage
import app.enjoylearning504.core.domain.repository.ProgressSyncRepository
import app.enjoylearning504.core.model.AppError
import app.enjoylearning504.core.model.Outcome
import app.enjoylearning504.core.network.ClassStatsDto
import app.enjoylearning504.core.network.ProgressApi
import app.enjoylearning504.core.network.SyncRequest
import app.enjoylearning504.core.network.WordProgressDto
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Pushes pending rows and merges the server's newer ones (see `UserDao.completeSync`).
 * When a different account signs in on this phone, everything local is sent again from version 0,
 * so the progress made here is merged into that account.
 */
class DefaultProgressSyncRepository(
    private val dao: UserDao,
    private val api: ProgressApi,
    private val tokens: TokenStorage,
    private val prefs: AppPreferences,
) : ProgressSyncRepository {
    private val mutex = Mutex()

    override suspend fun sync(): Outcome<Unit> = mutex.withLock {
        val session = tokens.current()
        if (session == null || session.isGuest) return Outcome.Success(Unit)

        val state = prefs.syncState()
        val since = if (state?.userId == session.userId) {
            state.version
        } else {
            dao.markAllPending()
            0
        }
        val words = dao.pendingWords()
        val classes = dao.pendingClasses()
        val request = SyncRequest(since, words.map { it.toDto() }, classes.map { it.toDto() })
        api.sync(request).toOutcome(AppError.InvalidCredentials) { response ->
            dao.completeSync(
                sentWords = words,
                sentClasses = classes,
                serverWords = response.words.map { it.toEntity() },
                serverClasses = response.classes.map { it.toEntity() },
            )
            prefs.setSyncState(SyncState(session.userId, response.version))
            Unit
        }
    }
}

private fun WordProgressEntity.toDto() = WordProgressDto(wordId, level, levelChangedAt, isDifficult, difficultChangedAt)

private fun ClassStatsEntity.toDto() = ClassStatsDto(classId, highScore)

private fun WordProgressDto.toEntity() =
    WordProgressEntity(wordId, level, difficult, levelChangedAt, difficultChangedAt, pending = false)

private fun ClassStatsDto.toEntity() = ClassStatsEntity(classId, highScore, pending = false)
