package com.talkeys.shared.presentation.events

data class SharedImage(
    val bytes: ByteArray,
    val mimeType: String,
    val fileName: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SharedImage) return false
        return bytes.contentEquals(other.bytes) &&
            mimeType == other.mimeType &&
            fileName == other.fileName
    }

    override fun hashCode(): Int {
        var result = bytes.contentHashCode()
        result = 31 * result + mimeType.hashCode()
        result = 31 * result + fileName.hashCode()
        return result
    }
}

data class OrganizerInfoDraft(
    val organizerName: String = "",
    val emailAddress: String = "",
    val contactNumber: String = "",
    val organizationName: String = "",
    val cityState: String = "",
    val socialMediaLinks: String = "",
    val organizerDocument: SharedImage? = null
)

data class EventDetailsDraft(
    val eventName: String = "",
    val eventType: String = "",
    val eventCategory: String = "",
    val eventDescription: String = "",
    val eventBanner: SharedImage? = null
)

data class EventScheduleDraft(
    val eventDates: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val registrationDeadline: String = "",
    val maxAttendees: String = "",
    val platformUsed: String = "",
    val willBeRecorded: String = ""
)

data class EventPricingDraft(
    val eventType: String = "",
    val ticketPrice: String = "",
    val discounts: String = "",
    val discountPercentage: String = "",
    val qrCheckIn: String = "",
    val refundPolicy: String = ""
)

data class EventAudienceDraft(
    val communityChat: String = "",
    val sponsors: String = "",
    val audienceType: String = "",
    val sponsorDeck: SharedImage? = null
)

data class EventReviewDraft(
    val isInfoAccurate: Boolean = false,
    val agreeToTerms: Boolean = false
)

data class EventCreationDraft(
    val organizerInfo: OrganizerInfoDraft = OrganizerInfoDraft(),
    val details: EventDetailsDraft = EventDetailsDraft(),
    val schedule: EventScheduleDraft = EventScheduleDraft(),
    val pricing: EventPricingDraft = EventPricingDraft(),
    val audience: EventAudienceDraft = EventAudienceDraft(),
    val review: EventReviewDraft = EventReviewDraft()
)

enum class EventCreationStep(val number: Int) {
    OrganizerInfo(1),
    Details(2),
    Schedule(3),
    Pricing(4),
    Audience(5),
    Review(6);

    fun next(): EventCreationStep = entries.firstOrNull { it.number == number + 1 } ?: this
    fun previous(): EventCreationStep = entries.firstOrNull { it.number == number - 1 } ?: this
}

data class ValidationResult(
    val isValid: Boolean,
    val errors: Map<String, String> = emptyMap()
)

data class EventCreationUiState(
    val draft: EventCreationDraft = EventCreationDraft(),
    val currentStep: EventCreationStep = EventCreationStep.OrganizerInfo,
    val validationErrors: Map<String, String> = emptyMap(),
    val isSubmitting: Boolean = false,
    val submissionError: String? = null,
    val submissionRequested: Boolean = false
)
