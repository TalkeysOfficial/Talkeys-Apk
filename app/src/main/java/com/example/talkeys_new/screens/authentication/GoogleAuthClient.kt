package com.example.talkeys_new.screens.authentication

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException


class GoogleAuthClient(
    context: Context,
    clientId: String? = GoogleSignInConfig.resolveServerClientId(context)
) {
    private val serverClientId = clientId
        ?.trim()
        ?.takeIf { it.endsWith(".apps.googleusercontent.com") }

    // Step 1: Create sign-in options using GoogleSignInOptions
    // This tells Google what information we want from the user (email, ID token)

    private val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .apply {
            serverClientId?.let { requestIdToken(it) }
        }
        .requestEmail()
        .requestProfile()
        .build()

    // Step 2: Create a sign-in client using the context and sign-in options
    // This client will help us launch the Google Sign-In screen and manage the login
    private val signInClient = GoogleSignIn.getClient(context, gso)

    val isConfigured: Boolean
        get() = serverClientId != null

    // Step 3: This function gives us the Intent (a message that Android uses to do things)
    // We use this Intent to launch the Google Sign-In screen
    fun getSignInIntent(): Intent = getSignInIntentOrNull()
        ?: error(GoogleSignInConfig.missingConfigMessage())

    fun getSignInIntentOrNull(): Intent? =
        if (isConfigured) signInClient.signInIntent else null

    // Step 4: This function extracts the ID token from the result of the Google Sign-In screen
    // The token is what we send to our backend to verify the user

    fun getIdTokenFromIntent(data: Intent?): String? {
        return try {
            // Try to get the Google account the user selected from the sign-in screen
            val account = GoogleSignIn.getSignedInAccountFromIntent(data)
                .getResult(ApiException::class.java) // This might throw an exception if something goes wrong

            // Return the ID token (used for verifying the user's identity)
            account?.idToken

        } catch (e: ApiException) {
            Log.e(TAG, "Google Sign-In failed with statusCode=${e.statusCode}", e)
            null
        } catch (e: Exception) {
            Log.e(TAG, "Google Sign-In failed", e)
            null
        }
    }

    // Step 5: This function signs the user out of their Google account
    // Useful if the user clicks "Logout" in the app
    fun signOut() = signInClient.signOut()
    
    // Step 6: This function revokes access and signs out completely
    // This ensures the account picker shows up on next login
    fun revokeAccess() = signInClient.revokeAccess()

    private companion object {
        const val TAG = "GoogleAuthClient"
    }
}
