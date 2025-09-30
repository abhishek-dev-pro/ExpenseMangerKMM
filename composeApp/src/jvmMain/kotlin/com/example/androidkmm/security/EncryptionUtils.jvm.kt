package com.example.androidkmm.security

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * JVM-specific encryption utilities using Java crypto APIs
 */
actual object EncryptionUtils {
    
    actual suspend fun encryptData(data: String, key: SecretKey): String {
        try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val iv = ByteArray(12) // GCM recommended IV length
            SecureRandom().nextBytes(iv)
            
            val parameterSpec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec)
            
            val encryptedBytes = cipher.doFinal(data.toByteArray())
            val combined = iv + encryptedBytes
            
            return Base64.getEncoder().encodeToString(combined)
        } catch (e: Exception) {
            throw SecurityException("Encryption failed", e)
        }
    }
    
    actual suspend fun decryptData(encryptedData: String, key: SecretKey): String {
        try {
            val encryptedBytes = Base64.getDecoder().decode(encryptedData)
            val iv = encryptedBytes.sliceArray(0 until 12)
            val cipherText = encryptedBytes.sliceArray(12 until encryptedBytes.size)
            
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val parameterSpec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec)
            
            val decryptedBytes = cipher.doFinal(cipherText)
            return String(decryptedBytes)
        } catch (e: Exception) {
            throw SecurityException("Decryption failed", e)
        }
    }
    
    actual fun generateKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(256)
        return keyGenerator.generateKey()
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

// JVM-specific SecretKey implementation
actual class SecretKeySpec actual constructor(
    private val keyBytes: ByteArray,
    private val algorithmName: String
) : SecretKey {
    override val encoded: ByteArray get() = keyBytes
    override val algorithm: String get() = algorithmName
    override val format: String get() = "RAW"
}

// JVM-specific SecretKey interface
actual interface SecretKey {
    val encoded: ByteArray
    val algorithm: String
    val format: String
}
