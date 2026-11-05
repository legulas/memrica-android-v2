package app.enjoylearning504.core.data

import android.database.sqlite.SQLiteDatabase
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.enjoylearning504.core.data.legacy.LegacyDatabaseMigrator
import app.enjoylearning504.core.database.user.UserDatabase
import app.enjoylearning504.core.datastore.AppPreferences
import app.enjoylearning504.core.domain.repository.LegacyMigrationResult
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LegacyDatabaseMigratorTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val legacy = context.getDatabasePath("my504")
    private lateinit var db: UserDatabase
    private lateinit var prefsFile: File
    private lateinit var prefs: AppPreferences

    @Before
    fun setUp() {
        cleanUp()
        db = Room.inMemoryDatabaseBuilder(context, UserDatabase::class.java).build()
        prefsFile = File(context.cacheDir, "migrator_test_${System.nanoTime()}.preferences_pb")
        prefs = AppPreferences(PreferenceDataStoreFactory.create { prefsFile })
        writeLegacyDatabase()
    }

    @After
    fun tearDown() {
        db.close()
        cleanUp()
        prefsFile.delete()
    }

    @Test
    fun copiesLevelsHeartsAndHighScoresThenRetiresTheOldFile() = runTest {
        val migrator = LegacyDatabaseMigrator(context, db.userDao(), prefs)

        val result = migrator.migrateIfNeeded()

        assertEquals(LegacyMigrationResult.Migrated(3), result)
        val rows = db.userDao().observeWordProgress().first().associateBy { it.wordId }
        assertEquals(5, rows.getValue(1).level)
        assertEquals(2, rows.getValue(2).level)
        assertTrue(rows.getValue(3).isDifficult)
        assertEquals(0, rows.getValue(3).level)
        // Everything imported waits to be uploaded; values the old app never set count as never changed
        assertTrue(rows.values.all { it.pending })
        assertEquals(0, rows.getValue(2).difficultChangedAt)
        assertEquals(0, rows.getValue(3).levelChangedAt)
        assertTrue(rows.getValue(3).difficultChangedAt > 0)
        assertEquals(4, db.userDao().observeClassStats().first().single { it.classId == 1 }.highScore)
        assertTrue(db.userDao().pendingClasses().isNotEmpty())
        assertFalse(legacy.exists())
        assertTrue(File(legacy.path + LegacyDatabaseMigrator.BACKUP_SUFFIX).exists())
    }

    @Test
    fun runningTwiceIsSafe() = runTest {
        val migrator = LegacyDatabaseMigrator(context, db.userDao(), prefs)
        migrator.migrateIfNeeded()
        db.userDao().setLevel(1, 0, now = 0) // progress made in v2 after migrating

        assertEquals(LegacyMigrationResult.NothingToMigrate, migrator.migrateIfNeeded())
        assertEquals(0, db.userDao().observeWordProgress().first().single { it.wordId == 1 }.level)
    }

    @Test
    fun freshInstallHasNothingToMigrate() = runTest {
        cleanUp()
        val migrator = LegacyDatabaseMigrator(context, db.userDao(), prefs)
        assertEquals(LegacyMigrationResult.NothingToMigrate, migrator.migrateIfNeeded())
    }

    private fun writeLegacyDatabase() {
        legacy.parentFile?.mkdirs()
        SQLiteDatabase.openOrCreateDatabase(legacy, null).use { db ->
            db.execSQL(
                "CREATE TABLE \"504\" (word_number INTEGER PRIMARY KEY, lesson_number INTEGER, class_number INTEGER, " +
                    "word_en TEXT, word_fa TEXT, last_word_level_status INTEGER NOT NULL, difficult INTEGER NOT NULL)",
            )
            db.execSQL("INSERT INTO \"504\" VALUES (1,1,1,'Abandon','x',5,0),(2,1,1,'Keen','x',2,0),(3,1,1,'Jealous','x',0,1),(4,1,1,'Tact','x',0,0)")
            db.execSQL(
                "CREATE TABLE class (id INTEGER PRIMARY KEY, class_number INTEGER, class_name TEXT, " +
                    "progress_percent INTEGER, number_of_words_completed INTEGER, high_score INTEGER)",
            )
            db.execSQL("INSERT INTO class VALUES (1,1,'c1',7,0,4),(2,2,'c2',0,0,0)")
        }
    }

    private fun cleanUp() {
        listOf("", "-journal", "-wal", "-shm").forEach { suffix ->
            File(legacy.path + suffix).delete()
            File(legacy.path + suffix + LegacyDatabaseMigrator.BACKUP_SUFFIX).delete()
        }
    }
}
