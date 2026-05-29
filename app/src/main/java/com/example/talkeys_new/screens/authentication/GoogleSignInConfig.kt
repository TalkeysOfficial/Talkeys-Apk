package com.example.talkeys_new.screens.authentication

import android.content.Context

object GoogleSignInConfig {
    private const val DEFAULT_WEB_CLIENT_ID = "default_web_client_id"

    fun resolveServerClientId(context: Context): String? {
        val resourceId = context.resources.getIdentifier(
            DEFAULT_WEB_CLIENT_ID,
            "string",
            context.packageName
        )
        if (resourceId == 0) return null

        val value = context.getString(resourceId).trim()
        return value.takeIf { it.endsWith(".apps.googleusercontent.com") }
    }

    fun missingConfigMessage(): String =
        "Google Sign-In is not configured. Add the Android OAuth client and SHA fingerprint in Firebase, then replace google-services.json."
}
