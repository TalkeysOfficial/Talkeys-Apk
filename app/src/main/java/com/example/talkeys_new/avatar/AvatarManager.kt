package com.example.talkeys_new.avatar

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap

/**
 * Immutable data class representing avatar configuration
 */
data class AvatarConfig(
    val style: String = AvatarConstants.AVATAR_STYLES[0],
    val backgroundColor: String = AvatarConstants.BACKGROUND_COLORS[0].value,
    val userName: String = "user",
    val seedModifier: String = "",
    val lastUpdated: Long = System.currentTimeMillis(),
    val cacheVersion: Int = 1
) {
    
    /**
     * Validates the current configuration
     */
    fun validate(): Result<Unit> {
        return try {
            when {
                !AvatarConstants.isValidStyle(style) -> 
                    Result.failure(IllegalArgumentException("Invalid avatar style: $style"))
                !AvatarConstants.isValidBackgroundColor(backgroundColor) -> 
                    Result.failure(IllegalArgumentException("Invalid background color: $backgroundColor"))
                userName.isBlank() -> 
                    Result.failure(IllegalArgumentException("User name cannot be blank"))
                seedModifier.length > 20 -> 
                    Result.failure(IllegalArgumentException("Seed modifier too long"))
                else -> Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Generates the main avatar URL with comprehensive error handling
     */
    fun generateAvatarUrl(size: Int = AvatarConstants.DEFAULT_AVATAR_SIZE): String {
        return try {
            val sanitizedSeed = generateSeed()
            AvatarConstants.generateAvatarUrl(
                style = style,
                seed = sanitizedSeed,
                backgroundColor = backgroundColor,
                size = size
            )
        } catch (e: Exception) {
            Log.e("AvatarConfig", "Error generating avatar URL", e)
            generateFallbackUrl(size)
        }
    }
    
    /**
     * Generates preview URL for style selection
     */
    fun generatePreviewUrl(
        previewStyle: String, 
        size: Int = AvatarConstants.PREVIEW_AVATAR_SIZE
    ): String {
        return try {
            val sanitizedSeed = generateSeed()
            AvatarConstants.generateAvatarUrl(
                style = previewStyle,
                seed = sanitizedSeed,
                backgroundColor = backgroundColor,
                size = size
            )
        } catch (e: Exception) {
            Log.e("AvatarConfig", "Error generating preview URL", e)
            generateFallbackUrl(size)
        }
    }
    
    /**
     * Generates a consistent seed for avatar generation
     */
    private fun generateSeed(): String {
        val baseSeed = AvatarConstants.sanitizeSeed(userName)
        return if (seedModifier.isNotEmpty()) {
            "${baseSeed}${AvatarConstants.sanitizeSeed(seedModifier)}"
        } else {
            baseSeed
        }
    }
    
    /**
     * Generates a fallback URL when primary generation fails
     */
    private fun generateFallbackUrl(size: Int): String {
        return AvatarConstants.generateAvatarUrl(
            style = AvatarConstants.DEFAULT_STYLE,
            seed = AvatarConstants.DEFAULT_USER_NAME,
            backgroundColor = AvatarConstants.DEFAULT_BACKGROUND_COLOR,
            size = size
        )
    }
    
    /**
     * Creates a copy with validated parameters
     */
    fun copyWithValidation(
        style: String = this.style,
        backgroundColor: String = this.backgroundColor,
        userName: String = this.userName,
        seedModifier: String = this.seedModifier
    ): AvatarConfig {
        return try {
            val newConfig = copy(
                style = if (AvatarConstants.isValidStyle(style)) style else this.style,
                backgroundColor = if (AvatarConstants.isValidBackgroundColor(backgroundColor)) 
                    backgroundColor else this.backgroundColor,
                userName = userName.takeIf { it.isNotBlank() } ?: this.userName,
                seedModifier = seedModifier.take(20), // Limit length
                lastUpdated = System.currentTimeMillis()
            )
            
            // Validate the new configuration
            newConfig.validate().getOrThrow()
            newConfig
        } catch (e: Exception) {
            Log.w("AvatarConfig", "Validation failed, keeping current config", e)
            this
        }
    }
}

/**
 * Professional avatar management system with comprehensive error handling
 */
class AvatarManager private constructor(
    private val context: Context,
    private val sharedPrefs: SharedPreferences
) {
    
    // Public constructor for backward compatibility
    constructor(context: Context) : this(
        context.applicationContext,
        context.applicationContext.getSharedPreferences(
            AvatarConstants.AVATAR_PREFS,
            Context.MODE_PRIVATE
        )
    )
    
    companion object {
        private const val TAG = "AvatarManager"
        
        // Thread-safe singleton implementation
        @Volatile
        private var INSTANCE: AvatarManager? = null
        private val instanceCache = ConcurrentHashMap<String, AvatarManager>()
        
        /**
         * Get or create AvatarManager instance
         */
        fun getInstance(context: Context): AvatarManager {
            val appContext = context.applicationContext
            val key = appContext.packageName
            
            return instanceCache.getOrPut(key) {
                val prefs = appContext.getSharedPreferences(
                    AvatarConstants.AVATAR_PREFS,
                    Context.MODE_PRIVATE
                )
                AvatarManager(appContext, prefs)
            }
        }
        
        /**
         * For testing purposes only
         */
        @VisibleForTesting
        internal fun createTestInstance(context: Context, prefs: SharedPreferences): AvatarManager {
            return AvatarManager(context, prefs)
        }
    }
    
    // Thread-safe state management
    private val _avatarConfig = MutableStateFlow(loadAvatarConfigSafely())
    val avatarConfig: StateFlow<AvatarConfig> = _avatarConfig.asStateFlow()
    
    // Performance monitoring
    private var lastLoadTime = 0L
    private var lastSaveTime = 0L
    
    /**
     * Safely loads avatar configuration with comprehensive error handling
     */
    private fun loadAvatarConfigSafely(): AvatarConfig {
        val startTime = System.currentTimeMillis()
        
        return try {
            val style = sharedPrefs.getString(AvatarConstants.KEY_AVATAR_STYLE, null)
                ?.takeIf { AvatarConstants.isValidStyle(it) }
                ?: AvatarConstants.AVATAR_STYLES[0]
            
            val backgroundColor = sharedPrefs.getString(AvatarConstants.KEY_AVATAR_BG, null)
                ?.takeIf { AvatarConstants.isValidBackgroundColor(it) }
                ?: AvatarConstants.BACKGROUND_COLORS[0].value
            
            val userName = sharedPrefs.getString(AvatarConstants.KEY_USER_NAME, null)
                ?.takeIf { it.isNotBlank() }
                ?: "user"
            
            val seedModifier = sharedPrefs.getString(AvatarConstants.KEY_SEED_MODIFIER, "")
                ?.take(20) // Limit length
                ?: ""
            
            val lastUpdated = sharedPrefs.getLong(AvatarConstants.KEY_LAST_UPDATE, System.currentTimeMillis())
            val cacheVersion = sharedPrefs.getInt(AvatarConstants.KEY_CACHE_VERSION, 1)
            
            val config = AvatarConfig(
                style = style,
                backgroundColor = backgroundColor,
                userName = userName,
                seedModifier = seedModifier,
                lastUpdated = lastUpdated,
                cacheVersion = cacheVersion
            )
            
            // Validate loaded configuration
            config.validate().getOrThrow()
            
            lastLoadTime = System.currentTimeMillis() - startTime
            Log.d(TAG, "Successfully loaded avatar config in ${lastLoadTime}ms")
            
            config
        } catch (e: Exception) {
            Log.e(TAG, "Error loading avatar config, using defaults", e)
            AvatarConfig() // Return safe default
        }
    }
    
    /**
     * Updates avatar style with validation and error handling
     */
    fun updateAvatarStyle(newStyle: String) {
        if (AvatarConstants.isValidStyle(newStyle)) {
            val currentConfig = _avatarConfig.value
            val newConfig = currentConfig.copyWithValidation(style = newStyle)
            if (saveAvatarConfigSafely(newConfig)) {
                _avatarConfig.value = newConfig
                Log.d(TAG, "Updated avatar style to: $newStyle")
            }
        } else {
            Log.w(TAG, "Invalid avatar style: $newStyle")
        }
    }
    
    /**
     * Updates background color with validation
     */
    fun updateBackgroundColor(newColor: String) {
        if (AvatarConstants.isValidBackgroundColor(newColor)) {
            val currentConfig = _avatarConfig.value
            val newConfig = currentConfig.copyWithValidation(backgroundColor = newColor)
            if (saveAvatarConfigSafely(newConfig)) {
                _avatarConfig.value = newConfig
                Log.d(TAG, "Updated background color to: $newColor")
            }
        } else {
            Log.w(TAG, "Invalid background color: $newColor")
        }
    }
    
    /**
     * Updates user name with validation and sanitization
     */
    fun updateUserName(newName: String) {
        if (newName.isNotBlank()) {
            val currentConfig = _avatarConfig.value
            val newConfig = currentConfig.copyWithValidation(userName = newName.trim())
            if (saveAvatarConfigSafely(newConfig)) {
                _avatarConfig.value = newConfig
                Log.d(TAG, "Updated avatar user name")
            }
        } else {
            Log.w(TAG, "Invalid avatar user name")
        }
    }
    
    /**
     * Updates seed modifier for avatar variations
     */
    fun updateSeedModifier(newModifier: String) {
        val currentConfig = _avatarConfig.value
        val newConfig = currentConfig.copyWithValidation(seedModifier = newModifier.take(20))
        if (saveAvatarConfigSafely(newConfig)) {
            _avatarConfig.value = newConfig
            Log.d(TAG, "Updated seed modifier to: ${newModifier.take(20)}")
        }
    }
    
    /**
     * Safely saves avatar configuration with error handling and atomic operations
     */
    private fun saveAvatarConfigSafely(config: AvatarConfig): Boolean {
        val startTime = System.currentTimeMillis()
        
        return try {
            // Validate before saving
            config.validate().getOrThrow()
            
            // Atomic save operation
            val success = sharedPrefs.edit().apply {
                putString(AvatarConstants.KEY_AVATAR_STYLE, config.style)
                putString(AvatarConstants.KEY_AVATAR_BG, config.backgroundColor)
                putString(AvatarConstants.KEY_USER_NAME, config.userName)
                putString(AvatarConstants.KEY_SEED_MODIFIER, config.seedModifier)
                putLong(AvatarConstants.KEY_LAST_UPDATE, config.lastUpdated)
                putInt(AvatarConstants.KEY_CACHE_VERSION, config.cacheVersion)
            }.commit() // Use commit() for synchronous operation with return value
            
            if (success) {
                lastSaveTime = System.currentTimeMillis() - startTime
                Log.d(TAG, "Successfully saved avatar config in ${lastSaveTime}ms")
            } else {
                Log.e(TAG, "Failed to save avatar config to SharedPreferences")
            }
            
            success
        } catch (e: Exception) {
            Log.e(TAG, "Error saving avatar config", e)
            false
        }
    }
    
    /**
     * Gets the current avatar URL with error handling
     */
    fun getCurrentAvatarUrl(size: Int = AvatarConstants.DEFAULT_AVATAR_SIZE): String {
        return try {
            val url = _avatarConfig.value.generateAvatarUrl(size)
            Log.d(TAG, "Generated avatar URL")
            url
        } catch (e: Exception) {
            Log.e(TAG, "Error generating current avatar URL", e)
            generateFallbackUrl(size)
        }
    }
    
    /**
     * Generates a test URL for API connectivity verification
     */
    fun getTestAvatarUrl(): String {
        return AvatarConstants.generateAvatarUrl(
            style = AvatarConstants.DEFAULT_STYLE,
            seed = "test",
            backgroundColor = AvatarConstants.DEFAULT_BACKGROUND_COLOR,
            size = AvatarConstants.DEFAULT_AVATAR_SIZE
        )
    }
    
    /**
     * Resets configuration to defaults
     */
    fun resetToDefaults() {
        val defaultConfig = AvatarConfig()
        if (saveAvatarConfigSafely(defaultConfig)) {
            _avatarConfig.value = defaultConfig
            Log.d(TAG, "Avatar config reset to defaults")
        }
    }
    
    /**
     * Generates fallback URL when primary generation fails
     */
    private fun generateFallbackUrl(size: Int): String {
        return AvatarConstants.generateAvatarUrl(
            style = AvatarConstants.DEFAULT_STYLE,
            seed = "user",
            backgroundColor = AvatarConstants.DEFAULT_BACKGROUND_COLOR,
            size = size
        )
    }
    
    /**
     * Gets performance metrics for monitoring
     */
    @VisibleForTesting
    fun getPerformanceMetrics(): Map<String, Long> {
        return mapOf(
            "lastLoadTime" to lastLoadTime,
            "lastSaveTime" to lastSaveTime,
            "configAge" to (System.currentTimeMillis() - _avatarConfig.value.lastUpdated)
        )
    }
    
    /**
     * Validates current configuration
     */
    fun validateCurrentConfig(): Result<Unit> {
        return _avatarConfig.value.validate()
    }
}
