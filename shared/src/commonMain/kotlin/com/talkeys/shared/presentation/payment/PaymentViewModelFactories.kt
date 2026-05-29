package com.talkeys.shared.presentation.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.talkeys.shared.data.payment.PaymentRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import kotlin.reflect.KClass

class PaymentCheckoutViewModelFactory(
    private val repository: PaymentRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
        require(modelClass == PaymentCheckoutViewModel::class) {
            "PaymentCheckoutViewModelFactory cannot create ${modelClass.simpleName}"
        }
        return PaymentCheckoutViewModel(repository) as T
    }
}

private object KoinHelper : KoinComponent

val paymentCheckoutViewModelFactory: ViewModelProvider.Factory
    get() = PaymentCheckoutViewModelFactory(KoinHelper.get())
