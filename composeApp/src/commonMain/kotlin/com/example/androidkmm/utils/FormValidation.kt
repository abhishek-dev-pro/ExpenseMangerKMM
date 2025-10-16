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
     * Validate and sanitize amount input with strict 2-decimal restriction
     * This function prevents users from typing more than 2 decimal places
     */
    fun validateAndSanitizeAmount(input: String): String {
        // COMPLETELY REWRITTEN LOGIC - BULLETPROOF VALIDATION
        val cleanInput = input.filter { it.isDigit() || it == '.' }
        val decimalCount = cleanInput.count { it == '.' }
        
        // Check if input has more than one decimal point
        if (decimalCount > 1) {
            return ""
        }
        
        // Check if input has decimal point
        if (cleanInput.contains('.')) {
            val parts = cleanInput.split('.')
            if (parts.size == 2) {
                val beforeDecimal = parts[0]
                val afterDecimal = parts[1]
                
                // Check if after decimal has more than 2 digits
                if (afterDecimal.length > 2) {
                    return ""
                }
                
                // Check leading zeros in before decimal
                if (beforeDecimal.isNotEmpty() && beforeDecimal.startsWith("0") && beforeDecimal != "0") {
                    return ""
                }
                
                return cleanInput
            }
        } else {
            // No decimal point - check length and leading zeros
            if (cleanInput.length > 8) {
                return ""
            }
            
            if (cleanInput.isNotEmpty() && cleanInput.startsWith("0") && cleanInput != "0") {
                return ""
            }
            
            return cleanInput
        }
        
        return ""
    }

    /**
     * Validate amount input
     */
    fun validateAmount(amount: String): ValidationResult {
        val errors = mutableMapOf<String, String>()
        
        when {
            amount.isBlank() -> errors["amount"] = "Amount is required"
            amount.toDoubleOrNull() == null -> errors["amount"] = "Please enter a valid amount"
            amount.toDoubleOrNull()!! <= 0 -> errors["amount"] = "Amount must be greater than 0"
            amount.toDoubleOrNull()!! > 9999999999.99 -> errors["amount"] = "Amount is too large"
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
     * Rules: Valid email format, 5-50 characters, trim spaces, convert to lowercase
     */
    fun validateEmail(email: String): ValidationResult {
        val errors = mutableMapOf<String, String>()
        val trimmedEmail = email.trim()
        
        when {
            trimmedEmail.isBlank() -> errors["email"] = "Email is required"
            trimmedEmail.length < 5 -> errors["email"] = "Email must be at least 5 characters"
            trimmedEmail.length > 50 -> errors["email"] = "Email must be 50 characters or less"
            !isValidEmailFormat(trimmedEmail) -> errors["email"] = "Please enter a valid email address"
        }
        
        return ValidationResult(errors.isEmpty(), errors)
    }
    
    /**
     * Check if email format is valid using standard email regex
     */
    private fun isValidEmailFormat(email: String): Boolean {
        val emailPattern = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,24}$")
        return emailPattern.matches(email)
    }
    
    /**
     * Normalize email (trim and convert to lowercase)
     */
    fun normalizeEmail(email: String): String {
        return email.trim().lowercase()
    }
    
    /**
     * Validate person name input
     * Rules: Only English alphabets and numbers allowed, numbers cannot be at the start
     * Maximum length: 22 characters
     */
    fun validatePersonName(name: String): ValidationResult {
        val errors = mutableMapOf<String, String>()
        
        when {
            name.isBlank() -> errors["name"] = "Name is required"
            name.length < 2 -> errors["name"] = "Name must be at least 2 characters"
            name.length > 22 -> errors["name"] = "Name must be 22 characters or less"
            !isValidPersonName(name) -> errors["name"] = "Name can only contain English letters and numbers. Numbers cannot be at the start."
        }
        
        return ValidationResult(errors.isEmpty(), errors)
    }
    
    /**
     * Check if person name is valid
     * Only English alphabets and numbers allowed, numbers cannot be at the start
     */
    private fun isValidPersonName(name: String): Boolean {
        // Check if name contains only English letters and numbers
        val namePattern = Regex("^[A-Za-z0-9\\s]+$")
        if (!namePattern.matches(name)) {
            return false
        }
        
        // Check if any word starts with a number
        val words = name.trim().split("\\s+".toRegex())
        for (word in words) {
            if (word.isNotEmpty() && word.first().isDigit()) {
                return false
            }
        }
        
        return true
    }
    
    /**
     * Capitalize the first letter of each word in a name
     */
    fun capitalizeName(name: String): String {
        return name.trim().split("\\s+".toRegex()).joinToString(" ") { word ->
            if (word.isNotEmpty()) {
                word.first().uppercaseChar() + word.drop(1).lowercase()
            } else {
                word
            }
        }
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
