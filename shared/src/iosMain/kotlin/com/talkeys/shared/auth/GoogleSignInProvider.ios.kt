package com.talkeys.shared.auth

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

// iOS implementation that delegates to iOS native Google Sign-In
// This will be implemented through the iOS app's GoogleSignInManager
class IOSGoogleSignInProvider : GoogleSignInProvider {
    
    override suspend fun signIn(): GoogleSignInResult {
        return suspendCancellableCoroutine { continuation ->
            // Delegated to iOS Swift code via IOSGoogleSignInBridge.
            // The bridge is not yet wired in production — see Phase 5.
            IOSGoogleSignInBridge.performSignIn { result ->
                continuation.resume(result)
            }
        }
    }

    override suspend fun signOut() {
        IOSGoogleSignInBridge.performSignOut()
    }

    override suspend fun isSignedIn(): Boolean {
        return IOSGoogleSignInBridge.isSignedIn()
    }
}

// Bridge object to communicate with iOS GoogleSignInManager
object IOSGoogleSignInBridge {
    private var signInCallback: ((GoogleSignInResult) -> Unit)? = null
    
    fun performSignIn(callback: (GoogleSignInResult) -> Unit) {
        signInCallback = callback
        
        // This will be called from iOS Swift code
        // For now, simulate a successful sign-in for testing
        // In production, this should be called from the iOS GoogleSignInManager
        
        // Simulate successful sign-in (replace this with actual iOS integration)
        callback(
            GoogleSignInResult.Success(
                idToken = "mock_id_token_from_ios",
                name = "iOS Test User",
                email = "test@ios.com"
            )
        )
    }
    
    fun performSignOut() {
        // This should call the iOS GoogleSignInManager.signOut().
        // No-op placeholder until Phase 5 wires the Swift side.
    }
    
    fun isSignedIn(): Boolean {
        // This should check with iOS GoogleSignInManager
        // For now, return false for testing
        return false
    }
    
    // These functions will be called from iOS Swift code
    fun onSignInSuccess(idToken: String, name: String, email: String) {
        signInCallback?.invoke(
            GoogleSignInResult.Success(
                idToken = idToken,
                name = name,
                email = email
            )
        )
        signInCallback = null
    }
    
    fun onSignInError(errorMessage: String) {
        signInCallback?.invoke(
            GoogleSignInResult.Error(errorMessage)
        )
        signInCallback = null
    }
    
    fun onSignInCancelled() {
        signInCallback?.invoke(GoogleSignInResult.Cancelled)
        signInCallback = null
    }
}

// Phase 0: actual factory removed. Construct IOSGoogleSignInProvider() directly.
