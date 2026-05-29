package com.example.talkeys_new.screens.authentication.signupScreen

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.talkeys_new.R
import com.example.talkeys_new.screens.authentication.GoogleAuthClient
import com.example.talkeys_new.screens.authentication.GoogleSignInConfig
import com.example.talkeys_new.screens.authentication.TokenManager
import com.talkeys.shared.auth.AuthRepository
import com.talkeys.shared.network.ApiResult
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun SignUpScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val tokenManager = remember { TokenManager(context) }
    val authRepository: AuthRepository = koinInject()

    var isCheckingToken by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val savedToken = tokenManager.token.first()
        if (!savedToken.isNullOrEmpty()) {
            navController.navigate("home") {
                popUpTo("signup") { inclusive = true }
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
        GoogleAuthClient(context = context)
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
                                popUpTo("signup") { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                        is ApiResult.Failure -> {
                            Toast.makeText(context, "Signup failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "Google sign-in failed", Toast.LENGTH_SHORT).show()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Create an\naccount",
                color = Color.White,
                fontSize = 28.sp,
                fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                fontWeight = FontWeight.Bold,
                lineHeight = 34.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            CustomOutlinedTextField("Username or Email", R.drawable.user)
            Spacer(modifier = Modifier.height(16.dp))
            CustomOutlinedTextField("Password", R.drawable.lock, R.drawable.eye, true)
            Spacer(modifier = Modifier.height(20.dp))
            CustomOutlinedTextField("Confirm Password", R.drawable.lock, R.drawable.eye, true)

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = buildAnnotatedString {
                    append("By clicking the ")
                    withStyle(style = SpanStyle(color = Color(0xFFFF0044))) { append("Register") }
                    append(" button, you agree to the public offer")
                },
                style = TextStyle(
                    fontSize = 12.sp,
                    fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                    color = Color(0xFFAAAAAA)
                )
            )

            Spacer(modifier = Modifier.height(30.dp))
            Button(
                onClick = { navController.navigate("home") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB768FF))
            ) {
                Text("Create Account", modifier = Modifier.padding(vertical = 8.dp), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(30.dp))
            Text("- OR CONTINUE WITH -", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = Color.White, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clickable {
                            val signInIntent = googleAuthClient.getSignInIntentOrNull()
                            if (signInIntent == null) {
                                Toast.makeText(context, GoogleSignInConfig.missingConfigMessage(), Toast.LENGTH_LONG).show()
                                return@clickable
                            }

                            launcher.launch(signInIntent)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.google_icon),
                        contentDescription = "Google Sign-In",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.width(24.dp))
                SocialIconButton(R.drawable.apple_icon)
                Spacer(modifier = Modifier.width(24.dp))
                SocialIconButton(R.drawable.ic_facebook_icon)
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("I Already Have an Account", color = Color.White, fontFamily = FontFamily(Font(R.font.urbanist_regular)))
                TextButton(onClick = { navController.navigate("login") }) {
                    Text("Login", fontFamily = FontFamily(Font(R.font.urbanist_regular)), fontWeight = FontWeight.SemiBold, color = Color(0xFFF83758))
                }
            }
        }
    }
}

@Composable
fun SocialIconButton(icon: Int) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clickable { /* Handle social login */ },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}


@Composable
fun CustomOutlinedTextField(
    placeholderText: String,
    leadingIcon: Int,
    trailingIcon: Int? = null,
    isPassword: Boolean = false,
) {
    val text = remember { mutableStateOf("") }
    val passwordVisible = remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(317.dp)
                .height(55.dp)
                .border(
                    width = 1.dp,
                    color = Color(0xFF9C27B0),
                    shape = RoundedCornerShape(size = 10.dp)
                )
                .background(color = Color(0xFF111111), shape = RoundedCornerShape(size = 10.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Leading Icon
                Image(
                    painter = painterResource(id = leadingIcon),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Text Field
                val transformation = if (isPassword && !passwordVisible.value)
                    PasswordVisualTransformation()
                else
                    VisualTransformation.None

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    BasicTextField(
                        value = text.value,
                        onValueChange = { text.value = it },
                        singleLine = true,
                        textStyle = TextStyle(
                            fontSize = 14.sp,
                            fontFamily = FontFamily(Font(R.font.open_sans)),
                            fontWeight = FontWeight(400),
                            color = Color(0xFF676767),
                            textAlign = TextAlign.Center
                        ),
                        visualTransformation = transformation,
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (text.value.isEmpty()) {
                                    Text(
                                        text = placeholderText,
                                        style = TextStyle(
                                            fontSize = 14.sp,
                                            fontFamily = FontFamily(Font(R.font.open_sans)),
                                            fontWeight = FontWeight.Normal,
                                            color = Color(0xFFAAAAAA),
                                        )
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }

                // Trailing Icon
                if (trailingIcon != null) {
                    Image(
                        painter = painterResource(id = trailingIcon),
                        contentDescription = if (isPassword) "Toggle password visibility" else null,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable {
                                if (isPassword) {
                                    passwordVisible.value = !passwordVisible.value
                                }
                            }
                    )
                }
            }
        }
    }
}
