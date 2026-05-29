package com.talkeys.shared.config

/**
 * Production Configuration
 * Centralized configuration for production environment
 */
object ProductionConfig {
    
    // Environment Settings
    const val IS_PRODUCTION = true // Production API mode
    const val IS_DEBUG_LOGGING_ENABLED = false
    
    // PhonePe-specific environment (separate from API environment)
    const val IS_PHONEPE_PRODUCTION = true
    
    // API Configuration - Production Backend
    const val API_BASE_URL = "https://api.talkeys.xyz"
    const val API_TIMEOUT_SECONDS = 30L
    const val API_RETRY_COUNT = 3
    
    // Payment Configuration
    const val DEFAULT_CURRENCY = "INR"
    const val PAYMENT_TIMEOUT_MINUTES = 15
    
    // Security Settings
    const val ENABLE_SSL_PINNING = false // Disabled for test environment
    const val ENABLE_REQUEST_ENCRYPTION = false // Disabled for easier debugging in test
    
    // Feature Flags
    const val ENABLE_PAYMENT_ANALYTICS = true
    const val ENABLE_CRASH_REPORTING = true
    const val ENABLE_PERFORMANCE_MONITORING = true
    
    // Validation Settings
    const val MIN_PAYMENT_AMOUNT = 1.0
    const val MAX_PAYMENT_AMOUNT = 100000.0
    const val MAX_FRIENDS_PER_BOOKING = 10
    
    /**
     * Get API base URL based on environment
     */
    fun getApiBaseUrl(): String {
        // Always use production API - only PhonePe environment changes for testing
        return API_BASE_URL
    }
    
    /**
     * Check if feature is enabled
     */
    fun isFeatureEnabled(feature: String): Boolean {
        return when (feature) {
            "payment_analytics" -> ENABLE_PAYMENT_ANALYTICS
            "crash_reporting" -> ENABLE_CRASH_REPORTING
            "performance_monitoring" -> ENABLE_PERFORMANCE_MONITORING
            else -> false
        }
    }
}
