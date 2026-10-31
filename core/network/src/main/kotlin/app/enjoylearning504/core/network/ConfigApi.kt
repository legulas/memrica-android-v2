package app.enjoylearning504.core.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlin.coroutines.cancellation.CancellationException

class ConfigApi(private val client: HttpClient) {
    /** Returns null when the file can't be read; callers fall back to defaults like the original app. */
    suspend fun fetch(): RemoteConfigDto? = try {
        client.get("conf.json").body<RemoteConfigDto>()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        null
    }
}
