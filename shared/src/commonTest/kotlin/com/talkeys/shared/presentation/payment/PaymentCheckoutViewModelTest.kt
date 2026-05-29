package com.talkeys.shared.presentation.payment

import com.talkeys.shared.data.payment.EventInfo
import com.talkeys.shared.data.payment.PaymentOrderData
import com.talkeys.shared.data.payment.PaymentRepository
import com.talkeys.shared.data.payment.PaymentStatusData
import com.talkeys.shared.network.ApiClient
import com.talkeys.shared.network.PaymentApiService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PaymentCheckoutViewModelTest {

    @Test
    fun urlBuilder_preservesFullCheckoutUrl() {
        val url = "https://mercury-t2.phonepe.com/transact/pg?token=abc"

        assertEquals(url, PhonePeCheckoutUrlBuilder.buildCheckoutUrl(url, isProduction = false))
    }

    @Test
    fun urlBuilder_usesSandboxBaseAndEncodesToken() {
        val url = PhonePeCheckoutUrlBuilder.buildCheckoutUrl("abc+123/=", isProduction = false)

        assertEquals("https://mercury-t2.phonepe.com/transact/pg?token=abc%2B123%2F%3D", url)
    }

    @Test
    fun startCheckout_requiresAuthToken() = runTest {
        val viewModel = PaymentCheckoutViewModel(fakeRepository(Result.success(orderData())))

        viewModel.startCheckout("event-1", "General", emptyList(), authToken = null)

        assertFalse(viewModel.checkoutState.value.isLoading)
        assertEquals("Please login to continue", viewModel.checkoutState.value.errorMessage)
    }

    @Test
    fun startCheckout_setsCheckoutDataFromRepositoryOrder() = runTest {
        val viewModel = PaymentCheckoutViewModel(
            repository = fakeRepository(Result.success(orderData(token = "token+abc"))),
            isPhonePeProduction = false
        )

        viewModel.startCheckout("event-1", "General", emptyList(), authToken = "jwt")
        advanceUntilIdle()

        val checkout = assertNotNull(viewModel.checkoutState.value.checkoutData)
        assertEquals("merchant-1", checkout.merchantOrderId)
        assertEquals("pass-1", checkout.passId)
        assertEquals("https://mercury-t2.phonepe.com/transact/pg?token=token%2Babc", checkout.paymentUrl)
    }

    @Test
    fun verifyPaymentStatus_setsCompletedStatus() = runTest {
        val viewModel = PaymentCheckoutViewModel(
            repository = fakeRepository(
                bookTicketResult = Result.success(orderData()),
                statusResult = Result.success(PaymentStatusData("pass-1", "uuid-1", "COMPLETED"))
            )
        )

        viewModel.verifyPaymentStatus("merchant-1", "jwt")
        advanceUntilIdle()

        assertEquals("COMPLETED", viewModel.verificationState.value.status)
        assertEquals("pass-1", viewModel.verificationState.value.passId)
        assertTrue(viewModel.verificationState.value.errorMessage == null)
    }

    private fun fakeRepository(
        bookTicketResult: Result<PaymentOrderData>,
        statusResult: Result<PaymentStatusData> = Result.failure(Exception("not used"))
    ): PaymentRepository = object : PaymentRepository(PaymentApiService(ApiClient())) {
        override suspend fun bookTicket(
            eventId: String,
            passType: String,
            friends: List<com.talkeys.shared.data.payment.Friend>,
            authToken: String?
        ): Result<PaymentOrderData> = bookTicketResult

        override suspend fun verifyPaymentStatus(
            merchantOrderId: String,
            authToken: String?
        ): Result<PaymentStatusData> = statusResult
    }

    private fun orderData(token: String = "token") = PaymentOrderData(
        passId = "pass-1",
        merchantOrderId = "merchant-1",
        orderId = "order-1",
        amount = 100,
        amountInPaisa = 10000,
        totalTickets = 1,
        token = token,
        event = EventInfo(id = "event-1"),
        qrStrings = emptyList(),
        friends = emptyList()
    )
}
