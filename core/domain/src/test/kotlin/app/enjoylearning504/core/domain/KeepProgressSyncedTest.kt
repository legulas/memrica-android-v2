package app.enjoylearning504.core.domain

import app.enjoylearning504.core.domain.repository.AuthRepository
import app.enjoylearning504.core.domain.sync.KeepProgressSyncedUseCase
import app.enjoylearning504.core.domain.sync.ProgressSyncScheduler
import app.enjoylearning504.core.model.AuthSession
import app.enjoylearning504.core.model.Outcome
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class KeepProgressSyncedTest {

    private class CountingScheduler : ProgressSyncScheduler {
        var now = 0
        var periodic = 0
        override fun syncNow() { now++ }
        override fun syncAfterChanges() = Unit
        override fun schedulePeriodic() { periodic++ }
    }

    private class FakeAuth : AuthRepository {
        override val session = MutableStateFlow<AuthSession?>(null)
        override suspend fun ensureGuestSession() = Unit
        override suspend fun registerWithEmail(email: String) = Outcome.Success(Unit)
        override suspend fun verifyEmail(email: String, code: String, password: String) = error("unused")
        override suspend fun loginWithEmail(email: String, password: String) = error("unused")
        override suspend fun sendOtp(phone: String) = Outcome.Success(Unit)
        override suspend fun verifyOtp(phone: String, code: String) = error("unused")
        override suspend fun forgotPassword(email: String) = Outcome.Success(Unit)
        override suspend fun resetPassword(email: String, code: String, password: String) = error("unused")
        override suspend fun logout() = Unit
    }

    private fun session(userId: String, isGuest: Boolean) = AuthSession(userId, "a", "r", 0, isGuest)

    @Test
    fun `syncs once per registered account signing in and never for guests`() = runTest {
        val auth = FakeAuth()
        val scheduler = CountingScheduler()
        val job = launch { KeepProgressSyncedUseCase(auth, scheduler)() }

        auth.session.value = session("g1", isGuest = true)
        advanceUntilIdle()
        assertEquals(0, scheduler.now)

        // Sign-up upgrades the guest: same id, now registered
        auth.session.value = session("g1", isGuest = false)
        advanceUntilIdle()
        assertEquals(1, scheduler.now)

        // A token refresh keeps the account: no extra sync
        auth.session.value = session("g1", isGuest = false).copy(accessToken = "a2")
        advanceUntilIdle()
        assertEquals(1, scheduler.now)

        auth.session.value = session("u2", isGuest = false)
        advanceUntilIdle()
        assertEquals(2, scheduler.now)
        assertEquals(1, scheduler.periodic)
        job.cancel()
    }
}
