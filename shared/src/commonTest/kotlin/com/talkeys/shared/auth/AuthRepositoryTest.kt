package com.talkeys.shared.auth

import com.talkeys.shared.network.ApiError
import com.talkeys.shared.network.ApiResult
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AuthRepositoryTest {

    private val jsonHeaders = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
    private val successJson = """{"accessToken":"jwt-xyz","name":"Gurnoor"}"""

    private fun jsonClient(engine: MockEngine) = HttpClient(engine) {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }

    @Test
    fun verifyGoogleTokenSuccess() = runTest {
        val tokenStorage = FakeTokenStorage()
        val client = jsonClient(MockEngine { respond(successJson, HttpStatusCode.OK, jsonHeaders) })
        val repo = AuthRepository(client, tokenStorage, "https://api.talkeys.xyz")

        val result = repo.verifyGoogleToken("google-id-token-abc")

        assertIs<ApiResult.Success<VerifyTokenResponse>>(result)
        assertEquals("jwt-xyz", result.data.accessToken)
        assertEquals("Gurnoor", result.data.name)
        assertEquals("jwt-xyz", tokenStorage.getToken())
        val state = repo.authState.value
        assertIs<AuthState.Authenticated>(state)
        assertEquals("Gurnoor", state.displayName)
        assertTrue(state.isAuthenticated)
    }

    @Test
    fun verifyGoogleTokenHttpError() = runTest {
        val tokenStorage = FakeTokenStorage()
        val client = jsonClient(MockEngine { respond("Unauthorized", HttpStatusCode.Unauthorized, jsonHeaders) })
        val repo = AuthRepository(client, tokenStorage, "https://api.talkeys.xyz")

        val result = repo.verifyGoogleToken("bad-token")

        assertIs<ApiResult.Failure>(result)
        assertIs<ApiError.HttpError>(result.error)
        assertEquals(401, (result.error as ApiError.HttpError).status)
        assertNull(tokenStorage.getToken())
        assertIs<AuthState.Error>(repo.authState.value)
        assertFalse(repo.authState.value.isAuthenticated)
    }

    @Test
    fun verifyGoogleTokenParseError() = runTest {
        val tokenStorage = FakeTokenStorage()
        val client = jsonClient(MockEngine { respond("""{"unexpected":"shape"}""", HttpStatusCode.OK, jsonHeaders) })
        val repo = AuthRepository(client, tokenStorage, "https://api.talkeys.xyz")

        val result = repo.verifyGoogleToken("google-token")

        assertIs<ApiResult.Failure>(result)
        assertIs<ApiError.ParseError>(result.error)
        assertNull(tokenStorage.getToken())
        assertIs<AuthState.Error>(repo.authState.value)
    }

    @Test
    fun verifyGoogleTokenSendsCorrectAuthorizationHeader() = runTest {
        var capturedAuth: String? = null
        val client = jsonClient(MockEngine { request ->
            capturedAuth = request.headers[HttpHeaders.Authorization]
            respond(successJson, HttpStatusCode.OK, jsonHeaders)
        })
        val repo = AuthRepository(client, FakeTokenStorage(), "https://api.talkeys.xyz")

        repo.verifyGoogleToken("my-google-id-token")

        assertEquals("Bearer my-google-id-token", capturedAuth)
    }

    @Test
    fun signOutClearsToken() = runTest {
        val tokenStorage = FakeTokenStorage()
        tokenStorage.saveToken("some-jwt")
        val client = jsonClient(MockEngine { respond("", HttpStatusCode.OK) })
        val repo = AuthRepository(client, tokenStorage, "https://api.talkeys.xyz")

        repo.signOut()

        assertNull(tokenStorage.getToken())
        assertIs<AuthState.Idle>(repo.authState.value)
    }

    @Test
    fun checkExistingAuthWithSavedNonExpiredToken() = runTest {
        val tokenStorage = FakeTokenStorage()
        tokenStorage.saveToken("saved-jwt")

        val client = jsonClient(MockEngine { respond("", HttpStatusCode.OK) })
        val repo = AuthRepository(client, tokenStorage, "https://api.talkeys.xyz")

        val state = repo.checkExistingAuth()

        assertIs<AuthState.Authenticated>(state)
        assertTrue(state.isAuthenticated)
    }

    @Test
    fun checkExistingAuthWithExpiredToken() = runTest {
        val tokenStorage = FakeTokenStorage(forceExpired = true)
        tokenStorage.saveToken("old-jwt")

        val client = jsonClient(MockEngine { respond("", HttpStatusCode.OK) })
        val repo = AuthRepository(client, tokenStorage, "https://api.talkeys.xyz")

        val state = repo.checkExistingAuth()

        assertIs<AuthState.Idle>(state)
    }

    @Test
    fun checkExistingAuthWithNoToken() = runTest {
        val tokenStorage = FakeTokenStorage()
        val client = jsonClient(MockEngine { respond("", HttpStatusCode.OK) })
        val repo = AuthRepository(client, tokenStorage, "https://api.talkeys.xyz")

        val state = repo.checkExistingAuth()

        assertIs<AuthState.Idle>(state)
    }
}

/**
 * In-memory [TokenStorage] for tests.
 * When [forceExpired] is true, [isTokenExpired] always returns true.
 */
private class FakeTokenStorage(private val forceExpired: Boolean = false) : TokenStorage {
    private var token: String? = null
    override suspend fun saveToken(token: String) { this.token = token }
    override suspend fun getToken(): String? = token
    override suspend fun clearToken() { token = null }
    override suspend fun hasToken(): Boolean = token != null
    override suspend fun isTokenExpired(): Boolean = forceExpired || token == null
}
