package app.enjoylearning504.core.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Mirrors internal/handler/auth_handler.go in the Memrica backend

@Serializable
data class RegisterRequest(val email: String = "", @SerialName("auth_type") val authType: String)

@Serializable
data class VerifyEmailRequest(val email: String, val code: String, val password: String)

@Serializable
data class LoginRequest(val email: String, val password: String, @SerialName("auth_type") val authType: String = "email")

@Serializable
data class SendOtpRequest(val phone: String)

@Serializable
data class VerifyOtpRequest(val phone: String, val code: String)

@Serializable
data class ForgotPasswordRequest(val email: String)

@Serializable
data class ResetPasswordRequest(val email: String, val code: String, val password: String)

@Serializable
data class RefreshRequest(@SerialName("refresh_token") val refreshToken: String)

@Serializable
data class AuthResponse(
    @SerialName("user_id") val userId: String,
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("expires_in") val expiresIn: Int,
    @SerialName("is_new_user") val isNewUser: Boolean = false,
)

/** conf.json from the original app's server. Field names are the server's. */
@Serializable
data class RemoteConfigDto(
    /** Latest version code. */
    val test: Int? = null,
    /** Ad turn: how many screens between interstitials. */
    val ttest: Int? = null,
)
