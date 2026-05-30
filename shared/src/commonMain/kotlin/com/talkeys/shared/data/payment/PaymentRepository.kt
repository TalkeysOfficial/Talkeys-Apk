package com.talkeys.shared.data.payment

import com.talkeys.shared.network.PaymentApiService
import com.talkeys.shared.config.ProductionConfig
import com.talkeys.shared.auth.TokenStorage
import co.touchlab.kermit.Logger

open class PaymentRepository(
    private val paymentApiService: PaymentApiService,
    private val tokenStorage: TokenStorage? = null
) {
    
    private val logger = Logger.withTag("PaymentRepository")
    
    init {
        // Production validation
        if (ProductionConfig.IS_PRODUCTION) {
            logger.i { "PaymentRepository initialized in PRODUCTION mode" }
        }
    }
    
    /**
     * Book ticket and get payment order details
     */
    open suspend fun bookTicket(
        eventId: String,
        passType: String,
        friends: List<Friend>,
        teamCode: String? = null,
        authToken: String? = null
    ): Result<PaymentOrderData> {
        // Production validation
        if (friends.size > ProductionConfig.MAX_FRIENDS_PER_BOOKING) {
            return Result.failure(Exception("Maximum ${ProductionConfig.MAX_FRIENDS_PER_BOOKING} friends allowed per booking"))
        }
        
        val resolvedAuthToken = resolveAuthToken(authToken)
            ?: return Result.failure(Exception("Please login to continue"))

        logger.i { "Production: Booking ticket for event: $eventId, passType: $passType, friends: ${friends.size}" }
        
        val request = BookTicketRequest(
            eventId = eventId,
            passType = passType,
            friends = friends,
            teamCode = teamCode?.trim()?.takeIf { it.isNotEmpty() }
        )
        
        return try {
            val result = paymentApiService.bookTicketApp(request, resolvedAuthToken)
            
            result.fold(
                onSuccess = { response ->
                    if (response.success && response.data != null) {
                        logger.d { "Ticket booking successful" }
                        Result.success(response.data)
                    } else {
                        val error = "Booking failed: ${response.message}"
                        logger.e { error }
                        Result.failure(Exception(error))
                    }
                },
                onFailure = { exception ->
                    logger.e(exception) { "Network error during ticket booking" }
                    Result.failure(exception)
                }
            )
        } catch (e: Exception) {
            logger.e(e) { "Unexpected error during ticket booking" }
            Result.failure(e)
        }
    }
    
    /**
     * Verify payment status after PhonePe payment completion
     */
    open suspend fun verifyPaymentStatus(merchantOrderId: String, authToken: String? = null): Result<PaymentStatusData> {
        val resolvedAuthToken = resolveAuthToken(authToken)
            ?: return Result.failure(Exception("Please login to verify payment"))

        logger.d { "Verifying payment status" }
        
        return try {
            val result = paymentApiService.checkPaymentStatus(merchantOrderId, resolvedAuthToken)
            
            result.fold(
                onSuccess = { response ->
                    if (response.success && response.data != null) {
                        logger.d { "Payment response status: ${response.status}" }
                        logger.d { "Payment data status: ${response.data.paymentStatus}" }
                        
                        // ✅ Additional validation for payment status data
                        val paymentData = response.data
                        if (paymentData.passId.isBlank()) {
                            logger.e { "Invalid payment data: passId is blank" }
                            return Result.failure(Exception("Invalid payment data: passId is missing"))
                        }
                        
                        if (paymentData.paymentStatus.isBlank()) {
                            logger.e { "Invalid payment data: paymentStatus is blank" }
                            return Result.failure(Exception("Invalid payment data: paymentStatus is missing"))
                        }
                        
                        if (paymentData.passUUID != null) {
                            logger.d { "Payment data includes a pass UUID" }
                        } else {
                            logger.d { "Payment data does not include passUUID (this is now optional)" }
                        }
                        
                        Result.success(paymentData)
                    } else {
                        val error = "Payment verification failed: success=${response.success}"
                        logger.e { error }
                        Result.failure(Exception(error))
                    }
                },
                onFailure = { exception ->
                    logger.e(exception) { "Network error during payment verification" }
                    
                    // ✅ Enhanced error logging for serialization issues
                    if (exception.message?.contains("passUUID") == true) {
                        logger.e { "SERIALIZATION ERROR: Backend response missing passUUID field" }
                        logger.e { "SOLUTION: This should now be fixed with optional passUUID" }
                        logger.e { "If this error persists, check backend response format" }
                    }
                    
                    Result.failure(exception)
                }
            )
        } catch (e: Exception) {
            logger.e(e) { "Unexpected error during payment verification" }
            Result.failure(e)
        }
    }

    private suspend fun resolveAuthToken(authToken: String?): String? {
        return authToken?.trim()?.takeIf { it.isNotBlank() }
            ?: tokenStorage?.getToken()?.trim()?.takeIf { it.isNotBlank() }
    }
}
