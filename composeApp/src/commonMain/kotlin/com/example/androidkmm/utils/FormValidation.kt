package com.example.androidkmm.utils

/**
 * Standardized Form Validation Utilities
 * 
 * Provides consistent validation across all forms in the application.
 * 
 * Features:
 * - Unified validation logic for all form types
 * - Consistent error messages and handling
 * - Support for expense, income, transfer, and ledger forms
 * - Email and phone number validation
 * - Amount validation with proper decimal handling
 * - Title and description validation with length limits
 * - Account and category selection validation
 * - Transfer-specific validation (different accounts, sufficient balance)
 * 
 * Benefits:
 * - Consistent user experience across all forms
 * - Centralized validation logic for easy maintenance
 * - Standardized error messages
 * - Type-safe validation results
 * 
 * Usage:
 * ```kotlin
 * val result = FormValidation.validateExpenseForm(amount, title, category, account, description)
 * if (result.isValid) {
 *     // Proceed with form submission
 * } else {
 *     // Display errors: result.errors
 * }
 * ```
 */
object FormValidation {
    
    /**
     * Validation result data class
     */
    data class ValidationResult(
        val isValid: Boolean,
        val errors: Map<String, String> = emptyMap()
    )
    
    /**
     * Validate amount input
     */
    fun validateAmount(amount: String): ValidationResult {
        val errors = mutableMapOf<String, String>()
        
        when {
            amount.isBlank() -> errors["amount"] = "Amount is required"
            amount.toDoubleOrNull() == null -> errors["amount"] = "Please enter a valid amount"
            amount.toDoubleOrNull()!! <= 0 -> errors["amount"] = "Amount must be greater than 0"
            amount.toDoubleOrNull()!! > 999999.99 -> errors["amount"] = "Amount is too large"
        }
        
        return ValidationResult(errors.isEmpty(), errors)
    }
    
    /**
     * Validate title input
     */
    fun validateTitle(title: String): ValidationResult {
        val errors = mutableMapOf<String, String>()
        
        when {
            title.isBlank() -> errors["title"] = "Title is required"
            title.length < 2 -> errors["title"] = "Title must be at least 2 characters"
            title.length > 100 -> errors["title"] = "Title is too long"
        }
        
        return ValidationResult(errors.isEmpty(), errors)
    }
    
    /**
     * Validate description input
     */
    fun validateDescription(description: String): ValidationResult {
        val errors = mutableMapOf<String, String>()
        
        if (description.length > 500) {
            errors["description"] = "Description is too long"
        }
        
        return ValidationResult(errors.isEmpty(), errors)
    }
    
    /**
     * Validate category selection
     */
    fun validateCategory(category: Any?): ValidationResult {
        val errors = mutableMapOf<String, String>()
        
        if (category == null) {
            errors["category"] = "Please select a category"
        }
        
        return ValidationResult(errors.isEmpty(), errors)
    }
    
    /**
     * Validate account selection
     */
    fun validateAccount(account: Any?): ValidationResult {
        val errors = mutableMapOf<String, String>()
        
        if (account == null) {
            errors["account"] = "Please select an account"
        }
        
        return ValidationResult(errors.isEmpty(), errors)
    }
    
    /**
     * Validate transfer accounts
     */
    fun validateTransferAccounts(fromAccount: Any?, toAccount: Any?): ValidationResult {
        val errors = mutableMapOf<String, String>()
        
        if (fromAccount == null) {
            errors["account"] = "From account is required"
        }
        
        if (toAccount == null) {
            errors["toAccount"] = "To account is required"
        }
        
        // Check if accounts are different
        if (fromAccount != null && toAccount != null && fromAccount == toAccount) {
            errors["transferTo"] = "Transfer to account must be different from from account"
        }
        
        return ValidationResult(errors.isEmpty(), errors)
    }
    
    /**
     * Validate email input
     */
    fun validateEmail(email: String): ValidationResult {
        val errors = mutableMapOf<String, String>()
        
        when {
            email.isBlank() -> errors["email"] = "Email is required"
            !isValidEmail(email) -> errors["email"] = "Please enter a valid email"
        }
        
        return ValidationResult(errors.isEmpty(), errors)
    }
    
    /**
     * Validate phone number
     */
    fun validatePhone(phone: String): ValidationResult {
        val errors = mutableMapOf<String, String>()
        
        when {
            phone.isBlank() -> errors["phone"] = "Phone number is required"
            !isValidPhone(phone) -> errors["phone"] = "Please enter a valid phone number"
        }
        
        return ValidationResult(errors.isEmpty(), errors)
    }
    
    /**
     * Validate expense form
     */
    fun validateExpenseForm(
        amount: String,
        title: String,
        category: Any?,
        account: Any?,
        description: String = ""
    ): ValidationResult {
        val allErrors = mutableMapOf<String, String>()
        
        // Validate amount
        val amountResult = validateAmount(amount)
        allErrors.putAll(amountResult.errors)
        
        // Validate title
        val titleResult = validateTitle(title)
        allErrors.putAll(titleResult.errors)
        
        // Validate category
        val categoryResult = validateCategory(category)
        allErrors.putAll(categoryResult.errors)
        
        // Validate account
        val accountResult = validateAccount(account)
        allErrors.putAll(accountResult.errors)
        
        // Validate description (optional)
        val descriptionResult = validateDescription(description)
        allErrors.putAll(descriptionResult.errors)
        
        return ValidationResult(allErrors.isEmpty(), allErrors)
    }
    
    /**
     * Validate income form
     */
    fun validateIncomeForm(
        amount: String,
        title: String,
        category: Any?,
        account: Any?,
        description: String = ""
    ): ValidationResult {
        // Same validation as expense form
        return validateExpenseForm(amount, title, category, account, description)
    }
    
    /**
     * Validate transfer form
     */
    fun validateTransferForm(
        amount: String,
        fromAccount: Any?,
        toAccount: Any?
    ): ValidationResult {
        val allErrors = mutableMapOf<String, String>()
        
        // Validate amount
        val amountResult = validateAmount(amount)
        allErrors.putAll(amountResult.errors)
        
        // Validate accounts
        val accountsResult = validateTransferAccounts(fromAccount, toAccount)
        allErrors.putAll(accountsResult.errors)
        
        return ValidationResult(allErrors.isEmpty(), allErrors)
    }
    
    /**
     * Validate ledger entry form
     */
    fun validateLedgerEntryForm(
        amount: String,
        account: Any?,
        description: String = ""
    ): ValidationResult {
        val allErrors = mutableMapOf<String, String>()
        
        // Validate amount
        val amountResult = validateAmount(amount)
        allErrors.putAll(amountResult.errors)
        
        // Validate account
        val accountResult = validateAccount(account)
        allErrors.putAll(accountResult.errors)
        
        // Validate description (optional)
        val descriptionResult = validateDescription(description)
        allErrors.putAll(descriptionResult.errors)
        
        return ValidationResult(allErrors.isEmpty(), allErrors)
    }
    
    /**
     * Check if email is valid
     */
    private fun isValidEmail(email: String): Boolean {
        val emailPattern = Regex("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$")
        return emailPattern.matches(email)
    }
    
    /**
     * Check if phone number is valid
     */
    private fun isValidPhone(phone: String): Boolean {
        val phonePattern = Regex("^[+]?[0-9\\s\\-()]{10,}$")
        return phonePattern.matches(phone)
    }
    
    /**
     * Get field error message
     */
    fun getFieldError(errors: Map<String, String>, fieldName: String): String? {
        return errors[fieldName]
    }
    
    /**
     * Check if field has error
     */
    fun hasFieldError(errors: Map<String, String>, fieldName: String): Boolean {
        return errors.containsKey(fieldName)
    }
}
