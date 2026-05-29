package com.talkeys.shared.data.events

import com.talkeys.shared.network.ApiError
import com.talkeys.shared.network.ApiResult
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class EventsApiTest {

    private val listJson = """
    {
      "status": "success",
      "data": {
        "events": [
          {
            "_id": "abc123",
            "name": "Tech Talk",
            "category": "Technology",
            "ticketPrice": 250,
            "mode": "online",
            "location": "Virtual",
            "duration": "2 hours",
            "slots": 5,
            "visibility": "public",
            "startDate": "2025-06-01T00:00:00.000Z",
            "startTime": "14:00",
            "endRegistrationDate": "2025-05-30",
            "totalSeats": "100",
            "eventDescription": "A tech talk",
            "photographs": ["https://img.example.com/1.jpg"],
            "prizes": "Certificates",
            "isTeamEvent": false,
            "isPaid": true,
            "isLive": true,
            "organizerName": "Org",
            "organizerEmail": "org@test.com",
            "organizerContact": "1234567890"
          }
        ],
        "pagination": {
          "total": 1,
          "page": 1,
          "pages": 1,
          "limit": 20
        }
      }
    }
    """.trimIndent()

    private val detailJson = """
    {
      "status": "success",
      "data": {
        "_id": "abc123",
        "name": "Tech Talk",
        "category": "Technology",
        "mode": "online",
        "location": "Virtual",
        "duration": "2 hours",
        "ticketPrice": "250",
        "totalSeats": 100,
        "slots": 5,
        "visibility": "public",
        "prizes": "Certificates",
        "photographs": ["https://img.example.com/1.jpg"],
        "startDate": "2025-06-01T00:00:00.000Z",
        "startTime": "14:00",
        "endDate": "2025-06-01",
        "endTime": "16:00",
        "endRegistrationDate": "2025-05-30",
        "eventDescription": "A tech talk",
        "isLive": true,
        "isPaid": true,
        "isTeamEvent": false,
        "organizerContact": "1234567890",
        "organizerEmail": "org@test.com",
        "organizerName": "Org",
        "registrationCount": 42,
        "sponserImages": [],
        "registrationLink": "https://example.com/register",
        "availableSeats": 58
      }
    }
    """.trimIndent()

    private fun apiFor(engine: MockEngine) = EventsApi(
        httpClient = HttpClient(engine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; isLenient = true })
            }
        },
        baseUrl = "https://api.talkeys.xyz"
    )

    // ── Successful list parsing ─────────────────────────────────────

    @Test
    fun getAllEvents_successfulParsing() = runTest {
        val engine = MockEngine { respondOk(listJson) }
        val api = apiFor(engine)

        val result = api.getAllEvents()
        assertIs<ApiResult.Success<EventListResponse>>(result)
        assertEquals(1, result.data.data.events.size)

        val event = result.data.data.events[0]
        assertEquals("abc123", event.id)
        assertEquals("Tech Talk", event.name)
        assertEquals("250", event.ticketPrice) // number -> String via FlexibleStringSerializer
        assertEquals(100, event.totalSeats)     // string "100" -> Int via FlexibleIntSerializer
        assertEquals(true, event.isLive)
    }

    // ── Successful detail parsing ───────────────────────────────────

    @Test
    fun getEventById_successfulParsing() = runTest {
        val engine = MockEngine { respondOk(detailJson) }
        val api = apiFor(engine)

        val result = api.getEventById("abc123")
        assertIs<ApiResult.Success<EventDetailResponse>>(result)

        val detail = result.data.data
        assertEquals("abc123", detail.id)
        assertEquals("250", detail.ticketPrice)
        assertEquals(100, detail.totalSeats)
        assertEquals(42, detail.registrationCount)
        assertEquals(58, detail.availableSeats)
        assertEquals("2025-06-01", detail.endDate)
        assertEquals("16:00", detail.endTime)
    }

    // ── Non-2xx maps to HttpError ───────────────────────────────────

    @Test
    fun getAllEvents_http404_mapsToHttpError() = runTest {
        val engine = MockEngine {
            respond("Not Found", HttpStatusCode.NotFound)
        }
        val api = apiFor(engine)

        val result = api.getAllEvents()
        assertIs<ApiResult.Failure>(result)
        val error = result.error
        assertIs<ApiError.HttpError>(error)
        assertEquals(404, error.status)
    }

    @Test
    fun getAllEvents_http500_mapsToHttpError() = runTest {
        val engine = MockEngine {
            respond("Internal Server Error", HttpStatusCode.InternalServerError)
        }
        val api = apiFor(engine)

        val result = api.getAllEvents()
        assertIs<ApiResult.Failure>(result)
        assertIs<ApiError.HttpError>(result.error)
        assertEquals(500, (result.error as ApiError.HttpError).status)
    }

    // ── Malformed JSON maps to ParseError ───────────────────────────

    @Test
    fun getAllEvents_malformedJson_mapsToParseError() = runTest {
        val engine = MockEngine {
            respondOk("{invalid json!!}")
        }
        val api = apiFor(engine)

        val result = api.getAllEvents()
        assertIs<ApiResult.Failure>(result)
        assertIs<ApiError.ParseError>(result.error)
    }

    // ── Helper ──────────────────────────────────────────────────────

    private fun MockRequestHandleScope.respondOk(content: String) =
        respond(
            content = content,
            status = HttpStatusCode.OK,
            headers = headersOf(HttpHeaders.ContentType, "application/json")
        )
}
