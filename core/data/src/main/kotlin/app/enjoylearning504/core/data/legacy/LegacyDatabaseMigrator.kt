package app.enjoylearning504.core.data.legacy

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import app.enjoylearning504.core.database.user.ClassStatsEntity
import app.enjoylearning504.core.database.user.UserDao
import app.enjoylearning504.core.database.user.WordProgressEntity
import app.enjoylearning504.core.datastore.AppPreferences
import app.enjoylearning504.core.domain.repository.LegacyDataMigrator
import app.enjoylearning504.core.domain.repository.LegacyMigrationResult
import java.io.File
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * Carries a user's progress over from the original Java app, which kept everything in
 * `databases/my504`: word levels and difficult flags in table `504`, high scores in `class`.
 *
 * Safe to run on every launch. The old file is only read, and is renamed (kept as a backup)
 * after the import has been committed. If anything fails it is left untouched and retried.
 */
class LegacyDatabaseMigrator(
    private val context: Context,
    private val userDao: UserDao,
    private val prefs: AppPreferences,
    private val io: CoroutineDispatcher = Dispatchers.IO,
    private val clock: () -> Long = System::currentTimeMillis,
) : LegacyDataMigrator {

    override suspend fun migrateIfNeeded(): LegacyMigrationResult = withContext(io) {
        val legacy = context.getDatabasePath(LEGACY_NAME)
        if (prefs.legacyMigrationDone.first() || !legacy.exists()) {
            return@withContext LegacyMigrationResult.NothingToMigrate
        }
        try {
            val (words, classes) = readLegacy(legacy)
            userDao.importLegacy(words, classes)
            carryOverPreferences()
            retire(legacy)
            prefs.setLegacyMigrationDone()
            LegacyMigrationResult.Migrated(words.size)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            LegacyMigrationResult.Failed(e)
        }
    }

    private fun readLegacy(file: File): Pair<List<WordProgressEntity>, List<ClassStatsEntity>> {
        val now = clock()
        SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READONLY).use { db ->
            val words = db.rawQuery(
                "SELECT word_number, last_word_level_status, difficult FROM \"504\" " +
                    "WHERE last_word_level_status > 0 OR difficult != 0",
                null,
            ).use { c ->
                buildList {
                    while (c.moveToNext()) {
                        val level = c.getInt(1).coerceIn(0, MAX_LEVEL)
                        val isDifficult = c.getInt(2) != 0
                        // A value the old app never set counts as never changed, so it can't override synced progress
                        add(
                            WordProgressEntity(
                                wordId = c.getInt(0),
                                level = level,
                                isDifficult = isDifficult,
                                levelChangedAt = if (level > 0) now else 0,
                                difficultChangedAt = if (isDifficult) now else 0,
                            ),
                        )
                    }
                }
            }
            val classes = db.rawQuery("SELECT class_number, high_score FROM class WHERE high_score > 0", null).use { c ->
                buildList { while (c.moveToNext()) add(ClassStatsEntity(c.getInt(0), c.getInt(1))) }
            }
            return words to classes
        }
    }

    /** The original app showed its privacy notice once; don't show it again to people who saw it. */
    private suspend fun carryOverPreferences() {
        val legacyPrefs = context.getSharedPreferences(LEGACY_PREFS, Context.MODE_PRIVATE)
        if (legacyPrefs.contains(LEGACY_FIRST_START) && !legacyPrefs.getBoolean(LEGACY_FIRST_START, true)) {
            prefs.setPrivacyNoticeShown()
        }
    }

    private fun retire(legacy: File) {
        listOf("", "-journal", "-wal", "-shm").forEach { suffix ->
            val file = File(legacy.path + suffix)
            if (file.exists()) file.renameTo(File(legacy.path + suffix + BACKUP_SUFFIX))
        }
    }

    companion object {
        const val LEGACY_NAME = "my504"
        const val BACKUP_SUFFIX = ".legacy.bak"
        private const val LEGACY_PREFS = "key_shared_preference_name"
        private const val LEGACY_FIRST_START = "key_is_first_start"
        private const val MAX_LEVEL = 5
    }
}
