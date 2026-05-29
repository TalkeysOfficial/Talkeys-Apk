package com.talkeys.shared.presentation.events

object EventCreationValidator {
    private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
    private val urlRegex = Regex("^(https?://)?([A-Za-z0-9-]+\\.)+[A-Za-z]{2,}(/.*)?$")

    fun validateOrganizerInfo(draft: OrganizerInfoDraft): ValidationResult = buildValidation {
        requireField("organizerName", draft.organizerName, "Organizer name is required")
        requireField("emailAddress", draft.emailAddress, "Email is required")
        if (draft.emailAddress.isNotBlank() && !emailRegex.matches(draft.emailAddress.trim())) {
            put("emailAddress", "Enter a valid email address")
        }
        requireField("contactNumber", draft.contactNumber, "Contact number is required")
        if (draft.contactNumber.isNotBlank() && !isValidPhoneNumber(draft.contactNumber)) {
            put("contactNumber", "Enter a valid contact number")
        }
        requireField("organizationName", draft.organizationName, "Organization name is required")
        requireField("cityState", draft.cityState, "City and state are required")
        requireField("socialMediaLinks", draft.socialMediaLinks, "Social media link is required")
        if (draft.socialMediaLinks.isNotBlank() && !urlRegex.matches(draft.socialMediaLinks.trim())) {
            put("socialMediaLinks", "Enter a valid social link")
        }
        if (draft.organizerDocument == null) {
            put("organizerDocument", "Upload organizer verification document")
        }
    }

    fun validateDetails(draft: EventDetailsDraft): ValidationResult = buildValidation {
        requireField("eventName", draft.eventName, "Event name is required")
        if (draft.eventName.trim().length in 1..2) {
            put("eventName", "Event name must be at least 3 characters")
        }
        requireField("eventType", draft.eventType, "Event type is required")
        requireField("eventCategory", draft.eventCategory, "Event category is required")
        requireField("eventDescription", draft.eventDescription, "Event description is required")
        if (draft.eventDescription.trim().length in 1..19) {
            put("eventDescription", "Description must be at least 20 characters")
        }
    }

    fun validateSchedule(draft: EventScheduleDraft): ValidationResult = buildValidation {
        requireField("eventDates", draft.eventDates, "Event date is required")
        requireField("startTime", draft.startTime, "Start time is required")
        requireField("endTime", draft.endTime, "End time is required")
        requireField("registrationDeadline", draft.registrationDeadline, "Registration deadline is required")
        requireField("maxAttendees", draft.maxAttendees, "Maximum attendees is required")
        if (draft.maxAttendees.isNotBlank() && draft.maxAttendees.toIntOrNull()?.let { it > 0 } != true) {
            put("maxAttendees", "Maximum attendees must be a positive number")
        }
        requireField("platformUsed", draft.platformUsed, "Platform or venue details are required")
        requireField("willBeRecorded", draft.willBeRecorded, "Recording preference is required")
    }

    fun validatePricing(draft: EventPricingDraft): ValidationResult = buildValidation {
        requireField("eventType", draft.eventType, "Choose free or paid")
        if (draft.eventType.equals("Paid", ignoreCase = true)) {
            requireField("ticketPrice", draft.ticketPrice, "Ticket price is required")
            if (draft.ticketPrice.isNotBlank() && draft.ticketPrice.toDoubleOrNull()?.let { it >= 0.0 } != true) {
                put("ticketPrice", "Ticket price must be a valid amount")
            }
        }
        requireField("discounts", draft.discounts, "Choose whether discounts apply")
        if (draft.discounts.equals("Yes", ignoreCase = true)) {
            requireField("discountPercentage", draft.discountPercentage, "Discount percentage is required")
            val discount = draft.discountPercentage.toIntOrNull()
            if (draft.discountPercentage.isNotBlank() && (discount == null || discount !in 1..100)) {
                put("discountPercentage", "Discount must be between 1 and 100")
            }
        }
        requireField("qrCheckIn", draft.qrCheckIn, "Choose QR check-in preference")
        requireField("refundPolicy", draft.refundPolicy, "Choose refund policy")
    }

    fun validateAudience(draft: EventAudienceDraft): ValidationResult = buildValidation {
        requireField("communityChat", draft.communityChat, "Choose community chat preference")
        requireField("sponsors", draft.sponsors, "Choose sponsor preference")
        requireField("audienceType", draft.audienceType, "Audience type is required")
    }

    fun validateReview(draft: EventReviewDraft): ValidationResult = buildValidation {
        if (!draft.isInfoAccurate) put("isInfoAccurate", "Confirm the event information is accurate")
        if (!draft.agreeToTerms) put("agreeToTerms", "Accept the terms and privacy policy")
    }

    fun validateStep(step: EventCreationStep, draft: EventCreationDraft): ValidationResult = when (step) {
        EventCreationStep.OrganizerInfo -> validateOrganizerInfo(draft.organizerInfo)
        EventCreationStep.Details -> validateDetails(draft.details)
        EventCreationStep.Schedule -> validateSchedule(draft.schedule)
        EventCreationStep.Pricing -> validatePricing(draft.pricing)
        EventCreationStep.Audience -> validateAudience(draft.audience)
        EventCreationStep.Review -> validateReview(draft.review)
    }

    fun validateAll(draft: EventCreationDraft): ValidationResult {
        val errors = EventCreationStep.entries
            .flatMap { validateStep(it, draft).errors.entries }
            .associate { it.key to it.value }
        return ValidationResult(errors.isEmpty(), errors)
    }

    private fun isValidPhoneNumber(phone: String): Boolean {
        val digits = phone.filter { it.isDigit() }
        return digits.length in 10..15
    }

    private inline fun buildValidation(block: MutableMap<String, String>.() -> Unit): ValidationResult {
        val errors = mutableMapOf<String, String>().apply(block)
        return ValidationResult(errors.isEmpty(), errors)
    }

    private fun MutableMap<String, String>.requireField(
        key: String,
        value: String,
        message: String
    ) {
        if (value.isBlank()) put(key, message)
    }
}
