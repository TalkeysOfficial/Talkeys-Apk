package com.talkeys.shared.auth

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.CoreFoundation.CFAutorelease
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFStringRef
import platform.CoreFoundation.CFTypeRef
import platform.CoreFoundation.CFTypeRefVar
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.SecItemUpdate
import platform.Security.errSecItemNotFound
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData

/**
 * iOS [SecureStorage] backed by the system Keychain.
 *
 * All values are stored as `kSecClassGenericPassword` items keyed by
 * [SERVICE_NAME] (service attribute) + the caller-supplied key (account
 * attribute).  Data is encrypted at rest by the Secure Enclave /
 * Data-Protection class and is not included in plain-text device backups.
 *
 * The service name `"com.talkeys.shared.auth"` scopes the entries to
 * this application's auth secrets.  Ordinary user preferences must NOT
 * be stored here — use `NSUserDefaults` / platform settings instead.
 */
class IosSecureStorage : SecureStorage {

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    override suspend fun getString(key: String): String? {
        val query = mapOf<Any?, Any?>(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to SERVICE_NAME,
            kSecAttrAccount to key,
            kSecMatchLimit to kSecMatchLimitOne,
            kSecReturnData to true,
        )
        memScoped {
            val result = alloc<CFTypeRefVar>()
            val status = SecItemCopyMatching(query.toCFDictionary(), result.ptr)
            if (status == errSecSuccess) {
                val data = CFBridgingRelease(result.value) as? NSData ?: return null
                return NSString.create(data = data, encoding = NSUTF8StringEncoding) as? String
            }
            return null
        }
    }

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    override suspend fun putString(key: String, value: String) {
        val encoded = (value as NSString).dataUsingEncoding(NSUTF8StringEncoding) ?: return

        // Try update first; if item doesn't exist yet, add it.
        val query = mapOf<Any?, Any?>(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to SERVICE_NAME,
            kSecAttrAccount to key,
        )
        val attrs = mapOf<Any?, Any?>(
            kSecValueData to encoded,
        )
        val updateStatus = SecItemUpdate(query.toCFDictionary(), attrs.toCFDictionary())
        if (updateStatus == errSecItemNotFound) {
            val addQuery = query + (kSecValueData to encoded)
            SecItemAdd(addQuery.toCFDictionary(), null)
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun remove(key: String) {
        val query = mapOf<Any?, Any?>(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to SERVICE_NAME,
            kSecAttrAccount to key,
        )
        SecItemDelete(query.toCFDictionary())
    }

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun clear() {
        val query = mapOf<Any?, Any?>(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to SERVICE_NAME,
        )
        SecItemDelete(query.toCFDictionary())
    }

    private companion object {
        /** Keychain service attribute — scopes entries to this app's auth. */
        const val SERVICE_NAME = "com.talkeys.shared.auth"
    }
}

/**
 * Bridge a Kotlin [Map] to a Core-Foundation dictionary reference that
 * the Security framework functions accept.
 *
 * The dictionary is autoreleased so it stays valid for the duration of
 * the calling scope without manual memory management.
 */
@OptIn(ExperimentalForeignApi::class)
@Suppress("UNCHECKED_CAST")
private fun Map<Any?, Any?>.toCFDictionary(): CFDictionaryRef? {
    val nsDict = this as? Map<Any, Any> ?: return null
    val cfRef = CFBridgingRetain(nsDict) as CFDictionaryRef?
    if (cfRef != null) CFAutorelease(cfRef)
    return cfRef
}
