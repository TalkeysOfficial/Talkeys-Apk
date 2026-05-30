package com.talkeys.shared.data.payment

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.JsonNames

@Serializable
data class BookTicketRequest(
    val eventId: String,
    val passType: String,
    val friends: List<Friend>,
    val teamCode: String? = null
)

@Serializable
data class Friend(
    val name: String,
    val email: String
)

@Serializable
data class BookTicketResponse(
    val success: Boolean,
    val message: String,
    val data: PaymentOrderData?
)

@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class PaymentOrderData(
    val passId: String,
    val merchantOrderId: String,
    @JsonNames("orderId", "phonePeOrderId")
    val orderId: String? = null,
    val amount: Int? = null,
    val amountInPaisa: Int? = null,
    val totalTickets: Int? = null,
    @JsonNames("token", "paymentToken", "payment_token")
    val token: String? = null,
    @SerialName("paymentUrl")
    @JsonNames("paymentUrl", "checkoutUrl", "redirectUrl", "url", "redirectURL")
    val paymentUrl: String? = null,
    val event: EventInfo? = null,
    val qrStrings: List<QrString> = emptyList(),
    val friends: List<Friend> = emptyList()
) {
    fun checkoutTokenOrUrl(): String? = paymentUrl?.takeIf { it.isNotBlank() }
        ?: token?.takeIf { it.isNotBlank() }
}

@Serializable
data class EventInfo(
    val id: String
)

@Serializable
data class QrString(
    val personName: String
)

@Serializable
data class PaymentStatusResponse(
    val success: Boolean,
    val status: String, // "COMPLETED", "FAILED", "PENDING"
    val data: PaymentStatusData?
)

@Serializable
data class PaymentStatusData(
    val passId: String,
    val passUUID: String? = null, // Make optional since backend may not always include it
    val paymentStatus: String
)
