package com.talkeys.shared.auth

// Platform-specific Google Sign-In interface
interface GoogleSignInProvider {
    suspend fun signIn(): GoogleSignInResult
    suspend fun signOut()
    suspend fun isSignedIn(): Boolean
}

sealed class GoogleSignInResult {
    data class Success(val idToken: String, val name: String, val email: String) : GoogleSignInResult()
    data class Error(val message: String) : GoogleSignInResult()
    object Cancelled : GoogleSignInResult()
}

// Phase 0: expect/actual factory removed — wiring is deferred to Phase 5.
// Construct a GoogleSignInProvider directly from each platform when needed
// (e.g. AndroidGoogleSignInProvider(context) or IOSGoogleSignInProvider()).
