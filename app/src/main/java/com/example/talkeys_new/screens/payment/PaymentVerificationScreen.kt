package com.example.talkeys_new.screens.payment

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.talkeys_new.R
import com.example.talkeys_new.screens.authentication.TokenManager
import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.HttpException

/**
 * Payment Verification Screen
 * Checks payment status and shows result to user
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentVerificationScreen(
    merchantOrderId: String,
    passId: String,
    navController: NavController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var verificationState by remember { mutableStateOf<VerificationState>(VerificationState.Checking) }
    var retryCount by remember { mutableStateOf(0) }
    val maxRetries = 3
    
    // Auto-verify on screen load
    LaunchedEffect(merchantOrderId) {
        verifyPayment(
            context = context,
            merchantOrderId = merchantOrderId,
            passId = passId,
            onResult = { state -> verificationState = state }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment Verification") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF8A44CB),
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Background image
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
                when (val state = verificationState) {
                    is VerificationState.Checking -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(64.dp),
                            color = Color(0xFF8A44CB)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Verifying Payment...",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Please wait while we confirm your payment",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                    }
                    
                    is VerificationState.Success -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF171717)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "✅",
                                    fontSize = 64.sp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Payment Successful!",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Green
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Your ticket has been confirmed",
                                    fontSize = 16.sp,
                                    color = Color.White.copy(alpha = 0.8f),
                                    textAlign = TextAlign.Center
                                )
                                
                                state.passUUID?.let { uuid ->
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Pass ID: ${state.passId}",
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(24.dp))
                                Button(
                                    onClick = {
                                        navController.navigate("registration_success") {
                                            popUpTo(0) { inclusive = false }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF8A44CB)
                                    )
                                ) {
                                    Text("View Ticket")
                                }
                            }
                        }
                    }
                    
                    is VerificationState.Failed -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF171717)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "❌",
                                    fontSize = 64.sp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Payment Failed",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Red
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = state.message,
                                    fontSize = 16.sp,
                                    color = Color.White.copy(alpha = 0.8f),
                                    textAlign = TextAlign.Center
                                )
                                
                                Spacer(modifier = Modifier.height(24.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { navController.popBackStack() },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Go Back", color = Color.White)
                                    }
                                    Button(
                                        onClick = {
                                            if (retryCount < maxRetries) {
                                                retryCount++
                                                verificationState = VerificationState.Checking
                                                scope.launch {
                                                    delay(1000)
                                                    verifyPayment(
                                                        context = context,
                                                        merchantOrderId = merchantOrderId,
                                                        passId = passId,
                                                        onResult = { state -> verificationState = state }
                                                    )
                                                }
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        enabled = retryCount < maxRetries,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF8A44CB)
                                        )
                                    ) {
                                        Text("Retry ($retryCount/$maxRetries)")
                                    }
                                }
                            }
                        }
                    }
                    
                    is VerificationState.Pending -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF171717)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "⏳",
                                    fontSize = 64.sp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Payment Pending",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Yellow
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Your payment is being processed. This may take a few minutes.",
                                    fontSize = 16.sp,
                                    color = Color.White.copy(alpha = 0.8f),
                                    textAlign = TextAlign.Center
                                )
                                
                                Spacer(modifier = Modifier.height(24.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { navController.popBackStack() },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Go Back", color = Color.White)
                                    }
                                    Button(
                                        onClick = {
                                            verificationState = VerificationState.Checking
                                            scope.launch {
                                                delay(2000)
                                                verifyPayment(
                                                    context = context,
                                                    merchantOrderId = merchantOrderId,
                                                    passId = passId,
                                                    onResult = { state -> verificationState = state }
                                                )
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF8A44CB)
                                        )
                                    ) {
                                        Text("Check Again")
                                    }
                                }
                            }
                        }
                    }
                    
                    is VerificationState.Error -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF171717)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "⚠️",
                                    fontSize = 64.sp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Verification Error",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF9800)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = state.message,
                                    fontSize = 16.sp,
                                    color = Color.White.copy(alpha = 0.8f),
                                    textAlign = TextAlign.Center
                                )
                                
                                Spacer(modifier = Modifier.height(24.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { navController.popBackStack() },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Go Back", color = Color.White)
                                    }
                                    Button(
                                        onClick = {
                                            verificationState = VerificationState.Checking
                                            scope.launch {
                                                delay(1000)
                                                verifyPayment(
                                                    context = context,
                                                    merchantOrderId = merchantOrderId,
                                                    passId = passId,
                                                    onResult = { state -> verificationState = state }
                                                )
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF8A44CB)
                                        )
                                    ) {
                                        Text("Try Again")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Verification states
 */
sealed class VerificationState {
    object Checking : VerificationState()
    data class Success(val passId: String, val passUUID: String?) : VerificationState()
    data class Failed(val message: String) : VerificationState()
    object Pending : VerificationState()
    data class Error(val message: String) : VerificationState()
}

/**
 * Verify payment status with backend
 */
private suspend fun verifyPayment(
    context: android.content.Context,
    merchantOrderId: String,
    passId: String,
    onResult: (VerificationState) -> Unit
) {
    try {
        Log.d("PaymentVerification", "Verifying payment status")
        
        // Get auth token
        val tokenManager = TokenManager(context)
        val tokenResult = tokenManager.getToken()
        
        val authToken = when (tokenResult) {
            is com.example.talkeys_new.utils.Result.Success -> tokenResult.data
            else -> null
        }
        
        // Call backend API to check status
        val paymentRepository = org.koin.core.context.GlobalContext.get().get<com.talkeys.shared.data.payment.PaymentRepository>()
        val result = paymentRepository.verifyPaymentStatus(merchantOrderId, authToken)
        
        result.fold(
            onSuccess = { statusData ->
                Log.d("PaymentVerification", "Status: ${statusData.paymentStatus}")
                when (statusData.paymentStatus.uppercase()) {
                    "COMPLETED" -> {
                        onResult(VerificationState.Success(statusData.passId, statusData.passUUID))
                    }
                    "PENDING" -> {
                        onResult(VerificationState.Pending)
                    }
                    "FAILED" -> {
                        onResult(VerificationState.Failed("Payment was not successful"))
                    }
                    else -> {
                        onResult(VerificationState.Error("Unknown payment status: ${statusData.paymentStatus}"))
                    }
                }
            },
            onFailure = { exception ->
                Log.e("PaymentVerification", "Error: ${exception.message}")
                onResult(VerificationState.Error(exception.message ?: "Failed to verify payment"))
            }
        )
    } catch (e: Exception) {
        Log.e("PaymentVerification", "Exception: ${e.message}")
        onResult(VerificationState.Error(e.message ?: "An error occurred"))
    }
}
