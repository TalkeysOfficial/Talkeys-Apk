package com.talkeys.shared.di

import com.talkeys.shared.auth.AuthRepository
import com.talkeys.shared.config.ProductionConfig
import com.talkeys.shared.data.events.EventsApi
import com.talkeys.shared.data.events.EventsRepository
import com.talkeys.shared.network.ApiClient
import com.talkeys.shared.network.PaymentApiService
import com.talkeys.shared.data.payment.PaymentRepository
import org.koin.dsl.module

val sharedModule = module {
    single { ApiClient() }
    single { PaymentApiService(get()) }
    single { PaymentRepository(get()) }

    // Phase 4 — Events vertical slice (read-only)
    single { EventsApi(get<ApiClient>()) }
    single { EventsRepository(get()) }

    // Phase 5 — Auth
    // SecureStorage and TokenStorage are provided by platformModule.
    single {
        AuthRepository(
            httpClient = get<ApiClient>().httpClient,
            tokenStorage = get(),
            baseUrl = ProductionConfig.API_BASE_URL,
        )
    }
}

// Platform-specific modules should be provided by each platform
expect val platformModule: org.koin.core.module.Module
