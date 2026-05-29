package com.talkeys.shared.presentation.events

import com.talkeys.shared.data.events.EventDetail
import com.talkeys.shared.data.events.EventsApi
import com.talkeys.shared.data.events.EventsRepository
import com.talkeys.shared.network.ApiClient
import com.talkeys.shared.network.ApiError
import com.talkeys.shared.network.ApiResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class EventDetailViewModelTest {

    private val detail = EventDetail(
        id = "event-1",
        name = "Tech Talk",
        category = "Technology",
        mode = "online",
        duration = "2 hours",
        ticketPrice = "250",
        totalSeats = 100,
        slots = 5,
        visibility = "public",
        startDate = "2025-06-01",
        startTime = "14:00",
        isLive = true,
        isPaid = true,
        isTeamEvent = false
    )

    @Test
    fun loadEvent_success_showsContent() = runTest {
        val viewModel = EventDetailViewModel(repositoryReturning(ApiResult.Success(detail)))

        viewModel.loadEvent("event-1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertIs<EventDetailUiState.Content>(state)
        assertEquals("event-1", state.event.id)
    }

    @Test
    fun loadEvent_failure_showsError() = runTest {
        val viewModel = EventDetailViewModel(repositoryReturning(ApiResult.Failure(ApiError.NetworkError)))

        viewModel.loadEvent("event-1")
        advanceUntilIdle()

        assertIs<EventDetailUiState.Error>(viewModel.uiState.value)
    }

    private fun repositoryReturning(result: ApiResult<EventDetail>) =
        object : EventsRepository(EventsApi(ApiClient())) {
            override suspend fun getEventById(
                eventId: String,
                forceRefresh: Boolean
            ): ApiResult<EventDetail> = result
        }
}
