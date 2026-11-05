package app.enjoylearning504.core.network

import io.ktor.client.HttpClient

/** Progress sync on the Memrica backend: one round trip pushes local changes and pulls the server's. */
class ProgressApi(private val client: HttpClient) {

    suspend fun sync(request: SyncRequest): NetworkResult<SyncResponse> = client.postJson("progress/sync", request)
}
