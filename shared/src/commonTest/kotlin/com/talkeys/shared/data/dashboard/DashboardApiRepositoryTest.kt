package com.talkeys.shared.data.dashboard

import com.talkeys.shared.auth.TokenStorage
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
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class DashboardApiRepositoryTest {

    private val jsonHeaders = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())

    @Test
    fun getUserProfileSendsBearerTokenAndParsesResponse() = runTest {
        var authHeader: String? = null
        val api = DashboardApi(client(MockEngine { request ->
            authHeader = request.headers[HttpHeaders.Authorization]
            respond(PROFILE_JSON, HttpStatusCode.OK, jsonHeaders)
        }))

        val result = api.getUserProfile("jwt-abc")

        assertEquals("Bearer jwt-abc", authHeader)
        assertIs<ApiResult.Success<UserProfile>>(result)
        assertEquals("Gurnoor", result.data.name)
        assertEquals(listOf("e1", "e2"), result.data.likedEvents)
    }

    @Test
    fun getRecentActivityKeepsRawJsonPayload() = runTest {
        val api = DashboardApi(client(MockEngine {
            respond("""{"registrations":2,"views":{"total":11}}""", HttpStatusCode.OK, jsonHeaders)
        }))

        val result = api.getRecentActivity("jwt-abc")

        assertIs<ApiResult.Success<kotlinx.serialization.json.JsonObject>>(result)
        assertNotNull(result.data["registrations"])
        assertNotNull(result.data["views"])
    }

    @Test
    fun getUserEventsSendsBookmarkedTypeAndParsesEvents() = runTest {
        var eventType: String? = null
        val api = DashboardApi(client(MockEngine { request ->
            eventType = request.url.parameters["type"]
            respond(EVENTS_JSON, HttpStatusCode.OK, jsonHeaders)
        }))

        val result = api.getUserEvents(
            token = "jwt-abc",
            type = UserEventType.Bookmarked.wireValue
        )

        assertEquals("bookmarked", eventType)
        assertIs<ApiResult.Success<UserEventsResponse>>(result)
        assertEquals("event-1", result.data.events.first().id)
    }

    @Test
    fun repositoryLoadsBookmarkedEventsThroughSharedContract() = runTest {
        val repo = DashboardRepository(
            DashboardApi(client(MockEngine { request ->
                assertEquals("bookmarked", request.url.parameters["type"])
                respond(EVENTS_JSON, HttpStatusCode.OK, jsonHeaders)
            })),
            FakeTokenStorage(token = "jwt-abc")
        )

        val result = repo.getUserEvents(UserEventType.Bookmarked)

        assertIs<ApiResult.Success<List<com.talkeys.shared.data.events.EventSummary>>>(result)
        assertEquals("event-1", result.data.first().id)
    }

    @Test
    fun repositoryReturnsAuthErrorWhenTokenMissing() = runTest {
        val repo = DashboardRepository(
            DashboardApi(client(MockEngine { respond(PROFILE_JSON, HttpStatusCode.OK, jsonHeaders) })),
            FakeTokenStorage(token = null)
        )

        val result = repo.getUserProfile()

        assertIs<ApiResult.Failure>(result)
        assertIs<ApiError.HttpError>(result.error)
        assertEquals(401, (result.error as ApiError.HttpError).status)
    }

    @Test
    fun repositoryCachesProfileUnlessForceRefresh() = runTest {
        var calls = 0
        val repo = DashboardRepository(
            DashboardApi(client(MockEngine {
                calls++
                respond(PROFILE_JSON, HttpStatusCode.OK, jsonHeaders)
            })),
            FakeTokenStorage(token = "jwt-abc")
        )

        assertIs<ApiResult.Success<UserProfile>>(repo.getUserProfile())
        assertIs<ApiResult.Success<UserProfile>>(repo.getUserProfile())
        assertEquals(1, calls)

        assertIs<ApiResult.Success<UserProfile>>(repo.getUserProfile(forceRefresh = true))
        assertEquals(2, calls)
    }

    @Test
    fun updateUserProfileParsesResponse() = runTest {
        val api = DashboardApi(client(MockEngine { request ->
            assertEquals("Bearer jwt-abc", request.headers[HttpHeaders.Authorization])
            respond(PROFILE_JSON, HttpStatusCode.OK, jsonHeaders)
        }))

        val result = api.updateUserProfile(
            token = "jwt-abc",
            profileData = UpdateUserProfileRequest(displayName = "GP", about = "Builder").toPatchMap()
        )

        assertIs<ApiResult.Success<UserProfile>>(result)
        assertEquals("Gurnoor", result.data.name)
    }

    @Test
    fun parseErrorReturnsFailure() = runTest {
        val api = DashboardApi(client(MockEngine {
            respond("""{"name":123}""", HttpStatusCode.OK, jsonHeaders)
        }))

        val result = api.getUserProfile("jwt-abc")

        assertIs<ApiResult.Failure>(result)
        assertIs<ApiError.ParseError>(result.error)
    }

    private fun client(engine: MockEngine): HttpClient = HttpClient(engine) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    private class FakeTokenStorage(
        private val token: String? = "jwt-abc",
        private val expired: Boolean = false
    ) : TokenStorage {
        override suspend fun saveToken(token: String) = Unit
        override suspend fun getToken(): String? = token
        override suspend fun clearToken() = Unit
        override suspend fun hasToken(): Boolean = token != null
        override suspend fun isTokenExpired(): Boolean = expired
    }

    private companion object {
        const val PROFILE_JSON = """
            {
              "_id": "user-1",
              "name": "Gurnoor",
              "email": "gurnoor@example.com",
              "displayName": "GP",
              "about": "Building Talkeys",
              "pronouns": "he/him",
              "avatarUrl": "https://example.com/avatar.png",
              "likedEvents": ["e1", "e2"]
            }
        """

        const val EVENTS_JSON = """
            {
              "events": [
                {
                  "_id": "event-1",
                  "name": "Shared Logic Night",
                  "category": "Tech",
                  "ticketPrice": 100,
                  "mode": "online",
                  "location": null,
                  "duration": "2h",
                  "slots": 50,
                  "visibility": "public",
                  "startDate": "2026-06-01",
                  "startTime": "18:00",
                  "endRegistrationDate": "2026-05-31",
                  "totalSeats": 100,
                  "eventDescription": "KMP migration",
                  "photographs": [],
                  "prizes": null,
                  "isTeamEvent": false,
                  "isPaid": true,
                  "isLive": true,
                  "organizerName": "Talkeys",
                  "organizerEmail": "team@talkeys.xyz",
                  "organizerContact": null
                }
              ]
            }
        """
    }
}
