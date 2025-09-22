package com.example.androidkmm.utils

/**
 * Comprehensive validation utilities for form inputs
 */
object ValidationUtils {
    
    /**
     * Validation result containing success status and error message
     */
    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null
    )
    
    /**
     * Validates amount input
     */
    fun validateAmount(amount: String): ValidationResult {
        return when {
            amount.isBlank() -> ValidationResult(false, "Amount is required")
            amount.toDoubleOrNull() == null -> ValidationResult(false, "Please enter a valid amount")
            amount.toDouble() <= 0 -> ValidationResult(false, "Amount must be greater than 0")
            amount.toDouble() > 999999.99 -> ValidationResult(false, "Amount is too large")
            else -> ValidationResult(true)
        }
    }
    
    /**
     * Validates title input
     */
    fun validateTitle(title: String): ValidationResult {
        return when {
            title.isBlank() -> ValidationResult(false, "Title is required")
            title.length < 2 -> ValidationResult(false, "Title must be at least 2 characters")
            title.length > 100 -> ValidationResult(false, "Title is too long")
            else -> ValidationResult(true)
        }
    }
    
    /**
     * Validates description input (optional)
     */
    fun validateDescription(description: String): ValidationResult {
        return when {
            description.length > 500 -> ValidationResult(false, "Description is too long")
            else -> ValidationResult(true)
        }
    }
    
    /**
     * Validates email input
     */
    fun validateEmail(email: String): ValidationResult {
        return when {
            email.isBlank() -> ValidationResult(false, "Email is required")
            !email.contains("@") -> ValidationResult(false, "Please enter a valid email")
            email.length > 254 -> ValidationResult(false, "Email is too long")
            else -> ValidationResult(true)
        }
    }
    
    /**
     * Validates name input
     */
    fun validateName(name: String): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult(false, "Name is required")
            name.length < 2 -> ValidationResult(false, "Name must be at least 2 characters")
            name.length > 50 -> ValidationResult(false, "Name is too long")
            !name.matches(Regex("^[a-zA-Z\\s]+$")) -> ValidationResult(false, "Name can only contain letters and spaces")
            else -> ValidationResult(true)
        }
    }
    
    /**
     * Validates account name input
     */
    fun validateAccountName(name: String): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult(false, "Account name is required")
            name.length < 2 -> ValidationResult(false, "Account name must be at least 2 characters")
            name.length > 50 -> ValidationResult(false, "Account name is too long")
            else -> ValidationResult(true)
        }
    }
    
    /**
     * Validates group name input
     */
    fun validateGroupName(name: String): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult(false, "Group name is required")
            name.length < 2 -> ValidationResult(false, "Group name must be at least 2 characters")
            name.length > 50 -> ValidationResult(false, "Group name is too long")
            else -> ValidationResult(true)
        }
    }
    
    /**
     * Validates person name input
     */
    fun validatePersonName(name: String): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult(false, "Person name is required")
            name.length < 2 -> ValidationResult(false, "Person name must be at least 2 characters")
            name.length > 50 -> ValidationResult(false, "Person name is too long")
            else -> ValidationResult(true)
        }
    }
    
    /**
     * Validates phone number input
     */
    fun validatePhoneNumber(phone: String): ValidationResult {
        return when {
            phone.isBlank() -> ValidationResult(false, "Phone number is required")
            !phone.matches(Regex("^[+]?[0-9\\s\\-()]{10,15}$")) -> ValidationResult(false, "Please enter a valid phone number")
            else -> ValidationResult(true)
        }
    }
    
    /**
     * Validates currency symbol input
     */
    fun validateCurrencySymbol(symbol: String): ValidationResult {
        return when {
            symbol.isBlank() -> ValidationResult(false, "Currency symbol is required")
            symbol.length > 3 -> ValidationResult(false, "Currency symbol is too long")
            else -> ValidationResult(true)
        }
    }
    
    /**
     * Validates date input
     */
    fun validateDate(date: String): ValidationResult {
        return when {
            date.isBlank() -> ValidationResult(false, "Date is required")
            !date.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$")) -> ValidationResult(false, "Please enter date in YYYY-MM-DD format")
            else -> ValidationResult(true)
        }
    }
    
    /**
     * Validates time input
     */
    fun validateTime(time: String): ValidationResult {
        return when {
            time.isBlank() -> ValidationResult(false, "Time is required")
            !time.matches(Regex("^\\d{2}:\\d{2}$")) -> ValidationResult(false, "Please enter time in HH:MM format")
            else -> ValidationResult(true)
        }
    }
    
    /**
     * Validates password input
     */
    fun validatePassword(password: String): ValidationResult {
        return when {
            password.isBlank() -> ValidationResult(false, "Password is required")
            password.length < 8 -> ValidationResult(false, "Password must be at least 8 characters")
            password.length > 128 -> ValidationResult(false, "Password is too long")
            !password.matches(Regex(".*[A-Z].*")) -> ValidationResult(false, "Password must contain at least one uppercase letter")
            !password.matches(Regex(".*[a-z].*")) -> ValidationResult(false, "Password must contain at least one lowercase letter")
            !password.matches(Regex(".*[0-9].*")) -> ValidationResult(false, "Password must contain at least one number")
            else -> ValidationResult(true)
        }
    }
    
    /**
     * Validates confirmation password input
     */
    fun validateConfirmPassword(password: String, confirmPassword: String): ValidationResult {
        return when {
            confirmPassword.isBlank() -> ValidationResult(false, "Please confirm your password")
            password != confirmPassword -> ValidationResult(false, "Passwords do not match")
            else -> ValidationResult(true)
        }
    }
    
    /**
     * Validates required selection
     */
    fun validateRequiredSelection(selection: Any?, fieldName: String): ValidationResult {
        return when {
            selection == null -> ValidationResult(false, "Please select a $fieldName")
            else -> ValidationResult(true)
        }
    }
    
    /**
     * Validates required list
     */
    fun validateRequiredList(list: List<Any>?, fieldName: String): ValidationResult {
        return when {
            list == null || list.isEmpty() -> ValidationResult(false, "Please add at least one $fieldName")
            else -> ValidationResult(true)
        }
    }
    
    /**
     * Validates positive number
     */
    fun validatePositiveNumber(number: String, fieldName: String): ValidationResult {
        return when {
            number.isBlank() -> ValidationResult(false, "$fieldName is required")
            number.toDoubleOrNull() == null -> ValidationResult(false, "Please enter a valid $fieldName")
            number.toDouble() <= 0 -> ValidationResult(false, "$fieldName must be greater than 0")
            else -> ValidationResult(true)
        }
    }
    
    /**
     * Validates non-negative number
     */
    fun validateNonNegativeNumber(number: String, fieldName: String): ValidationResult {
        return when {
            number.isBlank() -> ValidationResult(false, "$fieldName is required")
            number.toDoubleOrNull() == null -> ValidationResult(false, "Please enter a valid $fieldName")
            number.toDouble() < 0 -> ValidationResult(false, "$fieldName cannot be negative")
            else -> ValidationResult(true)
        }
    }
    
    /**
     * Validates integer input
     */
    fun validateInteger(value: String, fieldName: String): ValidationResult {
        return when {
            value.isBlank() -> ValidationResult(false, "$fieldName is required")
            value.toIntOrNull() == null -> ValidationResult(false, "Please enter a valid $fieldName")
            else -> ValidationResult(true)
        }
    }
    
    /**
     * Validates positive integer
     */
    fun validatePositiveInteger(value: String, fieldName: String): ValidationResult {
        return when {
            value.isBlank() -> ValidationResult(false, "$fieldName is required")
            value.toIntOrNull() == null -> ValidationResult(false, "Please enter a valid $fieldName")
            value.toInt() <= 0 -> ValidationResult(false, "$fieldName must be greater than 0")
            else -> ValidationResult(true)
        }
    }
    
    /**
     * Validates non-negative integer
     */
    fun validateNonNegativeInteger(value: String, fieldName: String): ValidationResult {
        return when {
            value.isBlank() -> ValidationResult(false, "$fieldName is required")
            value.toIntOrNull() == null -> ValidationResult(false, "Please enter a valid $fieldName")
            value.toInt() < 0 -> ValidationResult(false, "$fieldName cannot be negative")
            else -> ValidationResult(true)
        }
    }
    
    /**
     * Validates URL input
     */
    fun validateUrl(url: String): ValidationResult {
        return when {
            url.isBlank() -> ValidationResult(false, "URL is required")
            !url.matches(Regex("^https?://.*")) -> ValidationResult(false, "Please enter a valid URL")
            url.length > 2048 -> ValidationResult(false, "URL is too long")
            else -> ValidationResult(true)
        }
    }
    
    /**
     * Validates color hex input
     */
    fun validateColorHex(color: String): ValidationResult {
        return when {
            color.isBlank() -> ValidationResult(false, "Color is required")
            !color.matches(Regex("^#[0-9A-Fa-f]{6}$")) -> ValidationResult(false, "Please enter a valid color hex code")
            else -> ValidationResult(true)
        }
    }
    
    /**
     * Validates percentage input
     */
    fun validatePercentage(percentage: String): ValidationResult {
        return when {
            percentage.isBlank() -> ValidationResult(false, "Percentage is required")
            percentage.toDoubleOrNull() == null -> ValidationResult(false, "Please enter a valid percentage")
            percentage.toDouble() < 0 -> ValidationResult(false, "Percentage cannot be negative")
            percentage.toDouble() > 100 -> ValidationResult(false, "Percentage cannot be greater than 100")
            else -> ValidationResult(true)
        }
    }
    
    /**
     * Validates required field
     */
    fun validateRequired(value: String, fieldName: String): ValidationResult {
        return when {
            value.isBlank() -> ValidationResult(false, "$fieldName is required")
            else -> ValidationResult(true)
        }
    }
    
    /**
     * Validates minimum length
     */
    fun validateMinLength(value: String, minLength: Int, fieldName: String): ValidationResult {
        return when {
            value.length < minLength -> ValidationResult(false, "$fieldName must be at least $minLength characters")
            else -> ValidationResult(true)
        }
    }
    
    /**
     * Validates maximum length
     */
    fun validateMaxLength(value: String, maxLength: Int, fieldName: String): ValidationResult {
        return when {
            value.length > maxLength -> ValidationResult(false, "$fieldName must be no more than $maxLength characters")
            else -> ValidationResult(true)
        }
    }
    
    /**
     * Validates length range
     */
    fun validateLengthRange(value: String, minLength: Int, maxLength: Int, fieldName: String): ValidationResult {
        return when {
            value.length < minLength -> ValidationResult(false, "$fieldName must be at least $minLength characters")
            value.length > maxLength -> ValidationResult(false, "$fieldName must be no more than $maxLength characters")
            else -> ValidationResult(true)
        }
    }
    
    /**
     * Validates alphanumeric input
     */
    fun validateAlphanumeric(value: String, fieldName: String): ValidationResult {
        return when {
            !value.matches(Regex("^[a-zA-Z0-9]+$")) -> ValidationResult(false, "$fieldName can only contain letters and numbers")
            else -> ValidationResult(true)
        }
    }
    
    /**
     * Validates alphabetic input
     */
    fun validateAlphabetic(value: String, fieldName: String): ValidationResult {
        return when {
            !value.matches(Regex("^[a-zA-Z\\s]+$")) -> ValidationResult(false, "$fieldName can only contain letters and spaces")
            else -> ValidationResult(true)
        }
    }
    
    /**
     * Validates numeric input
     */
    fun validateNumeric(value: String, fieldName: String): ValidationResult {
        return when {
            !value.matches(Regex("^[0-9]+$")) -> ValidationResult(false, "$fieldName can only contain numbers")
            else -> ValidationResult(true)
        }
    }
    
    /**
     * Validates decimal input
     */
    fun validateDecimal(value: String, fieldName: String): ValidationResult {
        return when {
            !value.matches(Regex("^[0-9]+(\\.[0-9]+)?$")) -> ValidationResult(false, "$fieldName can only contain numbers and decimal point")
            else -> ValidationResult(true)
        }
    }
    
    /**
     * Validates email format
     */
    fun validateEmailFormat(email: String): ValidationResult {
        return when {
            !email.matches(Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) -> ValidationResult(false, "Please enter a valid email address")
            else -> ValidationResult(true)
        }
    }
    
    /**
     * Validates phone format
     */
    fun validatePhoneFormat(phone: String): ValidationResult {
        return when {
            !phone.matches(Regex("^[+]?[0-9\\s\\-()]{10,15}$")) -> ValidationResult(false, "Please enter a valid phone number")
            else -> ValidationResult(true)
        }
    }
    
    /**
     * Validates date format
     */
    fun validateDateFormat(date: String): ValidationResult {
        return when {
            !date.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$")) -> ValidationResult(false, "Please enter date in YYYY-MM-DD format")
            else -> ValidationResult(true)
        }
    }
    
    /**
     * Validates time format
     */
    fun validateTimeFormat(time: String): ValidationResult {
        return when {
            !time.matches(Regex("^\\d{2}:\\d{2}$")) -> ValidationResult(false, "Please enter time in HH:MM format")
            else -> ValidationResult(true)
        }
    }
    
    /**
     * Validates currency format
     */
    fun validateCurrencyFormat(currency: String): ValidationResult {
        return when {
            !currency.matches(Regex("^[A-Z]{3}$")) -> ValidationResult(false, "Please enter a valid currency code (e.g., USD, EUR)")
            else -> ValidationResult(true)
        }
    }
    
    /**
     * Validates color format
     */
    fun validateColorFormat(color: String): ValidationResult {
        return when {
            !color.matches(Regex("^#[0-9A-Fa-f]{6}$")) -> ValidationResult(false, "Please enter a valid color hex code")
            else -> ValidationResult(true)
        }
    }
    
    /**
     * Validates URL format
     */
    fun validateUrlFormat(url: String): ValidationResult {
        return when {
            !url.matches(Regex("^https?://.*")) -> ValidationResult(false, "Please enter a valid URL")
            else -> ValidationResult(true)
        }
    }
    
    /**
     * Validates password strength
     */
    fun validatePasswordStrength(password: String): ValidationResult {
        val hasUpperCase = password.matches(Regex(".*[A-Z].*"))
        val hasLowerCase = password.matches(Regex(".*[a-z].*"))
        val hasNumbers = password.matches(Regex(".*[0-9].*"))
        val hasSpecialChar = password.matches(Regex(".*[!@#$%^&*(),.?\":{}|<>].*"))
        
        return when {
            !hasUpperCase -> ValidationResult(false, "Password must contain at least one uppercase letter")
            !hasLowerCase -> ValidationResult(false, "Password must contain at least one lowercase letter")
            !hasNumbers -> ValidationResult(false, "Password must contain at least one number")
            !hasSpecialChar -> ValidationResult(false, "Password must contain at least one special character")
            else -> ValidationResult(true)
        }
    }
    
    /**
     * Validates password confirmation
     */
    fun validatePasswordConfirmation(password: String, confirmPassword: String): ValidationResult {
        return when {
            password != confirmPassword -> ValidationResult(false, "Passwords do not match")
            else -> ValidationResult(true)
        }
    }
    
    /**
     * Validates required field with custom message
     */
    fun validateRequiredWithMessage(value: String, message: String): ValidationResult {
        return when {
            value.isBlank() -> ValidationResult(false, message)
            else -> ValidationResult(true)
        }
    }
    
    /**
     * Validates field with custom validation function
     */
    fun validateWithCustomRule(value: String, rule: (String) -> Boolean, errorMessage: String): ValidationResult {
        return when {
            !rule(value) -> ValidationResult(false, errorMessage)
            else -> ValidationResult(true)
        }
    }
    
    /**
     * Validates multiple fields at once
     */
    fun validateMultipleFields(validations: List<ValidationResult>): ValidationResult {
        val errors = validations.filter { !it.isValid }.map { it.errorMessage ?: "Validation failed" }
        return when {
            errors.isEmpty() -> ValidationResult(true)
            else -> ValidationResult(false, errors.joinToString(", "))
        }
    }
    
    /**
     * Validates form with all fields
     */
    fun validateForm(fields: Map<String, ValidationResult>): ValidationResult {
        val errors = fields.filter { !it.value.isValid }.map { "${it.key}: ${it.value.errorMessage}" }
        return when {
            errors.isEmpty() -> ValidationResult(true)
            else -> ValidationResult(false, errors.joinToString(", "))
        }
    }
}
