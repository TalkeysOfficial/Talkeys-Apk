package com.example.talkeys_new.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import com.phonepe.intent.sdk.api.PhonePeKt
import com.phonepe.intent.sdk.api.models.PhonePeEnvironment
import com.talkeys.shared.data.payment.Friend

/**
 * Production-ready PhonePe Integration Helper
 * 
 * This class provides utility methods for PhonePe integration with proper error handling,
 * WebView fallback, and environment switching.
 */
object PhonePeIntegrationHelper {
    
    private const val TAG = "PhonePeHelper"
    
    /**
     * Complete payment flow with automatic backend integration
     * 
     * @param activity Current activity
     * @param eventId Event ID for ticket booking
     * @param passType Pass type (e.g., "standard", "premium")
     * @param friends List of friends for group booking
     * @param launcher Activity result launcher
     * @param authToken User authentication token
     * @param onResult Callback for payment result
     */
    fun initiateCompletePaymentFlow(
        activity: Activity,
        eventId: String,
        passType: String,
        friends: List<Friend> = emptyList(),
        launcher: ActivityResultLauncher<Intent>,
        authToken: String? = null,
        onResult: (PhonePePaymentManager.PaymentResult) -> Unit
    ) {
        Log.d(TAG, "🚀 Starting complete payment flow for event: $eventId")
        
        PhonePePaymentManager.bookTicketAndPay(
            activity = activity,
            eventId = eventId,
            passType = passType,
            friends = friends,
            activityResultLauncher = launcher,
            authToken = authToken,
            onResult = onResult
        )
    }
    
    /**
     * Manual payment verification for WebView issues
     * Call this when WebView shows errors but you want to check actual payment status
     */
    fun verifyPaymentManually(
        merchantOrderId: String,
        authToken: String? = null,
        onResult: (PhonePePaymentManager.PaymentResult) -> Unit
    ) {
        Log.d(TAG, "🔍 Manual payment verification requested")
        
        PhonePePaymentManager.manualPaymentVerification(
            merchantOrderId = merchantOrderId,
            authToken = authToken,
            onResult = onResult
        )
    }
    
    /**
     * Handle WebView JavaScript errors
     * Call this when you see JavaScript console errors in WebView
     */
    fun handleWebViewErrors(onResult: (PhonePePaymentManager.PaymentResult) -> Unit) {
        Log.d(TAG, "🚨 Handling WebView JavaScript errors")
        
        PhonePePaymentManager.handleWebViewError(onResult)
    }
    
    /**
     * Check PhonePe app installation status
     */
    fun isPhonePeAppInstalled(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo("com.phonepe.app", 0)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get current SDK status for debugging
     */
    fun getSDKStatus(): String {
        return """
            PhonePe SDK Status:
            - Environment: ${PhonePeConfig.getEnvironmentName()}
            - Production Mode: ${PhonePeConfig.IS_PRODUCTION}
            
            ${PhonePePaymentManager.getCurrentPaymentInfo()}
        """.trimIndent()
    }
    
    /**
     * Environment validation helper
     */
    fun validateEnvironmentSetup(): List<String> {
        val issues = mutableListOf<String>()
        
        // Check merchant ID format
        if (PhonePeConfig.MERCHANT_ID.isBlank()) {
            issues.add("Merchant ID is empty")
        }
        
        // Check client ID format
        if (PhonePeConfig.CLIENT_ID.isBlank()) {
            issues.add("Client ID is empty")
        }
        
        // Check environment consistency
        if (PhonePeConfig.IS_PRODUCTION) {
            if (PhonePeConfig.MERCHANT_ID.startsWith("PGTESTPAYUAT")) {
                issues.add("Production mode but using test merchant ID")
            }
        } else {
            if (!PhonePeConfig.MERCHANT_ID.startsWith("PGTESTPAYUAT") && 
                !PhonePeConfig.MERCHANT_ID.startsWith("M")) {
                issues.add("Sandbox mode but merchant ID format unclear")
            }
        }
        
        return issues
    }
    
    /**
     * Force payment verification - use when WebView shows errors
     */
    fun forceVerifyCurrentPayment(): String {
        return PhonePePaymentManager.forcePaymentVerification()
    }
    
    /**
     * Open PhonePe app directly (if installed)
     */
    fun openPhonePeApp(context: Context): Boolean {
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage("com.phonepe.app")
            if (intent != null) {
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open PhonePe app: ${e.message}")
            false
        }
    }
    
    /**
     * Open PhonePe in Play Store for installation
     */
    fun openPhonePeInPlayStore(context: Context) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.phonepe.app"))
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to web browser
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.phonepe.app"))
            context.startActivity(intent)
        }
    }
    
    /**
     * Reinitialize PhonePe SDK (for troubleshooting)
     */
    fun reinitializeSDK(context: Context): Boolean {
        return try {
            val environment = if (PhonePeConfig.IS_PRODUCTION) {
                PhonePeEnvironment.RELEASE
            } else {
                PhonePeEnvironment.SANDBOX
            }
            
            PhonePeKt.init(
                context = context,
                merchantId = PhonePeConfig.MERCHANT_ID,
                flowId = PhonePeConfig.generateFlowId(),
                phonePeEnvironment = environment,
                enableLogging = !PhonePeConfig.IS_PRODUCTION,
                appId = null
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to reinitialize SDK: ${e.message}")
            false
        }
    }
}
