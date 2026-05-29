package com.talkeys.shared.presentation.events

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class EventCreationViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(EventCreationUiState())
    val uiState: StateFlow<EventCreationUiState> = _uiState.asStateFlow()

    fun updateOrganizerInfo(value: OrganizerInfoDraft) {
        updateDraft { copy(organizerInfo = value) }
    }

    fun updateDetails(value: EventDetailsDraft) {
        updateDraft { copy(details = value) }
    }

    fun updateSchedule(value: EventScheduleDraft) {
        updateDraft { copy(schedule = value) }
    }

    fun updatePricing(value: EventPricingDraft) {
        updateDraft { copy(pricing = value) }
    }

    fun updateAudience(value: EventAudienceDraft) {
        updateDraft { copy(audience = value) }
    }

    fun updateReview(value: EventReviewDraft) {
        updateDraft { copy(review = value) }
    }

    fun validateCurrentStep(): Boolean {
        val state = _uiState.value
        val result = EventCreationValidator.validateStep(state.currentStep, state.draft)
        _uiState.value = state.copy(validationErrors = result.errors)
        return result.isValid
    }

    fun moveToStep(step: EventCreationStep) {
        _uiState.value = _uiState.value.copy(
            currentStep = step,
            validationErrors = emptyMap()
        )
    }

    fun goNext(): Boolean {
        if (!validateCurrentStep()) return false
        _uiState.value = _uiState.value.copy(
            currentStep = _uiState.value.currentStep.next(),
            validationErrors = emptyMap()
        )
        return true
    }

    fun goPrevious() {
        _uiState.value = _uiState.value.copy(
            currentStep = _uiState.value.currentStep.previous(),
            validationErrors = emptyMap()
        )
    }

    fun requestSubmit(): Boolean {
        val result = EventCreationValidator.validateAll(_uiState.value.draft)
        _uiState.value = _uiState.value.copy(
            validationErrors = result.errors,
            submissionRequested = result.isValid,
            submissionError = if (result.isValid) null else "Fix validation errors before submitting."
        )
        return result.isValid
    }

    fun markSubmissionHandled() {
        _uiState.value = _uiState.value.copy(submissionRequested = false)
    }

    fun resetDraft() {
        _uiState.value = EventCreationUiState()
    }

    private fun updateDraft(update: EventCreationDraft.() -> EventCreationDraft) {
        _uiState.value = _uiState.value.copy(
            draft = _uiState.value.draft.update(),
            validationErrors = emptyMap(),
            submissionError = null
        )
    }
}
