package com.talkeys.shared.auth

// Platform-agnostic token storage interface
interface TokenStorage {
    suspend fun saveToken(token: String)
    suspend fun getToken(): String?
    suspend fun clearToken()
    suspend fun hasToken(): Boolean
}

// Phase 0: expect/actual factory removed — wiring is deferred to Phase 5.
// Construct platform implementations directly (AndroidTokenStorage / IOSTokenStorage).
