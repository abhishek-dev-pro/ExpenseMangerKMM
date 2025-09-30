package com.example.androidkmm.security

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec as JavaSecretKeySpec

/**
 * Android-specific encryption utilities using Java crypto APIs
 */
actual object EncryptionUtils {
    
    actual suspend fun encryptData(data: String, key: SecretKey): String {
        try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val iv = ByteArray(12) // GCM recommended IV length
            SecureRandom().nextBytes(iv)
            
            val parameterSpec = GCMParameterSpec(128, iv)
            val javaKey = JavaSecretKeySpec(key.encoded, key.algorithm)
            cipher.init(Cipher.ENCRYPT_MODE, javaKey, parameterSpec)
            
            val encryptedBytes = cipher.doFinal(data.toByteArray())
            val combined = iv + encryptedBytes
            
            return Base64.encodeToString(combined, Base64.DEFAULT)
        } catch (e: Exception) {
            throw SecurityException("Encryption failed", e)
        }
    }
    
    actual suspend fun decryptData(encryptedData: String, key: SecretKey): String {
        try {
            val encryptedBytes = Base64.decode(encryptedData, Base64.DEFAULT)
            val iv = encryptedBytes.sliceArray(0 until 12)
            val cipherText = encryptedBytes.sliceArray(12 until encryptedBytes.size)
            
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val parameterSpec = GCMParameterSpec(128, iv)
            val javaKey = JavaSecretKeySpec(key.encoded, key.algorithm)
            cipher.init(Cipher.DECRYPT_MODE, javaKey, parameterSpec)
            
            val decryptedBytes = cipher.doFinal(cipherText)
            return String(decryptedBytes)
        } catch (e: Exception) {
            throw SecurityException("Decryption failed", e)
        }
    }
    
    actual fun generateKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(256)
        val javaKey = keyGenerator.generateKey()
        return SecretKeySpec(javaKey.encoded, javaKey.algorithm)
    }
    
    actual fun createKeyFromBytes(keyBytes: ByteArray): SecretKey {
        return SecretKeySpec(keyBytes, "AES")
    }
    
    actual fun generateRandomBytes(length: Int): ByteArray {
        val bytes = ByteArray(length)
        SecureRandom().nextBytes(bytes)
        return bytes
    }
    
    actual fun generateSecureRandomBytes(length: Int): ByteArray {
        return generateRandomBytes(length)
    }
    
    actual fun generateRandomString(length: Int): String {
        val bytes = generateRandomBytes(length)
        return bytes.joinToString("") { "%02x".format(it) }
    }
    
    actual fun sha256Hash(data: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(data.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }
    
    actual fun generateSecureRandomString(length: Int): String {
        return generateRandomString(length)
    }
}

// Android-specific SecretKey interface
actual interface SecretKey {
    actual val encoded: ByteArray
    actual val algorithm: String
    actual val format: String
}

// Android-specific SecretKey implementation
actual class SecretKeySpec actual constructor(
    private val keyBytes: ByteArray,
    private val algorithmName: String
) : SecretKey {
    override val encoded: ByteArray get() = keyBytes
    override val algorithm: String get() = algorithmName
    override val format: String get() = "RAW"
}
