package com.talkeys.shared.logging

import co.touchlab.kermit.Logger

/**
 * Minimal logging facade wrapping Kermit.
 *
 * Future shared repositories and ViewModels should use this instead of
 * platform-specific logging (Android `Log`, iOS `print/NSLog`).
 *
 * This phase establishes the primitive only; existing production logging
 * is not migrated here.
 */
object AppLogger {

    fun d(tag: String, message: String) {
        Logger.d(tag) { message }
    }

    fun i(tag: String, message: String) {
        Logger.i(tag) { message }
    }

    fun w(tag: String, message: String) {
        Logger.w(tag) { message }
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Logger.e(tag, throwable) { message }
        } else {
            Logger.e(tag) { message }
        }
    }
}
