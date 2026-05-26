package com.talkeys.shared.auth

import co.touchlab.kermit.Logger
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthRepository(
    private val httpClient: HttpClient,
    private val googleSignInProvider: GoogleSignInProvider,
    private val tokenStorage: TokenStorage
) {
    private val logger = Logger.withTag("AuthRepository")
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    companion object {
        private const val AUTH_ENDPOINT = "/api/auth/google-signin" // Update with your actual endpoint
    }
    
    suspend fun signInWithGoogle(): AuthState {
        _authState.value = AuthState.Loading
        logger.d { "Starting Google Sign-In process" }
        
        try {
            // Step 1: Perform platform-specific Google Sign-In
            when (val signInResult = googleSignInProvider.signIn()) {
                is GoogleSignInResult.Success -> {
                    logger.d { "Google Sign-In successful" }
                    
                    // Step 2: Verify token with backend
                    val authResult = verifyTokenWithBackend(signInResult.idToken)
                    _authState.value = authResult
                    
                    // Step 3: Store token if successful
                    if (authResult is AuthState.Success) {
                        tokenStorage.saveToken(authResult.token)
                        logger.d { "Token saved successfully" }
                    }
                    
                    return authResult
                }
                is GoogleSignInResult.Error -> {
                    val errorState = AuthState.Error("Google Sign-In failed: ${signInResult.message}")
                    _authState.value = errorState
                    logger.e { "Google Sign-In error: ${signInResult.message}" }
                    return errorState
                }
                is GoogleSignInResult.Cancelled -> {
                    val cancelledState = AuthState.Error("Sign-in was cancelled")
                    _authState.value = cancelledState
                    logger.d { "Google Sign-In cancelled by user" }
                    return cancelledState
                }
            }
        } catch (e: Exception) {
            val errorState = AuthState.Error("Authentication failed: ${e.message}")
            _authState.value = errorState
            logger.e(e) { "Exception during sign-in process" }
            return errorState
        }
    }
    
    private suspend fun verifyTokenWithBackend(idToken: String): AuthState {
        return try {
            logger.d { "Verifying token with backend" }
            
            val response = httpClient.post(AUTH_ENDPOINT) {
                contentType(ContentType.Application.Json)
                setBody(GoogleSignInRequest(idToken))
                headers {
                    append(HttpHeaders.Authorization, "Bearer $idToken")
                }
            }
            
            if (response.status.isSuccess()) {
                val authResponse: AuthResponse = response.body()
                if (authResponse.success && authResponse.user != null && authResponse.accessToken != null) {
                    logger.d { "Backend verification successful" }
                    AuthState.Success(authResponse.user, authResponse.accessToken)
                } else {
                    logger.e { "Backend verification failed: ${authResponse.message}" }
                    AuthState.Error(authResponse.message)
                }
            } else {
                logger.e { "Backend returned error: ${response.status}" }
                AuthState.Error("Server error: ${response.status.description}")
            }
        } catch (e: Exception) {
            logger.e(e) { "Network error during token verification" }
            AuthState.Error("Network error: ${e.message}")
        }
    }
    
    suspend fun signOut() {
        try {
            logger.d { "Signing out user" }
            googleSignInProvider.signOut()
            tokenStorage.clearToken()
            _authState.value = AuthState.Idle
            logger.d { "Sign-out successful" }
        } catch (e: Exception) {
            logger.e(e) { "Error during sign-out" }
        }
    }
    
    suspend fun checkExistingAuth(): AuthState {
        return try {
            val savedToken = tokenStorage.getToken()
            if (savedToken != null) {
                // TODO: Optionally validate token with backend
                // For now, we'll assume the saved token is valid
                // In production, you should validate the token
                logger.d { "Found existing token" }
                AuthState.Success(
                    user = User("", "", "", "", "", "", ""), // Placeholder - should fetch from token or API
                    token = savedToken
                )
            } else {
                logger.d { "No existing token found" }
                AuthState.Idle
            }
        } catch (e: Exception) {
            logger.e(e) { "Error checking existing auth" }
            AuthState.Idle
        }
    }
}
