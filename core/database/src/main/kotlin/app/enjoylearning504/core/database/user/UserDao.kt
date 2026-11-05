package app.enjoylearning504.core.database.user

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
abstract class UserDao {
    @Query("SELECT * FROM word_progress")
    abstract fun observeWordProgress(): Flow<List<WordProgressEntity>>

    @Query("SELECT * FROM class_stats")
    abstract fun observeClassStats(): Flow<List<ClassStatsEntity>>

    // SQLite UPSERT needs API 30, so rows are created first and then updated.
    // A created row is not pending on its own: only the update that follows marks what changed.

    @Query(
        "INSERT OR IGNORE INTO word_progress (word_id, level, is_difficult, level_changed_at, difficult_changed_at, pending) " +
            "VALUES (:wordId, 0, 0, 0, 0, 0)",
    )
    protected abstract suspend fun ensureWord(wordId: Int)

    @Query("UPDATE word_progress SET level = :level, level_changed_at = :now, pending = 1 WHERE word_id = :wordId")
    protected abstract suspend fun updateLevel(wordId: Int, level: Int, now: Long)

    @Query("UPDATE word_progress SET is_difficult = :isDifficult, difficult_changed_at = :now, pending = 1 WHERE word_id = :wordId")
    protected abstract suspend fun updateDifficult(wordId: Int, isDifficult: Boolean, now: Long)

    @Query("INSERT OR IGNORE INTO class_stats (class_id, high_score, pending) VALUES (:classId, 0, 0)")
    protected abstract suspend fun ensureClass(classId: Int)

    @Query("SELECT high_score FROM class_stats WHERE class_id = :classId")
    protected abstract suspend fun highScore(classId: Int): Int

    @Query("UPDATE class_stats SET high_score = :score, pending = 1 WHERE class_id = :classId")
    protected abstract suspend fun updateHighScore(classId: Int, score: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun replaceWordProgress(rows: List<WordProgressEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun replaceClassStats(rows: List<ClassStatsEntity>)

    @Transaction
    open suspend fun setLevel(wordId: Int, level: Int, now: Long) {
        ensureWord(wordId)
        updateLevel(wordId, level, now)
    }

    @Transaction
    open suspend fun setDifficult(wordId: Int, isDifficult: Boolean, now: Long) {
        ensureWord(wordId)
        updateDifficult(wordId, isDifficult, now)
    }

    @Transaction
    open suspend fun saveHighScoreIfBetter(classId: Int, score: Int): Boolean {
        ensureClass(classId)
        if (score <= highScore(classId)) return false
        updateHighScore(classId, score)
        return true
    }

    /** Writes everything carried over from the original app in one transaction. */
    @Transaction
    open suspend fun importLegacy(words: List<WordProgressEntity>, classes: List<ClassStatsEntity>) {
        replaceWordProgress(words)
        replaceClassStats(classes)
    }

    // Sync

    @Query("SELECT * FROM word_progress WHERE pending = 1")
    abstract suspend fun pendingWords(): List<WordProgressEntity>

    @Query("SELECT * FROM class_stats WHERE pending = 1")
    abstract suspend fun pendingClasses(): List<ClassStatsEntity>

    @Query("UPDATE word_progress SET pending = 1")
    protected abstract suspend fun markAllWordsPending()

    @Query("UPDATE class_stats SET pending = 1")
    protected abstract suspend fun markAllClassesPending()

    /** Every row goes to the server again, e.g. when a different account signs in on this phone. */
    @Transaction
    open suspend fun markAllPending() {
        markAllWordsPending()
        markAllClassesPending()
    }

    // A row edited while its sync was in flight no longer matches what was sent, so it stays pending

    @Query(
        "UPDATE word_progress SET pending = 0 WHERE word_id = :wordId " +
            "AND level_changed_at = :levelChangedAt AND difficult_changed_at = :difficultChangedAt",
    )
    protected abstract suspend fun clearWordPending(wordId: Int, levelChangedAt: Long, difficultChangedAt: Long)

    @Query("UPDATE class_stats SET pending = 0 WHERE class_id = :classId AND high_score = :highScore")
    protected abstract suspend fun clearClassPending(classId: Int, highScore: Int)

    // Server rows win only where they changed later than this phone's copy; high scores only grow

    @Query("UPDATE word_progress SET level = :level, level_changed_at = :changedAt WHERE word_id = :wordId AND level_changed_at < :changedAt")
    protected abstract suspend fun applyLevel(wordId: Int, level: Int, changedAt: Long)

    @Query(
        "UPDATE word_progress SET is_difficult = :isDifficult, difficult_changed_at = :changedAt " +
            "WHERE word_id = :wordId AND difficult_changed_at < :changedAt",
    )
    protected abstract suspend fun applyDifficult(wordId: Int, isDifficult: Boolean, changedAt: Long)

    @Query("UPDATE class_stats SET high_score = :score WHERE class_id = :classId AND high_score < :score")
    protected abstract suspend fun applyHighScore(classId: Int, score: Int)

    /** Settles one sync round: what the server confirmed is no longer pending, and its newer rows are merged in. */
    @Transaction
    open suspend fun completeSync(
        sentWords: List<WordProgressEntity>,
        sentClasses: List<ClassStatsEntity>,
        serverWords: List<WordProgressEntity>,
        serverClasses: List<ClassStatsEntity>,
    ) {
        sentWords.forEach { clearWordPending(it.wordId, it.levelChangedAt, it.difficultChangedAt) }
        sentClasses.forEach { clearClassPending(it.classId, it.highScore) }
        serverWords.forEach {
            ensureWord(it.wordId)
            applyLevel(it.wordId, it.level, it.levelChangedAt)
            applyDifficult(it.wordId, it.isDifficult, it.difficultChangedAt)
        }
        serverClasses.forEach {
            ensureClass(it.classId)
            applyHighScore(it.classId, it.highScore)
        }
    }
}
