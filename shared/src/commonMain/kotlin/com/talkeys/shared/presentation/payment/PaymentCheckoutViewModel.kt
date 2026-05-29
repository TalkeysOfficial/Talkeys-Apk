package com.talkeys.shared.presentation.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talkeys.shared.config.ProductionConfig
import com.talkeys.shared.data.payment.Friend
import com.talkeys.shared.data.payment.PaymentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PaymentCheckoutViewModel(
    private val repository: PaymentRepository,
    private val isPhonePeProduction: Boolean = ProductionConfig.IS_PHONEPE_PRODUCTION
) : ViewModel() {

    private val _checkoutState = MutableStateFlow(PaymentCheckoutUiState())
    val checkoutState: StateFlow<PaymentCheckoutUiState> = _checkoutState.asStateFlow()

    private val _verificationState = MutableStateFlow(PaymentVerificationUiState())
    val verificationState: StateFlow<PaymentVerificationUiState> = _verificationState.asStateFlow()

    fun startCheckout(
        eventId: String,
        passType: String,
        friends: List<Friend>,
        authToken: String?
    ) {
        if (authToken.isNullOrBlank()) {
            _checkoutState.value = PaymentCheckoutUiState(errorMessage = "Please login to continue")
            return
        }

        _checkoutState.value = PaymentCheckoutUiState(isLoading = true)
        viewModelScope.launch {
            repository.bookTicket(eventId, passType, friends, authToken)
                .onSuccess { paymentOrder ->
                    _checkoutState.value = PaymentCheckoutUiState(
                        checkoutData = PaymentCheckoutData(
                            paymentUrl = PhonePeCheckoutUrlBuilder.buildCheckoutUrl(
                                tokenOrUrl = paymentOrder.token,
                                isProduction = isPhonePeProduction
                            ),
                            merchantOrderId = paymentOrder.merchantOrderId,
                            passId = paymentOrder.passId
                        )
                    )
                }
                .onFailure { error ->
                    _checkoutState.value = PaymentCheckoutUiState(
                        errorMessage = error.message ?: "Failed to create payment"
                    )
                }
        }
    }

    fun verifyPaymentStatus(merchantOrderId: String, authToken: String?) {
        _verificationState.value = PaymentVerificationUiState(isLoading = true)
        viewModelScope.launch {
            repository.verifyPaymentStatus(merchantOrderId, authToken)
                .onSuccess { status ->
                    _verificationState.value = PaymentVerificationUiState(
                        passId = status.passId,
                        passUUID = status.passUUID,
                        status = status.paymentStatus
                    )
                }
                .onFailure { error ->
                    _verificationState.value = PaymentVerificationUiState(
                        errorMessage = error.message ?: "Failed to verify payment"
                    )
                }
        }
    }

    fun clearCheckout() {
        _checkoutState.value = PaymentCheckoutUiState()
    }

    fun clearVerification() {
        _verificationState.value = PaymentVerificationUiState()
    }
}
