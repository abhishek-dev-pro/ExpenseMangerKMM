package com.example.androidkmm.utils

/**
 * Utility functions for currency operations
 */
object CurrencyUtils {
    
    /**
     * Removes currency symbols from a string and returns the numeric value
     */
    fun removeCurrencySymbols(amount: String): String {
        return amount.replace(Regex("[₹$€£¥₽₩₪₫₨₴₸₺₼₾₿]"), "").trim()
    }
    
    /**
     * Gets the currency symbol based on locale or default to ₹
     */
    fun getCurrencySymbol(): String {
        return "₹" // Default to Indian Rupee
    }
    
    /**
     * Gets the currency symbol with a parameter (for backward compatibility)
     */
    fun getCurrencySymbol(locale: String = "IN"): String {
        return "₹" // Default to Indian Rupee
    }
    
    /**
     * Formats a double value to 2 decimal places
     */
    fun formatDouble(value: Double): String {
        return String.format("%.2f", value)
    }
    
    /**
     * Formats a double value with custom decimal places (for backward compatibility)
     */
    fun formatDouble(value: Double, decimalPlaces: Int = 2): String {
        return String.format("%.${decimalPlaces}f", value)
    }
}
