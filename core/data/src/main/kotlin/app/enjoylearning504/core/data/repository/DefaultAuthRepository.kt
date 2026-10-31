package app.enjoylearning504.core.data.repository

import app.enjoylearning504.core.datastore.StoredTokens
import app.enjoylearning504.core.datastore.TokenStorage
import app.enjoylearning504.core.domain.repository.AuthRepository
import app.enjoylearning504.core.model.AppError
import app.enjoylearning504.core.model.AuthSession
import app.enjoylearning504.core.model.Outcome
import app.enjoylearning504.core.network.AuthApi
import app.enjoylearning504.core.network.AuthResponse
import app.enjoylearning504.core.network.NetworkResult
import app.enjoylearning504.core.network.TokenProvider
import io.ktor.client.plugins.auth.providers.BearerTokens
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class DefaultAuthRepository(
    private val api: AuthApi,
    private val storage: TokenStorage,
    private val clock: () -> Long = { System.currentTimeMillis() / 1000 },
) : AuthRepository {
    private val guestMutex = Mutex()

    override val session: Flow<AuthSession?> = storage.tokens.map { it?.toSession() }

    override suspend fun ensureGuestSession() {
        guestMutex.withLock {
            if (storage.current() != null) return
            val result = api.registerGuest()
            if (result is NetworkResult.Success) save(result.value, isGuest = true)
        }
    }

    override suspend fun registerWithEmail(email: String): Outcome<Unit> =
        api.registerEmail(email.trim()).toOutcome(AppError.AccountExists) { }

    override suspend fun verifyEmail(email: String, code: String, password: String): Outcome<AuthSession> =
        api.verifyEmail(email.trim(), code.trim(), password).signedIn(AppError.InvalidCode)

    override suspend fun loginWithEmail(email: String, password: String): Outcome<AuthSession> =
        api.login(email.trim(), password).signedIn(AppError.InvalidCredentials)

    override suspend fun sendOtp(phone: String): Outcome<Unit> = api.sendOtp(phone.trim()).toOutcome(AppError.InvalidCode) { }

    override suspend fun verifyOtp(phone: String, code: String): Outcome<AuthSession> =
        api.verifyOtp(phone.trim(), code.trim()).signedIn(AppError.InvalidCode)

    override suspend fun forgotPassword(email: String): Outcome<Unit> =
        api.forgotPassword(email.trim()).toOutcome(AppError.InvalidCredentials) { }

    override suspend fun resetPassword(email: String, code: String, password: String): Outcome<AuthSession> =
        api.resetPassword(email.trim(), code.trim(), password).signedIn(AppError.InvalidCode)

    override suspend fun logout() {
        storage.current()?.let { api.logout(it.refreshToken) }
        storage.clear()
        api.clearCachedTokens()
        ensureGuestSession()
    }

    private suspend fun NetworkResult<AuthResponse>.signedIn(unauthorized: AppError): Outcome<AuthSession> =
        toOutcome(unauthorized) { save(it, isGuest = false) }

    private suspend fun save(response: AuthResponse, isGuest: Boolean): AuthSession {
        val tokens = response.toStored(isGuest, clock())
        storage.save(tokens)
        api.clearCachedTokens()
        return tokens.toSession()
    }
}

/** Gives the Ktor bearer plugin the stored tokens and keeps them updated after refreshes. */
class StoredTokenProvider(
    private val storage: TokenStorage,
    private val clock: () -> Long = { System.currentTimeMillis() / 1000 },
) : TokenProvider {
    override suspend fun load(): BearerTokens? =
        storage.current()?.let { BearerTokens(it.accessToken, it.refreshToken) }

    override suspend fun onRefreshed(response: AuthResponse) {
        val isGuest = storage.current()?.isGuest ?: true
        storage.save(response.toStored(isGuest, clock()))
    }

    override suspend fun onSessionExpired() = storage.clear()
}

private fun AuthResponse.toStored(isGuest: Boolean, nowSeconds: Long) = StoredTokens(
    userId = userId,
    accessToken = accessToken,
    refreshToken = refreshToken,
    expiresAtEpochSeconds = nowSeconds + expiresIn,
    isGuest = isGuest,
)

private fun StoredTokens.toSession() = AuthSession(userId, accessToken, refreshToken, expiresAtEpochSeconds, isGuest)

/** Maps the backend's status codes (see writeServiceError in auth_handler.go) to domain errors. */
internal suspend fun <T, R> NetworkResult<T>.toOutcome(unauthorized: AppError, onSuccess: suspend (T) -> R): Outcome<R> =
    when (this) {
        is NetworkResult.Success -> Outcome.Success(onSuccess(value))
        is NetworkResult.ConnectionError -> Outcome.Failure(AppError.NoConnection)
        is NetworkResult.HttpError -> Outcome.Failure(
            when (status) {
                400 -> AppError.Validation(message)
                401 -> unauthorized
                409 -> AppError.AccountExists
                429 -> AppError.RateLimited
                else -> AppError.Unexpected(message)
            },
        )
    }
