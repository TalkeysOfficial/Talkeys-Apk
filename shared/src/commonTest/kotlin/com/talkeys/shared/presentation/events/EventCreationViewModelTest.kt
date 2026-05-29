package com.talkeys.shared.presentation.events

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EventCreationViewModelTest {

    @Test
    fun organizerInfoValidation_requiresCoreFieldsAndDocument() {
        val result = EventCreationValidator.validateOrganizerInfo(OrganizerInfoDraft())

        assertFalse(result.isValid)
        assertTrue("organizerName" in result.errors)
        assertTrue("emailAddress" in result.errors)
        assertTrue("socialMediaLinks" in result.errors)
        assertTrue("organizerDocument" in result.errors)
    }

    @Test
    fun pricingValidation_requiresPriceOnlyForPaidEvents() {
        val freeResult = EventCreationValidator.validatePricing(
            EventPricingDraft(
                eventType = "Free",
                discounts = "No",
                qrCheckIn = "Yes",
                refundPolicy = "No refunds"
            )
        )
        assertTrue(freeResult.isValid)

        val paidResult = EventCreationValidator.validatePricing(
            EventPricingDraft(
                eventType = "Paid",
                discounts = "No",
                qrCheckIn = "Yes",
                refundPolicy = "No refunds"
            )
        )
        assertFalse(paidResult.isValid)
        assertEquals("Ticket price is required", paidResult.errors["ticketPrice"])
    }

    @Test
    fun goNext_staysOnCurrentStepWhenInvalid() {
        val viewModel = EventCreationViewModel()

        val moved = viewModel.goNext()

        assertFalse(moved)
        assertEquals(EventCreationStep.OrganizerInfo, viewModel.uiState.value.currentStep)
        assertTrue(viewModel.uiState.value.validationErrors.isNotEmpty())
    }

    @Test
    fun goNext_advancesWhenCurrentStepIsValid() {
        val viewModel = EventCreationViewModel()
        viewModel.updateOrganizerInfo(validOrganizerInfo())

        val moved = viewModel.goNext()

        assertTrue(moved)
        assertEquals(EventCreationStep.Details, viewModel.uiState.value.currentStep)
    }

    @Test
    fun moveToStep_setsCurrentStepAndClearsValidationErrors() {
        val viewModel = EventCreationViewModel()
        viewModel.goNext()

        viewModel.moveToStep(EventCreationStep.Pricing)

        assertEquals(EventCreationStep.Pricing, viewModel.uiState.value.currentStep)
        assertTrue(viewModel.uiState.value.validationErrors.isEmpty())
    }

    @Test
    fun requestSubmit_setsSubmissionRequestedOnlyForCompleteDraft() {
        val viewModel = EventCreationViewModel()
        viewModel.updateOrganizerInfo(validOrganizerInfo())
        viewModel.updateDetails(
            EventDetailsDraft(
                eventName = "Kotlin Meetup",
                eventType = "Workshop",
                eventCategory = "Tech",
                eventDescription = "A long enough event description"
            )
        )
        viewModel.updateSchedule(
            EventScheduleDraft(
                eventDates = "2026-01-01",
                startTime = "10:00",
                endTime = "12:00",
                registrationDeadline = "2025-12-25",
                maxAttendees = "100",
                platformUsed = "Google Meet",
                willBeRecorded = "No"
            )
        )
        viewModel.updatePricing(
            EventPricingDraft(
                eventType = "Free",
                discounts = "No",
                qrCheckIn = "Yes",
                refundPolicy = "No refunds"
            )
        )
        viewModel.updateAudience(
            EventAudienceDraft(
                communityChat = "Yes",
                sponsors = "No",
                audienceType = "Students"
            )
        )
        viewModel.updateReview(EventReviewDraft(isInfoAccurate = true, agreeToTerms = true))

        assertTrue(viewModel.requestSubmit())
        assertTrue(viewModel.uiState.value.submissionRequested)
        assertTrue(viewModel.uiState.value.validationErrors.isEmpty())
    }

    private fun validOrganizerInfo() = OrganizerInfoDraft(
        organizerName = "Talkeys",
        emailAddress = "team@talkeys.xyz",
        contactNumber = "9876543210",
        organizationName = "Talkeys",
        cityState = "Delhi, India",
        socialMediaLinks = "https://talkeys.xyz",
        organizerDocument = SharedImage(
            bytes = byteArrayOf(1, 2, 3),
            mimeType = "image/png",
            fileName = "doc.png"
        )
    )
}
