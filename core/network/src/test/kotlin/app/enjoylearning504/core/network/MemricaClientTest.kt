package app.enjoylearning504.core.network

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.client.plugins.auth.providers.BearerTokens
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MemricaClientTest {
    private val json = headersOf(HttpHeaders.ContentType, "application/json")

    private class FakeTokens(var tokens: BearerTokens?) : TokenProvider {
        var refreshed: AuthResponse? = null
        var expired = false
        override suspend fun load() = tokens
        override suspend fun onRefreshed(response: AuthResponse) { refreshed = response }
        override suspend fun onSessionExpired() { expired = true; tokens = null }
    }

    private fun authBody(access: String) =
        """{"user_id":"u1","access_token":"$access","refresh_token":"r2","expires_in":900,"is_new_user":false}"""

    @Test
    fun `guest registration posts auth_type guest and parses the session`() = runTest {
        val engine = MockEngine { request ->
            assertEquals("/auth/register", request.url.encodedPath)
            assertTrue(String(request.body.toByteArray()).contains("\"auth_type\":\"guest\""))
            respond(authBody("a1"), HttpStatusCode.OK, json)
        }
        val api = AuthApi(HttpClientFactory.memricaClient(FakeTokens(null), engine, "http://test/"))
        val result = api.registerGuest() as NetworkResult.Success
        assertEquals("a1", result.value.accessToken)
    }

    @Test
    fun `email sign-in sends auth_type email`() = runTest {
        // A default value would be left out of the JSON, and the server requires the field
        val engine = MockEngine { request ->
            assertEquals("/auth/login", request.url.encodedPath)
            assertTrue(String(request.body.toByteArray()).contains("\"auth_type\":\"email\""))
            respond(authBody("a1"), HttpStatusCode.OK, json)
        }
        val api = AuthApi(HttpClientFactory.memricaClient(FakeTokens(null), engine, "http://test/"))
        assertTrue(api.login("a@b.co", "password1") is NetworkResult.Success)
    }

    @Test
    fun `a 401 refreshes the token once and retries the request`() = runTest {
        val seen = mutableListOf<String>()
        val engine = MockEngine { request ->
            seen += "${request.url.encodedPath} ${request.headers[HttpHeaders.Authorization]}"
            when (request.url.encodedPath) {
                "/auth/refresh" -> respond(authBody("a2"), HttpStatusCode.OK, json)
                else -> if (request.headers[HttpHeaders.Authorization] == "Bearer a2") {
                    respond("Hello user: u1", HttpStatusCode.OK)
                } else {
                    respond("Unauthorized", HttpStatusCode.Unauthorized)
                }
            }
        }
        val tokens = FakeTokens(BearerTokens("a1", "r1"))
        val api = AuthApi(HttpClientFactory.memricaClient(tokens, engine, "http://test/"))

        val result = api.me()

        assertTrue(result is NetworkResult.Success)
        assertEquals("a2", tokens.refreshed?.accessToken)
        assertEquals(listOf("/me Bearer a1", "/auth/refresh null", "/me Bearer a2"), seen)
    }

    @Test
    fun `a rejected refresh ends the session`() = runTest {
        val engine = MockEngine { request ->
            when (request.url.encodedPath) {
                "/auth/refresh" -> respond("Invalid or expired refresh token", HttpStatusCode.Unauthorized)
                else -> respond("Unauthorized", HttpStatusCode.Unauthorized)
            }
        }
        val tokens = FakeTokens(BearerTokens("a1", "r1"))
        val api = AuthApi(HttpClientFactory.memricaClient(tokens, engine, "http://test/"))

        val result = api.me()

        assertEquals(401, (result as NetworkResult.HttpError).status)
        assertTrue(tokens.expired)
    }

    @Test
    fun `server error text is passed through`() = runTest {
        val engine = MockEngine { respond("user with this email already exists\n", HttpStatusCode.Conflict) }
        val api = AuthApi(HttpClientFactory.memricaClient(FakeTokens(null), engine, "http://test/"))
        val result = api.registerEmail("a@b.co") as NetworkResult.HttpError
        assertEquals(409, result.status)
        assertEquals("user with this email already exists", result.message)
    }

    @Test
    fun `no connection is reported as a connection error`() = runTest {
        val engine = MockEngine { throw java.io.IOException("offline") }
        val api = AuthApi(HttpClientFactory.memricaClient(FakeTokens(null), engine, "http://test/"))
        assertTrue(api.login("a@b.co", "password1") is NetworkResult.ConnectionError)
    }
}
