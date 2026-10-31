package app.enjoylearning504.core.domain.repository

import app.enjoylearning504.core.model.AuthSession
import app.enjoylearning504.core.model.Lesson
import app.enjoylearning504.core.model.Outcome
import app.enjoylearning504.core.model.SchoolClass
import app.enjoylearning504.core.model.Word
import app.enjoylearning504.core.model.WordLevel
import app.enjoylearning504.core.model.WordProgress
import kotlinx.coroutines.flow.Flow

/** The read-only course content: classes, lessons and words. */
interface ContentRepository {
    suspend fun classes(): List<SchoolClass>
    suspend fun lesson(lessonId: Int): Lesson
    suspend fun words(): List<Word>
    suspend fun word(wordId: Int): Word
}

/** The user's progress: word levels, difficult flags and class high scores. */
interface ProgressRepository {
    fun observeWordProgress(): Flow<Map<Int, WordProgress>>
    fun observeHighScores(): Flow<Map<Int, Int>>
    suspend fun setLevel(wordId: Int, level: WordLevel)
    suspend fun setDifficult(wordId: Int, isDifficult: Boolean)

    /** Stores [score] only if it beats the class's current high score. Returns true if it did. */
    suspend fun saveHighScoreIfBetter(classId: Int, score: Int): Boolean
}

/** Small app-level flags that drive the user lifecycle. */
interface AppStateRepository {
    val authPromptShown: Flow<Boolean>
    val privacyNoticeShown: Flow<Boolean>
    suspend fun markAuthPromptShown()
    suspend fun markPrivacyNoticeShown()
}

/** The remote config the original app read from conf.json. */
interface RemoteConfigRepository {
    /** How many screens pass between interstitial ads. */
    val adTurn: Flow<Int>
    suspend fun refresh()
}

interface AuthRepository {
    val session: Flow<AuthSession?>

    /** Creates a guest session if there is no session yet. Never throws. */
    suspend fun ensureGuestSession()
    suspend fun registerWithEmail(email: String): Outcome<Unit>
    suspend fun verifyEmail(email: String, code: String, password: String): Outcome<AuthSession>
    suspend fun loginWithEmail(email: String, password: String): Outcome<AuthSession>
    suspend fun sendOtp(phone: String): Outcome<Unit>
    suspend fun verifyOtp(phone: String, code: String): Outcome<AuthSession>
    suspend fun forgotPassword(email: String): Outcome<Unit>
    suspend fun resetPassword(email: String, code: String, password: String): Outcome<AuthSession>
    suspend fun logout()
}

/** Moves progress out of the original app's database, once. */
interface LegacyDataMigrator {
    suspend fun migrateIfNeeded(): LegacyMigrationResult
}

sealed interface LegacyMigrationResult {
    /** No database from the original app was found (fresh install) or it was migrated earlier. */
    data object NothingToMigrate : LegacyMigrationResult
    data class Migrated(val wordCount: Int) : LegacyMigrationResult
    /** Migration failed; the old database was left untouched and will be retried next launch. */
    data class Failed(val cause: Throwable) : LegacyMigrationResult
}
