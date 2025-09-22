package com.example.androidkmm.error

import com.example.androidkmm.utils.Logger

/**
 * Extension functions for Result type
 * 
 * Provides convenient methods for handling Result types with proper error handling.
 */
inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (isSuccess) {
        try {
            action(getOrNull()!!)
        } catch (e: Exception) {
            Logger.error("Error in success handler", "ResultExtensions", e)
        }
    }
    return this
}

inline fun <T> Result<T>.onFailure(action: (Throwable) -> Unit): Result<T> {
    if (isFailure) {
        try {
            action(exceptionOrNull()!!)
        } catch (e: Exception) {
            Logger.error("Error in failure handler", "ResultExtensions", e)
        }
    }
    return this
}

inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> {
    return if (isSuccess) {
        try {
            Result.success(transform(getOrNull()!!))
        } catch (e: Exception) {
            Logger.error("Error in map transformation", "ResultExtensions", e)
            Result.failure(e)
        }
    } else {
        Result.failure(exceptionOrNull()!!)
    }
}

inline fun <T, R> Result<T>.flatMap(transform: (T) -> Result<R>): Result<R> {
    return if (isSuccess) {
        try {
            transform(getOrNull()!!)
        } catch (e: Exception) {
            Logger.error("Error in flatMap transformation", "ResultExtensions", e)
            Result.failure(e)
        }
    } else {
        Result.failure(exceptionOrNull()!!)
    }
}

inline fun <T> Result<T>.recover(transform: (Throwable) -> T): Result<T> {
    return if (isFailure) {
        try {
            Result.success(transform(exceptionOrNull()!!))
        } catch (e: Exception) {
            Logger.error("Error in recover transformation", "ResultExtensions", e)
            Result.failure(e)
        }
    } else {
        this
    }
}

inline fun <T> Result<T>.recoverWith(transform: (Throwable) -> Result<T>): Result<T> {
    return if (isFailure) {
        try {
            transform(exceptionOrNull()!!)
        } catch (e: Exception) {
            Logger.error("Error in recoverWith transformation", "ResultExtensions", e)
            Result.failure(e)
        }
    } else {
        this
    }
}

fun <T> Result<T>.getOrElse(defaultValue: T): T {
    return getOrNull() ?: defaultValue
}

fun <T> Result<T>.getOrElse(transform: (Throwable) -> T): T {
    return if (isSuccess) {
        getOrNull()!!
    } else {
        transform(exceptionOrNull()!!)
    }
}

fun <T> Result<T>.fold(
    onSuccess: (T) -> Unit,
    onFailure: (Throwable) -> Unit
) {
    if (isSuccess) {
        onSuccess(getOrNull()!!)
    } else {
        onFailure(exceptionOrNull()!!)
    }
}

fun <T> Result<T>.isSuccessAnd(predicate: (T) -> Boolean): Boolean {
    return isSuccess && predicate(getOrNull()!!)
}

fun <T> Result<T>.isFailureAnd(predicate: (Throwable) -> Boolean): Boolean {
    return isFailure && predicate(exceptionOrNull()!!)
}

/**
 * Combine multiple Results into a single Result
 */
fun <T1, T2> combine(
    result1: Result<T1>,
    result2: Result<T2>
): Result<Pair<T1, T2>> {
    return if (result1.isSuccess && result2.isSuccess) {
        Result.success(Pair(result1.getOrNull()!!, result2.getOrNull()!!))
    } else {
        val errors = mutableListOf<Throwable>()
        if (result1.isFailure) errors.add(result1.exceptionOrNull()!!)
        if (result2.isFailure) errors.add(result2.exceptionOrNull()!!)
        Result.failure(CombinedException(errors))
    }
}

/**
 * Combine multiple Results into a single Result with custom combiner
 */
fun <T1, T2, R> combine(
    result1: Result<T1>,
    result2: Result<T2>,
    combiner: (T1, T2) -> R
): Result<R> {
    return if (result1.isSuccess && result2.isSuccess) {
        try {
            Result.success(combiner(result1.getOrNull()!!, result2.getOrNull()!!))
        } catch (e: Exception) {
            Logger.error("Error in combine transformation", "ResultExtensions", e)
            Result.failure(e)
        }
    } else {
        val errors = mutableListOf<Throwable>()
        if (result1.isFailure) errors.add(result1.exceptionOrNull()!!)
        if (result2.isFailure) errors.add(result2.exceptionOrNull()!!)
        Result.failure(CombinedException(errors))
    }
}

/**
 * Execute multiple operations and return the first successful result
 */
suspend fun <T> firstSuccessOf(
    vararg operations: suspend () -> Result<T>
): Result<T> {
    val errors = mutableListOf<Throwable>()
    
    for (operation in operations) {
        try {
            val result = operation()
            if (result.isSuccess) {
                return result
            } else {
                errors.add(result.exceptionOrNull()!!)
            }
        } catch (e: Exception) {
            errors.add(e)
        }
    }
    
    return Result.failure(CombinedException(errors))
}

/**
 * Execute multiple operations and return all successful results
 */
suspend fun <T> allSuccessOf(
    vararg operations: suspend () -> Result<T>
): Result<List<T>> {
    val results = mutableListOf<T>()
    val errors = mutableListOf<Throwable>()
    
    for (operation in operations) {
        try {
            val result = operation()
            if (result.isSuccess) {
                results.add(result.getOrNull()!!)
            } else {
                errors.add(result.exceptionOrNull()!!)
            }
        } catch (e: Exception) {
            errors.add(e)
        }
    }
    
    return if (errors.isEmpty()) {
        Result.success(results)
    } else {
        Result.failure(CombinedException(errors))
    }
}

/**
 * Exception for combined failures
 */
class CombinedException(
    val errors: List<Throwable>
) : Exception("Multiple errors occurred: ${errors.joinToString { it.message ?: "Unknown error" }}") {
    override val message: String
        get() = "Combined exception with ${errors.size} errors: ${errors.joinToString { it.message ?: "Unknown error" }}"
}
