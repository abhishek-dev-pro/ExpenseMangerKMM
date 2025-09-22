package com.example.androidkmm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidkmm.utils.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Base ViewModel with common functionality
 * 
 * Provides common state management patterns for all ViewModels.
 * Implements base functionality for loading, error handling, and state management.
 */
abstract class BaseViewModel : ViewModel() {
    
    // Common state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    /**
     * Set loading state
     */
    protected fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }
    
    /**
     * Set error state
     */
    protected fun setError(error: String?) {
        _error.value = error
    }
    
    /**
     * Clear error state
     */
    fun clearError() {
        _error.value = null
    }
    
    /**
     * Execute operation with error handling
     */
    protected fun executeOperation(
        operation: suspend () -> Unit,
        onError: (Throwable) -> Unit = { e ->
            Logger.error("Operation failed", "BaseViewModel", e)
            setError("Operation failed: ${e.message}")
        }
    ) {
        viewModelScope.launch {
            try {
                setLoading(true)
                setError(null)
                operation()
            } catch (e: Exception) {
                onError(e)
            } finally {
                setLoading(false)
            }
        }
    }
    
    /**
     * Execute operation with result handling
     */
    protected fun <T> executeOperationWithResult(
        operation: suspend () -> Result<T>,
        onSuccess: (T) -> Unit = {},
        onError: (Throwable) -> Unit = { e ->
            Logger.error("Operation failed", "BaseViewModel", e)
            setError("Operation failed: ${e.message}")
        }
    ) {
        viewModelScope.launch {
            try {
                setLoading(true)
                setError(null)
                
                val result = operation()
                result.fold(
                    onSuccess = { value ->
                        onSuccess(value)
                    },
                    onFailure = { error ->
                        onError(error)
                    }
                )
            } catch (e: Exception) {
                onError(e)
            } finally {
                setLoading(false)
            }
        }
    }
    
    /**
     * Execute operation with retry logic
     */
    protected fun executeOperationWithRetry(
        operation: suspend () -> Unit,
        maxRetries: Int = 3,
        onError: (Exception) -> Unit = { e ->
            Logger.error("Operation failed after $maxRetries retries", "BaseViewModel", e)
            setError("Operation failed after $maxRetries retries: ${e.message}")
        }
    ) {
        viewModelScope.launch {
            var retryCount = 0
            var lastException: Exception? = null
            
            while (retryCount < maxRetries) {
                try {
                    setLoading(true)
                    setError(null)
                    operation()
                    return@launch // Success, exit retry loop
                } catch (e: Exception) {
                    lastException = e
                    retryCount++
                    Logger.warning("Operation failed, retry $retryCount/$maxRetries", "BaseViewModel")
                    
                    if (retryCount < maxRetries) {
                        // Wait before retry (exponential backoff)
                        kotlinx.coroutines.delay(1000L * retryCount)
                    }
                }
            }
            
            // All retries failed
            onError(lastException ?: Exception("Operation failed"))
            setLoading(false)
        }
    }
    
    /**
     * Execute operation with fallback
     */
    protected fun <T> executeOperationWithFallback(
        primaryOperation: suspend () -> T,
        fallbackOperation: suspend () -> T,
        onError: (Exception) -> Unit = { e ->
            Logger.error("Both primary and fallback operations failed", "BaseViewModel", e)
            setError("Operation failed: ${e.message}")
        }
    ) {
        viewModelScope.launch {
            try {
                setLoading(true)
                setError(null)
                
                val result = primaryOperation()
                // Success with primary operation
                return@launch
            } catch (e: Exception) {
                Logger.warning("Primary operation failed, trying fallback", "BaseViewModel")
                
                try {
                    val fallbackResult = fallbackOperation()
                    // Success with fallback operation
                    return@launch
                } catch (fallbackError: Exception) {
                    onError(fallbackError)
                }
            } finally {
                setLoading(false)
            }
        }
    }
    
    /**
     * Execute operation with timeout
     */
    protected fun executeOperationWithTimeout(
        operation: suspend () -> Unit,
        timeoutMs: Long = 30000L,
        onError: (Exception) -> Unit = { e ->
            Logger.error("Operation timed out", "BaseViewModel", e)
            setError("Operation timed out")
        }
    ) {
        viewModelScope.launch {
            try {
                setLoading(true)
                setError(null)
                
                kotlinx.coroutines.withTimeout(timeoutMs) {
                    operation()
                }
            } catch (e: Exception) {
                onError(e)
            } finally {
                setLoading(false)
            }
        }
    }
    
    /**
     * Execute operation with circuit breaker
     */
    protected fun executeOperationWithCircuitBreaker(
        operation: suspend () -> Unit,
        circuitBreaker: CircuitBreaker,
        onError: (Exception) -> Unit = { e ->
            Logger.error("Operation failed in circuit breaker", "BaseViewModel", e)
            setError("Operation failed: ${e.message}")
        }
    ) {
        viewModelScope.launch {
            try {
                setLoading(true)
                setError(null)
                
                if (circuitBreaker.canExecute()) {
                    operation()
                    circuitBreaker.recordSuccess()
                } else {
                    throw Exception("Circuit breaker is open")
                }
            } catch (e: Exception) {
                circuitBreaker.recordFailure()
                onError(e)
            } finally {
                setLoading(false)
            }
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
