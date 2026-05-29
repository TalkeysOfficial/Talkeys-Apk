package com.talkeys.shared.auth

/**
 * Platform-agnostic secure key-value storage for auth secrets only.
 *
 * This interface is for access tokens, refresh tokens, ID tokens, and
 * other authentication secrets. Ordinary user preferences (theme, avatar,
 * liked events, notification settings) must NOT be stored here — use
 * platform-native preferences (DataStore on Android, UserDefaults on iOS).
 *
 * Platform implementations:
 * - Android: [AndroidSecureStorage] backed by EncryptedSharedPreferences
 * - iOS: [IosSecureStorage] backed by Keychain
 *
 * All operations are suspending to accommodate async platform APIs.
 */
interface SecureStorage {
    /** Retrieve a string value for the given key, or null if absent. */
    suspend fun getString(key: String): String?

    /** Store a string value under the given key. */
    suspend fun putString(key: String, value: String)

    /** Remove the value associated with the given key. */
    suspend fun remove(key: String)

    /** Remove all stored values. */
    suspend fun clear()
}
