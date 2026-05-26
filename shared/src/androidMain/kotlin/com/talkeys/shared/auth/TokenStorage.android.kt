package com.talkeys.shared.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "auth_prefs")

class AndroidTokenStorage(private val context: Context) : TokenStorage {
    
    companion object {
        private val TOKEN_KEY = stringPreferencesKey("auth_token")
    }
    
    override suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
    }
    
    override suspend fun getToken(): String? {
        return context.dataStore.data.map { preferences ->
            preferences[TOKEN_KEY]
        }.first()
    }
    
    override suspend fun clearToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
        }
    }
    
    override suspend fun hasToken(): Boolean {
        return getToken() != null
    }
}

// Phase 0: actual factory removed. Construct AndroidTokenStorage(context) directly.
