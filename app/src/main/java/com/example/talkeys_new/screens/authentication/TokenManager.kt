package com.example.talkeys_new.screens.authentication

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.preferencesDataStore
import com.talkeys.shared.auth.TokenStorage
import com.example.talkeys_new.utils.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

// DataStore for non-sensitive user preferences (used by GoogleSignInManager)
val Context.dataStore by preferencesDataStore(name = "user_prefs")

/**
 * TokenManager handles saving, retrieving, validating, and deleting the JWT token.
 *
 * **Phase 5 migration:** This class now delegates all storage operations to the
 * shared KMP [TokenStorage] (backed by [AndroidSecureStorage] →
 * EncryptedSharedPreferences). The same `"secure_prefs"` file and `"auth_token"`
 * / `"auth_token_expiry"` keys are used, so no re-login is required.
 *
 * [LoginScreenUI.kt] still creates `TokenManager(context)` and calls
 * `saveToken()` / `clearToken()` directly — this bridge keeps that path
 * working while routing storage through the shared module.
 *
 * Security features:
 * - Tokens are encrypted at rest using AES-256-GCM via Android Keystore
 * - Token expiry validation (24 hours)
 * - No plaintext storage
 * - No token values logged
 */
class TokenManager(private val context: Context) : KoinComponent {

    private val TAG = "TokenManager"

    // Shared KMP TokenStorage — resolves from Koin graph
    private val sharedTokenStorage: TokenStorage by inject()

    // MutableStateFlow to emit token updates
    private val _tokenFlow = MutableStateFlow<String?>(null)

    /**
     * This 'token' property is a Flow, which means it emits data updates over time.
     * It provides the current token value to observers.
     */
    val token: Flow<String?> = _tokenFlow.asStateFlow()

    init {
        // Load token on initialization
        loadTokenFromStorage()
    }

    /**
     * Loads the token from shared secure storage into the flow.
     * Called during initialization and after token updates.
     */
    private fun loadTokenFromStorage() {
        try {
            // runBlocking is acceptable here: this runs once at init,
            // the underlying EncryptedSharedPreferences read is fast,
            // and the existing code was also synchronous.
            val saved = runBlocking { sharedTokenStorage.getToken() }
            _tokenFlow.value = saved
        } catch (e: Exception) {
            Log.e(TAG, "Error loading token", e)
            _tokenFlow.value = null
        }
    }

    /**
     * Saves a token into the shared SecureStorage with proper error handling.
     * Token is encrypted before storage. Expiry time is set to 24 hours from now.
     *
     * @param token The JWT token to save (NEVER logged)
     * @return Result indicating success or failure
     */
    suspend fun saveToken(token: String): Result<Unit> {
        return try {
            sharedTokenStorage.saveToken(token)
            _tokenFlow.value = token
            Log.d(TAG, "Token saved successfully with expiry")
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error saving token", e)
            Result.Error(e, "Failed to save authentication token")
        }
    }

    /**
     * Clears the saved token from shared SecureStorage with proper error handling.
     * @return Result indicating success or failure
     */
    suspend fun clearToken(): Result<Unit> {
        return try {
            sharedTokenStorage.clearToken()
            _tokenFlow.value = null
            Log.d(TAG, "Token cleared successfully")
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error clearing token", e)
            Result.Error(e, "Failed to clear authentication token")
        }
    }

    /**
     * Gets the current token directly (not as a Flow) with proper error handling.
     * @return Result containing the token or an error
     */
    suspend fun getToken(): Result<String?> {
        return try {
            val value = sharedTokenStorage.getToken()
            Result.Success(value)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error retrieving token", e)
            Result.Error(e, "Failed to retrieve authentication token")
        }
    }

    /**
     * Checks if the token exists and is not expired.
     * Validates both token presence and expiry timestamp.
     *
     * @return true if the token is valid and not expired, false otherwise
     */
    suspend fun isTokenValid(): Boolean {
        return try {
            val hasToken = sharedTokenStorage.hasToken()
            if (!hasToken) {
                Log.d(TAG, "Token validation failed: no token found")
                return false
            }

            val expired = sharedTokenStorage.isTokenExpired()
            if (expired) {
                Log.d(TAG, "Token validation failed: token expired")
                return false
            }

            true
        } catch (e: Exception) {
            Log.e(TAG, "Error checking token validity", e)
            false
        }
    }
}
