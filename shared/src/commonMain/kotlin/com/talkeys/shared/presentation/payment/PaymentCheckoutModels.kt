package com.talkeys.shared.presentation.payment

data class PaymentCheckoutData(
    val paymentUrl: String,
    val merchantOrderId: String,
    val passId: String
)

data class PaymentCheckoutUiState(
    val isLoading: Boolean = false,
    val checkoutData: PaymentCheckoutData? = null,
    val errorMessage: String? = null
)

data class PaymentVerificationUiState(
    val isLoading: Boolean = false,
    val passId: String? = null,
    val passUUID: String? = null,
    val status: String? = null,
    val errorMessage: String? = null
)
