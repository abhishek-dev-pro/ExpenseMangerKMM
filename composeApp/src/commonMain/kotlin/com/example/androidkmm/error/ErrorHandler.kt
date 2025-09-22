package com.example.androidkmm.error

import com.example.androidkmm.utils.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Comprehensive error handling system
 * 
 * Features:
 * - Centralized error handling
 * - Retry logic with exponential backoff
 * - Error categorization and recovery strategies
 * - User-friendly error messages
 * - Error logging and monitoring
 * - Circuit breaker pattern
 */
object ErrorHandler {
    
    /**
     * Error types for categorization
     */
    enum class ErrorType {
        NETWORK_ERROR,
        DATABASE_ERROR,
        VALIDATION_ERROR,
        SECURITY_ERROR,
        BUSINESS_LOGIC_ERROR,
        UNKNOWN_ERROR
    }
    
    /**
     * Error severity levels
     */
    enum class ErrorSeverity {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }
    
    /**
     * Error recovery strategies
     */
    enum class RecoveryStrategy {
        RETRY,
        FALLBACK,
        IGNORE,
        ABORT,
        MANUAL_INTERVENTION
    }
    
    /**
     * Application error data class
     */
    data class AppError(
        val type: ErrorType,
        val severity: ErrorSeverity,
        val message: String,
        val originalException: Throwable? = null,
        val recoveryStrategy: RecoveryStrategy = RecoveryStrategy.ABORT,
        val retryCount: Int = 0,
        val maxRetries: Int = 3,
        val timestamp: Long = System.currentTimeMillis()
    )
    
    /**
     * Handle error with retry logic
     */
    suspend fun <T> handleWithRetry(
        operation: suspend () -> T,
        errorType: ErrorType = ErrorType.UNKNOWN_ERROR,
        maxRetries: Int = 3,
        initialDelay: Long = 1000L,
        scope: CoroutineScope? = null
    ): Result<T> {
        var lastError: AppError? = null
        var attempts = 0
        
        while (attempts < maxRetries) {
            try {
                val result = operation()
                return Result.success(result)
            } catch (e: Exception) {
                lastError = createAppError(e, errorType)
                attempts++
                
                if (attempts < maxRetries) {
                    val delay = initialDelay * (1L shl attempts) // Exponential backoff
                    Logger.warning("Retrying operation (attempt $attempts/$maxRetries) after ${delay}ms", "ErrorHandler")
                    delay(delay)
                }
            }
        }
        
        return Result.failure(
            lastError?.originalException ?: Exception(lastError?.message ?: "Operation failed")
        )
    }
    
    /**
     * Handle error with fallback
     */
    suspend fun <T> handleWithFallback(
        operation: suspend () -> T,
        fallback: suspend () -> T,
        errorType: ErrorType = ErrorType.UNKNOWN_ERROR
    ): Result<T> {
        return try {
            val result = operation()
            Result.success(result)
        } catch (e: Exception) {
            Logger.warning("Operation failed, using fallback", "ErrorHandler")
            try {
                val fallbackResult = fallback()
                Result.success(fallbackResult)
            } catch (fallbackError: Exception) {
                val appError = createAppError(fallbackError, errorType)
                Logger.error("Both operation and fallback failed", "ErrorHandler", fallbackError)
                Result.failure(appError.originalException ?: Exception(appError.message))
            }
        }
    }
    
    /**
     * Handle error with circuit breaker
     */
    suspend fun <T> handleWithCircuitBreaker(
        operation: suspend () -> T,
        circuitBreaker: CircuitBreaker,
        errorType: ErrorType = ErrorType.UNKNOWN_ERROR
    ): Result<T> {
        return if (circuitBreaker.canExecute()) {
            try {
                val result = operation()
                circuitBreaker.recordSuccess()
                Result.success(result)
            } catch (e: Exception) {
                circuitBreaker.recordFailure()
                val appError = createAppError(e, errorType)
                Logger.error("Operation failed in circuit breaker", "ErrorHandler", e)
                Result.failure(appError.originalException ?: Exception(appError.message))
            }
        } else {
            Logger.warning("Circuit breaker is open, operation skipped", "ErrorHandler")
            Result.failure(Exception("Circuit breaker is open"))
        }
    }
    
    /**
     * Create application error from exception
     */
    private fun createAppError(exception: Throwable, type: ErrorType): AppError {
        val severity = determineSeverity(exception, type)
        val recoveryStrategy = determineRecoveryStrategy(type, severity)
        
        return AppError(
            type = type,
            severity = severity,
            message = getUserFriendlyMessage(exception, type),
            originalException = exception,
            recoveryStrategy = recoveryStrategy
        )
    }
    
    /**
     * Determine error severity
     */
    private fun determineSeverity(exception: Throwable, type: ErrorType): ErrorSeverity {
        return when (type) {
            ErrorType.SECURITY_ERROR -> ErrorSeverity.CRITICAL
            ErrorType.DATABASE_ERROR -> ErrorSeverity.HIGH
            ErrorType.NETWORK_ERROR -> ErrorSeverity.MEDIUM
            ErrorType.VALIDATION_ERROR -> ErrorSeverity.LOW
            ErrorType.BUSINESS_LOGIC_ERROR -> ErrorSeverity.MEDIUM
            ErrorType.UNKNOWN_ERROR -> ErrorSeverity.HIGH
        }
    }
    
    /**
     * Determine recovery strategy
     */
    private fun determineRecoveryStrategy(type: ErrorType, severity: ErrorSeverity): RecoveryStrategy {
        return when (type) {
            ErrorType.NETWORK_ERROR -> RecoveryStrategy.RETRY
            ErrorType.DATABASE_ERROR -> when (severity) {
                ErrorSeverity.LOW -> RecoveryStrategy.RETRY
                ErrorSeverity.MEDIUM -> RecoveryStrategy.FALLBACK
                ErrorSeverity.HIGH -> RecoveryStrategy.MANUAL_INTERVENTION
                ErrorSeverity.CRITICAL -> RecoveryStrategy.ABORT
            }
            ErrorType.VALIDATION_ERROR -> RecoveryStrategy.IGNORE
            ErrorType.SECURITY_ERROR -> RecoveryStrategy.ABORT
            ErrorType.BUSINESS_LOGIC_ERROR -> RecoveryStrategy.FALLBACK
            ErrorType.UNKNOWN_ERROR -> RecoveryStrategy.ABORT
        }
    }
    
    /**
     * Get user-friendly error message
     */
    private fun getUserFriendlyMessage(exception: Throwable, type: ErrorType): String {
        return when (type) {
            ErrorType.NETWORK_ERROR -> "Network connection failed. Please check your internet connection."
            ErrorType.DATABASE_ERROR -> "Data operation failed. Please try again."
            ErrorType.VALIDATION_ERROR -> "Please check your input and try again."
            ErrorType.SECURITY_ERROR -> "Security error occurred. Please contact support."
            ErrorType.BUSINESS_LOGIC_ERROR -> "Operation failed. Please try again."
            ErrorType.UNKNOWN_ERROR -> "An unexpected error occurred. Please try again."
        }
    }
    
    /**
     * Log error with appropriate level
     */
    fun logError(error: AppError) {
        when (error.severity) {
            ErrorSeverity.LOW -> Logger.warning("Low severity error: ${error.message}", "ErrorHandler")
            ErrorSeverity.MEDIUM -> Logger.warning("Medium severity error: ${error.message}", "ErrorHandler")
            ErrorSeverity.HIGH -> Logger.error("High severity error: ${error.message}", "ErrorHandler", error.originalException)
            ErrorSeverity.CRITICAL -> Logger.error("Critical error: ${error.message}", "ErrorHandler", error.originalException)
        }
    }
}

/**
 * Circuit breaker implementation
 */
class CircuitBreaker(
    private val failureThreshold: Int = 5,
    private val timeout: Long = 60000L, // 1 minute
    private val halfOpenMaxCalls: Int = 3
) {
    private var state = CircuitBreakerState.CLOSED
    private var failureCount = 0
    private var lastFailureTime = 0L
    private var halfOpenCalls = 0
    
    enum class CircuitBreakerState {
        CLOSED,
        OPEN,
        HALF_OPEN
    }
    
    fun canExecute(): Boolean {
        return when (state) {
            CircuitBreakerState.CLOSED -> true
            CircuitBreakerState.OPEN -> {
                if (System.currentTimeMillis() - lastFailureTime > timeout) {
                    state = CircuitBreakerState.HALF_OPEN
                    halfOpenCalls = 0
                    true
                } else {
                    false
                }
            }
            CircuitBreakerState.HALF_OPEN -> halfOpenCalls < halfOpenMaxCalls
        }
    }
    
    fun recordSuccess() {
        when (state) {
            CircuitBreakerState.CLOSED -> {
                // Already closed, no action needed
            }
            CircuitBreakerState.OPEN -> {
                // Should not happen, but handle gracefully
            }
            CircuitBreakerState.HALF_OPEN -> {
                state = CircuitBreakerState.CLOSED
                failureCount = 0
                halfOpenCalls = 0
            }
        }
    }
    
    fun recordFailure() {
        failureCount++
        lastFailureTime = System.currentTimeMillis()
        
        when (state) {
            CircuitBreakerState.CLOSED -> {
                if (failureCount >= failureThreshold) {
                    state = CircuitBreakerState.OPEN
                }
            }
            CircuitBreakerState.OPEN -> {
                // Already open, no action needed
            }
            CircuitBreakerState.HALF_OPEN -> {
                state = CircuitBreakerState.OPEN
                halfOpenCalls = 0
            }
        }
    }
}
