package com.example.androidkmm.utils

import java.util.regex.Pattern

/**
 * Input Sanitization Utilities
 * 
 * Provides comprehensive input sanitization and validation for security and data integrity.
 * 
 * Features:
 * - Text sanitization (removes HTML/XML characters, control characters)
 * - Numeric input sanitization with decimal handling
 * - Email and phone number validation
 * - Account and category name sanitization
 * - Description and title sanitization with length limits
 * - Dangerous content detection
 * - Validation with user-friendly error messages
 * 
 * Security Benefits:
 * - Prevents XSS attacks by removing script tags
 * - Prevents SQL injection by sanitizing input
 * - Removes control characters that could cause issues
 * - Validates data format before processing
 * 
 * Usage:
 * ```kotlin
 * val sanitizedAmount = InputSanitizer.sanitizeAmount(userInput)
 * val (validatedTitle, error) = InputSanitizer.validateAndSanitizeTitle(title)
 * ```
 */
object InputSanitizer {
    
    /**
     * Sanitize text input by removing potentially dangerous characters
     */
    fun sanitizeText(input: String): String {
        return input
            .trim()
            .replace(Regex("[<>\"'&]"), "") // Remove HTML/XML characters
            .replace(Regex("[\\x00-\\x1F\\x7F]"), "") // Remove control characters
            .replace(Regex("\\s+"), " ") // Replace multiple spaces with single space
    }
    
    /**
     * Sanitize numeric input
     */
    fun sanitizeNumeric(input: String): String {
        return input
            .trim()
            .replace(Regex("[^0-9.]"), "") // Keep only digits and decimal point
            .replace(Regex("\\.{2,}"), ".") // Replace multiple dots with single dot
            .replace(Regex("^\\.+"), "") // Remove leading dots
            .replace(Regex("\\.+$"), "") // Remove trailing dots
    }
    
    /**
     * Sanitize amount input with proper decimal handling
     */
    fun sanitizeAmount(input: String): String {
        val sanitized = sanitizeNumeric(input)
        return if (sanitized.contains(".")) {
            val parts = sanitized.split(".")
            if (parts.size > 2) {
                // If multiple decimal points, keep only the first one
                parts[0] + "." + parts.drop(1).joinToString("")
            } else {
                // Limit to 2 decimal places
                val integerPart = parts[0]
                val decimalPart = parts[1].take(2)
                "$integerPart.$decimalPart"
            }
        } else {
            sanitized
        }
    }
    
    /**
     * Sanitize email input
     */
    fun sanitizeEmail(input: String): String {
        return input
            .trim()
            .lowercase()
            .replace(Regex("[^a-z0-9@._-]"), "") // Keep only valid email characters
    }
    
    /**
     * Sanitize phone number input
     */
    fun sanitizePhone(input: String): String {
        return input
            .trim()
            .replace(Regex("[^0-9+\\-()\\s]"), "") // Keep only valid phone characters
            .replace(Regex("\\s+"), " ") // Replace multiple spaces with single space
    }
    
    /**
     * Sanitize account name
     */
    fun sanitizeAccountName(input: String): String {
        return input
            .trim()
            .replace(Regex("[^a-zA-Z0-9\\s._-]"), "") // Keep only alphanumeric and safe characters
            .replace(Regex("\\s+"), " ") // Replace multiple spaces with single space
            .take(50) // Limit length
    }
    
    /**
     * Sanitize category name
     */
    fun sanitizeCategoryName(input: String): String {
        return input
            .trim()
            .replace(Regex("[^a-zA-Z0-9\\s._-]"), "") // Keep only alphanumeric and safe characters
            .replace(Regex("\\s+"), " ") // Replace multiple spaces with single space
            .take(30) // Limit length
    }
    
    /**
     * Sanitize description text
     */
    fun sanitizeDescription(input: String): String {
        return input
            .trim()
            .replace(Regex("[<>\"'&]"), "") // Remove HTML/XML characters
            .replace(Regex("[\\x00-\\x1F\\x7F]"), "") // Remove control characters
            .replace(Regex("\\s+"), " ") // Replace multiple spaces with single space
            .take(500) // Limit length
    }
    
    /**
     * Sanitize transaction title
     */
    fun sanitizeTitle(input: String): String {
        return input
            .trim()
            .replace(Regex("[<>\"'&]"), "") // Remove HTML/XML characters
            .replace(Regex("[\\x00-\\x1F\\x7F]"), "") // Remove control characters
            .replace(Regex("\\s+"), " ") // Replace multiple spaces with single space
            .take(100) // Limit length
    }
    
    /**
     * Validate and sanitize amount
     */
    fun validateAndSanitizeAmount(input: String): Pair<String, String?> {
        val sanitized = sanitizeAmount(input)
        
        return when {
            sanitized.isEmpty() -> Pair("", "Amount is required")
            sanitized.toDoubleOrNull() == null -> Pair(sanitized, "Invalid amount format")
            sanitized.toDoubleOrNull()!! <= 0 -> Pair(sanitized, "Amount must be positive")
            sanitized.toDoubleOrNull()!! > 9999999999.99 -> Pair(sanitized, "Amount is too large")
            else -> Pair(sanitized, null)
        }
    }
    
    /**
     * Validate and sanitize title
     */
    fun validateAndSanitizeTitle(input: String): Pair<String, String?> {
        val sanitized = sanitizeTitle(input)
        
        return when {
            sanitized.isEmpty() -> Pair(sanitized, "Title is required")
            sanitized.length < 2 -> Pair(sanitized, "Title must be at least 2 characters")
            sanitized.length > 100 -> Pair(sanitized, "Title is too long")
            else -> Pair(sanitized, null)
        }
    }
    
    /**
     * Validate and sanitize email
     */
    fun validateAndSanitizeEmail(input: String): Pair<String, String?> {
        val sanitized = sanitizeEmail(input)
        val emailPattern = Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$")
        
        return when {
            sanitized.isEmpty() -> Pair(sanitized, "Email is required")
            !emailPattern.matcher(sanitized).matches() -> Pair(sanitized, "Invalid email format")
            else -> Pair(sanitized, null)
        }
    }
    
    /**
     * Check if input contains potentially dangerous content
     */
    fun containsDangerousContent(input: String): Boolean {
        val dangerousPatterns = listOf(
            Regex("<script", RegexOption.IGNORE_CASE),
            Regex("javascript:", RegexOption.IGNORE_CASE),
            Regex("on\\w+\\s*=", RegexOption.IGNORE_CASE),
            Regex("eval\\s*\\("),
            Regex("expression\\s*\\(")
        )
        
        return dangerousPatterns.any { it.containsMatchIn(input) }
    }
    
    /**
     * Sanitize input and check for dangerous content
     */
    fun sanitizeAndCheck(input: String): Pair<String, Boolean> {
        val sanitized = sanitizeText(input)
        val isDangerous = containsDangerousContent(input)
        return Pair(sanitized, isDangerous)
    }
}
