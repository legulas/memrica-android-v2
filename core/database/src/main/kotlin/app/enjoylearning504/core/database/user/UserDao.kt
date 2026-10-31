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

    // SQLite UPSERT needs API 30, so rows are created first and then updated

    @Query("INSERT OR IGNORE INTO word_progress (word_id, level, is_difficult, updated_at) VALUES (:wordId, 0, 0, :now)")
    protected abstract suspend fun ensureWord(wordId: Int, now: Long)

    @Query("UPDATE word_progress SET level = :level, updated_at = :now WHERE word_id = :wordId")
    protected abstract suspend fun updateLevel(wordId: Int, level: Int, now: Long)

    @Query("UPDATE word_progress SET is_difficult = :isDifficult, updated_at = :now WHERE word_id = :wordId")
    protected abstract suspend fun updateDifficult(wordId: Int, isDifficult: Boolean, now: Long)

    @Query("INSERT OR IGNORE INTO class_stats (class_id, high_score) VALUES (:classId, 0)")
    protected abstract suspend fun ensureClass(classId: Int)

    @Query("SELECT high_score FROM class_stats WHERE class_id = :classId")
    protected abstract suspend fun highScore(classId: Int): Int

    @Query("UPDATE class_stats SET high_score = :score WHERE class_id = :classId")
    protected abstract suspend fun updateHighScore(classId: Int, score: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun replaceWordProgress(rows: List<WordProgressEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun replaceClassStats(rows: List<ClassStatsEntity>)

    @Transaction
    open suspend fun setLevel(wordId: Int, level: Int, now: Long) {
        ensureWord(wordId, now)
        updateLevel(wordId, level, now)
    }

    @Transaction
    open suspend fun setDifficult(wordId: Int, isDifficult: Boolean, now: Long) {
        ensureWord(wordId, now)
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
}
