package com.example.androidkmm.validation

import com.example.androidkmm.security.InputValidator
import com.example.androidkmm.utils.Logger

/**
 * Centralized validation manager
 * 
 * Consolidates all validation logic to eliminate duplication.
 * Provides a single source of truth for all validation rules.
 */
object ValidationManager {
    
    /**
     * Validation rule interface
     */
    interface ValidationRule<T> {
        fun validate(value: T): ValidationResult
    }
    
    /**
     * Validation result
     */
    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null,
        val sanitizedValue: String? = null
    )
    
    /**
     * Validation context
     */
    data class ValidationContext(
        val fieldName: String,
        val isRequired: Boolean = true,
        val maxLength: Int = 1000,
        val allowHtml: Boolean = false
    )
    
    // Predefined validation rules
    object AmountValidationRule : ValidationRule<String> {
        override fun validate(value: String): ValidationResult {
            val result = InputValidator.validateAmount(value)
            return ValidationResult(
                isValid = result.isValid,
                errorMessage = result.errorMessage,
                sanitizedValue = result.sanitizedValue
            )
        }
    }
    
    object TitleValidationRule : ValidationRule<String> {
        override fun validate(value: String): ValidationResult {
            val result = InputValidator.validateTextInput(
                input = value,
                fieldName = "Title",
                maxLength = 100,
                allowHtml = false
            )
            return ValidationResult(
                isValid = result.isValid,
                errorMessage = result.errorMessage,
                sanitizedValue = result.sanitizedValue
            )
        }
    }
    
    object DescriptionValidationRule : ValidationRule<String> {
        override fun validate(value: String): ValidationResult {
            val result = InputValidator.validateTextInput(
                input = value,
                fieldName = "Description",
                maxLength = 500,
                allowHtml = false
            )
            return ValidationResult(
                isValid = result.isValid,
                errorMessage = result.errorMessage,
                sanitizedValue = result.sanitizedValue
            )
        }
    }
    
    object EmailValidationRule : ValidationRule<String> {
        override fun validate(value: String): ValidationResult {
            val result = InputValidator.validateEmail(value)
            return ValidationResult(
                isValid = result.isValid,
                errorMessage = result.errorMessage,
                sanitizedValue = result.sanitizedValue
            )
        }
    }
    
    object PhoneValidationRule : ValidationRule<String> {
        override fun validate(value: String): ValidationResult {
            val result = InputValidator.validatePhoneNumber(value)
            return ValidationResult(
                isValid = result.isValid,
                errorMessage = result.errorMessage,
                sanitizedValue = result.sanitizedValue
            )
        }
    }
    
    object PasswordValidationRule : ValidationRule<String> {
        override fun validate(value: String): ValidationResult {
            val result = InputValidator.validatePassword(value)
            return ValidationResult(
                isValid = result.isValid,
                errorMessage = result.errorMessage,
                sanitizedValue = result.sanitizedValue
            )
        }
    }
    
    object AccountNameValidationRule : ValidationRule<String> {
        override fun validate(value: String): ValidationResult {
            val result = InputValidator.validateTextInput(
                input = value,
                fieldName = "Account Name",
                maxLength = 50,
                allowHtml = false
            )
            return ValidationResult(
                isValid = result.isValid,
                errorMessage = result.errorMessage,
                sanitizedValue = result.sanitizedValue
            )
        }
    }
    
    object CategoryNameValidationRule : ValidationRule<String> {
        override fun validate(value: String): ValidationResult {
            val result = InputValidator.validateTextInput(
                input = value,
                fieldName = "Category Name",
                maxLength = 50,
                allowHtml = false
            )
            return ValidationResult(
                isValid = result.isValid,
                errorMessage = result.errorMessage,
                sanitizedValue = result.sanitizedValue
            )
        }
    }
    
    object GroupNameValidationRule : ValidationRule<String> {
        override fun validate(value: String): ValidationResult {
            val result = InputValidator.validateTextInput(
                input = value,
                fieldName = "Group Name",
                maxLength = 50,
                allowHtml = false
            )
            return ValidationResult(
                isValid = result.isValid,
                errorMessage = result.errorMessage,
                sanitizedValue = result.sanitizedValue
            )
        }
    }
    
    object PersonNameValidationRule : ValidationRule<String> {
        override fun validate(value: String): ValidationResult {
            val result = InputValidator.validateTextInput(
                input = value,
                fieldName = "Person Name",
                maxLength = 50,
                allowHtml = false
            )
            return ValidationResult(
                isValid = result.isValid,
                errorMessage = result.errorMessage,
                sanitizedValue = result.sanitizedValue
            )
        }
    }
    
    /**
     * Generic text validation rule
     */
    class TextValidationRule(
        private val context: ValidationContext
    ) : ValidationRule<String> {
        override fun validate(value: String): ValidationResult {
            val result = InputValidator.validateTextInput(
                input = value,
                fieldName = context.fieldName,
                maxLength = context.maxLength,
                allowHtml = context.allowHtml
            )
            return ValidationResult(
                isValid = result.isValid,
                errorMessage = result.errorMessage,
                sanitizedValue = result.sanitizedValue
            )
        }
    }
    
    /**
     * Required field validation rule
     */
    class RequiredValidationRule<T>(
        private val fieldName: String
    ) : ValidationRule<T> {
        override fun validate(value: T): ValidationResult {
            return when {
                value == null -> ValidationResult(
                    isValid = false,
                    errorMessage = "$fieldName is required"
                )
                value is String && value.isBlank() -> ValidationResult(
                    isValid = false,
                    errorMessage = "$fieldName is required"
                )
                else -> ValidationResult(isValid = true)
            }
        }
    }
    
    /**
     * Length validation rule
     */
    class LengthValidationRule(
        private val minLength: Int,
        private val maxLength: Int,
        private val fieldName: String
    ) : ValidationRule<String> {
        override fun validate(value: String): ValidationResult {
            return when {
                value.length < minLength -> ValidationResult(
                    isValid = false,
                    errorMessage = "$fieldName must be at least $minLength characters"
                )
                value.length > maxLength -> ValidationResult(
                    isValid = false,
                    errorMessage = "$fieldName must be no more than $maxLength characters"
                )
                else -> ValidationResult(isValid = true)
            }
        }
    }
    
    /**
     * Pattern validation rule
     */
    class PatternValidationRule(
        private val pattern: Regex,
        private val fieldName: String,
        private val errorMessage: String
    ) : ValidationRule<String> {
        override fun validate(value: String): ValidationResult {
            return if (pattern.matches(value)) {
                ValidationResult(isValid = true)
            } else {
                ValidationResult(
                    isValid = false,
                    errorMessage = errorMessage
                )
            }
        }
    }
    
    /**
     * Range validation rule for numbers
     */
    class RangeValidationRule(
        private val min: Double,
        private val max: Double,
        private val fieldName: String
    ) : ValidationRule<String> {
        override fun validate(value: String): ValidationResult {
            val numericValue = value.toDoubleOrNull()
            return when {
                numericValue == null -> ValidationResult(
                    isValid = false,
                    errorMessage = "$fieldName must be a valid number"
                )
                numericValue < min -> ValidationResult(
                    isValid = false,
                    errorMessage = "$fieldName must be at least $min"
                )
                numericValue > max -> ValidationResult(
                    isValid = false,
                    errorMessage = "$fieldName must be no more than $max"
                )
                else -> ValidationResult(isValid = true)
            }
        }
    }
    
    /**
     * Composite validation rule
     */
    class CompositeValidationRule<T>(
        private val rules: List<ValidationRule<T>>
    ) : ValidationRule<T> {
        override fun validate(value: T): ValidationResult {
            for (rule in rules) {
                val result = rule.validate(value)
                if (!result.isValid) {
                    return result
                }
            }
            return ValidationResult(isValid = true)
        }
    }
    
    /**
     * Validate with multiple rules
     */
    fun <T> validateWithRules(
        value: T,
        rules: List<ValidationRule<T>>
    ): ValidationResult {
        return CompositeValidationRule(rules).validate(value)
    }
    
    /**
     * Validate transaction form
     */
    fun validateTransactionForm(
        amount: String,
        title: String,
        description: String = "",
        account: String = "",
        category: String = ""
    ): Map<String, ValidationResult> {
        return mapOf(
            "amount" to AmountValidationRule.validate(amount),
            "title" to TitleValidationRule.validate(title),
            "description" to DescriptionValidationRule.validate(description),
            "account" to if (account.isNotBlank()) AccountNameValidationRule.validate(account) else ValidationResult(isValid = true),
            "category" to if (category.isNotBlank()) CategoryNameValidationRule.validate(category) else ValidationResult(isValid = true)
        )
    }
    
    /**
     * Validate group form
     */
    fun validateGroupForm(
        name: String,
        description: String = ""
    ): Map<String, ValidationResult> {
        return mapOf(
            "name" to GroupNameValidationRule.validate(name),
            "description" to DescriptionValidationRule.validate(description)
        )
    }
    
    /**
     * Validate ledger person form
     */
    fun validateLedgerPersonForm(
        name: String,
        email: String = "",
        phone: String = ""
    ): Map<String, ValidationResult> {
        return mapOf(
            "name" to PersonNameValidationRule.validate(name),
            "email" to if (email.isNotBlank()) EmailValidationRule.validate(email) else ValidationResult(isValid = true),
            "phone" to if (phone.isNotBlank()) PhoneValidationRule.validate(phone) else ValidationResult(isValid = true)
        )
    }
    
    /**
     * Validate user settings form
     */
    fun validateUserSettingsForm(
        name: String,
        email: String = "",
        phone: String = ""
    ): Map<String, ValidationResult> {
        return mapOf(
            "name" to PersonNameValidationRule.validate(name),
            "email" to if (email.isNotBlank()) EmailValidationRule.validate(email) else ValidationResult(isValid = true),
            "phone" to if (phone.isNotBlank()) PhoneValidationRule.validate(phone) else ValidationResult(isValid = true)
        )
    }
    
    /**
     * Check if all validations pass
     */
    fun isFormValid(validationResults: Map<String, ValidationResult>): Boolean {
        return validationResults.values.all { it.isValid }
    }
    
    /**
     * Get error messages for invalid fields
     */
    fun getErrorMessages(validationResults: Map<String, ValidationResult>): Map<String, String> {
        return validationResults
            .filter { !it.value.isValid }
            .mapValues { it.value.errorMessage ?: "Invalid ${it.key}" }
    }
    
    /**
     * Get sanitized values
     */
    fun getSanitizedValues(validationResults: Map<String, ValidationResult>): Map<String, String> {
        return validationResults
            .filter { it.value.isValid }
            .mapValues { it.value.sanitizedValue ?: "" }
    }
}
