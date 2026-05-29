package com.talkeys.shared.data.events

import com.talkeys.shared.logging.AppLogger
import com.talkeys.shared.network.ApiClient
import com.talkeys.shared.network.ApiError
import com.talkeys.shared.network.ApiResult
import io.ktor.client.HttpClient
import io.ktor.client.call.*
import io.ktor.client.plugins.timeout
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.SerializationException

/**
 * Read-only events API client using the shared Ktor [ApiClient].
 *
 * Endpoint paths are copied from the confirmed Android Retrofit
 * `EventApiService`:
 *   - GET getEvents
 *   - GET getEventById/{id}
 */
class EventsApi(
    private val httpClient: HttpClient,
    baseUrl: String = ApiClient.BASE_URL
) {
    constructor(apiClient: ApiClient) : this(apiClient.httpClient)

    private val baseUrl = baseUrl.trimEnd('/')

    /**
     * Fetch the paginated events list.
     */
    suspend fun getAllEvents(): ApiResult<EventListResponse> = safeCall {
        httpClient.get("$baseUrl/getEvents") {
            timeout { requestTimeoutMillis = REQUEST_TIMEOUT_MILLIS }
        }
    }

    /**
     * Fetch full detail for a single event.
     */
    suspend fun getEventById(eventId: String): ApiResult<EventDetailResponse> = safeCall {
        httpClient.get("$baseUrl/getEventById/$eventId") {
            timeout { requestTimeoutMillis = REQUEST_TIMEOUT_MILLIS }
        }
    }

    // ── Internal helpers ────────────────────────────────────────────

    private suspend inline fun <reified T> safeCall(
        crossinline block: suspend () -> HttpResponse
    ): ApiResult<T> {
        return try {
            val response = block()
            if (response.status.isSuccess()) {
                try {
                    val body: T = response.body()
                    ApiResult.Success(body)
                } catch (e: SerializationException) {
                    AppLogger.e("EventsApi", "Parse error: ${e.message}")
                    ApiResult.Failure(ApiError.ParseError(e.message ?: "Deserialization failed"))
                } catch (e: Exception) {
                    AppLogger.e("EventsApi", "Body read error: ${e.message}")
                    ApiResult.Failure(ApiError.ParseError(e.message ?: "Response body read failed"))
                }
            } else {
                val safeBody = try {
                    response.bodyAsText().take(500)
                } catch (_: Exception) {
                    null
                }
                ApiResult.Failure(
                    ApiError.HttpError(response.status.value, safeBody)
                )
            }
        } catch (e: SerializationException) {
            AppLogger.e("EventsApi", "Parse error: ${e.message}")
            ApiResult.Failure(ApiError.ParseError(e.message ?: "Deserialization failed"))
        } catch (e: Exception) {
            // IOException, UnresolvedAddressException, ConnectException etc.
            val message = e.message ?: "Unknown network error"
            if (isNetworkError(e)) {
                AppLogger.e("EventsApi", "Network error: $message")
                ApiResult.Failure(ApiError.NetworkError)
            } else {
                AppLogger.e("EventsApi", "Unknown error: $message")
                ApiResult.Failure(ApiError.Unknown(message))
            }
        }
    }

    private fun isNetworkError(e: Exception): Boolean {
        val name = e::class.simpleName ?: ""
        return name.contains("IOException", ignoreCase = true) ||
            name.contains("ConnectException", ignoreCase = true) ||
            name.contains("UnresolvedAddress", ignoreCase = true) ||
            name.contains("SocketTimeout", ignoreCase = true) ||
            name.contains("Timeout", ignoreCase = true)
    }

    private companion object {
        const val REQUEST_TIMEOUT_MILLIS = 30_000L
    }
}
