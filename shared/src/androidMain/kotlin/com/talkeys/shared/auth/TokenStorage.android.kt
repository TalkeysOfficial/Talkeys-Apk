package com.talkeys.shared.auth

import kotlinx.datetime.Clock

/**
 * Android [TokenStorage] backed by [SecureStorage] (EncryptedSharedPreferences).
 *
 * Replaces the former DataStore-based implementation that stored the JWT
 * in **plain text**. Because [AndroidSecureStorage] uses the same preference
 * file (`"secure_prefs"`) and key name (`"auth_token"`) as the legacy
 * app-module `TokenManager`, tokens written by the old code path are
 * readable here without requiring re-login.
 *
 * Expiry: mirrors the existing Android `TokenManager` 24-hour validity
 * window (app/.../TokenManager.kt:35). Stored as epoch-millis under
 * the same key name (`"auth_token_expiry"`) for migration compatibility.
 */
class AndroidTokenStorage(private val secureStorage: SecureStorage) : TokenStorage {

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
        /** Must match the key used by the existing app-module TokenManager. */
        const val TOKEN_KEY = "auth_token"
        /** Must match the key used by the existing app-module TokenManager. */
        const val EXPIRY_KEY = "auth_token_expiry"
        /** 24 hours in milliseconds — matches TokenManager.VALIDITY_PERIOD. */
        const val TOKEN_VALIDITY_MS = 24 * 60 * 60 * 1000L
    }
}
