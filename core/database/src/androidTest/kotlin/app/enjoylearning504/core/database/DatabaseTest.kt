package app.enjoylearning504.core.database

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.enjoylearning504.core.database.content.ContentDatabase
import app.enjoylearning504.core.database.user.UserDatabase
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
}
