package com.example.talkeys_new.utils.cache

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

/**
 * Image caching utility that provides both memory and disk caching for images
 * Uses Android's built-in LruCache for memory caching and file system for disk caching
 */
class ImageCache private constructor(private val context: Context) {
    
    companion object {
        private const val TAG = "ImageCache"
        private const val DISK_CACHE_DIR = "image_cache"
        private const val DISK_CACHE_SIZE = 50 * 1024 * 1024L // 50MB
        private const val MEMORY_CACHE_SIZE = 1024 * 1024 * 10 // 10MB
        
        @Volatile
        private var INSTANCE: ImageCache? = null
        
        fun getInstance(context: Context): ImageCache {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ImageCache(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    // Memory cache for quick access to recently used images
    private val memoryCache: LruCache<String, Bitmap> = object : LruCache<String, Bitmap>(MEMORY_CACHE_SIZE) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return bitmap.byteCount
        }
        
        override fun entryRemoved(evicted: Boolean, key: String, oldValue: Bitmap, newValue: Bitmap?) {
            if (evicted) {
                Log.d(TAG, "Bitmap evicted from memory cache: $key")
            }
        }
    }
    
    // Disk cache directory
    private val diskCacheDir: File = File(context.cacheDir, DISK_CACHE_DIR).apply {
        if (!exists()) {
            mkdirs()
            Log.d(TAG, "Created disk cache directory: $absolutePath")
        }
    }
    
    init {
        Log.d(TAG, "ImageCache initialized with memory cache size: ${MEMORY_CACHE_SIZE / (1024 * 1024)}MB")
        cleanupOldFiles()
    }
    
    /**
     * Gets an image from cache or downloads it if not cached
     * @param imageUrl The URL of the image to retrieve
     * @param maxWidth Maximum width for the bitmap (optional, for memory optimization)
     * @param maxHeight Maximum height for the bitmap (optional, for memory optimization)
     * @return Bitmap or null if failed to load
     */
    suspend fun getImage(
        imageUrl: String,
        maxWidth: Int? = null,
        maxHeight: Int? = null
    ): Bitmap? = withContext(Dispatchers.IO) {
        if (imageUrl.isBlank()) {
            Log.w(TAG, "Image URL is blank")
            return@withContext null
        }
        
        val cacheKey = generateCacheKey(imageUrl)
        
        // Check memory cache first
        memoryCache.get(cacheKey)?.let { bitmap ->
            Log.d(TAG, "Image found in memory cache")
            return@withContext bitmap
        }
        
        // Check disk cache
        val diskFile = File(diskCacheDir, cacheKey)
        if (diskFile.exists()) {
            try {
                val bitmap = loadBitmapFromDisk(diskFile, maxWidth, maxHeight)
                if (bitmap != null) {
                    // Add to memory cache
                    memoryCache.put(cacheKey, bitmap)
                    Log.d(TAG, "Image loaded from disk cache")
                    return@withContext bitmap
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load image from disk cache", e)
                // Delete corrupted file
                diskFile.delete()
            }
        }
        
        // Download image
        try {
            val bitmap = downloadImage(imageUrl, maxWidth, maxHeight)
            if (bitmap != null) {
                // Save to disk cache
                saveBitmapToDisk(bitmap, diskFile)
                // Add to memory cache
                memoryCache.put(cacheKey, bitmap)
                Log.d(TAG, "Image downloaded and cached")
                return@withContext bitmap
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to download image", e)
        }
        
        return@withContext null
    }
    
    /**
     * Preloads an image into cache without returning it
     * Useful for preloading images that will be needed soon
     */
    suspend fun preloadImage(imageUrl: String) {
        getImage(imageUrl)
    }
    
    /**
     * Clears all cached images from memory and disk
     */
    suspend fun clearCache() = withContext(Dispatchers.IO) {
        // Clear memory cache
        memoryCache.evictAll()
        
        // Clear disk cache
        diskCacheDir.listFiles()?.forEach { file ->
            file.delete()
        }
        
        Log.d(TAG, "All caches cleared")
    }
    
    /**
     * Clears only memory cache, keeping disk cache intact
     */
    fun clearMemoryCache() {
        memoryCache.evictAll()
        Log.d(TAG, "Memory cache cleared")
    }
    
    /**
     * Gets cache statistics
     */
    fun getCacheStats(): Map<String, Any> {
        val diskFiles = diskCacheDir.listFiles() ?: emptyArray()
        val diskSizeBytes = diskFiles.sumOf { it.length() }
        
        return mapOf(
            "memoryCacheSize" to memoryCache.size(),
            "memoryCacheMaxSize" to memoryCache.maxSize(),
            "memoryCacheHitCount" to memoryCache.hitCount(),
            "memoryCacheMissCount" to memoryCache.missCount(),
            "diskCacheFiles" to diskFiles.size,
            "diskCacheSizeMB" to diskSizeBytes / (1024 * 1024)
        )
    }
    
    // Private helper methods
    
    private fun generateCacheKey(imageUrl: String): String {
        return try {
            val digest = MessageDigest.getInstance("MD5")
            val hashBytes = digest.digest(imageUrl.toByteArray())
            hashBytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate cache key, using hashCode", e)
            imageUrl.hashCode().toString()
        }
    }
    
    private suspend fun downloadImage(
        imageUrl: String,
        maxWidth: Int?,
        maxHeight: Int?
    ): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val url = URL(imageUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 10000
            connection.readTimeout = 15000
            connection.doInput = true
            
            connection.connect()
            
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = connection.inputStream
                
                // If size constraints are specified, decode with options
                if (maxWidth != null || maxHeight != null) {
                    val options = BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                    }
                    
                    // First decode to get dimensions
                    val bytes = inputStream.readBytes()
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
                    
                    // Calculate sample size
                    options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight)
                    options.inJustDecodeBounds = false
                    
                    return@withContext BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
                } else {
                    return@withContext BitmapFactory.decodeStream(inputStream)
                }
            } else {
                Log.e(TAG, "HTTP error downloading image: ${connection.responseCode}")
                return@withContext null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception downloading image", e)
            return@withContext null
        }
    }
    
    private fun loadBitmapFromDisk(
        file: File,
        maxWidth: Int?,
        maxHeight: Int?
    ): Bitmap? {
        return try {
            if (maxWidth != null || maxHeight != null) {
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                
                BitmapFactory.decodeFile(file.absolutePath, options)
                options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight)
                options.inJustDecodeBounds = false
                
                BitmapFactory.decodeFile(file.absolutePath, options)
            } else {
                BitmapFactory.decodeFile(file.absolutePath)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load bitmap from disk: ${file.absolutePath}", e)
            null
        }
    }
    
    private fun saveBitmapToDisk(bitmap: Bitmap, file: File) {
        try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
            }
        } catch (e: IOException) {
            Log.e(TAG, "Failed to save bitmap to disk: ${file.absolutePath}", e)
        }
    }
    
    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int?,
        reqHeight: Int?
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        
        if (reqHeight != null || reqWidth != null) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            
            while ((halfHeight / inSampleSize) >= (reqHeight ?: Int.MAX_VALUE) &&
                (halfWidth / inSampleSize) >= (reqWidth ?: Int.MAX_VALUE)
            ) {
                inSampleSize *= 2
            }
        }
        
        return inSampleSize
    }
    
    private fun cleanupOldFiles() {
        try {
            val files = diskCacheDir.listFiles() ?: return
            val currentTime = System.currentTimeMillis()
            val maxAge = 7 * 24 * 60 * 60 * 1000L // 7 days
            
            files.forEach { file ->
                if (currentTime - file.lastModified() > maxAge) {
                    file.delete()
                    Log.d(TAG, "Deleted old cache file: ${file.name}")
                }
            }
            
            // Also check total disk cache size
            val totalSize = files.sumOf { it.length() }
            if (totalSize > DISK_CACHE_SIZE) {
                // Sort by last modified and delete oldest files
                files.sortedBy { it.lastModified() }
                    .take((files.size * 0.3).toInt()) // Delete 30% of files
                    .forEach { file ->
                        file.delete()
                        Log.d(TAG, "Deleted cache file due to size limit: ${file.name}")
                    }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during cache cleanup", e)
        }
    }
}
