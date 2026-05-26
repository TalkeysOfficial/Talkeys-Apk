package com.example.talkeys_new.screens.authentication

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

data class UserProfile(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val profileImageUrl: String? = null,
    val givenName: String = "",
    val familyName: String = ""
)

class GoogleSignInManager(private val context: Context) {
    
    companion object {
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val USER_PROFILE_IMAGE_KEY = stringPreferencesKey("user_profile_image")
        private val USER_GIVEN_NAME_KEY = stringPreferencesKey("user_given_name")
        private val USER_FAMILY_NAME_KEY = stringPreferencesKey("user_family_name")
    }
    
    // Flow to observe user profile changes
    val userProfile: Flow<UserProfile> = context.dataStore.data.map { preferences ->
        UserProfile(
            id = preferences[USER_ID_KEY] ?: "",
            name = preferences[USER_NAME_KEY] ?: "",
            email = preferences[USER_EMAIL_KEY] ?: "",
            profileImageUrl = preferences[USER_PROFILE_IMAGE_KEY],
            givenName = preferences[USER_GIVEN_NAME_KEY] ?: "",
            familyName = preferences[USER_FAMILY_NAME_KEY] ?: ""
        )
    }
    
    // Save user profile data from Google Sign-In
    suspend fun saveUserProfile(userProfile: UserProfile) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userProfile.id
            preferences[USER_NAME_KEY] = userProfile.name
            preferences[USER_EMAIL_KEY] = userProfile.email
            preferences[USER_GIVEN_NAME_KEY] = userProfile.givenName
            preferences[USER_FAMILY_NAME_KEY] = userProfile.familyName
            userProfile.profileImageUrl?.let {
                preferences[USER_PROFILE_IMAGE_KEY] = it
            }
        }
    }

    // Get current user profile
    suspend fun getUserProfile(): UserProfile = userProfile.first()
    
    // Clear user profile data
    suspend fun clearUserProfile() {
        context.dataStore.edit { preferences ->
            preferences.remove(USER_ID_KEY)
            preferences.remove(USER_NAME_KEY)
            preferences.remove(USER_EMAIL_KEY)
            preferences.remove(USER_PROFILE_IMAGE_KEY)
            preferences.remove(USER_GIVEN_NAME_KEY)
            preferences.remove(USER_FAMILY_NAME_KEY)
        }
    }
    
    // Check if user is signed in
    suspend fun isUserSignedIn(): Boolean {
        val profile = getUserProfile()
        return profile.id.isNotEmpty() && profile.email.isNotEmpty()
    }
}