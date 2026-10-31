package app.enjoylearning504.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.call.body
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/** Reads and writes the tokens the Memrica client sends. Implemented by the data layer. */
interface TokenProvider {
    suspend fun load(): BearerTokens?

    /** Called after a successful refresh. */
    suspend fun onRefreshed(response: AuthResponse)

    /** Called when the refresh token is rejected; the session is over. */
    suspend fun onSessionExpired()
}

object HttpClientFactory {

    internal val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    fun configClient(engine: HttpClientEngine = OkHttp.create()): HttpClient = HttpClient(engine) {
        expectSuccess = true
        install(ContentNegotiation) {
            // conf.json may be served as text/plain
            json(json)
            json(json, ContentType.Text.Plain)
        }
        install(HttpTimeout) { requestTimeoutMillis = TIMEOUT_MS }
        defaultRequest { url(BuildConfig.CONFIG_BASE_URL) }
    }

    fun memricaClient(
        tokens: TokenProvider,
        engine: HttpClientEngine = OkHttp.create(),
        baseUrl: String = BuildConfig.MEMRICA_BASE_URL,
    ): HttpClient = HttpClient(engine) {
        install(ContentNegotiation) { json(json) }
        install(HttpTimeout) { requestTimeoutMillis = TIMEOUT_MS }
        if (BuildConfig.DEBUG) {
            install(Logging) { level = LogLevel.INFO }
        }
        install(Auth) {
            bearer {
                loadTokens { tokens.load() }
                // Optional-auth endpoints (sign-up, sign-in) need the guest token to upgrade the guest
                sendWithoutRequest { true }
                refreshTokens {
                    val refreshToken = oldTokens?.refreshToken ?: return@refreshTokens null
                    val response = runCatching {
                        client.post("auth/refresh") {
                            markAsRefreshTokenRequest()
                            contentType(ContentType.Application.Json)
                            setBody(RefreshRequest(refreshToken))
                        }
                    }.getOrNull()
                    if (response != null && response.status.isSuccess()) {
                        val body = response.body<AuthResponse>()
                        tokens.onRefreshed(body)
                        BearerTokens(body.accessToken, body.refreshToken)
                    } else {
                        if (response != null) tokens.onSessionExpired()
                        null
                    }
                }
            }
        }
        defaultRequest { url(baseUrl) }
    }

    private const val TIMEOUT_MS = 15_000L
}
