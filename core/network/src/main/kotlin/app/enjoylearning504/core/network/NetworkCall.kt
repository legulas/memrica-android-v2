package app.enjoylearning504.core.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlin.coroutines.cancellation.CancellationException

/** Runs a request and turns its outcome into a [NetworkResult]; Ktor exceptions never leave this module. */
internal suspend inline fun <reified T> networkCall(block: () -> HttpResponse): NetworkResult<T> = try {
    val response = block()
    if (response.status.isSuccess()) {
        @Suppress("UNCHECKED_CAST")
        NetworkResult.Success(if (T::class == Unit::class) Unit as T else response.body<T>())
    } else {
        NetworkResult.HttpError(response.status.value, response.bodyAsText().trim())
    }
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    NetworkResult.ConnectionError(e)
}

internal suspend inline fun <reified T> HttpClient.postJson(path: String, body: Any): NetworkResult<T> = networkCall {
    post(path) {
        contentType(ContentType.Application.Json)
        setBody(body)
    }
}
