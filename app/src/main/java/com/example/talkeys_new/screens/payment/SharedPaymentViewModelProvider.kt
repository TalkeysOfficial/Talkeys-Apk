package com.example.talkeys_new.screens.payment

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.talkeys.shared.data.payment.PaymentRepository
import com.talkeys.shared.presentation.payment.PaymentCheckoutViewModel
import com.talkeys.shared.presentation.payment.PaymentCheckoutViewModelFactory
import org.koin.compose.koinInject

@Composable
fun sharedPaymentCheckoutViewModel(
    repository: PaymentRepository = koinInject()
): PaymentCheckoutViewModel = viewModel(
    factory = PaymentCheckoutViewModelFactory(repository)
)
