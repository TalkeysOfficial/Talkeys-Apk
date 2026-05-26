package com.example.talkeys_new.utils

/**
 * PhonePe Configuration
 * 
 * SECURITY WARNING: 
 * - These are your REAL PhonePe credentials
 * - Keep this file secure and never commit to public repositories
 * - Consider using BuildConfig or encrypted storage for production
 */
object PhonePeConfig {
    
    // PhonePe Business Dashboard Credentials
    // SECURITY: CLIENT_SECRET must never be stored in the mobile binary.
    // It belongs on the backend server only. The value previously committed here
    // has been removed and MUST be rotated — see CURRENT_CLIENT_API_AUDIT.md.
    const val MERCHANT_ID = "M22ZDT307F584"
    const val CLIENT_ID = "SU2504181253408025787154"
    
    // Environment Configuration
    const val IS_PRODUCTION = true // ⚠️ PRODUCTION MODE - REAL MONEY WILL BE CHARGED!
    
    // API Endpoints (you'll need these for backend integration)
    const val SANDBOX_BASE_URL = "https://api-preprod.phonepe.com/apis/pg-sandbox"
    const val PRODUCTION_BASE_URL = "https://api.phonepe.com/apis/hermes"
    
    fun getBaseUrl(): String {
        return if (IS_PRODUCTION) PRODUCTION_BASE_URL else SANDBOX_BASE_URL
    }
    
    fun getEnvironmentName(): String {
        return if (IS_PRODUCTION) "PRODUCTION" else "SANDBOX"
    }
    
    /**
     * Generate unique flow ID for each payment session
     */
    fun generateFlowId(): String {
        return "TALKEYS_${System.currentTimeMillis()}"
    }
    
    /**
     * CLIENT_ID is used for SDK initialization (safe to embed in the app).
     * CLIENT_SECRET belongs on the backend only — never in a mobile binary.
     */
}