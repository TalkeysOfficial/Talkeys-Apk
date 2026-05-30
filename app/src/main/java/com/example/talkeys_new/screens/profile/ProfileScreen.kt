package com.example.talkeys_new.screens.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import androidx.compose.ui.platform.LocalConfiguration
import coil.request.ImageRequest
import com.example.talkeys_new.R
import com.example.talkeys_new.screens.authentication.GoogleSignInManager
import com.example.talkeys_new.screens.authentication.UserProfile
import com.example.talkeys_new.avatar.AvatarImageWithFallback
import com.example.talkeys_new.avatar.ProfileAvatarSection
import com.example.talkeys_new.screens.dashboard.sharedProfileViewModel
import com.talkeys.shared.presentation.dashboard.ProfileUiState
import kotlinx.coroutines.launch
import android.util.Log
import com.example.talkeys_new.screens.authentication.TokenManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val googleSignInManager = remember { GoogleSignInManager(context) }
    val profileViewModel = sharedProfileViewModel()
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    // Get screen dimensions for responsive design
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    // More conservative responsive detection
    val isSmallScreen = screenWidth < 380.dp || screenHeight < 700.dp
    val isVerySmallScreen = screenWidth < 340.dp

    // User profile state with proper error handling
    val localGoogleProfile by googleSignInManager.userProfile.collectAsState(initial = UserProfile())
    val sharedProfileState by profileViewModel.uiState.collectAsState()
    val userProfile = remember(sharedProfileState, localGoogleProfile) {
        val content = sharedProfileState as? ProfileUiState.Content
        content?.profile?.let { profile ->
            val resolvedDisplayName = firstNonBlank(
                profile.displayName,
                profile.name,
                localGoogleProfile.givenName,
                localGoogleProfile.name
            )
            val resolvedEmail = firstNonBlank(profile.email, localGoogleProfile.email)
            UserProfile(
                id = firstNonBlank(profile.id, localGoogleProfile.id),
                name = resolvedDisplayName,
                email = resolvedEmail,
                profileImageUrl = profile.avatarUrl ?: localGoogleProfile.profileImageUrl,
                givenName = resolvedDisplayName,
                familyName = localGoogleProfile.familyName
            )
        } ?: localGoogleProfile
    }
    var mutualCommunities by remember { mutableStateOf(2) }
    val userBio = (sharedProfileState as? ProfileUiState.Content)
        ?.profile
        ?.about
        ?: "this is how your card will look like to others and this is your sample bio"
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // Check for signed-in Google account with comprehensive error handling
    LaunchedEffect(Unit) {
        try {
            isLoading = true
            error = null

            val lastSignedInAccount =
                com.google.android.gms.auth.api.signin.GoogleSignIn.getLastSignedInAccount(context)

            if (lastSignedInAccount != null) {
                // Validate account data before saving
                val accountId = lastSignedInAccount.id
                val displayName = lastSignedInAccount.displayName
                val email = lastSignedInAccount.email

                if (accountId != null && displayName != null && email != null) {
                    if (localGoogleProfile.name.isEmpty()) {
                        val profile = UserProfile(
                            id = accountId,
                            name = displayName,
                            email = email,
                            profileImageUrl = lastSignedInAccount.photoUrl?.toString(),
                            givenName = lastSignedInAccount.givenName ?: displayName.split(" ").firstOrNull() ?: "",
                            familyName = lastSignedInAccount.familyName ?: displayName.split(" ").drop(1).joinToString(" ")
                        )
                        googleSignInManager.saveUserProfile(profile)
                    }
                } else {
                    error = "Incomplete account information"
                }
            } else {
                error = "No signed-in account found"
            }
        } catch (e: Exception) {
            Log.e("ProfileScreen", "Error checking Google account", e)
            error = "Failed to load account information"
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(sharedProfileState, localGoogleProfile) {
        isLoading = sharedProfileState is ProfileUiState.Loading && localGoogleProfile.name.isEmpty()
        error = (sharedProfileState as? ProfileUiState.Error)
            ?.message
            ?.takeIf { localGoogleProfile.name.isEmpty() }
    }

    Scaffold(
        topBar = {
            // Custom top bar with properly centered title
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .height(64.dp)
                    .background(Color(0xFF262626))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Back button on the left
                IconButton(
                    onClick = {
                        try {
                            navController.popBackStack()
                        } catch (e: Exception) {
                            Log.e("ProfileScreen", "Navigation error", e)
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Centered title
                Text(
                    text = "Dashboard",
                    style = TextStyle(
                        fontSize = if (isSmallScreen) 18.sp else 20.sp,
                        fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Black)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(if (isVerySmallScreen) 8.dp else 12.dp))
            // Enhanced Profile Section with rounded corners (removed spacing above)
            ProfileSection(
                userProfile = userProfile,
                userBio = userBio,
                mutualCommunities = mutualCommunities,
                isLoading = isLoading,
                error = error,
                clipboardManager = clipboardManager,
                navController = navController,
                isSmallScreen = isSmallScreen,
                isVerySmallScreen = isVerySmallScreen
            )

            Spacer(modifier = Modifier.height(if (isVerySmallScreen) 24.dp else 32.dp))

            // Menu Items Section
            MenuItemsSection(
                navController = navController,
                isSmallScreen = isSmallScreen,
                isVerySmallScreen = isVerySmallScreen
            )

            Spacer(modifier = Modifier.height(if (isVerySmallScreen) 24.dp else 32.dp))

            // Logout Section
            LogoutSection(
                tokenManager = tokenManager,
                googleSignInManager = googleSignInManager,
                navController = navController,
                scope = scope,
                isSmallScreen = isSmallScreen,
                isVerySmallScreen = isVerySmallScreen
            )

            Spacer(modifier = Modifier.height(if (isVerySmallScreen) 24.dp else 40.dp))
        }
    }
}

@Composable
private fun ProfileSection(
    userProfile: UserProfile,
    userBio: String,
    mutualCommunities: Int,
    isLoading: Boolean,
    error: String?,
    clipboardManager: androidx.compose.ui.platform.ClipboardManager,
    navController: NavController,
    isSmallScreen: Boolean = false,
    isVerySmallScreen: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (isVerySmallScreen) 12.dp else if (isSmallScreen) 16.dp else 20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF171717)),
        shape = RoundedCornerShape(if (isSmallScreen) 12.dp else 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                LoadingProfileContent()
            } else if (error != null) {
                ErrorProfileContent(error = error)
            } else {
                ProfileContent(
                    userProfile = userProfile,
                    userBio = userBio,
                    mutualCommunities = mutualCommunities,
                    clipboardManager = clipboardManager,
                    navController = navController,
                    isSmallScreen = isSmallScreen,
                    isVerySmallScreen = isVerySmallScreen
                )
            }
        }
    }
}

@Composable
private fun LoadingProfileContent() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            color = Color(0xFF6A4C93),
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Loading profile...",
            style = TextStyle(
                fontSize = 16.sp,
                fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                color = Color.White
            )
        )
    }
}

@Composable
private fun ErrorProfileContent(error: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = "Error",
            tint = Color.Red,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = error,
            style = TextStyle(
                fontSize = 16.sp,
                fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                color = Color.Red,
                textAlign = TextAlign.Center
            )
        )
    }
}

@Composable
private fun ProfileContent(
    userProfile: UserProfile,
    userBio: String,
    mutualCommunities: Int,
    clipboardManager: androidx.compose.ui.platform.ClipboardManager,
    navController: NavController,
    isSmallScreen: Boolean = false,
    isVerySmallScreen: Boolean = false
) {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // Purple Strip at top - Responsive height for avatar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isVerySmallScreen) 45.dp else if (isSmallScreen) 48.dp else 53.dp)
                    .background(
                        color = Color(0xFF6A4C93), // Purple color
                        shape = RoundedCornerShape(
                            topStart = if (isSmallScreen) 12.dp else 16.dp,
                            topEnd = if (isSmallScreen) 12.dp else 16.dp
                        )
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(end = if (isVerySmallScreen) 10.dp else 15.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Friend request icon
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = "Friend Request",
                        tint = Color.White,
                        modifier = Modifier.size(if (isSmallScreen) 18.dp else 20.dp)
                    )

                    Spacer(modifier = Modifier.width(if (isVerySmallScreen) 6.dp else 8.dp))

                    // Three dots icon
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More Options",
                        tint = Color.White,
                        modifier = Modifier.size(if (isSmallScreen) 18.dp else 20.dp)
                    )
                }
            }

            // Rest of the profile content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color(0xFF171717),
                        shape = RoundedCornerShape(
                            bottomStart = if (isSmallScreen) 12.dp else 16.dp,
                            bottomEnd = if (isSmallScreen) 12.dp else 16.dp
                        )
                    )
                    .padding(if (isVerySmallScreen) 12.dp else if (isSmallScreen) 16.dp else 20.dp)
            ) {
                Column {
                    // Space for avatar overlap - increased to accommodate avatar
                    Spacer(modifier = Modifier.height(if (isVerySmallScreen) 50.dp else if (isSmallScreen) 55.dp else 60.dp))

                    // User info section - now properly below the avatar
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = if (isVerySmallScreen) 4.dp else 8.dp)
                    ) {
                        // User Name with Copy Icon
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = getUserDisplayName(userProfile),
                                style = TextStyle(
                                    fontSize = if (isVerySmallScreen) 20.sp else if (isSmallScreen) 22.sp else 24.sp,
                                    fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )

                            Spacer(modifier = Modifier.width(if (isVerySmallScreen) 6.dp else 8.dp))

                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Username",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(if (isSmallScreen) 14.dp else 16.dp)
                                    .clickable {
                                        val username = getUserDisplayName(userProfile)
                                        clipboardManager.setText(AnnotatedString(username))
                                    }
                            )
                        }

                        Spacer(modifier = Modifier.height(if (isVerySmallScreen) 6.dp else 8.dp))

                        // Email with Pronouns
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = getUserEmail(userProfile),
                                style = TextStyle(
                                    fontSize = if (isVerySmallScreen) 12.sp else 14.sp,
                                    fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                                    fontWeight = FontWeight.Normal,
                                    color = Color(0xFFA3A3A3),
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )

                            Spacer(modifier = Modifier.width(if (isVerySmallScreen) 6.dp else 8.dp))

                            Text(
                                text = "•",
                                style = TextStyle(
                                    fontSize = if (isVerySmallScreen) 12.sp else 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                )
                            )

                            Spacer(modifier = Modifier.width(if (isVerySmallScreen) 6.dp else 8.dp))

                            Text(
                                text = "he/him",
                                style = TextStyle(
                                    fontSize = if (isVerySmallScreen) 12.sp else 14.sp,
                                    fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                                    fontWeight = FontWeight.Normal,
                                    color = Color(0xFFA3A3A3),
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(if (isVerySmallScreen) 12.dp else 16.dp))

                    // Communities Information
                    CommunitiesInfo(
                        mutualCommunities = mutualCommunities,
                        isSmallScreen = isSmallScreen,
                        isVerySmallScreen = isVerySmallScreen
                    )

                    Spacer(modifier = Modifier.height(if (isVerySmallScreen) 12.dp else 16.dp))

                    // Bio Section with responsive sizing
                    BioSection(
                        userBio = userBio,
                        isSmallScreen = isSmallScreen,
                        isVerySmallScreen = isVerySmallScreen
                    )
                }
            }
        }

        // Profile Avatar (overlapping the purple strip) - Using Avatar System
        ProfileAvatarSection(
            userProfile = userProfile,
            navController = navController,
            modifier = Modifier.offset(
                x = if (isVerySmallScreen) 12.dp else if (isSmallScreen) 16.dp else 20.dp,
                y = if (isVerySmallScreen) 15.dp else if (isSmallScreen) 18.dp else 20.dp
            ),
            avatarUrl = userProfile.profileImageUrl
        )
    }
}

@Composable
private fun ProfileImage(userProfile: UserProfile) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .border(
                width = 3.dp,
                color = Color(0xFF6A4C93),
                shape = CircleShape
            )
            .clip(CircleShape)
            .background(Color(0xFFD9D9D9)),
        contentAlignment = Alignment.Center
    ) {
        if (!userProfile.profileImageUrl.isNullOrEmpty()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(userProfile.profileImageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Profile Picture",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Default Profile",
                tint = Color.Gray,
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

@Composable
private fun UserInformation(
    userProfile: UserProfile,
    clipboardManager: androidx.compose.ui.platform.ClipboardManager
) {
    Column {
        // Username with copy functionality
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = getUserDisplayName(userProfile),
                style = TextStyle(
                    fontSize = 24.sp,
                    fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    val username = getUserDisplayName(userProfile)
                    clipboardManager.setText(AnnotatedString(username))
                },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy Username",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Email and pronouns
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = getUserEmail(userProfile),
                style = TextStyle(
                    fontSize = 14.sp,
                    fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFFA3A3A3),
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = " • ",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            )

            Text(
                text = "he/him",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFFA3A3A3),
                )
            )
        }
    }
}

@Composable
private fun CommunitiesInfo(
    mutualCommunities: Int,
    isSmallScreen: Boolean = false,
    isVerySmallScreen: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Group,
            contentDescription = "Communities",
            tint = Color.White,
            modifier = Modifier.size(if (isSmallScreen) 18.dp else 20.dp)
        )

        Spacer(modifier = Modifier.width(if (isVerySmallScreen) 8.dp else 12.dp))

        Text(
            text = "$mutualCommunities mutual communities joined",
            style = TextStyle(
                fontSize = if (isVerySmallScreen) 14.sp else 16.sp,
                fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                fontWeight = FontWeight.Normal,
                color = Color.White,
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun BioSection(
    userBio: String,
    isSmallScreen: Boolean = false,
    isVerySmallScreen: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (isVerySmallScreen) 4.dp else 8.dp)
            .wrapContentHeight(),
        colors = CardDefaults.cardColors(containerColor = Color.Black),
        shape = RoundedCornerShape(if (isSmallScreen) 8.dp else 12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        SelectionContainer {
            Text(
                text = userBio,
                style = TextStyle(
                    fontSize = if (isVerySmallScreen) 12.sp else 14.sp,
                    fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFFA3A3A3),
                    lineHeight = if (isVerySmallScreen) 16.sp else 20.sp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(if (isVerySmallScreen) 12.dp else if (isSmallScreen) 16.dp else 20.dp)
            )
        }
    }
}

@Composable
private fun MenuItemsSection(
    navController: NavController,
    isSmallScreen: Boolean = false,
    isVerySmallScreen: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (isVerySmallScreen) 12.dp else if (isSmallScreen) 16.dp else 20.dp)
    ) {
        // Menu items with icons
        val menuItemsWithIcons = listOf(
            Triple(R.drawable.dialogbox_profile_icon, "Profile") { /* Navigate to profile edit */ },
            Triple(R.drawable.registered_events, "Registered events") {
                safeNavigate(navController, "registered_events")
            },
            Triple(R.drawable.ic_liked_events, "Liked events") {
                safeNavigate(navController, "liked_events")
            },
            Triple(R.drawable.hosted_events, "Hosted events") {
                safeNavigate(navController, "hosted_events")
            },
            Triple(R.drawable.organiser_dashboard_icon, "Organizer Dashboard") {
                safeNavigate(navController, "organizer_dashboard")
            },
            Triple(R.drawable.dropdown_hostevent_icon, "Host new event") {
                safeNavigate(navController, "create_event_1")
            },
            Triple(R.drawable.dialogbox_notification_icon, "Notifications") {
                /* Navigate to notifications */
            }
        )

        menuItemsWithIcons.forEach { (iconRes, title, onClick) ->
            EnhancedMenuItem(
                iconRes = iconRes,
                title = title,
                onClick = onClick,
                isSmallScreen = isSmallScreen,
                isVerySmallScreen = isVerySmallScreen
            )
            Spacer(modifier = Modifier.height(2.dp))
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Menu items without icons
        val menuItemsNoIcons = listOf(
            "About us" to { safeNavigate(navController, "about_us") },
            "Contact us" to { safeNavigate(navController, "contact_us") },
            "Privacy policy" to { safeNavigate(navController, "privacy_policy") },
            "Terms of Service" to { safeNavigate(navController, "tas") }
        )

        menuItemsNoIcons.forEach { (title, onClick) ->
            EnhancedMenuItemNoIcon(
                title = title,
                onClick = onClick,
                isSmallScreen = isSmallScreen,
                isVerySmallScreen = isVerySmallScreen
            )
            Spacer(modifier = Modifier.height(2.dp))
        }
    }
}

@Composable
private fun LogoutSection(
    tokenManager: TokenManager,
    googleSignInManager: GoogleSignInManager,
    navController: NavController,
    scope: kotlinx.coroutines.CoroutineScope,
    isSmallScreen: Boolean = false,
    isVerySmallScreen: Boolean = false
) {
    val context = LocalContext.current
    var isLoggingOut by remember { mutableStateOf(false) }

    Button(
        onClick = {
            if (!isLoggingOut) {
                isLoggingOut = true
                scope.launch {
                    try {
                        // Clear app tokens and user profile
                        tokenManager.clearToken()
                        googleSignInManager.clearUserProfile()

                        // Sign out from Google to force account picker on next login
                        val googleAuthClient = com.example.talkeys_new.screens.authentication.GoogleAuthClient(
                            context = context
                        )

                        // Revoke access to force account selection on next login
                        googleAuthClient.revokeAccess().addOnCompleteListener {
                            Log.d("ProfileScreen", "Google access revoked successfully")
                        }.addOnFailureListener { exception ->
                            Log.e("ProfileScreen", "Failed to revoke Google access", exception)
                            // Even if revoke fails, still sign out
                            googleAuthClient.signOut().addOnCompleteListener {
                                Log.d("ProfileScreen", "Google sign out completed")
                            }
                        }

                        navController.navigate("landingpage") {
                            popUpTo(0) { inclusive = true }
                        }
                    } catch (e: Exception) {
                        Log.e("ProfileScreen", "Logout error", e)
                    } finally {
                        isLoggingOut = false
                    }
                }
            }
        },
        enabled = !isLoggingOut,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (isVerySmallScreen) 12.dp else if (isSmallScreen) 16.dp else 20.dp)
            .height(if (isVerySmallScreen) 44.dp else 50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF4C4C4C),
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(if (isSmallScreen) 8.dp else 12.dp)
    ) {
        if (isLoggingOut) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(if (isSmallScreen) 18.dp else 20.dp)
            )
        } else {
            Text(
                text = "Logout",
                style = TextStyle(
                    fontSize = if (isVerySmallScreen) 16.sp else 18.sp,
                    fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            )
        }
    }
}

@Composable
private fun EnhancedMenuItem(
    iconRes: Int,
    title: String,
    onClick: () -> Unit,
    isSmallScreen: Boolean = false,
    isVerySmallScreen: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (isVerySmallScreen) 48.dp else 56.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF171717)),
        shape = RoundedCornerShape(if (isSmallScreen) 8.dp else 12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = if (isVerySmallScreen) 12.dp else 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                modifier = Modifier.size(if (isSmallScreen) 20.dp else 24.dp)
            )

            Spacer(modifier = Modifier.width(if (isVerySmallScreen) 12.dp else 16.dp))

            Text(
                text = title,
                style = TextStyle(
                    fontSize = if (isVerySmallScreen) 14.sp else 16.sp,
                    fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                    fontWeight = FontWeight.Normal,
                    color = Color.White,
                ),
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = Color.White,
                modifier = Modifier.size(if (isSmallScreen) 18.dp else 20.dp)
            )
        }
    }
}

@Composable
private fun EnhancedMenuItemNoIcon(
    title: String,
    onClick: () -> Unit,
    isSmallScreen: Boolean = false,
    isVerySmallScreen: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (isVerySmallScreen) 48.dp else 56.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF171717)),
        shape = RoundedCornerShape(if (isSmallScreen) 8.dp else 12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = if (isVerySmallScreen) 12.dp else 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = TextStyle(
                    fontSize = if (isVerySmallScreen) 14.sp else 16.sp,
                    fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                    fontWeight = FontWeight.Normal,
                    color = Color.White,
                ),
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = Color.White,
                modifier = Modifier.size(if (isSmallScreen) 18.dp else 20.dp)
            )
        }
    }
}

// Helper functions
private fun getUserDisplayName(userProfile: UserProfile): String {
    return when {
        userProfile.givenName.isNotEmpty() -> userProfile.givenName
        userProfile.name.isNotEmpty() -> userProfile.name
        else -> "User"
    }
}

private fun firstNonBlank(vararg values: String?): String {
    return values.firstOrNull { !it.isNullOrBlank() }?.trim().orEmpty()
}

private fun getUserEmail(userProfile: UserProfile): String {
    return if (userProfile.email.isNotEmpty()) {
        "@${userProfile.email.substringBefore("@")}"
    } else {
        "@user"
    }
}

private fun safeNavigate(navController: NavController, route: String) {
    try {
        navController.navigate(route)
    } catch (e: Exception) {
        Log.e("ProfileScreen", "Navigation error to $route", e)
    }
}
