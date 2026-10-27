package pe.edu.upn.reciclatru.core.network

import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.coroutines.cancellation.CancellationException
import retrofit2.HttpException

/** Tipo de falla que la pantalla puede explicar sin conocer Retrofit ni OkHttp. */
enum class NetworkErrorKind {
    NoConnection,
    Timeout,
    Unauthorized,
    Forbidden,
    NotFound,
    Conflict,
    Server,
    Unknown,
}

/**
 * Error de red o de la API ya traducido. El mensaje está en español y se puede mostrar
 * tal cual; la pantalla decide dónde (alerta, campo o estado de error).
 */
class NetworkException(
    val kind: NetworkErrorKind,
    override val message: String,
    val statusCode: Int? = null,
    cause: Throwable? = null,
) : IOException(message, cause)

private const val NoConnectionMessage = "No tienes conexión. Revisa tu red e inténtalo de nuevo."
private const val TimeoutMessage = "La conexión tardó demasiado. Inténtalo de nuevo."
private const val ServerMessage = "Tuvimos un problema en el servidor. Inténtalo de nuevo en un momento."
private const val UnknownMessage = "No pudimos completar la operación. Inténtalo de nuevo."

private fun httpError(statusCode: Int): NetworkException = when (statusCode) {
    401 -> NetworkException(
        kind = NetworkErrorKind.Unauthorized,
        message = "Tu sesión venció. Vuelve a ingresar.",
        statusCode = statusCode,
    )
    403 -> NetworkException(
        kind = NetworkErrorKind.Forbidden,
        message = "No tienes permiso para hacer esto.",
        statusCode = statusCode,
    )
    404 -> NetworkException(
        kind = NetworkErrorKind.NotFound,
        message = "No encontramos la información que buscabas.",
        statusCode = statusCode,
    )
    409 -> NetworkException(
        kind = NetworkErrorKind.Conflict,
        message = "Ese dato ya está en uso. Revisa e inténtalo de nuevo.",
        statusCode = statusCode,
    )
    in 500..599 -> NetworkException(
        kind = NetworkErrorKind.Server,
        message = ServerMessage,
        statusCode = statusCode,
    )
    else -> NetworkException(
        kind = NetworkErrorKind.Unknown,
        message = UnknownMessage,
        statusCode = statusCode,
    )
}

private fun Throwable.toNetworkException(): NetworkException = when (this) {
    is NetworkException -> this
    is HttpException -> httpError(code())
    is SocketTimeoutException -> NetworkException(NetworkErrorKind.Timeout, TimeoutMessage, cause = this)
    is UnknownHostException -> NetworkException(NetworkErrorKind.NoConnection, NoConnectionMessage, cause = this)
    is IOException -> NetworkException(NetworkErrorKind.NoConnection, NoConnectionMessage, cause = this)
    else -> NetworkException(NetworkErrorKind.Unknown, UnknownMessage, cause = this)
}

/**
 * Envuelve una llamada a la API: devuelve `Result.success` o un [NetworkException] ya
 * listo para mostrar. No atrapa [CancellationException]: cancelar no es un error.
 */
suspend fun <T> apiCall(block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch (cancellation: CancellationException) {
    throw cancellation
} catch (error: Exception) {
    Result.failure(error.toNetworkException())
}

/** Mensaje para el usuario; si no es un error de red, usa [fallback]. */
fun Throwable.toUserMessage(fallback: String = UnknownMessage): String =
    (this as? NetworkException)?.message ?: fallback
