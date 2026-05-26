package com.talkeys.shared.auth

import android.app.Activity
import android.content.Context
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class AndroidGoogleSignInProvider(
    private val context: Context,
    private val clientId: String = "563385258779-75kq583ov98fk7h3dqp5em0639769a61.apps.googleusercontent.com"
) : GoogleSignInProvider {
    
    private val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(clientId)
        .requestEmail()
        .requestProfile()
        .build()
    
    private val signInClient = GoogleSignIn.getClient(context, gso)
    
    override suspend fun signIn(): GoogleSignInResult {
        return suspendCancellableCoroutine { continuation ->
            try {
                val account = GoogleSignIn.getLastSignedInAccount(context)
                if (account != null && account.idToken != null) {
                    // User is already signed in
                    continuation.resume(
                        GoogleSignInResult.Success(
                            idToken = account.idToken!!,
                            name = account.displayName ?: "",
                            email = account.email ?: ""
                        )
                    )
                } else {
                    // Need to sign in - this would require Activity context
                    // For now, return an error suggesting to use activity-based sign-in
                    continuation.resume(
                        GoogleSignInResult.Error("Please use activity-based sign-in method")
                    )
                }
            } catch (e: Exception) {
                continuation.resume(GoogleSignInResult.Error(e.message ?: "Sign-in failed"))
            }
        }
    }
    
    override suspend fun signOut() {
        try {
            signInClient.signOut()
        } catch (e: Exception) {
            // Handle sign-out error
        }
    }
    
    override suspend fun isSignedIn(): Boolean {
        return try {
            GoogleSignIn.getLastSignedInAccount(context) != null
        } catch (e: Exception) {
            false
        }
    }
}

// For Activity-based sign-in (more common pattern)
class ActivityGoogleSignInProvider(
    private val activity: Activity,
    private val clientId: String = "563385258779-75kq583ov98fk7h3dqp5em0639769a61.apps.googleusercontent.com"
) : GoogleSignInProvider {
    
    private val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(clientId)
        .requestEmail()
        .requestProfile()
        .build()
    
    private val signInClient = GoogleSignIn.getClient(activity, gso)
    
    override suspend fun signIn(): GoogleSignInResult {
        return try {
            val account = GoogleSignIn.getLastSignedInAccount(activity)
            if (account != null && account.idToken != null) {
                GoogleSignInResult.Success(
                    idToken = account.idToken!!,
                    name = account.displayName ?: "",
                    email = account.email ?: ""
                )
            } else {
                GoogleSignInResult.Error("Manual sign-in required - use Intent-based flow")
            }
        } catch (e: Exception) {
            GoogleSignInResult.Error(e.message ?: "Sign-in failed")
        }
    }
    
    fun getSignInIntent() = signInClient.signInIntent
    
    fun handleSignInResult(data: android.content.Intent?): GoogleSignInResult {
        return try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            
            if (account?.idToken != null) {
                GoogleSignInResult.Success(
                    idToken = account.idToken!!,
                    name = account.displayName ?: "",
                    email = account.email ?: ""
                )
            } else {
                GoogleSignInResult.Error("No ID token received")
            }
        } catch (e: ApiException) {
            GoogleSignInResult.Error("Google Sign-In failed: ${e.message}")
        } catch (e: Exception) {
            GoogleSignInResult.Error("Sign-in failed: ${e.message}")
        }
    }
    
    override suspend fun signOut() {
        signInClient.signOut()
    }
    
    override suspend fun isSignedIn(): Boolean {
        return GoogleSignIn.getLastSignedInAccount(activity) != null
    }
}

// Phase 0: actual factory removed. Use AndroidGoogleSignInProvider(context)
// or ActivityGoogleSignInProvider(activity) directly.
