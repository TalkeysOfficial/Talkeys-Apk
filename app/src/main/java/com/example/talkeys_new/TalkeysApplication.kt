package com.example.talkeys_new

import android.app.Application
import com.phonepe.intent.sdk.api.PhonePeKt
import com.phonepe.intent.sdk.api.models.PhonePeEnvironment
import com.example.talkeys_new.utils.PhonePeConfig
import com.talkeys.shared.initKoin
import org.koin.android.ext.koin.androidContext

class TalkeysApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize KMP shared module — must happen in Application, not Activity,
        // to avoid KoinApplicationAlreadyStartedException on Activity recreation.
        initKoin {
            androidContext(this@TalkeysApplication)
        }

        // Initialize PhonePe SDK
        initializePhonePeSDK()
    }
    
    private fun initializePhonePeSDK() {
        try {
            val environment = if (PhonePeConfig.IS_PRODUCTION) {
                PhonePeEnvironment.RELEASE
            } else {
                PhonePeEnvironment.SANDBOX
            }
            
            android.util.Log.d("PhonePe", "Initializing PhonePe SDK (${PhonePeConfig.getEnvironmentName()})")
            
            val result = PhonePeKt.init(
                context = this,
                merchantId = PhonePeConfig.MERCHANT_ID,
                flowId = PhonePeConfig.generateFlowId(),
                phonePeEnvironment = environment,
                enableLogging = !PhonePeConfig.IS_PRODUCTION, // Enable logging in debug
                appId = null // Optional - can be your app's package name
            )
            
            if (result) {
                android.util.Log.d("PhonePe", "PhonePe SDK initialized successfully")
                
                // Verify SDK state
                verifySDKState()
            } else {
                android.util.Log.e("PhonePe", "PhonePe SDK initialization failed")
            }
        } catch (e: Exception) {
            android.util.Log.e("PhonePe", "Exception during SDK initialization: ${e.message}", e)
        }
    }
    
    /**
     * Verify SDK state after initialization
     */
    private fun verifySDKState() {
        try {
            android.util.Log.d("PhonePe", "🔍 Verifying SDK state...")
            
            // Check if PhonePe app is installed
            val packageManager = packageManager
            try {
                packageManager.getPackageInfo("com.phonepe.app", 0)
                android.util.Log.d("PhonePe", "📱 PhonePe app is installed - will use native app")
            } catch (e: Exception) {
                android.util.Log.d("PhonePe", "🌐 PhonePe app not installed - will use WebView fallback")
            }
            
            android.util.Log.d("PhonePe", "✅ SDK verification complete")
            
        } catch (e: Exception) {
            android.util.Log.w("PhonePe", "⚠️ SDK verification failed: ${e.message}")
        }
    }
    
}
