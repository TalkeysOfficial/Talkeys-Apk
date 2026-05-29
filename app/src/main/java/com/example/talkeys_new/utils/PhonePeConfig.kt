package com.example.talkeys_new.utils

import com.talkeys.shared.config.ProductionConfig

/**
 * PhonePe mobile configuration.
 *
 * Only public SDK identifiers belong here. Order creation, signing, and
 * verification must stay on the backend.
 */
object PhonePeConfig {
    
    // Public identifiers used by the mobile SDK.
    const val MERCHANT_ID = "M22ZDT307F584"
    const val CLIENT_ID = ProductionConfig.PHONEPE_CLIENT_ID
    
    // Environment Configuration
    const val IS_PRODUCTION = ProductionConfig.IS_PHONEPE_PRODUCTION
    
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
     * CLIENT_ID is used for SDK initialization and is safe to embed in the app.
     */
}
