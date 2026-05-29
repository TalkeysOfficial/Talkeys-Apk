package com.talkeys.shared.di

import com.talkeys.shared.auth.AndroidSecureStorage
import com.talkeys.shared.auth.AndroidTokenStorage
import com.talkeys.shared.auth.SecureStorage
import com.talkeys.shared.auth.TokenStorage
import org.koin.dsl.module

actual val platformModule = module {
    // Phase 5 — Secure auth storage
    single<SecureStorage> { AndroidSecureStorage(get()) }
    single<TokenStorage> { AndroidTokenStorage(get()) }
}
