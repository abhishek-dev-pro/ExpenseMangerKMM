package com.example.androidkmm.utils

import androidx.compose.ui.input.key.Key.Companion.U
import kotlin.math.pow
import kotlin.math.round

fun formatDouble(value: Double, digits: Int = 2): String {
    val multiplier = 10.0.pow(digits)
    val rounded = round(value * multiplier) / multiplier
    val parts = rounded.toString().split(".")
    return if (parts.size == 1) {
        "${parts[0]}.${"0".repeat(digits)}"
    } else {
        val decimal = parts[1].padEnd(digits, '0').take(digits)
        "${parts[0]}.$decimal"
    }
}

/**
 * Maps currency codes to their respective symbols
 */
fun getCurrencySymbol(currencyCode: String): String {
    return when (currencyCode.uppercase()) {
        "USD" -> "$"
        "EUR" -> "€"
        "GBP" -> "£"
        "INR" -> "₹"
        "JPY" -> "¥"
        "CAD" -> "C$"
        "AUD" -> "A$"
        "CHF" -> "CHF"
        "CNY" -> "¥"
        "KRW" -> "₩"
        "SGD" -> "S$"
        "HKD" -> "HK$"
        "NZD" -> "NZ$"
        "SEK" -> "kr"
        "NOK" -> "kr"
        "DKK" -> "kr"
        "PLN" -> "zł"
        "CZK" -> "Kč"
        "HUF" -> "Ft"
        "RUB" -> "₽"
        "BRL" -> "R$"
        "MXN" -> "$"
        "ARS" -> "$"
        "CLP" -> "$"
        "COP" -> "$"
        "PEN" -> "S/"
        "UYU" -> "$U"
        "VEF" -> "Bs"
        "ZAR" -> "R"
        "EGP" -> "£"
        "NGN" -> "₦"
        "KES" -> "KSh"
        "GHS" -> "₵"
        "MAD" -> "د.م."
        "TND" -> "د.ت"
        "DZD" -> "د.ج"
        "LYD" -> "ل.د"
        "ETB" -> "Br"
        "UGX" -> "USh"
        "TZS" -> "TSh"
        "ZMW" -> "ZK"
        "BWP" -> "P"
        "SZL" -> "L"
        "LSL" -> "L"
        "NAD" -> "N$"
        "AOA" -> "Kz"
        "MZN" -> "MT"
        "MWK" -> "MK"
        "ZWL" -> "Z$"
        "BIF" -> "FBu"
        "RWF" -> "RF"
        "DJF" -> "Fdj"
        "SOS" -> "S"
        "ERN" -> "Nfk"
        "ETB" -> "Br"
        "KMF" -> "CF"
        "MGA" -> "Ar"
        "MUR" -> "₨"
        "SCR" -> "₨"
        "SLL" -> "Le"
        "GMD" -> "D"
        "GNF" -> "FG"
        "LRD" -> "L$"
        "CDF" -> "FC"
        "XAF" -> "FCFA"
        "XOF" -> "CFA"
        "XPF" -> "₣"
        else -> "$" // Default to dollar if currency not found
    }
}

/**
 * Formats an amount with the appropriate currency symbol
 */
fun formatCurrency(amount: Double, currencyCode: String = "USD"): String {
    val symbol = getCurrencySymbol(currencyCode)
    val formattedAmount = formatDouble(amount, 2)
    return "$symbol$formattedAmount"
}
