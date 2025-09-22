package com.example.androidkmm.security

import com.example.androidkmm.utils.Logger
import com.example.androidkmm.screens.ledger.LedgerPerson

/**
 * Security utilities for data protection and validation
 */
object SecurityUtils {
    
    /**
     * Sanitize user input to prevent injection attacks
     */
    fun sanitizeInput(input: String): String {
        return input
            .replace(Regex("[<>\"'&]"), "") // Remove potentially dangerous characters
            .trim()
            .take(1000) // Limit length
    }
    
    /**
     * Validate input for security
     */
    fun validateInput(input: String): Boolean {
        return input.isNotBlank() && 
               input.length <= 1000 && 
               !containsMaliciousPatterns(input)
    }
    
    /**
     * Check for malicious patterns
     */
    private fun containsMaliciousPatterns(input: String): Boolean {
        val maliciousPatterns = listOf(
            Regex("<script", RegexOption.IGNORE_CASE),
            Regex("javascript:", RegexOption.IGNORE_CASE),
            Regex("onload=", RegexOption.IGNORE_CASE),
            Regex("onerror=", RegexOption.IGNORE_CASE),
            Regex("eval\\(", RegexOption.IGNORE_CASE),
            Regex("exec\\(", RegexOption.IGNORE_CASE)
        )
        
        return maliciousPatterns.any { it.containsMatchIn(input) }
    }
    
    /**
     * Encrypt sensitive data using AES-256-GCM
     */
    suspend fun encryptData(data: String, key: javax.crypto.SecretKey): String {
        return try {
            EncryptionUtils.encryptData(data, key)
        } catch (e: Exception) {
            Logger.error("Data encryption failed", "SecurityUtils", e)
            throw SecurityException("Failed to encrypt sensitive data", e)
        }
    }
    
    /**
     * Decrypt sensitive data using AES-256-GCM
     */
    suspend fun decryptData(encryptedData: String, key: javax.crypto.SecretKey): String {
        return try {
            EncryptionUtils.decryptData(encryptedData, key)
        } catch (e: Exception) {
            Logger.error("Data decryption failed", "SecurityUtils", e)
            throw SecurityException("Failed to decrypt sensitive data", e)
        }
    }
    
    /**
     * Hash password using PBKDF2 with salt
     */
    fun hashPassword(password: String, salt: ByteArray): String {
        return try {
            EncryptionUtils.hashPassword(password, salt)
        } catch (e: Exception) {
            Logger.error("Password hashing failed", "SecurityUtils", e)
            throw SecurityException("Failed to hash password", e)
        }
    }
    
    /**
     * Verify password against hash
     */
    fun verifyPassword(password: String, hash: String, salt: ByteArray): Boolean {
        return try {
            EncryptionUtils.verifyPassword(password, hash, salt)
        } catch (e: Exception) {
            Logger.error("Password verification failed", "SecurityUtils", e)
            false
        }
    }
    
    /**
     * Verify password using hash comparison
     */
    fun verifyPasswordHash(password: String, hashedPassword: String, salt: ByteArray): Boolean {
        return hashPassword(password, salt) == hashedPassword
    }
    
    /**
     * Generate secure random string
     */
    fun generateSecureRandom(length: Int = 32): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..length)
            .map { chars.random() }
            .joinToString("")
    }
    
    /**
     * Generate secure token
     */
    fun generateSecureToken(): String {
        return generateSecureRandom(64)
    }
    
    /**
     * Validate email format
     */
    fun validateEmail(email: String): Boolean {
        val emailRegex = Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
        return emailRegex.matches(email)
    }
    
    /**
     * Validate phone number format
     */
    fun validatePhoneNumber(phone: String): Boolean {
        val phoneRegex = Regex("^[+]?[0-9\\s\\-()]{10,15}$")
        return phoneRegex.matches(phone)
    }
    
    /**
     * Check password strength
     */
    fun checkPasswordStrength(password: String): PasswordStrength {
        return when {
            password.length < 8 -> PasswordStrength.WEAK
            password.length < 12 && !hasUpperCase(password) -> PasswordStrength.WEAK
            password.length < 12 && !hasLowerCase(password) -> PasswordStrength.WEAK
            password.length < 12 && !hasNumbers(password) -> PasswordStrength.WEAK
            password.length < 12 && !hasSpecialChars(password) -> PasswordStrength.WEAK
            password.length < 16 -> PasswordStrength.MEDIUM
            else -> PasswordStrength.STRONG
        }
    }
    
    private fun hasUpperCase(password: String): Boolean {
        return password.any { it.isUpperCase() }
    }
    
    private fun hasLowerCase(password: String): Boolean {
        return password.any { it.isLowerCase() }
    }
    
    private fun hasNumbers(password: String): Boolean {
        return password.any { it.isDigit() }
    }
    
    private fun hasSpecialChars(password: String): Boolean {
        return password.any { "!@#$%^&*(),.?\":{}|<>".contains(it) }
    }
    
    /**
     * Password strength enum
     */
    enum class PasswordStrength {
        WEAK, MEDIUM, STRONG
    }
    
    /**
     * Rate limiting for API calls
     */
    class RateLimiter(private val maxRequests: Int, private val timeWindowMs: Long) {
        private val requests = mutableListOf<Long>()
        
        fun isAllowed(): Boolean {
            val now = System.currentTimeMillis()
            requests.removeAll { now - it > timeWindowMs }
            
            return if (requests.size < maxRequests) {
                requests.add(now)
                true
            } else {
                false
            }
        }
    }
    
    /**
     * Input validation rules
     */
    object InputValidation {
        
        /**
         * Validate transaction title
         */
        fun validateTransactionTitle(title: String): Boolean {
            return title.isNotBlank() && 
                   title.length <= 100 && 
                   !containsMaliciousPatterns(title)
        }
        
        /**
         * Validate transaction amount
         */
        fun validateTransactionAmount(amount: String): Boolean {
            return amount.isNotBlank() && 
                   amount.toDoubleOrNull() != null && 
                   amount.toDouble() > 0 && 
                   amount.toDouble() <= 999999.99
        }
        
        /**
         * Validate account name
         */
        fun validateAccountName(name: String): Boolean {
            return name.isNotBlank() && 
                   name.length <= 50 && 
                   !containsMaliciousPatterns(name)
        }
        
        /**
         * Validate group name
         */
        fun validateGroupName(name: String): Boolean {
            return name.isNotBlank() && 
                   name.length <= 50 && 
                   !containsMaliciousPatterns(name)
        }
        
        /**
         * Validate person name
         */
        fun validatePersonName(name: String): Boolean {
            return name.isNotBlank() && 
                   name.length <= 50 && 
                   !containsMaliciousPatterns(name)
        }
        
        /**
         * Validate description
         */
        fun validateDescription(description: String): Boolean {
            return description.length <= 500 && 
                   !containsMaliciousPatterns(description)
        }
    }
    
    /**
     * Data sanitization
     */
    object DataSanitization {
        
        /**
         * Sanitize transaction data
         */
        fun sanitizeTransaction(transaction: com.example.androidkmm.models.Transaction): com.example.androidkmm.models.Transaction {
            return transaction.copy(
                title = sanitizeInput(transaction.title),
                description = sanitizeInput(transaction.description),
                category = sanitizeInput(transaction.category),
                account = sanitizeInput(transaction.account)
            )
        }
        
        /**
         * Sanitize account data
         */
        fun sanitizeAccount(account: com.example.androidkmm.models.Account): com.example.androidkmm.models.Account {
            return account.copy(
                name = sanitizeInput(account.name),
                type = sanitizeInput(account.type)
            )
        }
        
        /**
         * Sanitize group data
         */
        fun sanitizeGroup(group: com.example.androidkmm.models.Group): com.example.androidkmm.models.Group {
            return group.copy(
                name = sanitizeInput(group.name),
                description = sanitizeInput(group.description)
            )
        }
        
    }
    
    /**
     * Security logging
     */
    object SecurityLogging {
        
        /**
         * Log security event
         */
        fun logSecurityEvent(event: String, details: String = "") {
            Logger.warning("Security Event: $event - $details", "SecurityUtils")
        }
        
        /**
         * Log suspicious activity
         */
        fun logSuspiciousActivity(activity: String, details: String = "") {
            Logger.error("Suspicious Activity: $activity - $details", "SecurityUtils")
        }
        
        /**
         * Log authentication attempt
         */
        fun logAuthenticationAttempt(success: Boolean, details: String = "") {
            val status = if (success) "SUCCESS" else "FAILED"
            Logger.info("Authentication Attempt: $status - $details", "SecurityUtils")
        }
    }
    
    /**
     * Security configuration
     */
    object SecurityConfig {
        
        /**
         * Maximum input length
         */
        const val MAX_INPUT_LENGTH = 1000
        
        /**
         * Maximum transaction amount
         */
        const val MAX_TRANSACTION_AMOUNT = 999999.99
        
        /**
         * Minimum password length
         */
        const val MIN_PASSWORD_LENGTH = 8
        
        /**
         * Maximum login attempts
         */
        const val MAX_LOGIN_ATTEMPTS = 5
        
        /**
         * Session timeout (in milliseconds)
         */
        const val SESSION_TIMEOUT = 30 * 60 * 1000L // 30 minutes
        
        /**
         * Rate limiting configuration
         */
        const val RATE_LIMIT_REQUESTS = 100
        const val RATE_LIMIT_WINDOW_MS = 60 * 1000L // 1 minute
    }
}
