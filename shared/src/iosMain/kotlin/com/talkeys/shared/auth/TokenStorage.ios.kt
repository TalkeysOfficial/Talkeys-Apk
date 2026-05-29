package com.talkeys.shared.auth

import kotlinx.datetime.Clock

/**
 * iOS [TokenStorage] backed by [SecureStorage] (Keychain).
 *
 * Replaces the former NSUserDefaults-based implementation that stored the
 * JWT in **plain text**. Tokens are now encrypted at rest by the iOS
 * Keychain / Secure Enclave.
 *
 * Expiry: mirrors the existing Android `TokenManager` 24-hour validity
 * window (app/.../TokenManager.kt:35) so both platforms behave identically.
 */
class IOSTokenStorage(private val secureStorage: SecureStorage) : TokenStorage {

    override suspend fun saveToken(token: String) {
        secureStorage.putString(TOKEN_KEY, token)
        val expiryMs = Clock.System.now().toEpochMilliseconds() + TOKEN_VALIDITY_MS
        secureStorage.putString(EXPIRY_KEY, expiryMs.toString())
    }

    override suspend fun getToken(): String? {
        return secureStorage.getString(TOKEN_KEY)
    }

    override suspend fun clearToken() {
        secureStorage.remove(TOKEN_KEY)
        secureStorage.remove(EXPIRY_KEY)
    }

    override suspend fun hasToken(): Boolean {
        return getToken() != null
    }

    override suspend fun isTokenExpired(): Boolean {
        val expiryStr = secureStorage.getString(EXPIRY_KEY) ?: return true
        val expiryMs = expiryStr.toLongOrNull() ?: return true
        return Clock.System.now().toEpochMilliseconds() > expiryMs
    }

    private companion object {
        const val TOKEN_KEY = "auth_token"
        const val EXPIRY_KEY = "auth_token_expiry"
        /** 24 hours in milliseconds — matches Android TokenManager. */
        const val TOKEN_VALIDITY_MS = 24 * 60 * 60 * 1000L
    }
}
