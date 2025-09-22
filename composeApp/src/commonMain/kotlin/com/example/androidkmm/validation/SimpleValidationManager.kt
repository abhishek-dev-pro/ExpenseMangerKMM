package com.example.androidkmm.validation

import com.example.androidkmm.security.InputValidator

/**
 * Simplified validation manager
 * 
 * Provides basic validation functionality without complex type issues.
 */
object SimpleValidationManager {
    
    /**
     * Validate transaction form
     */
    fun validateTransactionForm(
        amount: String,
        title: String,
        description: String = "",
        account: String = "",
        category: String = ""
    ): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        
        // Validate amount
        val amountResult = InputValidator.validateAmount(amount)
        if (!amountResult.isValid) {
            errors["amount"] = amountResult.errorMessage ?: "Invalid amount"
        }
        
        // Validate title
        val titleResult = InputValidator.validateTextInput(
            input = title,
            fieldName = "Title",
            maxLength = 100,
            allowHtml = false
        )
        if (!titleResult.isValid) {
            errors["title"] = titleResult.errorMessage ?: "Invalid title"
        }
        
        // Validate description
        val descriptionResult = InputValidator.validateTextInput(
            input = description,
            fieldName = "Description",
            maxLength = 500,
            allowHtml = false
        )
        if (!descriptionResult.isValid) {
            errors["description"] = descriptionResult.errorMessage ?: "Invalid description"
        }
        
        // Validate account
        if (account.isNotBlank()) {
            val accountResult = InputValidator.validateTextInput(
                input = account,
                fieldName = "Account",
                maxLength = 50,
                allowHtml = false
            )
            if (!accountResult.isValid) {
                errors["account"] = accountResult.errorMessage ?: "Invalid account"
            }
        }
        
        // Validate category
        if (category.isNotBlank()) {
            val categoryResult = InputValidator.validateTextInput(
                input = category,
                fieldName = "Category",
                maxLength = 50,
                allowHtml = false
            )
            if (!categoryResult.isValid) {
                errors["category"] = categoryResult.errorMessage ?: "Invalid category"
            }
        }
        
        return errors
    }
    
    /**
     * Validate group form
     */
    fun validateGroupForm(
        name: String,
        description: String = ""
    ): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        
        // Validate name
        val nameResult = InputValidator.validateTextInput(
            input = name,
            fieldName = "Group Name",
            maxLength = 50,
            allowHtml = false
        )
        if (!nameResult.isValid) {
            errors["name"] = nameResult.errorMessage ?: "Invalid group name"
        }
        
        // Validate description
        val descriptionResult = InputValidator.validateTextInput(
            input = description,
            fieldName = "Description",
            maxLength = 500,
            allowHtml = false
        )
        if (!descriptionResult.isValid) {
            errors["description"] = descriptionResult.errorMessage ?: "Invalid description"
        }
        
        return errors
    }
    
    /**
     * Validate ledger person form
     */
    fun validateLedgerPersonForm(
        name: String,
        email: String = "",
        phone: String = ""
    ): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        
        // Validate name
        val nameResult = InputValidator.validateTextInput(
            input = name,
            fieldName = "Person Name",
            maxLength = 50,
            allowHtml = false
        )
        if (!nameResult.isValid) {
            errors["name"] = nameResult.errorMessage ?: "Invalid person name"
        }
        
        // Validate email (optional)
        if (email.isNotBlank()) {
            val emailResult = InputValidator.validateEmail(email)
            if (!emailResult.isValid) {
                errors["email"] = emailResult.errorMessage ?: "Invalid email"
            }
        }
        
        // Validate phone (optional)
        if (phone.isNotBlank()) {
            val phoneResult = InputValidator.validatePhoneNumber(phone)
            if (!phoneResult.isValid) {
                errors["phone"] = phoneResult.errorMessage ?: "Invalid phone number"
            }
        }
        
        return errors
    }
    
    /**
     * Check if form is valid
     */
    fun isFormValid(errors: Map<String, String>): Boolean {
        return errors.isEmpty()
    }
}
