package com.example.androidkmm.utils

/**
 * Centralized logging utility for the Android KMM application
 * 
 * Provides consistent logging across all platforms with different log levels.
 * Replaces scattered println statements with a structured logging approach.
 * 
 * Usage:
 * ```kotlin
 * Logger.debug("User clicked on transaction", "TransactionScreen")
 * Logger.error("Database connection failed", "DatabaseManager", exception)
 * ```
 */
object Logger {
    
    /**
     * Log levels for different types of messages
     */
    enum class LogLevel {
        DEBUG, INFO, WARNING, ERROR
    }
    
    /**
     * Log a debug message
     * 
     * @param message The message to log
     * @param tag Optional tag to identify the source
     */
    fun debug(message: String, tag: String = "App") {
        log(LogLevel.DEBUG, message, tag)
    }
    
    /**
     * Log an info message
     * 
     * @param message The message to log
     * @param tag Optional tag to identify the source
     */
    fun info(message: String, tag: String = "App") {
        log(LogLevel.INFO, message, tag)
    }
    
    /**
     * Log a warning message
     * 
     * @param message The message to log
     * @param tag Optional tag to identify the source
     */
    fun warning(message: String, tag: String = "App") {
        log(LogLevel.WARNING, message, tag)
    }
    
    /**
     * Log an error message
     * 
     * @param message The message to log
     * @param tag Optional tag to identify the source
     * @param throwable Optional exception to include
     */
    fun error(message: String, tag: String = "App", throwable: Throwable? = null) {
        log(LogLevel.ERROR, message, tag, throwable)
    }
    
    /**
     * Internal logging method
     * 
     * @param level The log level
     * @param message The message to log
     * @param tag The source tag
     * @param throwable Optional exception
     */
    private fun log(level: LogLevel, message: String, tag: String, throwable: Throwable? = null) {
        val timestamp = getCurrentTimestamp()
        val levelString = level.name.padEnd(7)
        val tagString = "[$tag]"
        
        val logMessage = when (throwable) {
            null -> "$timestamp $levelString $tagString $message"
            else -> "$timestamp $levelString $tagString $message\nException: ${throwable.message}\n${throwable.stackTraceToString()}"
        }
        
        // Use platform-specific logging
        when (level) {
            LogLevel.DEBUG -> println("DEBUG: $logMessage")
            LogLevel.INFO -> println("INFO: $logMessage")
            LogLevel.WARNING -> println("WARNING: $logMessage")
            LogLevel.ERROR -> println("ERROR: $logMessage")
        }
    }
    
    /**
     * Get current timestamp for logging
     * 
     * @return Formatted timestamp string
     */
    private fun getCurrentTimestamp(): String {
        return try {
            // Simple timestamp without complex time API
            val now = System.currentTimeMillis()
            val date = java.util.Date(now)
            val formatter = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            formatter.format(date)
        } catch (e: Exception) {
            "Unknown"
        }
    }
}
