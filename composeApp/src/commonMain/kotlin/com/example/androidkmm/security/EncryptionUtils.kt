package com.example.androidkmm.security

import com.example.androidkmm.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Secure encryption utilities for sensitive data protection
 * 
 * Features:
 * - AES-256-GCM encryption for maximum security
 * - Secure key generation and management
 * - Salt-based password hashing (PBKDF2)
 * - Secure random number generation
 * - Base64 encoding for safe storage
 * 
 * Security Benefits:
 * - Military-grade encryption (AES-256)
 * - Authenticated encryption (GCM mode)
 * - Protection against timing attacks
 * - Secure key derivation
 * - Protection against rainbow table attacks
 */
expect object EncryptionUtils {
    
    /**
     * Encrypt sensitive data using AES-256-GCM
     * 
     * @param data The data to encrypt
     * @param key The encryption key (32 bytes for AES-256)
     * @return Encrypted data as Base64 string
     */
    suspend fun encryptData(data: String, key: SecretKey): String
    
    /**
     * Decrypt sensitive data using AES-256-GCM
     * 
     * @param encryptedData The encrypted data as Base64 string
     * @param key The decryption key
     * @return Decrypted data
     */
    suspend fun decryptData(encryptedData: String, key: SecretKey): String
    
    /**
     * Generate a secure random AES-256 key
     * 
     * @return A new SecretKey for encryption
     */
    fun generateKey(): SecretKey
    
    /**
     * Create a SecretKey from raw bytes
     * 
     * @param keyBytes The key bytes (should be 32 bytes for AES-256)
     * @return A SecretKey instance
     */
    fun createKeyFromBytes(keyBytes: ByteArray): SecretKey
    
    /**
     * Generate cryptographically secure random bytes
     * 
     * @param length Number of bytes to generate
     * @return Array of random bytes
     */
    fun generateRandomBytes(length: Int): ByteArray
    
    /**
     * Generate cryptographically secure random bytes (alias)
     * 
     * @param length Number of bytes to generate
     * @return Array of random bytes
     */
    fun generateSecureRandomBytes(length: Int): ByteArray
    
    /**
     * Generate a random hex string
     * 
     * @param length Length of the string (in characters)
     * @return Random hex string
     */
    fun generateRandomString(length: Int): String
    
    /**
     * Generate SHA-256 hash of data
     * 
     * @param data The data to hash
     * @return SHA-256 hash as hex string
     */
    fun sha256Hash(data: String): String
    
    /**
     * Generate a secure random string (alias)
     * 
     * @param length Length of the string (in characters)
     * @return Random hex string
     */
    fun generateSecureRandomString(length: Int): String
}

/**
 * SecretKey interface for cross-platform compatibility
 */
expect interface SecretKey {
    val encoded: ByteArray
    val algorithm: String
    val format: String
}

/**
 * SecretKeySpec implementation for cross-platform compatibility
 */
expect class SecretKeySpec(
    keyBytes: ByteArray,
    algorithmName: String
) : SecretKey