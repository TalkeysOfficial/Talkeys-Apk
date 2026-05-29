package com.example.talkeys_new.screens.events.createEvent

import android.content.Context
import android.net.Uri
import com.talkeys.shared.presentation.events.SharedImage

fun Uri?.toSharedImage(context: Context, fileName: String): SharedImage? {
    if (this == null) return null
    val mimeType = context.contentResolver.getType(this) ?: "application/octet-stream"
    val bytes = context.contentResolver.openInputStream(this)?.use { it.readBytes() } ?: return null
    return SharedImage(
        bytes = bytes,
        mimeType = mimeType,
        fileName = fileName.takeUnless { it.isBlank() || it == "No file selected" } ?: "selected-file"
    )
}
