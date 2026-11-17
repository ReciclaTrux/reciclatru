package pe.edu.upn.reciclatru.core.network

import pe.edu.upn.reciclatru.BuildConfig

private const val ApiPathMarker = "/api/"

fun String.toAbsoluteApiUrl(baseUrl: String = BuildConfig.API_BASE_URL): String {
    if (startsWith("http://") || startsWith("https://")) return this
    val origin = baseUrl.substringBefore(ApiPathMarker)
    return origin + if (startsWith("/")) this else "/$this"
}
