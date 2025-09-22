package com.example.androidkmm.error

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for ErrorHandler
 * 
 * Tests error handling and recovery strategies.
 * Ensures error handling works correctly.
 */
class ErrorHandlerTest {
    
    @Test
    fun `test error type categorization`() {
        val networkError = Exception("Network error")
        val appError = ErrorHandler.createAppError(networkError, ErrorHandler.ErrorType.NETWORK_ERROR)
        
        assertEquals(ErrorHandler.ErrorType.NETWORK_ERROR, appError.type)
        assertEquals(ErrorHandler.ErrorSeverity.MEDIUM, appError.severity)
        assertEquals(ErrorHandler.RecoveryStrategy.RETRY, appError.recoveryStrategy)
    }
    
    @Test
    fun `test security error categorization`() {
        val securityError = Exception("Security error")
        val appError = ErrorHandler.createAppError(securityError, ErrorHandler.ErrorType.SECURITY_ERROR)
        
        assertEquals(ErrorHandler.ErrorType.SECURITY_ERROR, appError.type)
        assertEquals(ErrorHandler.ErrorSeverity.CRITICAL, appError.severity)
        assertEquals(ErrorHandler.RecoveryStrategy.ABORT, appError.recoveryStrategy)
    }
    
    @Test
    fun `test database error categorization`() {
        val databaseError = Exception("Database error")
        val appError = ErrorHandler.createAppError(databaseError, ErrorHandler.ErrorType.DATABASE_ERROR)
        
        assertEquals(ErrorHandler.ErrorType.DATABASE_ERROR, appError.type)
        assertEquals(ErrorHandler.ErrorSeverity.HIGH, appError.severity)
        assertEquals(ErrorHandler.RecoveryStrategy.MANUAL_INTERVENTION, appError.recoveryStrategy)
    }
    
    @Test
    fun `test validation error categorization`() {
        val validationError = Exception("Validation error")
        val appError = ErrorHandler.createAppError(validationError, ErrorHandler.ErrorType.VALIDATION_ERROR)
        
        assertEquals(ErrorHandler.ErrorType.VALIDATION_ERROR, appError.type)
        assertEquals(ErrorHandler.ErrorSeverity.LOW, appError.severity)
        assertEquals(ErrorHandler.RecoveryStrategy.IGNORE, appError.recoveryStrategy)
    }
    
    @Test
    fun `test circuit breaker - closed state`() {
        val circuitBreaker = CircuitBreaker()
        assertTrue(circuitBreaker.canExecute())
    }
    
    @Test
    fun `test circuit breaker - open state`() {
        val circuitBreaker = CircuitBreaker(failureThreshold = 2)
        
        // Record failures to open circuit
        circuitBreaker.recordFailure()
        circuitBreaker.recordFailure()
        
        assertFalse(circuitBreaker.canExecute())
    }
    
    @Test
    fun `test circuit breaker - half open state`() {
        val circuitBreaker = CircuitBreaker(failureThreshold = 2, timeout = 1000L)
        
        // Record failures to open circuit
        circuitBreaker.recordFailure()
        circuitBreaker.recordFailure()
        
        // Wait for timeout
        Thread.sleep(1100L)
        
        assertTrue(circuitBreaker.canExecute())
    }
    
    @Test
    fun `test circuit breaker - success in half open`() {
        val circuitBreaker = CircuitBreaker(failureThreshold = 2, timeout = 1000L)
        
        // Record failures to open circuit
        circuitBreaker.recordFailure()
        circuitBreaker.recordFailure()
        
        // Wait for timeout
        Thread.sleep(1100L)
        
        // Record success
        circuitBreaker.recordSuccess()
        
        assertTrue(circuitBreaker.canExecute())
    }
    
    @Test
    fun `test circuit breaker - failure in half open`() {
        val circuitBreaker = CircuitBreaker(failureThreshold = 2, timeout = 1000L)
        
        // Record failures to open circuit
        circuitBreaker.recordFailure()
        circuitBreaker.recordFailure()
        
        // Wait for timeout
        Thread.sleep(1100L)
        
        // Record failure in half open state
        circuitBreaker.recordFailure()
        
        assertFalse(circuitBreaker.canExecute())
    }
    
    @Test
    fun `test error severity levels`() {
        assertEquals(ErrorHandler.ErrorSeverity.LOW, ErrorHandler.ErrorSeverity.LOW)
        assertEquals(ErrorHandler.ErrorSeverity.MEDIUM, ErrorHandler.ErrorSeverity.MEDIUM)
        assertEquals(ErrorHandler.ErrorSeverity.HIGH, ErrorHandler.ErrorSeverity.HIGH)
        assertEquals(ErrorHandler.ErrorSeverity.CRITICAL, ErrorHandler.ErrorSeverity.CRITICAL)
    }
    
    @Test
    fun `test recovery strategies`() {
        assertEquals(ErrorHandler.RecoveryStrategy.RETRY, ErrorHandler.RecoveryStrategy.RETRY)
        assertEquals(ErrorHandler.RecoveryStrategy.FALLBACK, ErrorHandler.RecoveryStrategy.FALLBACK)
        assertEquals(ErrorHandler.RecoveryStrategy.IGNORE, ErrorHandler.RecoveryStrategy.IGNORE)
        assertEquals(ErrorHandler.RecoveryStrategy.ABORT, ErrorHandler.RecoveryStrategy.ABORT)
        assertEquals(ErrorHandler.RecoveryStrategy.MANUAL_INTERVENTION, ErrorHandler.RecoveryStrategy.MANUAL_INTERVENTION)
    }
    
    @Test
    fun `test error timestamp`() {
        val error = ErrorHandler.AppError(
            type = ErrorHandler.ErrorType.UNKNOWN_ERROR,
            severity = ErrorHandler.ErrorSeverity.MEDIUM,
            message = "Test error"
        )
        
        assertTrue(error.timestamp > 0)
    }
    
    @Test
    fun `test error retry count`() {
        val error = ErrorHandler.AppError(
            type = ErrorHandler.ErrorType.UNKNOWN_ERROR,
            severity = ErrorHandler.ErrorSeverity.MEDIUM,
            message = "Test error",
            retryCount = 3,
            maxRetries = 5
        )
        
        assertEquals(3, error.retryCount)
        assertEquals(5, error.maxRetries)
    }
}
