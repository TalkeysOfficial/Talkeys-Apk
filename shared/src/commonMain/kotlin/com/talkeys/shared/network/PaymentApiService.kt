package com.talkeys.shared.network

import com.talkeys.shared.data.payment.BookTicketRequest
import com.talkeys.shared.data.payment.BookTicketResponse
import com.talkeys.shared.data.payment.PaymentStatusResponse
import io.ktor.client.call.*
import io.ktor.client.plugins.timeout
import io.ktor.client.request.*
import io.ktor.http.*

class PaymentApiService(private val apiClient: ApiClient) {
    
    /**
     * Book ticket and create payment order
     * POST /api/book-ticket-app
     */
    suspend fun bookTicketApp(request: BookTicketRequest, authToken: String? = null): Result<BookTicketResponse> {
        return try {
            val response = apiClient.httpClient.post("${ApiClient.BASE_URL}/api/book-ticket-app") {
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
                Result.success(response.body<BookTicketResponse>())
            } else {
                Result.failure(Exception("HTTP ${response.status.value}: ${response.status.description}"))
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
                Result.failure(Exception("HTTP ${response.status.value}: ${response.status.description}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}