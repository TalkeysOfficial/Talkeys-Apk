package com.talkeys.shared.presentation.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talkeys.shared.data.events.EventDetail
import com.talkeys.shared.data.events.EventsRepository
import com.talkeys.shared.network.ApiError
import com.talkeys.shared.network.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI state for the event detail screen. Swift-friendly through SKIE.
 */
sealed interface EventDetailUiState {
    data object Loading : EventDetailUiState
    data class Content(val event: EventDetail) : EventDetailUiState
    data class Error(val message: String) : EventDetailUiState
}

/**
 * Shared ViewModel for the event detail screen.
 */
class EventDetailViewModel(
    private val repository: EventsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<EventDetailUiState>(EventDetailUiState.Loading)
    val uiState: StateFlow<EventDetailUiState> = _uiState.asStateFlow()

    private var currentEventId: String? = null

    fun loadEvent(eventId: String, forceRefresh: Boolean = false) {
        currentEventId = eventId
        viewModelScope.launch {
            _uiState.value = EventDetailUiState.Loading
            when (val result = repository.getEventById(eventId, forceRefresh)) {
                is ApiResult.Success -> {
                    _uiState.value = EventDetailUiState.Content(result.data)
                }
                is ApiResult.Failure -> {
                    _uiState.value = EventDetailUiState.Error(errorMessage(result.error))
                }
            }
        }
    }

    fun retry() {
        currentEventId?.let { loadEvent(it, forceRefresh = true) }
    }

    private fun errorMessage(error: ApiError): String = when (error) {
        is ApiError.NetworkError -> "Please check your internet connection and try again."
        is ApiError.HttpError -> "Server error (${error.status}). Please try again."
        is ApiError.ParseError -> "Could not read event details. Please try again."
        is ApiError.Unknown -> error.cause
    }
}
