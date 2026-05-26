package com.talkeys.shared.di

import com.talkeys.shared.network.ApiClient
import com.talkeys.shared.network.PaymentApiService
import com.talkeys.shared.data.payment.PaymentRepository
import org.koin.dsl.module

// NOTE: Phase 0 — shared AuthRepository wiring intentionally removed.
// The interfaces TokenStorage, GoogleSignInProvider, and the AuthRepository class
// remain in the module for Phase 5 work, but are not registered with Koin because
// their platform factories are not yet implemented end-to-end. Android login still
// uses the app-module AuthService/TokenManager path.
val sharedModule = module {
    single { ApiClient() }
    single { PaymentApiService(get()) }
    single { PaymentRepository(get()) }
}

// Platform-specific modules should be provided by each platform
expect val platformModule: org.koin.core.module.Module
