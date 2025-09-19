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

/**
 * Removes all common currency symbols from a string to get the numeric value
 */
fun removeCurrencySymbols(balanceString: String): String {
    return balanceString
        .replace("$", "")
        .replace("₹", "")
        .replace("€", "")
        .replace("£", "")
        .replace("¥", "")
        .replace("₩", "")
        .replace("₽", "")
        .replace("₦", "")
        .replace("₨", "")
        .replace("₵", "")
        .replace("kr", "")
        .replace("zł", "")
        .replace("Kč", "")
        .replace("Ft", "")
        .replace("R$", "")
        .replace("S/", "")
        .replace("Bs", "")
        .replace("R", "")
        .replace("KSh", "")
        .replace("₵", "")
        .replace("د.م.", "")
        .replace("د.ت", "")
        .replace("د.ج", "")
        .replace("ل.د", "")
        .replace("Br", "")
        .replace("USh", "")
        .replace("TSh", "")
        .replace("ZK", "")
        .replace("P", "")
        .replace("L", "")
        .replace("N$", "")
        .replace("Kz", "")
        .replace("MT", "")
        .replace("MK", "")
        .replace("Z$", "")
        .replace("FBu", "")
        .replace("RF", "")
        .replace("Fdj", "")
        .replace("S", "")
        .replace("Nfk", "")
        .replace("CF", "")
        .replace("Ar", "")
        .replace("Le", "")
        .replace("D", "")
        .replace("FG", "")
        .replace("L$", "")
        .replace("FC", "")
        .replace("FCFA", "")
        .replace("CFA", "")
        .replace("₣", "")
        .replace("+", "")
        .replace("-", "")
        .replace(",", "")
        .trim()
}
