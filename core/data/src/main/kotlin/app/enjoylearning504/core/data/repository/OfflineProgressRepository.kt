package app.enjoylearning504.core.data.repository

import app.enjoylearning504.core.database.user.UserDao
import app.enjoylearning504.core.domain.repository.ProgressRepository
import app.enjoylearning504.core.domain.sync.ProgressSyncScheduler
import app.enjoylearning504.core.model.WordLevel
import app.enjoylearning504.core.model.WordProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OfflineProgressRepository(
    private val dao: UserDao,
    private val syncScheduler: ProgressSyncScheduler,
    private val clock: () -> Long = System::currentTimeMillis,
) : ProgressRepository {

    override fun observeWordProgress(): Flow<Map<Int, WordProgress>> =
        dao.observeWordProgress().map { rows ->
            rows.associate { it.wordId to WordProgress(it.wordId, WordLevel(it.level.coerceIn(0, WordLevel.MAX)), it.isDifficult) }
        }

    override fun observeHighScores(): Flow<Map<Int, Int>> =
        dao.observeClassStats().map { rows -> rows.associate { it.classId to it.highScore } }

    override suspend fun setLevel(wordId: Int, level: WordLevel) {
        dao.setLevel(wordId, level.value, clock())
        syncScheduler.syncAfterChanges()
    }

    override suspend fun setDifficult(wordId: Int, isDifficult: Boolean) {
        dao.setDifficult(wordId, isDifficult, clock())
        syncScheduler.syncAfterChanges()
    }

    override suspend fun saveHighScoreIfBetter(classId: Int, score: Int): Boolean =
        dao.saveHighScoreIfBetter(classId, score).also { saved -> if (saved) syncScheduler.syncAfterChanges() }
}
