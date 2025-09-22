package com.example.androidkmm.security

import com.example.androidkmm.utils.Logger

/**
 * SQL Security Audit utility
 * 
 * Provides security auditing for database operations to prevent SQL injection.
 * Validates that all database operations use parameterized queries.
 */
object SQLSecurityAudit {
    
    /**
     * Audit database operation for security vulnerabilities
     */
    fun auditDatabaseOperation(
        operation: String,
        parameters: Map<String, Any?> = emptyMap(),
        context: String = "Database"
    ): SecurityAuditResult {
        return try {
            // Check for potential SQL injection patterns
            val injectionPatterns = listOf(
                Regex("'.*'.*or.*'.*'", RegexOption.IGNORE_CASE),
                Regex("'.*'.*and.*'.*'", RegexOption.IGNORE_CASE),
                Regex("union.*select", RegexOption.IGNORE_CASE),
                Regex("drop.*table", RegexOption.IGNORE_CASE),
                Regex("delete.*from", RegexOption.IGNORE_CASE),
                Regex("insert.*into", RegexOption.IGNORE_CASE),
                Regex("update.*set", RegexOption.IGNORE_CASE),
                Regex("exec\\s*\\(", RegexOption.IGNORE_CASE),
                Regex("execute\\s*\\(", RegexOption.IGNORE_CASE)
            )
            
            // Check operation string for suspicious patterns
            val operationSuspicious = injectionPatterns.any { it.containsMatchIn(operation) }
            
            // Check parameters for suspicious values
            val parameterSuspicious = parameters.values.any { value ->
                value?.toString()?.let { str ->
                    injectionPatterns.any { it.containsMatchIn(str) }
                } ?: false
            }
            
            when {
                operationSuspicious -> {
                    Logger.error("Suspicious SQL operation detected: $operation", "SQLSecurityAudit")
                    SecurityAuditResult(
                        isSecure = false,
                        riskLevel = SecurityRiskLevel.HIGH,
                        message = "Operation contains potentially malicious SQL patterns",
                        recommendations = listOf("Use parameterized queries", "Validate input parameters")
                    )
                }
                parameterSuspicious -> {
                    Logger.warning("Suspicious parameter values detected", "SQLSecurityAudit")
                    SecurityAuditResult(
                        isSecure = false,
                        riskLevel = SecurityRiskLevel.MEDIUM,
                        message = "Parameters contain potentially malicious values",
                        recommendations = listOf("Sanitize input parameters", "Use parameterized queries")
                    )
                }
                else -> {
                    SecurityAuditResult(
                        isSecure = true,
                        riskLevel = SecurityRiskLevel.LOW,
                        message = "Operation appears secure",
                        recommendations = emptyList()
                    )
                }
            }
        } catch (e: Exception) {
            Logger.error("Security audit failed", "SQLSecurityAudit", e)
            SecurityAuditResult(
                isSecure = false,
                riskLevel = SecurityRiskLevel.HIGH,
                message = "Security audit failed: ${e.message}",
                recommendations = listOf("Review operation implementation", "Contact security team")
            )
        }
    }
    
    /**
     * Validate parameter values for security
     */
    fun validateParameterValue(
        parameterName: String,
        value: Any?,
        expectedType: ParameterType = ParameterType.STRING
    ): ParameterValidationResult {
        return try {
            when (expectedType) {
                ParameterType.STRING -> validateStringParameter(parameterName, value)
                ParameterType.NUMBER -> validateNumberParameter(parameterName, value)
                ParameterType.DATE -> validateDateParameter(parameterName, value)
                ParameterType.BOOLEAN -> validateBooleanParameter(parameterName, value)
                ParameterType.ID -> validateIdParameter(parameterName, value)
            }
        } catch (e: Exception) {
            Logger.error("Parameter validation failed for $parameterName", "SQLSecurityAudit", e)
            ParameterValidationResult(
                isValid = false,
                message = "Parameter validation failed: ${e.message}",
                sanitizedValue = null
            )
        }
    }
    
    /**
     * Validate string parameter
     */
    private fun validateStringParameter(parameterName: String, value: Any?): ParameterValidationResult {
        if (value == null) {
            return ParameterValidationResult(
                isValid = false,
                message = "Parameter $parameterName cannot be null",
                sanitizedValue = null
            )
        }
        
        val stringValue = value.toString()
        
        // Check for SQL injection patterns
        val injectionPatterns = listOf(
            Regex("'.*'.*or.*'.*'", RegexOption.IGNORE_CASE),
            Regex("'.*'.*and.*'.*'", RegexOption.IGNORE_CASE),
            Regex("union.*select", RegexOption.IGNORE_CASE),
            Regex("drop.*table", RegexOption.IGNORE_CASE),
            Regex("delete.*from", RegexOption.IGNORE_CASE),
            Regex("insert.*into", RegexOption.IGNORE_CASE),
            Regex("update.*set", RegexOption.IGNORE_CASE),
            Regex("exec\\s*\\(", RegexOption.IGNORE_CASE),
            Regex("execute\\s*\\(", RegexOption.IGNORE_CASE)
        )
        
        val containsInjection = injectionPatterns.any { it.containsMatchIn(stringValue) }
        
        return if (containsInjection) {
            ParameterValidationResult(
                isValid = false,
                message = "Parameter $parameterName contains potentially malicious content",
                sanitizedValue = null
            )
        } else {
            val sanitized = sanitizeString(stringValue)
            ParameterValidationResult(
                isValid = true,
                message = "Parameter $parameterName is valid",
                sanitizedValue = sanitized
            )
        }
    }
    
    /**
     * Validate number parameter
     */
    private fun validateNumberParameter(parameterName: String, value: Any?): ParameterValidationResult {
        if (value == null) {
            return ParameterValidationResult(
                isValid = false,
                message = "Parameter $parameterName cannot be null",
                sanitizedValue = null
            )
        }
        
        return try {
            val numberValue = when (value) {
                is Number -> value
                is String -> value.toDoubleOrNull() ?: throw IllegalArgumentException("Invalid number format")
                else -> throw IllegalArgumentException("Invalid number type")
            }
            
            ParameterValidationResult(
                isValid = true,
                message = "Parameter $parameterName is valid",
                sanitizedValue = numberValue
            )
        } catch (e: Exception) {
            ParameterValidationResult(
                isValid = false,
                message = "Parameter $parameterName is not a valid number: ${e.message}",
                sanitizedValue = null
            )
        }
    }
    
    /**
     * Validate date parameter
     */
    private fun validateDateParameter(parameterName: String, value: Any?): ParameterValidationResult {
        if (value == null) {
            return ParameterValidationResult(
                isValid = false,
                message = "Parameter $parameterName cannot be null",
                sanitizedValue = null
            )
        }
        
        return try {
            val dateValue = value.toString()
            // Basic date format validation (YYYY-MM-DD)
            val datePattern = Regex("^\\d{4}-\\d{2}-\\d{2}$")
            
            if (!datePattern.matches(dateValue)) {
                ParameterValidationResult(
                    isValid = false,
                    message = "Parameter $parameterName is not in valid date format (YYYY-MM-DD)",
                    sanitizedValue = null
                )
            } else {
                ParameterValidationResult(
                    isValid = true,
                    message = "Parameter $parameterName is valid",
                    sanitizedValue = dateValue
                )
            }
        } catch (e: Exception) {
            ParameterValidationResult(
                isValid = false,
                message = "Parameter $parameterName is not a valid date: ${e.message}",
                sanitizedValue = null
            )
        }
    }
    
    /**
     * Validate boolean parameter
     */
    private fun validateBooleanParameter(parameterName: String, value: Any?): ParameterValidationResult {
        if (value == null) {
            return ParameterValidationResult(
                isValid = false,
                message = "Parameter $parameterName cannot be null",
                sanitizedValue = null
            )
        }
        
        return try {
            val booleanValue = when (value) {
                is Boolean -> value
                is String -> value.toBooleanStrictOrNull() ?: throw IllegalArgumentException("Invalid boolean format")
                is Number -> value.toInt() != 0
                else -> throw IllegalArgumentException("Invalid boolean type")
            }
            
            ParameterValidationResult(
                isValid = true,
                message = "Parameter $parameterName is valid",
                sanitizedValue = booleanValue
            )
        } catch (e: Exception) {
            ParameterValidationResult(
                isValid = false,
                message = "Parameter $parameterName is not a valid boolean: ${e.message}",
                sanitizedValue = null
            )
        }
    }
    
    /**
     * Validate ID parameter
     */
    private fun validateIdParameter(parameterName: String, value: Any?): ParameterValidationResult {
        if (value == null) {
            return ParameterValidationResult(
                isValid = false,
                message = "Parameter $parameterName cannot be null",
                sanitizedValue = null
            )
        }
        
        val idValue = value.toString()
        
        // Check for valid ID format (alphanumeric, hyphens, underscores)
        val idPattern = Regex("^[a-zA-Z0-9_-]+$")
        
        return if (!idPattern.matches(idValue)) {
            ParameterValidationResult(
                isValid = false,
                message = "Parameter $parameterName is not a valid ID format",
                sanitizedValue = null
            )
        } else {
            ParameterValidationResult(
                isValid = true,
                message = "Parameter $parameterName is valid",
                sanitizedValue = idValue
            )
        }
    }
    
    /**
     * Sanitize string value
     */
    private fun sanitizeString(value: String): String {
        return value
            .replace(Regex("[<>\"'&]"), "")
            .trim()
            .take(1000) // Limit length
    }
}

/**
 * Security audit result
 */
data class SecurityAuditResult(
    val isSecure: Boolean,
    val riskLevel: SecurityRiskLevel,
    val message: String,
    val recommendations: List<String>
)

/**
 * Parameter validation result
 */
data class ParameterValidationResult(
    val isValid: Boolean,
    val message: String,
    val sanitizedValue: Any?
)

/**
 * Security risk levels
 */
enum class SecurityRiskLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

/**
 * Parameter types
 */
enum class ParameterType {
    STRING,
    NUMBER,
    DATE,
    BOOLEAN,
    ID
}
