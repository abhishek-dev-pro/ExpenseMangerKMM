package com.example.androidkmm.validation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for ValidationManager
 * 
 * Tests all validation rules and edge cases.
 * Ensures validation logic works correctly.
 */
class ValidationManagerTest {
    
    @Test
    fun `test amount validation - valid amount`() {
        val result = ValidationManager.AmountValidationRule.validate("100.50")
        assertTrue(result.isValid)
        assertEquals("100.50", result.sanitizedValue)
    }
    
    @Test
    fun `test amount validation - invalid amount`() {
        val result = ValidationManager.AmountValidationRule.validate("invalid")
        assertFalse(result.isValid)
        assertTrue(result.errorMessage?.contains("valid amount") == true)
    }
    
    @Test
    fun `test amount validation - negative amount`() {
        val result = ValidationManager.AmountValidationRule.validate("-100")
        assertFalse(result.isValid)
        assertTrue(result.errorMessage?.contains("greater than 0") == true)
    }
    
    @Test
    fun `test amount validation - zero amount`() {
        val result = ValidationManager.AmountValidationRule.validate("0")
        assertFalse(result.isValid)
        assertTrue(result.errorMessage?.contains("greater than 0") == true)
    }
    
    @Test
    fun `test amount validation - empty amount`() {
        val result = ValidationManager.AmountValidationRule.validate("")
        assertFalse(result.isValid)
        assertTrue(result.errorMessage?.contains("required") == true)
    }
    
    @Test
    fun `test title validation - valid title`() {
        val result = ValidationManager.TitleValidationRule.validate("Valid Title")
        assertTrue(result.isValid)
        assertEquals("Valid Title", result.sanitizedValue)
    }
    
    @Test
    fun `test title validation - empty title`() {
        val result = ValidationManager.TitleValidationRule.validate("")
        assertFalse(result.isValid)
        assertTrue(result.errorMessage?.contains("required") == true)
    }
    
    @Test
    fun `test title validation - too long title`() {
        val longTitle = "a".repeat(101)
        val result = ValidationManager.TitleValidationRule.validate(longTitle)
        assertFalse(result.isValid)
        assertTrue(result.errorMessage?.contains("too long") == true)
    }
    
    @Test
    fun `test email validation - valid email`() {
        val result = ValidationManager.EmailValidationRule.validate("test@example.com")
        assertTrue(result.isValid)
        assertEquals("test@example.com", result.sanitizedValue)
    }
    
    @Test
    fun `test email validation - invalid email`() {
        val result = ValidationManager.EmailValidationRule.validate("invalid-email")
        assertFalse(result.isValid)
        assertTrue(result.errorMessage?.contains("valid email") == true)
    }
    
    @Test
    fun `test phone validation - valid phone`() {
        val result = ValidationManager.PhoneValidationRule.validate("+1234567890")
        assertTrue(result.isValid)
        assertEquals("+1234567890", result.sanitizedValue)
    }
    
    @Test
    fun `test phone validation - invalid phone`() {
        val result = ValidationManager.PhoneValidationRule.validate("invalid")
        assertFalse(result.isValid)
        assertTrue(result.errorMessage?.contains("valid phone") == true)
    }
    
    @Test
    fun `test password validation - valid password`() {
        val result = ValidationManager.PasswordValidationRule.validate("Password123!")
        assertTrue(result.isValid)
        assertEquals("Password123!", result.sanitizedValue)
    }
    
    @Test
    fun `test password validation - weak password`() {
        val result = ValidationManager.PasswordValidationRule.validate("weak")
        assertFalse(result.isValid)
        assertTrue(result.errorMessage?.contains("at least 8 characters") == true)
    }
    
    @Test
    fun `test transaction form validation - valid form`() {
        val results = ValidationManager.validateTransactionForm(
            amount = "100.50",
            title = "Valid Title",
            description = "Valid Description",
            account = "Valid Account",
            category = "Valid Category"
        )
        
        assertTrue(ValidationManager.isFormValid(results))
        assertTrue(results["amount"]?.isValid == true)
        assertTrue(results["title"]?.isValid == true)
        assertTrue(results["description"]?.isValid == true)
        assertTrue(results["account"]?.isValid == true)
        assertTrue(results["category"]?.isValid == true)
    }
    
    @Test
    fun `test transaction form validation - invalid form`() {
        val results = ValidationManager.validateTransactionForm(
            amount = "",
            title = "",
            description = "",
            account = "",
            category = ""
        )
        
        assertFalse(ValidationManager.isFormValid(results))
        assertTrue(results["amount"]?.isValid == false)
        assertTrue(results["title"]?.isValid == false)
    }
    
    @Test
    fun `test group form validation - valid form`() {
        val results = ValidationManager.validateGroupForm(
            name = "Valid Group",
            description = "Valid Description"
        )
        
        assertTrue(ValidationManager.isFormValid(results))
        assertTrue(results["name"]?.isValid == true)
        assertTrue(results["description"]?.isValid == true)
    }
    
    @Test
    fun `test group form validation - invalid form`() {
        val results = ValidationManager.validateGroupForm(
            name = "",
            description = ""
        )
        
        assertFalse(ValidationManager.isFormValid(results))
        assertTrue(results["name"]?.isValid == false)
    }
    
    @Test
    fun `test ledger person form validation - valid form`() {
        val results = ValidationManager.validateLedgerPersonForm(
            name = "Valid Person",
            email = "test@example.com",
            phone = "+1234567890"
        )
        
        assertTrue(ValidationManager.isFormValid(results))
        assertTrue(results["name"]?.isValid == true)
        assertTrue(results["email"]?.isValid == true)
        assertTrue(results["phone"]?.isValid == true)
    }
    
    @Test
    fun `test ledger person form validation - invalid form`() {
        val results = ValidationManager.validateLedgerPersonForm(
            name = "",
            email = "invalid-email",
            phone = "invalid-phone"
        )
        
        assertFalse(ValidationManager.isFormValid(results))
        assertTrue(results["name"]?.isValid == false)
        assertTrue(results["email"]?.isValid == false)
        assertTrue(results["phone"]?.isValid == false)
    }
    
    @Test
    fun `test get error messages`() {
        val results = ValidationManager.validateTransactionForm(
            amount = "",
            title = "",
            description = "",
            account = "",
            category = ""
        )
        
        val errorMessages = ValidationManager.getErrorMessages(results)
        assertTrue(errorMessages.isNotEmpty())
        assertTrue(errorMessages.containsKey("amount"))
        assertTrue(errorMessages.containsKey("title"))
    }
    
    @Test
    fun `test get sanitized values`() {
        val results = ValidationManager.validateTransactionForm(
            amount = "100.50",
            title = "Valid Title",
            description = "Valid Description",
            account = "Valid Account",
            category = "Valid Category"
        )
        
        val sanitizedValues = ValidationManager.getSanitizedValues(results)
        assertTrue(sanitizedValues.isNotEmpty())
        assertEquals("100.50", sanitizedValues["amount"])
        assertEquals("Valid Title", sanitizedValues["title"])
    }
    
    @Test
    fun `test composite validation rule`() {
        val rules = listOf(
            ValidationManager.RequiredValidationRule("Test Field"),
            ValidationManager.LengthValidationRule(2, 10, "Test Field")
        )
        
        val result = ValidationManager.validateWithRules("Valid", rules)
        assertTrue(result.isValid)
    }
    
    @Test
    fun `test composite validation rule - fails first rule`() {
        val rules = listOf(
            ValidationManager.RequiredValidationRule("Test Field"),
            ValidationManager.LengthValidationRule(2, 10, "Test Field")
        )
        
        val result = ValidationManager.validateWithRules("", rules)
        assertFalse(result.isValid)
        assertTrue(result.errorMessage?.contains("required") == true)
    }
    
    @Test
    fun `test composite validation rule - fails second rule`() {
        val rules = listOf(
            ValidationManager.RequiredValidationRule("Test Field"),
            ValidationManager.LengthValidationRule(2, 10, "Test Field")
        )
        
        val result = ValidationManager.validateWithRules("a", rules)
        assertFalse(result.isValid)
        assertTrue(result.errorMessage?.contains("at least 2 characters") == true)
    }
    
    @Test
    fun `test range validation rule`() {
        val rule = ValidationManager.RangeValidationRule(0.0, 100.0, "Test Field")
        
        val validResult = rule.validate("50.0")
        assertTrue(validResult.isValid)
        
        val invalidResult = rule.validate("150.0")
        assertFalse(invalidResult.isValid)
        assertTrue(invalidResult.errorMessage?.contains("no more than 100") == true)
    }
    
    @Test
    fun `test pattern validation rule`() {
        val rule = ValidationManager.PatternValidationRule(
            Regex("^[A-Za-z]+$"),
            "Test Field",
            "Only letters allowed"
        )
        
        val validResult = rule.validate("Valid")
        assertTrue(validResult.isValid)
        
        val invalidResult = rule.validate("Invalid123")
        assertFalse(invalidResult.isValid)
        assertTrue(invalidResult.errorMessage?.contains("Only letters allowed") == true)
    }
}
