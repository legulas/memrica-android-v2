package app.enjoylearning504.core.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import io.ktor.client.request.get

/** The Memrica backend's auth endpoints (internal/routes/routes.go). */
class AuthApi(private val client: HttpClient) {

    suspend fun registerGuest(): NetworkResult<AuthResponse> = post("auth/register", RegisterRequest(authType = "guest"))

    suspend fun registerEmail(email: String): NetworkResult<Unit> =
        post<Unit>("auth/register", RegisterRequest(email = email, authType = "email"))

    suspend fun verifyEmail(email: String, code: String, password: String): NetworkResult<AuthResponse> =
        post("auth/verify-email", VerifyEmailRequest(email, code, password))

    suspend fun login(email: String, password: String): NetworkResult<AuthResponse> =
        post("auth/login", LoginRequest(email, password, authType = "email"))

    suspend fun sendOtp(phone: String): NetworkResult<Unit> = post("auth/send-otp", SendOtpRequest(phone))

    suspend fun verifyOtp(phone: String, code: String): NetworkResult<AuthResponse> =
        post("auth/verify-otp", VerifyOtpRequest(phone, code))

    suspend fun forgotPassword(email: String): NetworkResult<Unit> =
        post("auth/forgot-password", ForgotPasswordRequest(email))

    suspend fun resetPassword(email: String, code: String, password: String): NetworkResult<AuthResponse> =
        post("auth/reset-password", ResetPasswordRequest(email, code, password))

    suspend fun logout(refreshToken: String): NetworkResult<Unit> = post("auth/logout", RefreshRequest(refreshToken))

    suspend fun me(): NetworkResult<String> = networkCall { client.get("me") }

    /** Forgets the bearer tokens the client cached, after sign-in, sign-out or a new guest session. */
    fun clearCachedTokens() {
        client.authProvider<BearerAuthProvider>()?.clearToken()
    }

    private suspend inline fun <reified T> post(path: String, body: Any): NetworkResult<T> = client.postJson(path, body)

}
