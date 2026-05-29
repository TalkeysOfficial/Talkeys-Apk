package com.example.talkeys_new.screens.authentication.loginScreen

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.talkeys_new.R
import com.example.talkeys_new.screens.authentication.GoogleAuthClient
import com.example.talkeys_new.screens.authentication.TokenManager
import com.example.talkeys_new.screens.authentication.GoogleSignInManager
import com.example.talkeys_new.screens.authentication.UserProfile
import com.example.talkeys_new.screens.authentication.signupScreen.CustomOutlinedTextField
import com.talkeys.shared.auth.AuthRepository
import com.talkeys.shared.network.ApiResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import android.util.Log
import org.koin.compose.koinInject

@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val tokenManager = remember { TokenManager(context) }
    val authRepository: AuthRepository = koinInject()
    val urbanistFont = FontFamily(Font(R.font.urbanist_regular))

    var isCheckingToken by remember { mutableStateOf(true) }
    var isGoogleSignInLoading by remember { mutableStateOf(false) }

    // ✅ Check for existing token
    LaunchedEffect(Unit) {
        val savedToken = tokenManager.token.first()
        if (!savedToken.isNullOrEmpty()) {
            navController.navigate("home") {
                popUpTo("login") { inclusive = true }
                launchSingleTop = true
            }
        } else {
            isCheckingToken = false
        }
    }

    if (isCheckingToken) {
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
    
    val googleSignInManager = remember { GoogleSignInManager(context) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        Log.d("LoginScreen", "Google Sign-In result received")
        val token = googleAuthClient.getIdTokenFromIntent(result.data)
        Log.d("LoginScreen", "ID token present=${token != null}")

        if (token != null) {
            coroutineScope.launch {
                try {
                    // Extract Google user information from the sign-in result
                    val googleAccount = try {
                        GoogleSignIn.getSignedInAccountFromIntent(result.data)
                            .getResult(ApiException::class.java)
                    } catch (e: ApiException) {
                        Log.e("LoginScreen", "Failed to get Google account: ${e.message}")
                        null
                    }
                    
                    // Save Google user profile data
                    googleAccount?.let { account ->
                        val userProfile = UserProfile(
                            id = account.id ?: "",
                            name = account.displayName ?: "",
                            email = account.email ?: "",
                            profileImageUrl = account.photoUrl?.toString(),
                            givenName = account.givenName ?: "",
                            familyName = account.familyName ?: ""
                        )
                        googleSignInManager.saveUserProfile(userProfile)
                        Log.d("LoginScreen", "Google profile saved locally")
                    } ?: run {
                        Log.e("LoginScreen", "Google account is null")
                    }

                    // Verify token with backend through the shared KMP auth repository.
                    Log.d("LoginScreen", "Verifying token with backend")
                    when (val authResult = authRepository.verifyGoogleToken(token)) {
                        is ApiResult.Success -> {
                            Log.d("LoginScreen", "Backend verification ok")
                            Toast.makeText(context, "Welcome ${authResult.data.name}", Toast.LENGTH_SHORT).show()
                            navController.navigate("home") {
                                popUpTo("login") { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                        is ApiResult.Failure -> {
                            Log.e("LoginScreen", "Backend verification failed: ${authResult.error}")
                            Toast.makeText(context, "Login failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("LoginScreen", "Exception during login: ${e.message}", e)
                    Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Log.e("LoginScreen", "Google sign-in failed - no token received")
            Toast.makeText(context, "Google sign-in failed", Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF111111))
    ) {
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = "Background texture",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.weight(0.2f))

            Text(
                text = "Welcome\nBack!",
                color = Color.White,
                fontSize = 32.sp,
                fontFamily = FontFamily(Font(R.font.domine)),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            CustomOutlinedTextField(
                placeholderText = "Username or Email",
                leadingIcon = R.drawable.user,
                isPassword = false
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column {
                CustomOutlinedTextField(
                    placeholderText = "Password",
                    leadingIcon = R.drawable.lock,
                    trailingIcon = R.drawable.eye,
                    isPassword = true
                )

                Text(
                    text = "Forgot Password?",
                    color = Color(0xFFFF0033),
                    fontSize = 12.sp,
                    fontFamily = urbanistFont,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, end = 8.dp),
                    textAlign = TextAlign.End
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { navController.navigate("home") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF9C27B0)
                )
            ) {
                Text(
                    text = "Login",
                    fontSize = 16.sp,
                    fontFamily = urbanistFont,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.weight(0.8f))
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    launcher.launch(googleAuthClient.getSignInIntent())
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.google_icon),
                        contentDescription = "Google Sign-In",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Sign in with Google",
                        fontSize = 14.sp,
                        fontFamily = urbanistFont,
                        color = Color.Black
                    )
                }
            }
        }
    }
}
