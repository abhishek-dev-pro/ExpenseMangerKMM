package com.example.androidkmm.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Comprehensive error handling for database operations
 */
object DatabaseErrorHandler {
    
    /**
     * Error types for database operations
     */
    enum class DatabaseErrorType {
        CONNECTION_ERROR,
        CONSTRAINT_VIOLATION,
        DATA_INTEGRITY_ERROR,
        TRANSACTION_ERROR,
        ROLLBACK_ERROR,
        VALIDATION_ERROR,
        UNKNOWN_ERROR
    }
    
    /**
     * Database error data class
     */
    data class DatabaseError(
        val type: DatabaseErrorType,
        val message: String,
        val originalException: Throwable? = null,
        val operation: String = "",
        val timestamp: Long = System.currentTimeMillis()
    )
    
    /**
     * Error recovery strategies
     */
    enum class RecoveryStrategy {
        RETRY,
        ROLLBACK,
        SKIP,
        ABORT,
        MANUAL_INTERVENTION
    }
    
    /**
     * Handle database operation with comprehensive error handling
     */
    suspend fun <T> handleDatabaseOperation(
        operation: suspend () -> T,
        operationName: String = "Database Operation",
        onSuccess: (T) -> Unit = {},
        onError: (DatabaseError) -> Unit = {},
        retryCount: Int = 3,
        scope: CoroutineScope? = null
    ): Result<T> {
        var lastError: DatabaseError? = null
        var attempts = 0
        
        while (attempts < retryCount) {
            try {
                val result = operation()
                onSuccess(result)
                return Result.success(result)
            } catch (e: Exception) {
                lastError = createDatabaseError(e, operationName)
                attempts++
                
                val recoveryStrategy = determineRecoveryStrategy(lastError)
                
                when (recoveryStrategy) {
                    RecoveryStrategy.RETRY -> {
                        if (attempts < retryCount) {
                            println("DEBUG: Retrying database operation '$operationName' (attempt $attempts/$retryCount)")
                            kotlinx.coroutines.delay(1000L * attempts) // Exponential backoff
                            continue
                        }
                    }
                    RecoveryStrategy.ROLLBACK -> {
                        println("ERROR: Rollback required for operation '$operationName'")
                        onError(lastError)
                        return Result.failure(lastError.originalException ?: Exception(lastError.message))
                    }
                    RecoveryStrategy.SKIP -> {
                        println("WARNING: Skipping operation '$operationName' due to error")
                        onError(lastError)
                        return Result.failure(lastError.originalException ?: Exception(lastError.message))
                    }
                    RecoveryStrategy.ABORT -> {
                        println("ERROR: Aborting operation '$operationName' due to critical error")
                        onError(lastError)
                        return Result.failure(lastError.originalException ?: Exception(lastError.message))
                    }
                    RecoveryStrategy.MANUAL_INTERVENTION -> {
                        println("CRITICAL: Manual intervention required for operation '$operationName'")
                        onError(lastError)
                        return Result.failure(lastError.originalException ?: Exception(lastError.message))
                    }
                }
            }
        }
        
        // All retries failed
        onError(lastError ?: DatabaseError(
            DatabaseErrorType.UNKNOWN_ERROR,
            "Unknown error occurred during database operation",
            operation = operationName
        ))
        return Result.failure(lastError?.originalException ?: Exception("Database operation failed after $retryCount attempts"))
    }
    
    /**
     * Create database error from exception
     */
    private fun createDatabaseError(exception: Throwable, operation: String): DatabaseError {
        val errorType = when {
            exception.message?.contains("constraint", ignoreCase = true) == true -> DatabaseErrorType.CONSTRAINT_VIOLATION
            exception.message?.contains("integrity", ignoreCase = true) == true -> DatabaseErrorType.DATA_INTEGRITY_ERROR
            exception.message?.contains("transaction", ignoreCase = true) == true -> DatabaseErrorType.TRANSACTION_ERROR
            exception.message?.contains("connection", ignoreCase = true) == true -> DatabaseErrorType.CONNECTION_ERROR
            exception.message?.contains("validation", ignoreCase = true) == true -> DatabaseErrorType.VALIDATION_ERROR
            else -> DatabaseErrorType.UNKNOWN_ERROR
        }
        
        return DatabaseError(
            type = errorType,
            message = exception.message ?: "Unknown database error",
            originalException = exception,
            operation = operation
        )
    }
    
    /**
     * Determine recovery strategy based on error type
     */
    private fun determineRecoveryStrategy(error: DatabaseError): RecoveryStrategy {
        return when (error.type) {
            DatabaseErrorType.CONNECTION_ERROR -> RecoveryStrategy.RETRY
            DatabaseErrorType.CONSTRAINT_VIOLATION -> RecoveryStrategy.ROLLBACK
            DatabaseErrorType.DATA_INTEGRITY_ERROR -> RecoveryStrategy.MANUAL_INTERVENTION
            DatabaseErrorType.TRANSACTION_ERROR -> RecoveryStrategy.ROLLBACK
            DatabaseErrorType.ROLLBACK_ERROR -> RecoveryStrategy.MANUAL_INTERVENTION
            DatabaseErrorType.VALIDATION_ERROR -> RecoveryStrategy.SKIP
            DatabaseErrorType.UNKNOWN_ERROR -> RecoveryStrategy.ABORT
        }
    }
    
    /**
     * Handle transaction with rollback capability
     */
    suspend fun <T> handleTransaction(
        transaction: suspend () -> T,
        rollback: suspend () -> Unit = {},
        operationName: String = "Transaction",
        onSuccess: (T) -> Unit = {},
        onError: (DatabaseError) -> Unit = {},
        scope: CoroutineScope? = null
    ): Result<T> {
        return handleDatabaseOperation(
            operation = {
                try {
                    transaction()
                } catch (e: Exception) {
                    // Attempt rollback on failure
                    try {
                        rollback()
                        println("DEBUG: Rollback completed successfully")
                    } catch (rollbackError: Exception) {
                        println("ERROR: Rollback failed: ${rollbackError.message}")
                        Logger.error("Rollback failed", "DatabaseErrorHandler", rollbackError)
                    }
                    throw e
                }
            },
            operationName = operationName,
            onSuccess = onSuccess,
            onError = onError,
            retryCount = 1, // Don't retry transactions
            scope = scope
        )
    }
    
    /**
     * Handle batch operations with individual error handling
     */
    suspend fun <T> handleBatchOperation(
        operations: List<suspend () -> T>,
        operationName: String = "Batch Operation",
        onSuccess: (List<T>) -> Unit = {},
        onError: (DatabaseError) -> Unit = {},
        continueOnError: Boolean = false,
        scope: CoroutineScope? = null
    ): Result<List<T>> {
        val results = mutableListOf<T>()
        val errors = mutableListOf<DatabaseError>()
        
        for ((index, operation) in operations.withIndex()) {
            val result = handleDatabaseOperation(
                operation = operation,
                operationName = "$operationName (item ${index + 1})",
                onSuccess = { results.add(it) },
                onError = { errors.add(it) },
                retryCount = 1,
                scope = scope
            )
            
            if (result.isFailure && !continueOnError) {
                // Stop on first error if continueOnError is false
                return Result.failure(result.exceptionOrNull() ?: Exception("Batch operation failed"))
            }
        }
        
        if (errors.isNotEmpty()) {
            val errorMessage = "Batch operation completed with ${errors.size} errors"
            println("WARNING: $errorMessage")
            onError(DatabaseError(
                DatabaseErrorType.UNKNOWN_ERROR,
                errorMessage,
                operation = operationName
            ))
        }
        
        onSuccess(results)
        return Result.success(results)
    }
    
    /**
     * Log database error with context
     */
    fun logDatabaseError(error: DatabaseError, context: String = "") {
        val logMessage = "Database Error [${error.type}] in ${error.operation}: ${error.message}"
        println("ERROR: $logMessage")
        Logger.error(logMessage, "DatabaseErrorHandler", error.originalException)
        
        if (context.isNotEmpty()) {
            println("CONTEXT: $context")
        }
    }
    
    /**
     * Get user-friendly error message
     */
    fun getUserFriendlyMessage(error: DatabaseError): String {
        return when (error.type) {
            DatabaseErrorType.CONNECTION_ERROR -> "Unable to connect to database. Please check your connection and try again."
            DatabaseErrorType.CONSTRAINT_VIOLATION -> "The data you entered conflicts with existing data. Please check your input and try again."
            DatabaseErrorType.DATA_INTEGRITY_ERROR -> "The data you entered is invalid. Please check your input and try again."
            DatabaseErrorType.TRANSACTION_ERROR -> "Unable to complete the transaction. Please try again."
            DatabaseErrorType.ROLLBACK_ERROR -> "Unable to undo the previous operation. Please contact support."
            DatabaseErrorType.VALIDATION_ERROR -> "The data you entered is not valid. Please check your input and try again."
            DatabaseErrorType.UNKNOWN_ERROR -> "An unexpected error occurred. Please try again or contact support."
        }
    }
}
