package app.enjoylearning504.core.network

sealed interface NetworkResult<out T> {
    data class Success<T>(val value: T) : NetworkResult<T>

    /** The server answered with an error status; [message] is its plain-text body. */
    data class HttpError(val status: Int, val message: String) : NetworkResult<Nothing>

    /** No usable response: offline, timeout, DNS, TLS. */
    data class ConnectionError(val cause: Throwable) : NetworkResult<Nothing>
}
