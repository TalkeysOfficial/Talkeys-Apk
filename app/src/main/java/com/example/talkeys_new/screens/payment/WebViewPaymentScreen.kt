package com.example.talkeys_new.screens.payment

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.http.SslError
import android.webkit.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * WebView Payment Screen
 * Opens PhonePe payment in a WebView using the same flow as the website
 */
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewPaymentScreen(
    paymentUrl: String,
    merchantOrderId: String,
    passId: String,
    navController: NavController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var isLoading by remember { mutableStateOf(true) }
    var loadProgress by remember { mutableStateOf(0) }
    var currentUrl by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isVerifyingPayment by remember { mutableStateOf(false) }
    
    // Auto-verify payment after timeout
    LaunchedEffect(merchantOrderId) {
        delay(60000) // Wait 60 seconds
        if (!isVerifyingPayment) {
            Log.d("WebViewPayment", "Auto-verifying payment after timeout")
            isVerifyingPayment = true
            // Navigate to verification screen
            navController.navigate("payment_verification/$merchantOrderId/$passId")
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Complete Payment")
                        if (isLoading) {
                            Text(
                                "Loading... $loadProgress%",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        // Show confirmation dialog before closing
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF8A44CB),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // WebView
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            setSupportZoom(true)
                            builtInZoomControls = false
                            loadWithOverviewMode = true
                            useWideViewPort = true
                            javaScriptCanOpenWindowsAutomatically = true
                            mediaPlaybackRequiresUserGesture = false
                            
                            // Allow mixed content for payment gateways
                            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        }
                        
                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                                currentUrl = url ?: ""
                                isLoading = true
                                Log.d("WebViewPayment", "Payment page started loading")
                            }
                            
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                isLoading = false
                                currentUrl = url ?: ""
                                Log.d("WebViewPayment", "Payment page finished loading")
                                
                                // Check for session expired error
                                view?.evaluateJavascript(
                                    "(function() { return document.body.innerText; })();"
                                ) { html ->
                                    val bodyText = html?.replace("\\u003C", "<")?.replace("\\u003E", ">") ?: ""
                                    if (bodyText.contains("session expired", ignoreCase = true) ||
                                        bodyText.contains("Session expired", ignoreCase = false)) {
                                        Log.w("WebViewPayment", "Session expired detected in page content")
                                        showError = true
                                        errorMessage = "Payment session expired. The payment link is only valid for a short time. Please go back and try again."
                                    }
                                }
                                
                                // Check if we're on a callback URL
                                url?.let { checkCallbackUrl(it, navController, merchantOrderId, passId) }
                            }
                            
                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): Boolean {
                                val url = request?.url?.toString() ?: return false
                                Log.d("WebViewPayment", "Payment navigation received")
                                
                                // Check for callback URLs first
                                if (checkCallbackUrl(url, navController, merchantOrderId, passId)) {
                                    Log.d("WebViewPayment", "✅ Callback URL detected, navigating...")
                                    return true
                                }
                                
                                // Check for merchant redirect (when session expires)
                                if (url.contains("api.talkeys.xyz") || url.contains("talkeys")) {
                                    Log.d("WebViewPayment", "Merchant redirect detected")
                                    // This is likely a redirect back to merchant after session expiry
                                    // Navigate to verification screen to check payment status
                                    navController.navigate("payment_verification/$merchantOrderId/$passId")
                                    return true
                                }
                                
                                // Allow PhonePe and payment URLs
                                return when {
                                    url.contains("phonepe.com") -> false
                                    url.contains("mercury") -> false
                                    url.startsWith("upi://") -> {
                                        // Handle UPI intent
                                        try {
                                            val intent = android.content.Intent(
                                                android.content.Intent.ACTION_VIEW,
                                                android.net.Uri.parse(url)
                                            )
                                            context.startActivity(intent)
                                            true
                                        } catch (e: Exception) {
                                            Log.e("WebViewPayment", "Failed to open UPI: ${e.message}")
                                            false
                                        }
                                    }
                                    else -> false
                                }
                            }
                            
                            override fun onReceivedError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                error: WebResourceError?
                            ) {
                                super.onReceivedError(view, request, error)
                                if (request?.isForMainFrame == true) {
                                    showError = true
                                    errorMessage = error?.description?.toString() ?: "Failed to load payment page"
                                    Log.e("WebViewPayment", "Error loading page: $errorMessage")
                                }
                            }
                            
                            override fun onReceivedSslError(
                                view: WebView?,
                                handler: SslErrorHandler?,
                                error: SslError?
                            ) {
                                // For production, you should NOT proceed on SSL errors
                                // This is only for testing with self-signed certificates
                                Log.w("WebViewPayment", "SSL Error: ${error?.toString()}")
                                handler?.cancel() // Reject SSL errors in production
                            }
                        }
                        
                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                super.onProgressChanged(view, newProgress)
                                loadProgress = newProgress
                                if (newProgress == 100) {
                                    isLoading = false
                                }
                            }
                            
                            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean = true
                        }
                        
                        // Load the payment URL
                        Log.d("WebViewPayment", "Loading payment page")
                        loadUrl(paymentUrl)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            
            // Loading indicator
            if (isLoading && loadProgress < 100) {
                LinearProgressIndicator(
                    progress = loadProgress / 100f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                    color = Color(0xFF8A44CB)
                )
            }
            
            // Error message
            if (showError) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.BottomCenter),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Red.copy(alpha = 0.9f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Error Loading Payment",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showError = false },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White
                                )
                            ) {
                                Text("Dismiss", color = Color.Red)
                            }
                            Button(
                                onClick = {
                                    // Navigate to verification
                                    navController.navigate("payment_verification/$merchantOrderId/$passId")
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF8A44CB)
                                )
                            ) {
                                Text("Verify Payment", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Check if URL is a callback URL and handle navigation
 */
private fun checkCallbackUrl(
    url: String,
    navController: NavController,
    merchantOrderId: String,
    passId: String
): Boolean {
    Log.d("WebViewPayment", "Checking payment callback")
    
    return when {
        // Success callback
        url.contains("/ticket/success") || url.contains("payment/success") -> {
            Log.d("WebViewPayment", "Success callback detected")
            // Extract passUUID if present
            val passUUID = extractQueryParam(url, "uuid") ?: extractQueryParam(url, "passUUID")
            navController.navigate("registration_success") {
                popUpTo(0) { inclusive = false }
            }
            true
        }
        
        // Failure callback
        url.contains("/ticket/failure") || url.contains("payment/failure") -> {
            Log.d("WebViewPayment", "Failure callback detected")
            navController.navigate("payment_verification/$merchantOrderId/$passId")
            true
        }
        
        // Pending callback
        url.contains("/ticket/pending") || url.contains("payment/pending") -> {
            Log.d("WebViewPayment", "Pending callback detected")
            navController.navigate("payment_verification/$merchantOrderId/$passId")
            true
        }
        
        // Error callback
        url.contains("/ticket/error") || url.contains("payment/error") -> {
            Log.d("WebViewPayment", "Error callback detected")
            Log.e("WebViewPayment", "Payment error callback detected")
            navController.popBackStack()
            true
        }
        
        else -> false
    }
}

/**
 * Extract query parameter from URL
 */
private fun extractQueryParam(url: String, param: String): String? {
    return try {
        val uri = android.net.Uri.parse(url)
        uri.getQueryParameter(param)
    } catch (e: Exception) {
        null
    }
}
