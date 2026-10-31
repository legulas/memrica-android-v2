package app.enjoylearning504.core.model

data class AuthSession(
    val userId: String,
    val accessToken: String,
    val refreshToken: String,
    val expiresAtEpochSeconds: Long,
    val isGuest: Boolean,
)

/** Outcome of an operation that can fail for a reason the user should see. */
sealed interface Outcome<out T> {
    data class Success<T>(val value: T) : Outcome<T>
    data class Failure(val error: AppError) : Outcome<Nothing>
}

sealed interface AppError {
    data object NoConnection : AppError
    data object InvalidCredentials : AppError
    data object InvalidCode : AppError
    data object AccountExists : AppError
    data object RateLimited : AppError
    data class Validation(val message: String) : AppError
    data class Unexpected(val message: String?) : AppError
}
