package com.talkeys.shared.auth

/**
 * Platform-agnostic token storage interface.
 *
 * Stores an access token together with a client-side expiry timestamp.
 * The 24-hour validity window mirrors the existing Android `TokenManager`
 * behaviour (app/.../TokenManager.kt:35-39).
 *
 * The backend has no refresh-token or token-validation endpoint
 * (CURRENT_CLIENT_API_AUDIT.md §3), so expiry is local-only.
 */
interface TokenStorage {
    suspend fun saveToken(token: String)
    suspend fun getToken(): String?
    suspend fun clearToken()
    suspend fun hasToken(): Boolean

    /**
     * Returns `true` when either no expiry was recorded or the recorded
     * expiry is in the past. Callers should treat an expired token like
     * a missing one and redirect to sign-in.
     */
    suspend fun isTokenExpired(): Boolean
}

// Phase 0: expect/actual factory removed — wiring is deferred to Phase 5.
// Platform implementations delegate to SecureStorage (Phase 5).
