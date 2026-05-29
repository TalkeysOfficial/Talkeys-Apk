package com.talkeys.shared.auth

import kotlinx.serialization.Serializable

/**
 * Backend response for `POST /verify`.
 *
 * Shape matches the documented live auth contract:
 *   `{ accessToken: String, name: String }`
 * Citation: CURRENT_CLIENT_API_AUDIT.md §1
 *
 * No other fields are present in the observed backend response.
 */
@Serializable
data class VerifyTokenResponse(
    val accessToken: String,
    val name: String,
)

/**
 * Shared auth state exposed to ViewModels and UI.
 *
 * The access token is **not** exposed here — it is stored internally
 * in [TokenStorage] and retrieved via [AuthRepository] when needed
 * for authenticated API calls. UI layers should observe
 * [isAuthenticated] and [displayName] only.
 */
sealed class AuthState {
    data object Idle : AuthState()
    data object Loading : AuthState()
    data class Authenticated(val displayName: String) : AuthState()
    data class Error(val message: String) : AuthState()

    val isAuthenticated: Boolean get() = this is Authenticated
}
