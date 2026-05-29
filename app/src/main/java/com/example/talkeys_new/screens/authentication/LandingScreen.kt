package com.example.talkeys_new.screens.authentication

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.talkeys_new.R
import com.talkeys.shared.auth.AuthRepository
import com.talkeys.shared.network.ApiResult
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import org.koin.compose.koinInject

@Composable
fun LandingPage(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val tokenManager = remember { TokenManager(context) }
    val authRepository: AuthRepository = koinInject()

    var isCheckingToken by remember { mutableStateOf(true) }
    var isGoogleSignInLoading by remember { mutableStateOf(false) }

    // ✅ Token auto-login logic with flicker prevention
    LaunchedEffect(Unit) {
        val token = tokenManager.token.first()
        if (!token.isNullOrEmpty()) {
            navController.navigate("home") {
                popUpTo("landing") { inclusive = true }
                launchSingleTop = true
            }
        } else {
            isCheckingToken = false
        }
    }

    if (isCheckingToken) {
        // Optional: loading spinner or just blank while checking token
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color.White)
        }
        return
    }

    val googleAuthClient = remember {
        GoogleAuthClient(
            context = context,
            clientId = "563385258779-75kq583ov98fk7h3dqp5em0639769a61.apps.googleusercontent.com"
        )
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val token = googleAuthClient.getIdTokenFromIntent(result.data)

        if (token != null) {
            coroutineScope.launch {
                try {
                    when (val authResult = authRepository.verifyGoogleToken(token)) {
                        is ApiResult.Success -> {
                            Toast.makeText(context, "Welcome ${authResult.data.name}", Toast.LENGTH_SHORT).show()
                            navController.navigate("home") {
                                popUpTo("landing") { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                        is ApiResult.Failure -> {
                            Toast.makeText(context, "Login failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    isGoogleSignInLoading = false
                }
            }
        } else {
            Toast.makeText(context, "Google sign-in failed", Toast.LENGTH_SHORT).show()
            isGoogleSignInLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.splash_bg),
            contentDescription = "Splash Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Unlimited\nentertainment,\nall in one place",
                color = Color(0xFFFFFCFC),
                fontSize = 24.sp,
                fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Your Stage, Your Voice\nEvents Reimagined",
                color = Color.White,
                fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (!isGoogleSignInLoading) {
                        isGoogleSignInLoading = true
                        launcher.launch(googleAuthClient.getSignInIntent())
                        
                        // Timeout mechanism - reset loading state after 30 seconds if no response
                        coroutineScope.launch {
                            delay(30000) // 30 seconds timeout
                            if (isGoogleSignInLoading) {
                                isGoogleSignInLoading = false
                                Toast.makeText(context, "Google Sign-In timed out. Please try again.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                enabled = !isGoogleSignInLoading,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isGoogleSignInLoading) Color(0xFF9C4DCD) else Color(0xFFB768FF),
                    disabledContainerColor = Color(0xFF9C4DCD)
                )
            ) {
                if (isGoogleSignInLoading) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "SIGNING IN...",
                            modifier = Modifier.padding(vertical = 8.dp),
                            fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                } else {
                    Text(
                        text = "LOGIN WITH GOOGLE",
                        modifier = Modifier.padding(vertical = 8.dp),
                        fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
