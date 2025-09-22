package com.example.androidkmm.validation

import com.example.androidkmm.security.InputValidator
import com.example.androidkmm.utils.Logger

/**
 * Comprehensive form validation system
 * 
 * Features:
 * - Real-time validation
 * - Field-specific validation rules
 * - Cross-field validation
 * - Security validation
 * - User-friendly error messages
 * - Validation state management
 */
object FormValidator {
    
    /**
     * Validation result for form fields
     */
    data class FieldValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null,
        val sanitizedValue: String? = null
    )
    
    /**
     * Form validation result
     */
    data class FormValidationResult(
        val isValid: Boolean,
        val fieldErrors: Map<String, String> = emptyMap(),
        val globalErrors: List<String> = emptyList()
    )
    
    /**
     * Validate transaction form
     */
    fun validateTransactionForm(
        amount: String,
        title: String,
        description: String = "",
        account: String = "",
        category: String = "",
        type: com.example.androidkmm.models.TransactionType
    ): FormValidationResult {
        val fieldErrors = mutableMapOf<String, String>()
        
        // Validate amount
        val amountResult = validateAmount(amount)
        if (!amountResult.isValid) {
            fieldErrors["amount"] = amountResult.errorMessage ?: "Invalid amount"
        }
        
        // Validate title
        val titleResult = validateTitle(title)
        if (!titleResult.isValid) {
            fieldErrors["title"] = titleResult.errorMessage ?: "Invalid title"
        }
        
        // Validate description
        val descriptionResult = validateDescription(description)
        if (!descriptionResult.isValid) {
            fieldErrors["description"] = descriptionResult.errorMessage ?: "Invalid description"
        }
        
        // Validate account
        if (account.isNotBlank()) {
            val accountResult = validateAccount(account)
            if (!accountResult.isValid) {
                fieldErrors["account"] = accountResult.errorMessage ?: "Invalid account"
            }
        }
        
        // Validate category
        if (category.isNotBlank()) {
            val categoryResult = validateCategory(category)
            if (!categoryResult.isValid) {
                fieldErrors["category"] = categoryResult.errorMessage ?: "Invalid category"
            }
        }
        
        // Cross-field validation
        val globalErrors = mutableListOf<String>()
        
        // Validate business rules
        when (type) {
            com.example.androidkmm.models.TransactionType.TRANSFER -> {
                if (account.isBlank()) {
                    globalErrors.add("Source account is required for transfers")
                }
            }
            com.example.androidkmm.models.TransactionType.INCOME -> {
                if (account.isBlank()) {
                    globalErrors.add("Account is required for income transactions")
                }
            }
            com.example.androidkmm.models.TransactionType.EXPENSE -> {
                if (account.isBlank()) {
                    globalErrors.add("Account is required for expense transactions")
                }
            }
        }
        
        return FormValidationResult(
            isValid = fieldErrors.isEmpty() && globalErrors.isEmpty(),
            fieldErrors = fieldErrors,
            globalErrors = globalErrors
        )
    }
    
    /**
     * Validate amount field
     */
    fun validateAmount(amount: String): FieldValidationResult {
        return try {
            val result = InputValidator.validateAmount(amount)
            FieldValidationResult(
                isValid = result.isValid,
                errorMessage = result.errorMessage,
                sanitizedValue = result.sanitizedValue
            )
        } catch (e: Exception) {
            Logger.error("Amount validation failed", "FormValidator", e)
            FieldValidationResult(
                isValid = false,
                errorMessage = "Amount validation failed"
            )
        }
    }
    
    /**
     * Validate title field
     */
    fun validateTitle(title: String): FieldValidationResult {
        return try {
            val result = InputValidator.validateTextInput(
                input = title,
                fieldName = "Title",
                maxLength = 100,
                allowHtml = false
            )
            FieldValidationResult(
                isValid = result.isValid,
                errorMessage = result.errorMessage,
                sanitizedValue = result.sanitizedValue
            )
        } catch (e: Exception) {
            Logger.error("Title validation failed", "FormValidator", e)
            FieldValidationResult(
                isValid = false,
                errorMessage = "Title validation failed"
            )
        }
    }
    
    /**
     * Validate description field
     */
    fun validateDescription(description: String): FieldValidationResult {
        return try {
            val result = InputValidator.validateTextInput(
                input = description,
                fieldName = "Description",
                maxLength = 500,
                allowHtml = false
            )
            FieldValidationResult(
                isValid = result.isValid,
                errorMessage = result.errorMessage,
                sanitizedValue = result.sanitizedValue
            )
        } catch (e: Exception) {
            Logger.error("Description validation failed", "FormValidator", e)
            FieldValidationResult(
                isValid = false,
                errorMessage = "Description validation failed"
            )
        }
    }
    
    /**
     * Validate account field
     */
    fun validateAccount(account: String): FieldValidationResult {
        return try {
            val result = InputValidator.validateTextInput(
                input = account,
                fieldName = "Account",
                maxLength = 50,
                allowHtml = false
            )
            FieldValidationResult(
                isValid = result.isValid,
                errorMessage = result.errorMessage,
                sanitizedValue = result.sanitizedValue
            )
        } catch (e: Exception) {
            Logger.error("Account validation failed", "FormValidator", e)
            FieldValidationResult(
                isValid = false,
                errorMessage = "Account validation failed"
            )
        }
    }
    
    /**
     * Validate category field
     */
    fun validateCategory(category: String): FieldValidationResult {
        return try {
            val result = InputValidator.validateTextInput(
                input = category,
                fieldName = "Category",
                maxLength = 50,
                allowHtml = false
            )
            FieldValidationResult(
                isValid = result.isValid,
                errorMessage = result.errorMessage,
                sanitizedValue = result.sanitizedValue
            )
        } catch (e: Exception) {
            Logger.error("Category validation failed", "FormValidator", e)
            FieldValidationResult(
                isValid = false,
                errorMessage = "Category validation failed"
            )
        }
    }
    
    /**
     * Validate email field
     */
    fun validateEmail(email: String): FieldValidationResult {
        return try {
            val result = InputValidator.validateEmail(email)
            FieldValidationResult(
                isValid = result.isValid,
                errorMessage = result.errorMessage,
                sanitizedValue = result.sanitizedValue
            )
        } catch (e: Exception) {
            Logger.error("Email validation failed", "FormValidator", e)
            FieldValidationResult(
                isValid = false,
                errorMessage = "Email validation failed"
            )
        }
    }
    
    /**
     * Validate phone number field
     */
    fun validatePhoneNumber(phone: String): FieldValidationResult {
        return try {
            val result = InputValidator.validatePhoneNumber(phone)
            FieldValidationResult(
                isValid = result.isValid,
                errorMessage = result.errorMessage,
                sanitizedValue = result.sanitizedValue
            )
        } catch (e: Exception) {
            Logger.error("Phone validation failed", "FormValidator", e)
            FieldValidationResult(
                isValid = false,
                errorMessage = "Phone validation failed"
            )
        }
    }
    
    /**
     * Validate password field
     */
    fun validatePassword(password: String): FieldValidationResult {
        return try {
            val result = InputValidator.validatePassword(password)
            FieldValidationResult(
                isValid = result.isValid,
                errorMessage = result.errorMessage,
                sanitizedValue = result.sanitizedValue
            )
        } catch (e: Exception) {
            Logger.error("Password validation failed", "FormValidator", e)
            FieldValidationResult(
                isValid = false,
                errorMessage = "Password validation failed"
            )
        }
    }
    
    /**
     * Validate group form
     */
    fun validateGroupForm(
        name: String,
        description: String = ""
    ): FormValidationResult {
        val fieldErrors = mutableMapOf<String, String>()
        
        // Validate name
        val nameResult = validateGroupName(name)
        if (!nameResult.isValid) {
            fieldErrors["name"] = nameResult.errorMessage ?: "Invalid group name"
        }
        
        // Validate description
        val descriptionResult = validateDescription(description)
        if (!descriptionResult.isValid) {
            fieldErrors["description"] = descriptionResult.errorMessage ?: "Invalid description"
        }
        
        return FormValidationResult(
            isValid = fieldErrors.isEmpty(),
            fieldErrors = fieldErrors
        )
    }
    
    /**
     * Validate group name
     */
    fun validateGroupName(name: String): FieldValidationResult {
        return try {
            val result = InputValidator.validateTextInput(
                input = name,
                fieldName = "Group Name",
                maxLength = 50,
                allowHtml = false
            )
            FieldValidationResult(
                isValid = result.isValid,
                errorMessage = result.errorMessage,
                sanitizedValue = result.sanitizedValue
            )
        } catch (e: Exception) {
            Logger.error("Group name validation failed", "FormValidator", e)
            FieldValidationResult(
                isValid = false,
                errorMessage = "Group name validation failed"
            )
        }
    }
    
    /**
     * Validate ledger person form
     */
    fun validateLedgerPersonForm(
        name: String,
        email: String = "",
        phone: String = ""
    ): FormValidationResult {
        val fieldErrors = mutableMapOf<String, String>()
        
        // Validate name
        val nameResult = validatePersonName(name)
        if (!nameResult.isValid) {
            fieldErrors["name"] = nameResult.errorMessage ?: "Invalid person name"
        }
        
        // Validate email (optional)
        if (email.isNotBlank()) {
            val emailResult = validateEmail(email)
            if (!emailResult.isValid) {
                fieldErrors["email"] = emailResult.errorMessage ?: "Invalid email"
            }
        }
        
        // Validate phone (optional)
        if (phone.isNotBlank()) {
            val phoneResult = validatePhoneNumber(phone)
            if (!phoneResult.isValid) {
                fieldErrors["phone"] = phoneResult.errorMessage ?: "Invalid phone number"
            }
        }
        
        return FormValidationResult(
            isValid = fieldErrors.isEmpty(),
            fieldErrors = fieldErrors
        )
    }
    
    /**
     * Validate person name
     */
    fun validatePersonName(name: String): FieldValidationResult {
        return try {
            val result = InputValidator.validateTextInput(
                input = name,
                fieldName = "Person Name",
                maxLength = 50,
                allowHtml = false
            )
            FieldValidationResult(
                isValid = result.isValid,
                errorMessage = result.errorMessage,
                sanitizedValue = result.sanitizedValue
            )
        } catch (e: Exception) {
            Logger.error("Person name validation failed", "FormValidator", e)
            FieldValidationResult(
                isValid = false,
                errorMessage = "Person name validation failed"
            )
        }
    }
}
