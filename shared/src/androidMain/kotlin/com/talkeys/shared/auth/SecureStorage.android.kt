package com.talkeys.shared.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Android [SecureStorage] backed by [EncryptedSharedPreferences].
 *
 * Keys and values are encrypted using AES-256-SIV / AES-256-GCM
 * via the Android Keystore. This is the same encryption scheme used
 * by the existing app-module `com.example.talkeys_new.security.SecureStorage`.
 *
 * **Migration note:** this class uses the same preference file name
 * (`"secure_prefs"`) and the same key name for the auth token
 * (`"auth_token"`) as the existing Android `TokenManager`. This means
 * tokens written by the old code path are readable by this implementation
 * without requiring re-login.
 */
class AndroidSecureStorage(context: Context) : SecureStorage {

    private val prefs: SharedPreferences = createEncryptedPrefs(context)

    override suspend fun getString(key: String): String? = withContext(Dispatchers.IO) {
        prefs.getString(key, null)
    }

    override suspend fun putString(key: String, value: String) = withContext(Dispatchers.IO) {
        prefs.edit().putString(key, value).apply()
    }

    override suspend fun remove(key: String) = withContext(Dispatchers.IO) {
        prefs.edit().remove(key).apply()
    }

    override suspend fun clear() = withContext(Dispatchers.IO) {
        prefs.edit().clear().apply()
    }

    private companion object {
        /** Must match the file name used by the existing app-module SecureStorage. */
        const val PREFS_FILE = "secure_prefs"

        fun createEncryptedPrefs(context: Context): SharedPreferences {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            return EncryptedSharedPreferences.create(
                context,
                PREFS_FILE,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
    }
}
