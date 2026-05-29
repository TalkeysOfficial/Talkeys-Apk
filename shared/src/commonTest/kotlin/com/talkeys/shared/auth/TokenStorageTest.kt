package com.talkeys.shared.auth

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for the common [TokenStorage] contract.
 *
 * Uses a [TestTokenStorage] that delegates to [FakeSecureStorage] —
 * identical structure to both [AndroidTokenStorage] and [IOSTokenStorage],
 * which differ only in how they obtain their [SecureStorage].
 */
class TokenStorageTest {

    @Test
    fun saveAndRetrieveToken() = runTest {
        val storage = TestTokenStorage()
        storage.saveToken("jwt-abc-123")
        assertEquals("jwt-abc-123", storage.getToken())
        assertTrue(storage.hasToken())
    }

    @Test
    fun getTokenReturnsNullWhenEmpty() = runTest {
        val storage = TestTokenStorage()
        assertNull(storage.getToken())
        assertFalse(storage.hasToken())
    }

    @Test
    fun clearTokenRemovesValueAndExpiry() = runTest {
        val storage = TestTokenStorage()
        storage.saveToken("jwt-abc-123")
        storage.clearToken()
        assertNull(storage.getToken())
        assertFalse(storage.hasToken())
        // After clear, isTokenExpired returns true (no expiry stored)
        assertTrue(storage.isTokenExpired())
    }

    @Test
    fun saveTokenOverwritesPrevious() = runTest {
        val storage = TestTokenStorage()
        storage.saveToken("old-token")
        storage.saveToken("new-token")
        assertEquals("new-token", storage.getToken())
    }

    @Test
    fun freshTokenIsNotExpired() = runTest {
        val storage = TestTokenStorage()
        storage.saveToken("jwt-fresh")
        assertFalse(storage.isTokenExpired())
    }

    @Test
    fun missingExpiryTreatedAsExpired() = runTest {
        val storage = TestTokenStorage()
        // Token present but no expiry key → expired
        storage.secureStorage.putString("auth_token", "orphan")
        assertTrue(storage.isTokenExpired())
    }

    @Test
    fun pastExpiryIsExpired() = runTest {
        val storage = TestTokenStorage()
        storage.secureStorage.putString("auth_token", "old-jwt")
        // Set expiry to 1ms in the past
        storage.secureStorage.putString("auth_token_expiry", "1")
        assertTrue(storage.isTokenExpired())
    }
}

/**
 * Test-only [TokenStorage] that mirrors the platform implementations'
 * SecureStorage delegation and 24-hour expiry logic.
 */
class TestTokenStorage(
    val secureStorage: FakeSecureStorage = FakeSecureStorage()
) : TokenStorage {
    override suspend fun saveToken(token: String) {
        secureStorage.putString("auth_token", token)
        val expiryMs = kotlinx.datetime.Clock.System.now().toEpochMilliseconds() + 24 * 60 * 60 * 1000L
        secureStorage.putString("auth_token_expiry", expiryMs.toString())
    }
    override suspend fun getToken(): String? = secureStorage.getString("auth_token")
    override suspend fun clearToken() {
        secureStorage.remove("auth_token")
        secureStorage.remove("auth_token_expiry")
    }
    override suspend fun hasToken(): Boolean = getToken() != null
    override suspend fun isTokenExpired(): Boolean {
        val expiryStr = secureStorage.getString("auth_token_expiry") ?: return true
        val expiryMs = expiryStr.toLongOrNull() ?: return true
        return kotlinx.datetime.Clock.System.now().toEpochMilliseconds() > expiryMs
    }
}
