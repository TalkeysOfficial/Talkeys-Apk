package com.talkeys.shared.data.dashboard

import com.talkeys.shared.network.ApiClient
import com.talkeys.shared.network.ApiError
import com.talkeys.shared.network.ApiResult
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.timeout
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonObject

class DashboardApi(
    private val httpClient: HttpClient,
    baseUrl: String = ApiClient.BASE_URL
) {
    constructor(apiClient: ApiClient) : this(apiClient.httpClient)

    private val baseUrl = baseUrl.trimEnd('/')

    suspend fun getUserProfile(token: String): ApiResult<UserProfile> = safeCall {
        httpClient.get("$baseUrl/dashboard/profile") {
            bearerAuth(token)
            timeout { requestTimeoutMillis = REQUEST_TIMEOUT_MILLIS }
        }
    }

    suspend fun updateUserProfile(
        token: String,
        profileData: Map<String, String>
    ): ApiResult<UserProfile> = safeCall {
        httpClient.patch("$baseUrl/dashboard/profile") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(profileData)
            timeout { requestTimeoutMillis = REQUEST_TIMEOUT_MILLIS }
        }
    }

    suspend fun getUserEvents(
        token: String,
        type: String,
        status: String? = null,
        period: String? = null
    ): ApiResult<UserEventsResponse> = safeCall {
        httpClient.get("$baseUrl/dashboard/events") {
            bearerAuth(token)
            parameter("type", type)
            status?.let { parameter("status", it) }
            period?.let { parameter("period", it) }
            timeout { requestTimeoutMillis = REQUEST_TIMEOUT_MILLIS }
        }
    }

    suspend fun getRecentActivity(
        token: String,
        range: String = DashboardPeriod.OneMonth.wireValue
    ): ApiResult<JsonObject> = safeCall {
        httpClient.get("$baseUrl/dashboard/activity") {
            bearerAuth(token)
            parameter("range", range)
            timeout { requestTimeoutMillis = REQUEST_TIMEOUT_MILLIS }
        }
    }

    private suspend inline fun <reified T> safeCall(
        crossinline block: suspend () -> HttpResponse
    ): ApiResult<T> {
        return try {
            val response = block()
            if (response.status.isSuccess()) {
                ApiResult.Success(response.body())
            } else {
                val safeBody = runCatching { response.bodyAsText().take(500) }.getOrNull()
                ApiResult.Failure(ApiError.HttpError(response.status.value, safeBody))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            val message = e.message ?: "Unknown network error"
            when {
                isParseError(e) -> ApiResult.Failure(ApiError.ParseError(message))
                isNetworkError(e) -> ApiResult.Failure(ApiError.NetworkError)
                else -> ApiResult.Failure(ApiError.Unknown(message))
            }
        }
    }

    private fun isParseError(e: Exception): Boolean =
        generateSequence<Throwable>(e) { it.cause }
            .any { it is SerializationException || it::class.simpleName?.contains("JsonConvert", ignoreCase = true) == true }

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
