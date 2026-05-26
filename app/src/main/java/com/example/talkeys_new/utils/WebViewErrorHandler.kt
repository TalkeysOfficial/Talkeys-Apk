package com.example.talkeys_new.utils

import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView

/**
 * WebView Error Handler for PhonePe Integration
 * 
 * This class helps capture and handle WebView JavaScript errors that occur
 * during PhonePe payment processing. These errors are often cosmetic and
 * don't indicate actual payment failure.
 */
class WebViewErrorHandler {
    
    companion object {
        private const val TAG = "WebViewError"
        
        /**
         * Create a WebChromeClient that captures JavaScript console messages
         */
        fun createErrorCapturingWebChromeClient(): WebChromeClient {
            return object : WebChromeClient() {
                override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                    val message = consoleMessage.message()
                    val level = consoleMessage.messageLevel()
                    when (level) {
                        ConsoleMessage.MessageLevel.ERROR -> {
                            Log.e(TAG, "WebView JavaScript error received")
                            
                            // Check for known PhonePe WebView errors
                            analyzePhonePeError(message)
                        }
                        ConsoleMessage.MessageLevel.WARNING -> {
                            Log.w(TAG, "WebView warning received")
                        }
                        ConsoleMessage.MessageLevel.LOG -> {
                            Log.d(TAG, "WebView console log received")
                        }
                        else -> {
                            Log.i(TAG, "WebView console info received")
                        }
                    }
                    
                    return true // Return true to prevent default handling
                }
            }
        }
        
        /**
         * Analyze PhonePe-specific WebView errors
         */
        private fun analyzePhonePeError(message: String) {
            when {
                message.contains("Content Security Policy") -> {
                    Log.d(TAG, "💡 CSP Error Analysis:")
                    Log.d(TAG, "  - This is a Content Security Policy violation")
                    Log.d(TAG, "  - Common in PhonePe WebView, usually cosmetic")
                    Log.d(TAG, "  - Payment can still succeed despite this error")
                    Log.d(TAG, "  - Always verify payment status via backend API")
                }
                
                message.contains("worker") && message.contains("blob:") -> {
                    Log.d(TAG, "💡 Web Worker Error Analysis:")
                    Log.d(TAG, "  - PhonePe trying to create web worker")
                    Log.d(TAG, "  - Blocked by CSP policy")
                    Log.d(TAG, "  - This doesn't affect payment processing")
                    Log.d(TAG, "  - Payment verification should still work")
                }
                
                message.contains("Error during payment processing") -> {
                    Log.d(TAG, "💡 Payment Processing Error Analysis:")
                    Log.d(TAG, "  - Generic payment processing error")
                    Log.d(TAG, "  - Could be cosmetic WebView error")
                    Log.d(TAG, "  - CRITICAL: Verify payment status on backend")
                    Log.d(TAG, "  - Use PhonePeIntegrationHelper.handleWebViewErrors()")
                }
                
                message.contains("phonepe.com") -> {
                    Log.d(TAG, "💡 PhonePe Domain Error Analysis:")
                    Log.d(TAG, "  - Error from PhonePe domain")
                    Log.d(TAG, "  - Likely WebView compatibility issue")
                    Log.d(TAG, "  - Payment might have succeeded on server")
                    Log.d(TAG, "  - Check payment status via API")
                }
                
                else -> {
                    Log.d(TAG, "💡 Unknown WebView Error:")
                    Log.d(TAG, "  - Unrecognized error pattern")
                    Log.d(TAG, "  - Monitor for payment impact")
                    Log.d(TAG, "  - Verify payment status if needed")
                }
            }
        }
        
        /**
         * Handle WebView errors and suggest next steps
         */
        fun handleWebViewError(
            errorMessage: String,
            onSuggestVerification: () -> Unit
        ) {
            Log.d(TAG, "WebView error handler invoked")
            
            val shouldVerify = when {
                errorMessage.contains("Error during payment processing") -> {
                    Log.d(TAG, "  Recommendation: VERIFY PAYMENT STATUS")
                    true
                }
                errorMessage.contains("Content Security Policy") -> {
                    Log.d(TAG, "  Recommendation: Monitor, likely cosmetic")
                    false
                }
                errorMessage.contains("worker") -> {
                    Log.d(TAG, "  Recommendation: Ignore, cosmetic error")
                    false
                }
                else -> {
                    Log.d(TAG, "  Recommendation: Monitor and verify if payment expected")
                    true
                }
            }
            
            if (shouldVerify) {
                Log.d(TAG, "  🚨 Triggering payment verification...")
                onSuggestVerification()
            }
        }
        
        /**
         * Log WebView error statistics
         */
        fun logErrorStatistics() {
            Log.d(TAG, "📊 WebView Error Statistics:")
            Log.d(TAG, "  - CSP Errors: Common, usually harmless")
            Log.d(TAG, "  - Worker Errors: Common, cosmetic only")
            Log.d(TAG, "  - Payment Errors: Require verification")
            Log.d(TAG, "  - Unknown Errors: Monitor and investigate")
        }
        
        /**
         * Get recommendations for WebView errors
         */
        fun getErrorRecommendations(): List<String> {
            return listOf(
                "✅ Always verify payment status via backend API",
                "⚠️ Don't rely on WebView result codes alone",
                "🔄 Use automatic payment verification with retry",
                "📱 Consider encouraging PhonePe app installation",
                "🌐 WebView is fallback when app not installed",
                "🚨 JavaScript errors are often cosmetic",
                "💡 Payment can succeed despite WebView errors"
            )
        }
    }
}
