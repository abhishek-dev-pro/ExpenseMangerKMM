package com.example.androidkmm.security

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * iOS-specific encryption utilities - simplified implementation
 * Note: This is a simplified implementation for iOS compatibility
 * For production use, consider using platform-specific crypto libraries
 */
actual object EncryptionUtils {
    
    actual suspend fun encryptData(data: String, key: SecretKey): String = withContext(Dispatchers.Default) {
        try {
            // Simplified encryption for iOS - in production, use proper crypto
            val combined = data + "|" + key.algorithm
            combined.encodeToByteArray().joinToString("") { formatHex(it.toInt()) }
        } catch (e: Exception) {
            throw Exception("Encryption failed", e)
        }
    }
    
    actual suspend fun decryptData(encryptedData: String, key: SecretKey): String = withContext(Dispatchers.Default) {
        try {
            // Simplified decryption for iOS - in production, use proper crypto
            val bytes = encryptedData.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
            val decrypted = bytes.decodeToString()
            val parts = decrypted.split("|")
            if (parts.size == 2) parts[0] else decrypted
        } catch (e: Exception) {
            throw Exception("Decryption failed", e)
        }
    }
    
    actual fun generateKey(): SecretKey {
        val keyBytes = ByteArray(32) // 256 bits
        for (i in keyBytes.indices) {
            keyBytes[i] = (kotlin.random.Random.nextInt(256) - 128).toByte()
        }
        return SecretKeySpec(keyBytes, "AES")
    }
    
    actual fun createKeyFromBytes(keyBytes: ByteArray): SecretKey {
        return SecretKeySpec(keyBytes, "AES")
    }
    
    actual fun generateRandomBytes(length: Int): ByteArray {
        val bytes = ByteArray(length)
        for (i in bytes.indices) {
            bytes[i] = (kotlin.random.Random.nextInt(256) - 128).toByte()
        }
        return bytes
    }
    
    actual fun generateSecureRandomBytes(length: Int): ByteArray {
        return generateRandomBytes(length)
    }
    
    actual fun generateRandomString(length: Int): String {
        val bytes = generateRandomBytes(length)
        return bytes.joinToString("") { formatHex(it.toInt()) }
    }
    
    actual fun sha256Hash(data: String): String {
        // Simplified hash for iOS - in production, use proper crypto
        val bytes = data.encodeToByteArray()
        var hash = 0L
        for (byte in bytes) {
            hash = hash * 31 + byte.toLong()
        }
        return hash.toString(16).padStart(16, '0')
    }
    
    actual fun generateSecureRandomString(length: Int): String {
        return generateRandomString(length)
    }
    
    private fun formatHex(value: Int): String {
        val hex = value.toString(16)
        return if (hex.length < 2) "0$hex" else hex
    }
}

// iOS-specific SecretKey interface
actual interface SecretKey {
    actual val encoded: ByteArray
    actual val algorithm: String
    actual val format: String
}

// iOS-specific SecretKey implementation
actual class SecretKeySpec actual constructor(
    private val keyBytes: ByteArray,
    private val algorithmName: String
) : SecretKey {
    override val encoded: ByteArray get() = keyBytes
    override val algorithm: String get() = algorithmName
    override val format: String get() = "RAW"
}