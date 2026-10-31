package app.enjoylearning504.core.network

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ConfigApiTest {
    @Test
    fun `reads conf json even when served as text`() = runTest {
        val engine = MockEngine { request ->
            assertEquals("/504/conf.json", request.url.encodedPath)
            respond("""{"test":25,"ttest":2}""", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "text/plain"))
        }
        val config = ConfigApi(HttpClientFactory.configClient(engine)).fetch()
        assertEquals(2, config?.ttest)
        assertEquals(25, config?.test)
    }

    @Test
    fun `missing file gives null so callers use defaults`() = runTest {
        val engine = MockEngine { respond("not found", HttpStatusCode.NotFound) }
        assertNull(ConfigApi(HttpClientFactory.configClient(engine)).fetch())
    }
}
