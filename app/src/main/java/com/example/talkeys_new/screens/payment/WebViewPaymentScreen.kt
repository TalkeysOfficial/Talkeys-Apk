package com.example.talkeys_new.screens.payment

import android.net.Uri
import android.util.Log
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.example.talkeys_new.R

/**
 * Payment handoff screen.
 *
 * Despite the legacy file name, this screen now launches the hosted PhonePe
 * checkout in a Chrome Custom Tab instead of an embedded WebView. When the
 * user returns to the app, we continue to the existing payment-verification
 * screen and let shared KMP logic confirm the final status.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebViewPaymentScreen(
    paymentUrl: String,
    merchantOrderId: String,
    passId: String,
    navController: NavController
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var browserOpened by remember(paymentUrl) { mutableStateOf(false) }
    var appBackgrounded by remember { mutableStateOf(false) }
    var verificationLaunched by remember { mutableStateOf(false) }

    fun openPaymentBrowser() {
        val customTabsIntent = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .setUrlBarHidingEnabled(false)
            .setDefaultColorSchemeParams(
                CustomTabColorSchemeParams.Builder()
                    .setToolbarColor(android.graphics.Color.parseColor("#8A44CB"))
                    .build()
            )
            .build()

        browserOpened = true
        verificationLaunched = false
        Log.d("PhonePeCheckout", "Opening Chrome Custom Tab for payment")
        customTabsIntent.launchUrl(context, Uri.parse(paymentUrl))
    }

    fun navigateToVerification() {
        if (verificationLaunched) return
        verificationLaunched = true
        navController.navigate("payment_verification/$merchantOrderId/$passId") {
            popUpTo("webview_payment/$paymentUrl/$merchantOrderId/$passId") {
                inclusive = true
            }
        }
    }

    LaunchedEffect(paymentUrl) {
        openPaymentBrowser()
    }

    DisposableEffect(lifecycleOwner, browserOpened, appBackgrounded, verificationLaunched) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    if (browserOpened && !verificationLaunched) {
                        appBackgrounded = true
                    }
                }

                Lifecycle.Event.ON_RESUME -> {
                    if (browserOpened && appBackgrounded && !verificationLaunched) {
                        Log.d("PhonePeCheckout", "Returned from browser, starting verification")
                        navigateToVerification()
                    }
                }

                else -> Unit
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Complete Payment") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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
            Image(
                painter = painterResource(id = R.drawable.background),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF171717)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Secure Browser Checkout",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "We opened PhonePe in a secure browser tab for better compatibility. Complete the payment there, then return to the app.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.85f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "If the browser did not open or you closed it, you can reopen checkout or verify the payment manually.",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { openPaymentBrowser() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8A44CB))
                        ) {
                            Text("Open Payment Page Again", color = Color.White)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { navigateToVerification() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("I Completed Payment", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
