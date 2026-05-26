package com.talkeys.shared.auth

import platform.Foundation.NSUserDefaults

class IOSTokenStorage : TokenStorage {
    
    companion object {
        private const val TOKEN_KEY = "auth_token"
    }
    
    private val userDefaults = NSUserDefaults.standardUserDefaults
    
    override suspend fun saveToken(token: String) {
        userDefaults.setObject(token, TOKEN_KEY)
        userDefaults.synchronize()
    }
    
    override suspend fun getToken(): String? {
        return userDefaults.stringForKey(TOKEN_KEY)
    }
    
    override suspend fun clearToken() {
        userDefaults.removeObjectForKey(TOKEN_KEY)
        userDefaults.synchronize()
    }
    
    override suspend fun hasToken(): Boolean {
        return getToken() != null
    }
}

// Phase 0: actual factory removed. Construct IOSTokenStorage() directly.
