package com.example.androidkmm.utils

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

/**
 * Platform-agnostic date formatting utilities
 */
object DateFormatUtils {
    
    fun formatDate(date: LocalDate, pattern: String): String {
        return date.toString() // Simple implementation for now
    }
    
    fun formatDateTime(dateTime: LocalDateTime, pattern: String): String {
        return dateTime.toString() // Simple implementation for now
    }
    
    fun formatDouble(value: Double, pattern: String): String {
        return when (pattern) {
            "%.2f" -> formatDouble2Decimals(value)
            "%.1f" -> formatDouble1Decimal(value)
            else -> value.toString()
        }
    }
    
    fun formatFloat(value: Float, pattern: String): String {
        return when (pattern) {
            "%.2f" -> formatDouble2Decimals(value.toDouble())
            "%.1f" -> formatDouble1Decimal(value.toDouble())
            else -> value.toString()
        }
    }
    
    fun formatInt(value: Int, pattern: String): String {
        return when (pattern) {
            "%02d" -> formatInt2Digits(value)
            "%04d" -> formatInt4Digits(value)
            "%02x" -> formatIntHex(value)
            else -> value.toString()
        }
    }
    
    // Manual formatting functions
    private fun formatDouble2Decimals(value: Double): String {
        val rounded = (value * 100.0).toLong()
        val integerPart = rounded / 100
        val decimalPart = (rounded % 100).toInt()
        return "$integerPart.${decimalPart.toString().padStart(2, '0')}"
    }
    
    private fun formatDouble1Decimal(value: Double): String {
        val rounded = (value * 10.0).toLong()
        val integerPart = rounded / 10
        val decimalPart = (rounded % 10).toInt()
        return "$integerPart.$decimalPart"
    }
    
    private fun formatInt2Digits(value: Int): String {
        return if (value < 10) "0$value" else value.toString()
    }
    
    private fun formatInt4Digits(value: Int): String {
        return when {
            value < 10 -> "000$value"
            value < 100 -> "00$value"
            value < 1000 -> "0$value"
            else -> value.toString()
        }
    }
    
    private fun formatIntHex(value: Int): String {
        val hex = value.toString(16)
        return if (hex.length < 2) "0$hex" else hex
    }
}
