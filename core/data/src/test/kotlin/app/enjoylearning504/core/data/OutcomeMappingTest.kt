package app.enjoylearning504.core.data

import app.enjoylearning504.core.data.repository.toOutcome
import app.enjoylearning504.core.model.AppError
import app.enjoylearning504.core.model.Outcome
import app.enjoylearning504.core.network.NetworkResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class OutcomeMappingTest {
    private suspend fun map(result: NetworkResult<Unit>) = result.toOutcome(AppError.InvalidCode) { }

    @Test
    fun `backend status codes map to domain errors`() = runTest {
        assertEquals(Outcome.Failure(AppError.InvalidCode), map(NetworkResult.HttpError(401, "Invalid or expired code")))
        assertEquals(Outcome.Failure(AppError.AccountExists), map(NetworkResult.HttpError(409, "exists")))
        assertEquals(Outcome.Failure(AppError.RateLimited), map(NetworkResult.HttpError(429, "slow down")))
        assertEquals(Outcome.Failure(AppError.Validation("bad phone")), map(NetworkResult.HttpError(400, "bad phone")))
        assertEquals(Outcome.Failure(AppError.NoConnection), map(NetworkResult.ConnectionError(RuntimeException())))
        assertEquals(Outcome.Success(Unit), map(NetworkResult.Success(Unit)))
    }
}
