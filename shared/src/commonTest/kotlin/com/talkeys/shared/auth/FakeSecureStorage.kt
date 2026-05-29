package com.talkeys.shared.auth

/**
 * In-memory [SecureStorage] for unit tests.
 * Thread-safe enough for single-threaded test coroutines.
 */
class FakeSecureStorage : SecureStorage {
    private val store = mutableMapOf<String, String>()

    override suspend fun getString(key: String): String? = store[key]

    override suspend fun putString(key: String, value: String) {
        store[key] = value
    }

    override suspend fun remove(key: String) {
        store.remove(key)
    }

    override suspend fun clear() {
        store.clear()
    }
}
