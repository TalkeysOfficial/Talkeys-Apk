package com.talkeys.shared.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ObjCClass
import kotlinx.cinterop.getOriginalKotlinClass

/**
 * Generic iOS-side helper for resolving AndroidX ViewModels from a [ViewModelStore].
 *
 * Follows the official AndroidX KMP ViewModel guidance:
 * https://developer.android.com/kotlin/multiplatform/viewmodel
 *
 * Swift callers pass the ViewModel's ObjCClass (e.g. `CounterViewModel.self`),
 * which is converted to a KClass via [getOriginalKotlinClass].
 */
@OptIn(BetaInteropApi::class)
@Throws(IllegalArgumentException::class)
fun ViewModelStore.resolveViewModel(
    objCClass: ObjCClass,
    key: String? = null,
    factory: ViewModelProvider.Factory,
    extras: CreationExtras = CreationExtras.Empty
): ViewModel {
    val kClass = getOriginalKotlinClass(objCClass)
        ?: throw IllegalArgumentException(
            "Cannot convert ObjCClass to KClass: $objCClass"
        )

    val provider = ViewModelProvider.create(
        store = this,
        factory = factory,
        extras = extras,
    )

    @Suppress("UNCHECKED_CAST")
    return if (key != null) {
        provider[key, kClass as kotlin.reflect.KClass<ViewModel>]
    } else {
        provider[kClass as kotlin.reflect.KClass<ViewModel>]
    }
}
