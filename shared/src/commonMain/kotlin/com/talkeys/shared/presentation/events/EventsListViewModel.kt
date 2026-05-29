package com.talkeys.shared.presentation.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talkeys.shared.data.events.EventSummary
import com.talkeys.shared.data.events.EventsRepository
import com.talkeys.shared.network.ApiError
import com.talkeys.shared.network.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI state for the events list screen. Designed to be Swift-friendly
 * through SKIE — no Result<T>, Android resources, or opaque maps.
 */
sealed interface EventsListUiState {
    data object Loading : EventsListUiState
    data class Content(
        val events: List<EventSummary>,
        val showLiveOnly: Boolean = true
    ) : EventsListUiState
    data class Error(val message: String) : EventsListUiState
}

/**
 * Shared ViewModel for the events list screen.
 *
 * Exposes [uiState] as a [StateFlow] that both Android Compose
 * (via collectAsState) and SwiftUI (via SKIE AsyncSequence) can observe.
 */
class EventsListViewModel(
    private val repository: EventsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<EventsListUiState>(EventsListUiState.Loading)
    val uiState: StateFlow<EventsListUiState> = _uiState.asStateFlow()

    private var allEvents: List<EventSummary> = emptyList()
    private var showLiveOnly: Boolean = true

    init {
        loadEvents()
    }

    fun loadEvents(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = EventsListUiState.Loading
            when (val result = repository.getAllEvents(forceRefresh)) {
                is ApiResult.Success -> {
                    allEvents = result.data
                    applyFilter()
                }
                is ApiResult.Failure -> {
                    _uiState.value = EventsListUiState.Error(errorMessage(result.error))
                }
            }
        }
    }

    fun toggleFilter() {
        showLiveOnly = !showLiveOnly
        applyFilter()
    }

    fun retry() {
        loadEvents(forceRefresh = true)
    }

    fun dismissError() {
        _uiState.value = EventsListUiState.Content(emptyList(), showLiveOnly)
    }

    private fun applyFilter() {
        val filtered = if (showLiveOnly) {
            allEvents.filter { it.isLive }
        } else {
            allEvents.filter { !it.isLive }
        }
        _uiState.value = EventsListUiState.Content(
            events = filtered,
            showLiveOnly = showLiveOnly
        )
    }

    private fun errorMessage(error: ApiError): String = when (error) {
        is ApiError.NetworkError -> "Please check your internet connection and try again."
        is ApiError.HttpError -> "Server error (${error.status}). Please try again."
        is ApiError.ParseError -> "Could not read event data. Please try again."
        is ApiError.Unknown -> error.cause
    }
}
