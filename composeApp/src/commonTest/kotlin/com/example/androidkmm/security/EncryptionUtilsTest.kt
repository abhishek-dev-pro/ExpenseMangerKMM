package com.example.androidkmm.security

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for EncryptionUtils
 * 
 * Tests encryption and decryption functionality.
 * Ensures security measures work correctly.
 */
class EncryptionUtilsTest {
    
    @Test
    fun `test generate secret key`() {
        val key = EncryptionUtils.generateSecretKey()
        assertTrue(key.encoded.isNotEmpty())
        assertEquals("AES", key.algorithm)
    }
    
    @Test
    fun `test generate salt`() {
        val salt = EncryptionUtils.generateSalt()
        assertEquals(32, salt.size)
        
        // Generate another salt and ensure they're different
        val anotherSalt = EncryptionUtils.generateSalt()
        assertFalse(salt.contentEquals(anotherSalt))
    }
    
    @Test
    fun `test hash password`() {
        val password = "testPassword123"
        val salt = EncryptionUtils.generateSalt()
        
        val hash1 = EncryptionUtils.hashPassword(password, salt)
        val hash2 = EncryptionUtils.hashPassword(password, salt)
        
        // Same password and salt should produce same hash
        assertEquals(hash1, hash2)
        
        // Different password should produce different hash
        val differentHash = EncryptionUtils.hashPassword("differentPassword", salt)
        assertFalse(hash1 == differentHash)
    }
    
    @Test
    fun `test verify password`() {
        val password = "testPassword123"
        val salt = EncryptionUtils.generateSalt()
        val hash = EncryptionUtils.hashPassword(password, salt)
        
        // Correct password should verify
        assertTrue(EncryptionUtils.verifyPassword(password, hash, salt))
        
        // Wrong password should not verify
        assertFalse(EncryptionUtils.verifyPassword("wrongPassword", hash, salt))
    }
    
    @Test
    fun `test generate random string`() {
        val randomString = EncryptionUtils.generateRandomString(10)
        assertEquals(10, randomString.length)
        
        // Generate another string and ensure they're different
        val anotherString = EncryptionUtils.generateRandomString(10)
        assertFalse(randomString == anotherString)
    }
    
    @Test
    fun `test generate random number`() {
        val randomNumber = EncryptionUtils.generateRandomNumber(1, 100)
        assertTrue(randomNumber >= 1)
        assertTrue(randomNumber < 100)
    }
    
    @Test
    fun `test generate secure token`() {
        val token = EncryptionUtils.generateSecureToken()
        assertEquals(32, token.length)
        
        // Generate another token and ensure they're different
        val anotherToken = EncryptionUtils.generateSecureToken()
        assertFalse(token == anotherToken)
    }
    
    @Test
    fun `test sha256 hash`() {
        val data = "test data"
        val hash1 = EncryptionUtils.sha256Hash(data)
        val hash2 = EncryptionUtils.sha256Hash(data)
        
        // Same data should produce same hash
        assertEquals(hash1, hash2)
        
        // Different data should produce different hash
        val differentHash = EncryptionUtils.sha256Hash("different data")
        assertFalse(hash1 == differentHash)
    }
    
    @Test
    fun `test generate secure UUID`() {
        val uuid = EncryptionUtils.generateSecureUUID()
        assertTrue(uuid.isNotEmpty())
        
        // Generate another UUID and ensure they're different
        val anotherUuid = EncryptionUtils.generateSecureUUID()
        assertFalse(uuid == anotherUuid)
    }
}
