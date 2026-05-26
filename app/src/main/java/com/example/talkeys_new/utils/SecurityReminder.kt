package com.example.talkeys_new.utils

/**
 * PhonePe signing secrets belong only on the backend.
 *
 * A production client secret was previously embedded in app source. It has
 * been removed from the client, but must be rotated outside this repository.
 */

object SecurityReminder {
    const val REMINDER = "Keep PhonePe signing secrets on the backend only."
}
