package com.talkeys.shared.auth

import co.touchlab.kermit.Logger
import com.talkeys.shared.network.ApiError
import com.talkeys.shared.network.ApiResult
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Shared authentication repository.
 *
 * Responsible for:
 * 1. Verifying a Google ID token with the backend via `POST /verify`
 *    (Citation: CURRENT_CLIENT_API_AUDIT.md §1)
 * 2. Saving the returned JWT access token into [TokenStorage]
 * 3. Clearing tokens on sign-out (local only — no backend logout endpoint exists)
 *
 * This class does **not** trigger the platform Google Sign-In UI; the caller
 * (Android Activity / iOS SwiftUI) obtains the Google ID token via platform
 * APIs and passes it to [verifyGoogleToken].
 */
class AuthRepository(
    private val httpClient: HttpClient,
    private val tokenStorage: TokenStorage,
    private val baseUrl: String,
) {
    private val logger = Logger.withTag("AuthRepository")

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    /**
     * Send the Google ID token to `POST <baseUrl>/verify` and, on success,
     * persist the returned JWT.
     *
     * Request shape — exactly matches the documented live Android auth call:
     *   Header `Authorization: Bearer <google_id_token>`
     *   No body.
     * Citation: CURRENT_CLIENT_API_AUDIT.md §1
     *
     * Response shape — `{ accessToken, name }`:
     * Citation: app/.../DataClasses.kt:3-4
     */
    suspend fun verifyGoogleToken(googleIdToken: String): ApiResult<VerifyTokenResponse> {
        _authState.value = AuthState.Loading
        logger.d { "Verifying Google token with backend" }

        return try {
            val response = httpClient.post("$baseUrl/verify") {
                headers {
                    append(HttpHeaders.Authorization, "Bearer $googleIdToken")
                }
                // No body — the backend reads the Google ID token from Authorization.
            }

            if (response.status.isSuccess()) {
                val body: VerifyTokenResponse = response.body()
                tokenStorage.saveToken(body.accessToken)
                logger.d { "Backend verification succeeded, token saved" }
                val state = AuthState.Authenticated(
                    displayName = body.name,
                )
                _authState.value = state
                ApiResult.Success(body)
            } else {
                val errorBody = runCatching { response.bodyAsText() }.getOrNull()
                logger.e { "Backend verification failed: ${response.status}" }
                val failure = ApiResult.Failure(
                    ApiError.HttpError(response.status.value, errorBody)
                )
                _authState.value = AuthState.Error("Server error: ${response.status.description}")
                failure
            }
        } catch (e: Exception) {
            // Ktor wraps SerializationException inside JsonConvertException or
            // other wrapper types — walk the cause chain to detect parse errors.
            val isParseError = generateSequence<Throwable>(e) { it.cause }
                .any { it is kotlinx.serialization.SerializationException }
            if (isParseError) {
                logger.e(e) { "Parse error during token verification" }
                _authState.value = AuthState.Error("Unexpected server response")
                ApiResult.Failure(ApiError.ParseError(e.message ?: "parse error"))
            } else {
                logger.e(e) { "Network error during token verification" }
                _authState.value = AuthState.Error("Network error: ${e.message}")
                ApiResult.Failure(ApiError.NetworkError)
            }
        }
    }

    /**
     * Check whether a saved, non-expired access token already exists.
     * Returns [AuthState.Authenticated] if present and not expired,
     * or [AuthState.Idle] otherwise.
     *
     * Note: the backend has no token-validation or refresh endpoint
     * (CURRENT_CLIENT_API_AUDIT.md §3). The existing Android app applies a
     * client-side 24-hour expiry (TokenManager.kt:35). This implementation
     * mirrors that policy via [TokenStorage.isTokenExpired].
     */
    suspend fun checkExistingAuth(): AuthState {
        return try {
            val savedToken = tokenStorage.getToken()
            if (savedToken != null && !tokenStorage.isTokenExpired()) {
                logger.d { "Found existing non-expired token" }
                val state = AuthState.Authenticated(
                    displayName = "", // name not persisted; caller can fetch profile if needed
                )
                _authState.value = state
                state
            } else {
                logger.d { "No existing token found" }
                _authState.value = AuthState.Idle
                AuthState.Idle
            }
        } catch (e: Exception) {
            logger.e(e) { "Error checking existing auth" }
            _authState.value = AuthState.Idle
            AuthState.Idle
        }
    }

    /**
     * Clear all local auth state.
     *
     * No backend logout/revocation endpoint exists
     * (CURRENT_CLIENT_API_AUDIT.md §5). The caller is responsible for
     * revoking the platform Google Sign-In session via platform APIs.
     */
    suspend fun signOut() {
        try {
            logger.d { "Signing out — clearing local token" }
            tokenStorage.clearToken()
            _authState.value = AuthState.Idle
        } catch (e: Exception) {
            logger.e(e) { "Error during sign-out" }
        }
    }
}
