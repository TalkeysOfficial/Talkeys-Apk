package com.talkeys.shared.presentation.counter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import kotlin.reflect.KClass

/**
 * Narrowly scoped factory for [CounterViewModel].
 * Used by the iOS lifecycle bridge to create instances through AndroidX ViewModelProvider.
 */
val counterViewModelFactory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
        return CounterViewModel() as T
    }
}
