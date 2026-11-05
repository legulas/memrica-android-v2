package app.enjoylearning504.core.database

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.enjoylearning504.core.database.content.ContentDatabase
import app.enjoylearning504.core.database.user.ClassStatsEntity
import app.enjoylearning504.core.database.user.UserDatabase
import app.enjoylearning504.core.database.user.WordProgressEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun shippedContentDatabaseMatchesTheRoomSchema() = runTest {
        // Room validates the prebuilt file against the entities when it opens it
        val db = ContentDatabase.build(context)
        val dao = db.contentDao()
        assertEquals(7, dao.classes().size)
        assertEquals(42, dao.lessons().size)
        val words = dao.words()
        assertEquals(504, words.size)
        assertEquals("Abandon", words.first().english)
        assertEquals("audio/abandon.mp3", words.first().audioAsset)
        db.close()
    }

    @Test
    fun progressWritesAndHighScoreOnlyGoesUp() = runTest {
        val db = Room.inMemoryDatabaseBuilder(context, UserDatabase::class.java).build()
        val dao = db.userDao()
        dao.setLevel(1, 3, now = 1)
        dao.setDifficult(1, true, now = 2)
        dao.setDifficult(2, true, now = 3)
        val rows = dao.observeWordProgress().first().associateBy { it.wordId }
        assertEquals(3, rows.getValue(1).level)
        assertTrue(rows.getValue(1).isDifficult)
        assertEquals(0, rows.getValue(2).level)

        assertTrue(dao.saveHighScoreIfBetter(1, 5))
        assertFalse(dao.saveHighScoreIfBetter(1, 4))
        assertEquals(5, dao.observeClassStats().first().single().highScore)
        db.close()
    }

    @Test
    fun eachFieldKeepsItsOwnChangeTimeAndMarksTheRowPending() = runTest {
        val db = Room.inMemoryDatabaseBuilder(context, UserDatabase::class.java).build()
        val dao = db.userDao()
        dao.setLevel(1, 3, now = 10)
        dao.setDifficult(1, true, now = 20)
        val row = dao.pendingWords().single()
        assertEquals(10, row.levelChangedAt)
        assertEquals(20, row.difficultChangedAt)
        assertTrue(row.pending)
        db.close()
    }

    @Test
    fun completeSyncClearsWhatWasSentAndMergesNewerServerRows() = runTest {
        val db = Room.inMemoryDatabaseBuilder(context, UserDatabase::class.java).build()
        val dao = db.userDao()
        dao.setLevel(1, 2, now = 100)
        dao.setLevel(2, 4, now = 100)
        dao.saveHighScoreIfBetter(1, 5)
        val sentWords = dao.pendingWords()
        val sentClasses = dao.pendingClasses()

        // Word 2 changes again while the sync is in flight
        dao.setLevel(2, 5, now = 150)

        dao.completeSync(
            sentWords = sentWords,
            sentClasses = sentClasses,
            serverWords = listOf(
                WordProgressEntity(1, level = 5, isDifficult = true, levelChangedAt = 50, difficultChangedAt = 200),
                WordProgressEntity(3, level = 1, isDifficult = false, levelChangedAt = 300, difficultChangedAt = 0),
            ),
            serverClasses = listOf(ClassStatsEntity(1, highScore = 3), ClassStatsEntity(2, highScore = 7)),
        )

        val words = dao.observeWordProgress().first().associateBy { it.wordId }
        // Older server level loses, newer server heart wins
        assertEquals(2, words.getValue(1).level)
        assertTrue(words.getValue(1).isDifficult)
        assertFalse(words.getValue(1).pending)
        // Edited mid-sync: stays pending with the new value
        assertEquals(5, words.getValue(2).level)
        assertTrue(words.getValue(2).pending)
        // New from the server, not pending
        assertEquals(1, words.getValue(3).level)
        assertFalse(words.getValue(3).pending)

        val classes = dao.observeClassStats().first().associateBy { it.classId }
        assertEquals(5, classes.getValue(1).highScore)
        assertEquals(7, classes.getValue(2).highScore)
        assertEquals(listOf(2), dao.pendingWords().map { it.wordId })
        assertTrue(dao.pendingClasses().isEmpty())

        dao.markAllPending()
        assertEquals(3, dao.pendingWords().size)
        assertEquals(2, dao.pendingClasses().size)
        db.close()
    }
}
