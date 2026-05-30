package com.talkeys.shared.network

import com.talkeys.shared.data.payment.BookTicketRequest
import com.talkeys.shared.data.payment.BookTicketResponse
import com.talkeys.shared.data.payment.PaymentStatusResponse
import io.ktor.client.call.*
import io.ktor.client.statement.bodyAsText
import io.ktor.client.plugins.timeout
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class PaymentApiService(private val apiClient: ApiClient) {
    private val json = Json {
        isLenient = true
        ignoreUnknownKeys = true
    }
    
    /**
     * Book ticket and create payment order
     * POST /api/book-ticket
     */
    suspend fun bookTicketApp(request: BookTicketRequest, authToken: String? = null): Result<BookTicketResponse> {
        return try {
            val response = apiClient.httpClient.post("${ApiClient.BASE_URL}/api/book-ticket") {
                contentType(ContentType.Application.Json)
                authToken?.let { token ->
                    header("Authorization", "Bearer $token")
                }
                timeout {
                    requestTimeoutMillis = 30000
                }
                setBody(request)
            }

            if (response.status.isSuccess()) {
                val rawBody = response.bodyAsText()
                val parsed = try {
                    json.decodeFromString<BookTicketResponse>(rawBody)
                } catch (exception: SerializationException) {
                    return Result.failure(
                        Exception(
                            buildString {
                                append("Unable to read payment response")
                                val shape = describePayloadShape(rawBody)
                                if (shape != null) {
                                    append(": ")
                                    append(shape)
                                }
                            },
                            exception
                        )
                    )
                }
                Result.success(parsed)
            } else {
                val errorBody = runCatching { response.bodyAsText() }.getOrNull()
                Result.failure(Exception(formatHttpError(response.status, errorBody)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check payment status after PhonePe payment completion
     * GET /api/payment/app-status-check/:merchantOrderId
     */
    suspend fun checkPaymentStatus(merchantOrderId: String, authToken: String? = null): Result<PaymentStatusResponse> {
        return try {
            val response = apiClient.httpClient.get("${ApiClient.BASE_URL}/api/payment/app-status-check/$merchantOrderId") {
                
                // Add authentication header if token is provided
                authToken?.let { token ->
                    header("Authorization", "Bearer $token")
                }
                
                // Request timeout (30 seconds)
                timeout {
                    requestTimeoutMillis = 30000
                }
            }
            
            if (response.status.isSuccess()) {
                val statusResponse = response.body<PaymentStatusResponse>()
                Result.success(statusResponse)
            } else {
                val errorBody = runCatching { response.bodyAsText() }.getOrNull()
                Result.failure(Exception(formatHttpError(response.status, errorBody)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun formatHttpError(status: HttpStatusCode, body: String?): String {
        val trimmedBody = body?.trim()?.takeIf { it.isNotEmpty() }
        return buildString {
            append("HTTP ${status.value}: ${status.description}")
            if (trimmedBody != null) {
                append(" - ")
                append(trimmedBody)
            }
        }
    }

    private fun describePayloadShape(rawBody: String): String? {
        val root = runCatching { json.parseToJsonElement(rawBody).jsonObject }.getOrNull() ?: return null
        val data = root["data"] as? JsonObject
        val dataKeys = data?.keys?.sorted().orEmpty()

        val success = (root["success"] as? JsonPrimitive)?.booleanOrNull
                val message = (root["message"] as? JsonPrimitive)?.stringOrNull()

        return buildString {
            append("response keys=")
            append(root.keys.sorted())
            if (success != null) {
                append(", success=")
                append(success)
            }
            if (!message.isNullOrBlank()) {
                append(", message=")
                append(message)
            }
            if (data != null) {
                append(", data keys=")
                append(dataKeys)
                val merchantOrderId = (data["merchantOrderId"] as? JsonPrimitive)?.stringOrNull()
                val passId = (data["passId"] as? JsonPrimitive)?.stringOrNull()
                val hasToken = (data["token"] as? JsonPrimitive)?.stringOrNull()?.isNotBlank() == true
                val hasPaymentUrl = listOf("paymentUrl", "checkoutUrl", "redirectUrl", "url")
                    .any { key -> (data[key] as? JsonPrimitive)?.stringOrNull()?.isNotBlank() == true }
                val amount = (data["amount"] as? JsonPrimitive)?.intOrNull
                if (!merchantOrderId.isNullOrBlank()) {
                    append(", merchantOrderId present")
                }
                if (!passId.isNullOrBlank()) {
                    append(", passId present")
                }
                if (amount != null) {
                    append(", amount=")
                    append(amount)
                }
                append(", hasToken=")
                append(hasToken)
                append(", hasPaymentUrl=")
                append(hasPaymentUrl)
            }
        }
    }

    private fun JsonPrimitive.stringOrNull(): String? = runCatching { content }.getOrNull()
}
