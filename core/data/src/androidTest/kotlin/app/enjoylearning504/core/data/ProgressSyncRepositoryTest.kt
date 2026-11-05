package app.enjoylearning504.core.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.enjoylearning504.core.data.sync.DefaultProgressSyncRepository
import app.enjoylearning504.core.database.user.UserDatabase
import app.enjoylearning504.core.datastore.AppPreferences
import app.enjoylearning504.core.datastore.StoredTokens
import app.enjoylearning504.core.datastore.SyncState
import app.enjoylearning504.core.datastore.TokenStorage
import app.enjoylearning504.core.model.AppError
import app.enjoylearning504.core.model.Outcome
import app.enjoylearning504.core.network.AuthResponse
import app.enjoylearning504.core.network.HttpClientFactory
import app.enjoylearning504.core.network.ProgressApi
import app.enjoylearning504.core.network.SyncRequest
import app.enjoylearning504.core.network.TokenProvider
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import java.io.File
import java.io.IOException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProgressSyncRepositoryTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val json = Json { ignoreUnknownKeys = true }
    private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")
    private val files = mutableListOf<File>()
    private lateinit var db: UserDatabase
    private lateinit var prefs: AppPreferences
    private lateinit var tokens: TokenStorage
    private val requests = mutableListOf<SyncRequest>()

    /** What the fake server answers; null = the connection fails. */
    private var serverReply: String? = """{"version":1,"words":[],"classes":[]}"""

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(context, UserDatabase::class.java).build()
        prefs = AppPreferences(PreferenceDataStoreFactory.create { tempFile("prefs") })
        tokens = TokenStorage(PreferenceDataStoreFactory.create { tempFile("tokens") })
    }

    @After
    fun tearDown() {
        db.close()
        files.forEach { it.delete() }
    }

    private fun tempFile(name: String) =
        File(context.cacheDir, "sync_test_${name}_${System.nanoTime()}.preferences_pb").also { files += it }

    private fun repository(): DefaultProgressSyncRepository {
        val engine = MockEngine { request ->
            assertEquals("/progress/sync", request.url.encodedPath)
            requests += json.decodeFromString<SyncRequest>(String(request.body.toByteArray()))
            val reply = serverReply ?: throw IOException("offline")
            respond(reply, HttpStatusCode.OK, jsonHeaders)
        }
        val provider = object : TokenProvider {
            override suspend fun load() = BearerTokens("a", "r")
            override suspend fun onRefreshed(response: AuthResponse) = Unit
            override suspend fun onSessionExpired() = Unit
        }
        val api = ProgressApi(HttpClientFactory.memricaClient(provider, engine, "http://test/"))
        return DefaultProgressSyncRepository(db.userDao(), api, tokens, prefs)
    }

    private suspend fun signIn(userId: String, isGuest: Boolean = false) =
        tokens.save(StoredTokens(userId, "a", "r", expiresAtEpochSeconds = 0, isGuest = isGuest))

    @Test
    fun guestsNeverSync() = runTest {
        signIn("g1", isGuest = true)
        db.userDao().setLevel(1, 2, now = 10)

        assertEquals(Outcome.Success(Unit), repository().sync())

        assertTrue(requests.isEmpty())
        assertEquals(1, db.userDao().pendingWords().size)
    }

    @Test
    fun firstSyncSendsEverythingThenOnlyChangesFromTheSavedVersion() = runTest {
        signIn("u1")
        val dao = db.userDao()
        dao.setLevel(1, 2, now = 10)
        dao.saveHighScoreIfBetter(1, 5)
        serverReply = """{"version":7,
            "words":[{"word_id":1,"level":2,"level_changed_at":10,"difficult":false,"difficult_changed_at":0},
                     {"word_id":9,"level":4,"level_changed_at":20,"difficult":true,"difficult_changed_at":20}],
            "classes":[{"class_id":1,"high_score":5}]}"""
        val repository = repository()

        assertEquals(Outcome.Success(Unit), repository.sync())

        assertEquals(0L, requests[0].since)
        assertEquals(listOf(1), requests[0].words.map { it.wordId })
        assertEquals(listOf(1), requests[0].classes.map { it.classId })
        assertTrue(dao.pendingWords().isEmpty())
        assertTrue(dao.pendingClasses().isEmpty())
        val word9 = dao.observeWordProgress().first().single { it.wordId == 9 }
        assertEquals(4, word9.level)
        assertTrue(word9.isDifficult)
        assertEquals(SyncState("u1", 7), prefs.syncState())

        dao.setDifficult(2, true, now = 30)
        serverReply = """{"version":8,"words":[],"classes":[]}"""
        repository.sync()
        assertEquals(7L, requests[1].since)
        assertEquals(listOf(2), requests[1].words.map { it.wordId })
    }

    @Test
    fun anotherAccountOnThisPhoneGetsAllLocalProgressFromVersionZero() = runTest {
        signIn("u1")
        val dao = db.userDao()
        dao.setLevel(1, 2, now = 10)
        dao.setLevel(2, 3, now = 10)
        val repository = repository()
        repository.sync()
        assertTrue(dao.pendingWords().isEmpty())

        signIn("u2")
        repository.sync()

        assertEquals(0L, requests[1].since)
        assertEquals(listOf(1, 2), requests[1].words.map { it.wordId }.sorted())
        assertEquals("u2", prefs.syncState()?.userId)
    }

    @Test
    fun aFailedSyncKeepsEverythingPending() = runTest {
        signIn("u1")
        db.userDao().setLevel(1, 2, now = 10)
        serverReply = null

        val outcome = repository().sync()

        assertEquals(Outcome.Failure(AppError.NoConnection), outcome)
        assertEquals(1, db.userDao().pendingWords().size)
        assertFalse(prefs.syncState() != null)
    }
}
