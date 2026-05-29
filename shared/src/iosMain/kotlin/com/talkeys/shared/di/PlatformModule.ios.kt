package com.talkeys.shared.di

import com.talkeys.shared.auth.IosSecureStorage
import com.talkeys.shared.auth.IOSTokenStorage
import com.talkeys.shared.auth.SecureStorage
import com.talkeys.shared.auth.TokenStorage
import org.koin.dsl.module

actual val platformModule = module {
    // Phase 5 — Secure auth storage
    single<SecureStorage> { IosSecureStorage() }
    single<TokenStorage> { IOSTokenStorage(get()) }
}
