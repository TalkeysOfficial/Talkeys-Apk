package com.talkeys.shared

/**
 * Stable Swift-facing facade for the shared Kotlin module.
 *
 * iOS calls [SharedApp.initialize] once at app launch, then reads
 * [SharedApp.greeting] to confirm the framework loaded successfully.
 *
 * This object intentionally does not expose auth, networking, storage,
 * or payment APIs. Those will be wired in later phases.
 */
object SharedApp {
    fun initialize() {
        initKoin()
    }

    fun greeting(): String = "Talkeys shared module loaded"
}
