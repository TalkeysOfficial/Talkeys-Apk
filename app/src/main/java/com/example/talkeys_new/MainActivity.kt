package com.example.talkeys_new

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.example.talkeys_new.navigation.AppNavigation
import com.example.talkeys_new.screens.authentication.TokenManager
import com.example.talkeys_new.ui.theme.Talkeys_NewTheme
import com.example.talkeys_new.utils.ConsentDialogHelper
import com.example.talkeys_new.utils.FCMInitializationManager
import com.example.talkeys_new.utils.FCMTokenManager
import com.example.talkeys_new.utils.PhonePePaymentManager
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.first
// Koin is initialized in TalkeysApplication.onCreate()

class MainActivity : ComponentActivity() {
    
    // Declare the launcher at the top of your Activity
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
            Log.d("FCM_PERMISSION", "Notification permission granted")
        } else {
            // Inform user that your app will not show notifications.
            Log.d("FCM_PERMISSION", "Notification permission denied")
        }
    }
    
    // PhonePe payment result launcher with enhanced handling
    private val phonePePaymentLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d(TAG, "🔍 PhonePe payment result received")
        Log.d(TAG, "📋 Result code: ${result.resultCode}")
        Log.d(TAG, "📋 Result includes data=${result.data != null}")
        
        // ✅ CRITICAL: Always verify payment status regardless of result code
        // WebView JavaScript errors can cause wrong result codes even when payment succeeds
        PhonePePaymentManager.handlePaymentResult(
            resultCode = result.resultCode,
            data = result.data
        ) { paymentResult ->
            Log.d(TAG, "💳 Payment result processed: $paymentResult")
            handlePhonePePaymentResult(paymentResult)
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // Check Google Play Services availability
        if (checkGooglePlayServices()) {
            // Log FCM initialization status
            FCMInitializationManager.logInitializationStatus(this)
            
            // Check if FCM is disabled by default (due to manifest settings)
            if (!FCMInitializationManager.isAutoInitEnabled()) {
                Log.d(TAG, "🔒 FCM auto-init is disabled by default (privacy-first approach)")
                Log.d(TAG, "📋 User consent required before enabling notifications")
                
                // Show user consent dialog or enable based on your app's logic
                handleFCMConsent()
            } else {
                Log.d(TAG, "✅ FCM auto-init is already enabled")
                initializeFCMFeatures()
            }
        } else {
            Log.e(TAG, "Google Play Services not available - FCM will not work")
        }
        
        setContent {
            Talkeys_NewTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavigation(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
    
    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
                Log.d("FCM_PERMISSION", "Notification permission already granted")
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
                Log.d("FCM_PERMISSION", "Should show permission rationale")
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                // Directly ask for the permission
                Log.d("FCM_PERMISSION", "Requesting notification permission")
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            // For Android 12L and below, permission is handled automatically
            Log.d("FCM_PERMISSION", "Android version < 33, no runtime permission needed")
        }
    }

    override fun onResume() {
        super.onResume()
        // Check Google Play Services again when app resumes
        checkGooglePlayServices()
    }
    
    private fun checkGooglePlayServices(): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this)
        
        return when (resultCode) {
            ConnectionResult.SUCCESS -> {
                Log.d(TAG, "✅ Google Play Services is available")
                true
            }
            ConnectionResult.SERVICE_MISSING,
            ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED,
            ConnectionResult.SERVICE_DISABLED -> {
                Log.w(TAG, "⚠️ Google Play Services issue: $resultCode")
                
                if (googleApiAvailability.isUserResolvableError(resultCode)) {
                    // Show dialog to user to resolve the issue
                    googleApiAvailability.makeGooglePlayServicesAvailable(this)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d(TAG, " Google Play Services resolved successfully")
                                // Retry FCM initialization
                                retrieveFCMToken()
                            } else {
                                Log.e(TAG, " Failed to resolve Google Play Services")
                            }
                        }
                } else {
                    Log.e(TAG, " Google Play Services error cannot be resolved")
                }
                false
            }
            else -> {
                Log.e(TAG, " Unknown Google Play Services error: $resultCode")
                false
            }
        }
    }
    
    private fun retrieveFCMToken() {
        FCMTokenManager.getCurrentToken { token ->
            if (token != null) {
                Log.d(TAG, "FCM registration token received")
                FCMTokenManager.storeToken(this, token)
                sendTokenToServer(token)
            } else {
                Log.e(TAG, "Failed to retrieve FCM token")
            }
        }
    }

    private fun sendTokenToServer(token: String) {
        // TODO: Implement this method to send token to your app server
        // This is where you would typically make an API call to your backend
        // to associate this token with the current user
        Log.d(TAG, "TODO: send FCM token to backend (not yet implemented)")
    }
    
    /**
     * Handle FCM consent - called when FCM is disabled by default
     * This is where you would show a consent dialog to the user
     */
    private fun handleFCMConsent() {
        Log.d(TAG, " Determining FCM consent strategy...")
        
        // Option 1: Check if user has previously made a choice
        val userConsent = getUserConsentPreference()
        
        when (userConsent) {
            ConsentStatus.GRANTED -> {
                Log.d(TAG, " User previously consented to notifications")
                enableFCMForUser()
            }
            ConsentStatus.DENIED -> {
                Log.d(TAG, " User previously denied notifications")
                // Keep FCM disabled
            }
            ConsentStatus.NOT_SET -> {
                Log.d(TAG, " User hasn't made a choice yet")
                
                // TESTING MODE: Auto-enable FCM for testing
                // TODO: Replace with showConsentDialog() for production
                Log.d(TAG, " TESTING: Auto-enabling FCM for notification testing")
                Log.d(TAG, " This will generate your FCM registration token")
                enableFCMForUser()
                saveUserConsentPreference(ConsentStatus.GRANTED)
                
                // For production, uncomment this instead:
                // showConsentDialog()
            }
        }
    }
    
    /**
     * Show consent dialog to user
     */
    private fun showConsentDialog() {
        Log.d(TAG, "📋 Showing FCM consent dialog to user")
        
        ConsentDialogHelper.showFCMConsentDialog(this) { granted ->
            if (granted) {
                Log.d(TAG, "✅ User granted consent for notifications")
                enableFCMForUser()
                saveUserConsentPreference(ConsentStatus.GRANTED)
            } else {
                Log.d(TAG, "❌ User denied consent for notifications")
                saveUserConsentPreference(ConsentStatus.DENIED)
                // Keep FCM disabled
            }
        }
    }
    
    /**
     * Initialize FCM features after consent is granted
     */
    private fun initializeFCMFeatures() {
        Log.d(TAG, "🚀 Initializing FCM features...")
        
        // Ask for notification permission
        askNotificationPermission()
        
        // Log current token status
        FCMTokenManager.logTokenStatus(this)
        
        // Retrieve FCM registration token
        retrieveFCMToken()
    }
    
    /**
     * Get user's consent preference from storage
     */
    private fun getUserConsentPreference(): ConsentStatus {
        val sharedPref = getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
        val consentValue = sharedPref.getInt("fcm_consent", ConsentStatus.NOT_SET.value)
        return ConsentStatus.fromValue(consentValue)
    }
    
    /**
     * Save user's consent preference
     */
    private fun saveUserConsentPreference(consent: ConsentStatus) {
        val sharedPref = getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putInt("fcm_consent", consent.value)
            apply()
        }
        Log.d(TAG, "💾 Saved user consent: $consent")
    }
    
    // Example methods for controlling FCM initialization
    // Call these based on user preferences or app requirements
    
    private fun enableFCMForUser() {
        Log.d(TAG, "🔓 User opted in to notifications")
        FCMInitializationManager.enableAll(this)
        initializeFCMFeatures()
    }
    
    private fun disableFCMForUser() {
        Log.d(TAG, "🔒 User opted out of notifications")
        FCMInitializationManager.disableAll(this)
        FCMTokenManager.clearToken(this)
        saveUserConsentPreference(ConsentStatus.DENIED)
    }
    
    private fun enableOnlyFCM() {
        Log.d(TAG, "📱 Enabling FCM without Analytics")
        FCMInitializationManager.enableAutoInit()
        FCMInitializationManager.disableAnalytics(this)
        initializeFCMFeatures()
    }
    
    /**
     * Public methods for user settings screen
     */
    fun toggleNotifications(enable: Boolean) {
        if (enable) {
            enableFCMForUser()
            saveUserConsentPreference(ConsentStatus.GRANTED)
        } else {
            disableFCMForUser()
        }
    }

    /**
     * Force enable FCM for testing purposes
     * Call this method to quickly enable FCM without going through consent flow
     */
    private fun forceEnableFCMForTesting() {
        Log.d(TAG, "🧪 TESTING MODE: Force enabling FCM")
        FCMInitializationManager.enableAll(this)
        initializeFCMFeatures()
        Log.d(TAG, "🧪 FCM enabled for testing")
    }

    /**
     * Debug method to get current token status
     * Call this to see if token exists and what it is
     */
    private fun debugTokenStatus() {
        val isEnabled = FCMInitializationManager.isAutoInitEnabled()
        val hasToken = FCMTokenManager.getStoredToken(this) != null
        Log.d(TAG, "FCM autoInit=$isEnabled, hasStoredToken=$hasToken")
        if (isEnabled) retrieveFCMToken()
    }
    
    enum class ConsentStatus(val value: Int) {
        NOT_SET(0),
        GRANTED(1), 
        DENIED(2);
        
        companion object {
            fun fromValue(value: Int): ConsentStatus {
                return values().find { it.value == value } ?: NOT_SET
            }
        }
    }

    /**
     * Handle PhonePe payment result
     * This method is called after the payment flow completes
     */
    private fun handlePhonePePaymentResult(paymentResult: PhonePePaymentManager.PaymentResult) {
        when (paymentResult) {
            is PhonePePaymentManager.PaymentResult.Success -> {
                Log.d(TAG, "Payment successful")
                navigateToRegistrationSuccess()
            }
            is PhonePePaymentManager.PaymentResult.Failed -> {
                Log.d(TAG, "Payment failed: ${paymentResult.message}")
                showPaymentFailedMessage(paymentResult.message)
            }
            is PhonePePaymentManager.PaymentResult.Pending -> {
                Log.d(TAG, "Payment pending: ${paymentResult.message}")
                showPaymentPendingMessage(paymentResult.message)
            }
            is PhonePePaymentManager.PaymentResult.Completed -> {
                Log.d(TAG, "Payment completed: ${paymentResult.message}")
                // Legacy support - this should now be handled by automatic verification
                navigateToRegistrationSuccess()
            }
            is PhonePePaymentManager.PaymentResult.Cancelled -> {
                Log.d(TAG, "Payment cancelled: ${paymentResult.message}")
                showPaymentCancelledMessage()
            }
            is PhonePePaymentManager.PaymentResult.Error -> {
                Log.e(TAG, "Payment error: ${paymentResult.message}")
                showPaymentErrorMessage(paymentResult.message)
            }
        }
    }
    
    /**
     * Navigate to registration success screen
     */
    private fun navigateToRegistrationSuccess() {
        // Since we can't directly navigate from MainActivity, we'll use a different approach
        // You might want to use a shared ViewModel or event bus for this
        Log.d(TAG, "Payment successful - user should be navigated to success screen")
        
        // For now, just log the success
        // The actual navigation should be handled in the payment screen or through a callback
    }
    
    /**
     * Initiate integrated payment flow (book ticket + PhonePe payment)
     * This is the recommended method that handles the complete flow
     */
    fun initiateIntegratedPayment(
        eventId: String,
        passType: String,
        friends: List<com.talkeys.shared.data.payment.Friend>,
        authToken: String? = null,
        onResult: (PhonePePaymentManager.PaymentResult) -> Unit
    ) {
        Log.d(TAG, "Initiating integrated payment for event: $eventId")
        
        PhonePePaymentManager.bookTicketAndPay(
            activity = this,
            eventId = eventId,
            passType = passType,
            friends = friends,
            activityResultLauncher = phonePePaymentLauncher,
            authToken = authToken,
            onResult = onResult
        )
    }
    
    /**
     * Legacy method: Initiate PhonePe payment with pre-obtained token
     * Use initiateIntegratedPayment() for the complete flow
     * 
     * @param token Payment token from your backend (Create Order API response)
     * @param orderId Order ID from your backend (Create Order API response)
     */
    fun initiatePhonePePayment(token: String, orderId: String) {
        Log.d(TAG, "Initiating PhonePe payment")
        
        PhonePePaymentManager.startCheckout(
            activity = this,
            token = token,
            orderId = orderId,
            activityResultLauncher = phonePePaymentLauncher
        )
    }
    
    /**
     * Check order status after payment completion
     * This should call your backend's Order Status API
     */
    private fun checkOrderStatus() {
        Log.d(TAG, "Checking order status via backend API")
        
        // Get the current merchant order ID (you'll need to store this during payment)
        val merchantOrderId = getCurrentMerchantOrderId()
        
        if (merchantOrderId != null) {
            // Use PhonePePaymentManager to verify payment status
            PhonePePaymentManager.verifyPaymentStatusOnServer(
                merchantOrderId = merchantOrderId,
                authToken = getCurrentAuthToken()
            ) { result ->
                when (result) {
                    is PhonePePaymentManager.PaymentResult.Success -> {
                        Log.d(TAG, "✅ Payment verified as successful: ${result.message}")
                        handlePaymentSuccess(result.passId, result.passUUID)
                    }
                    is PhonePePaymentManager.PaymentResult.Failed -> {
                        Log.d(TAG, "❌ Payment verified as failed: ${result.message}")
                        handlePaymentFailure(result.message)
                    }
                    is PhonePePaymentManager.PaymentResult.Pending -> {
                        Log.d(TAG, "⏳ Payment still pending: ${result.message}")
                        handlePaymentPending(result.message)
                    }
                    is PhonePePaymentManager.PaymentResult.Error -> {
                        Log.e(TAG, "🚨 Error checking payment status: ${result.message}")
                        handlePaymentError(result.message)
                    }
                    else -> {
                        Log.w(TAG, "⚠️ Unknown payment result: $result")
                        handlePaymentError("Unknown payment status")
                    }
                }
            }
        } else {
            Log.e(TAG, "No merchant order ID found for status check")
            handlePaymentError("No order ID available for verification")
        }
    }
    
    /**
     * Get current merchant order ID (implement based on how you store it)
     */
    private fun getCurrentMerchantOrderId(): String? {
        // TODO: Implement based on how you store the merchant order ID
        // This could be from SharedPreferences, ViewModel, or passed as parameter
        
        // Example implementation:
        // return getSharedPreferences("payment", Context.MODE_PRIVATE)
        //     .getString("current_merchant_order_id", null)
        
        // For testing, you can use your recent order ID:
        return "TKT_1760870435366_hz7wihgz7" // Replace with actual implementation
    }
    
    /**
     * Get current auth token (implement based on your auth system)
     */
    private fun getCurrentAuthToken(): String? {
        // TODO: Implement based on your authentication system
        // This could be from TokenManager, SharedPreferences, etc.
        
        // Example implementation:
        // return TokenManager.getCurrentToken()
        
        return null // Replace with actual token retrieval
    }
    
    /**
     * Handle successful payment verification
     */
    private fun handlePaymentSuccess(passId: String, passUUID: String?) {
        Log.d(TAG, "Payment success confirmed")

        // Navigate to success screen or show success message
        // Example: navigateToRegistrationSuccess(passId, passUUID)

        clearCurrentMerchantOrderId()
    }
    
    /**
     * Handle failed payment verification
     */
    private fun handlePaymentFailure(message: String) {
        Log.d(TAG, "Payment failure confirmed: $message")
        
        // Show failure message to user
        // Example: showPaymentFailedDialog(message)
        
        // Clear stored order ID
        clearCurrentMerchantOrderId()
    }
    
    /**
     * Handle pending payment verification
     */
    private fun handlePaymentPending(message: String) {
        Log.d(TAG, "Payment still pending: $message")
        
        // Show pending message and maybe retry after delay
        // Example: showPaymentPendingDialog(message)
    }
    
    /**
     * Handle payment verification error
     */
    private fun handlePaymentError(message: String) {
        Log.e(TAG, "Payment verification error: $message")
        
        // Show error message to user
        // Example: showPaymentErrorDialog(message)
        
        // Clear stored order ID
        clearCurrentMerchantOrderId()
    }
    
    /**
     * Clear stored merchant order ID
     */
    private fun clearCurrentMerchantOrderId() {
        // TODO: Implement based on how you store the merchant order ID
        // Example:
        // getSharedPreferences("payment", Context.MODE_PRIVATE)
        //     .edit()
        //     .remove("current_merchant_order_id")
        //     .apply()
    }
    
    /**
     * Show payment cancelled message to user
     */
    private fun showPaymentCancelledMessage() {
        Log.d(TAG, "TODO: Show payment cancelled UI to user")
        // TODO: Implement UI to show payment was cancelled
    }
    
    /**
     * Show payment failed message to user
     */
    private fun showPaymentFailedMessage(errorMessage: String) {
        Log.e(TAG, "TODO: Show payment failed UI to user: $errorMessage")
        // TODO: Implement UI to show payment failed
    }
    
    /**
     * Show payment pending message to user
     */
    private fun showPaymentPendingMessage(message: String) {
        Log.d(TAG, "TODO: Show payment pending UI to user: $message")
        // TODO: Implement UI to show payment is pending
    }
    
    /**
     * Show payment error message to user
     */
    private fun showPaymentErrorMessage(errorMessage: String) {
        Log.e(TAG, "TODO: Show payment error UI to user: $errorMessage")
        // TODO: Implement UI to show payment error
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
