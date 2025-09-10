package com.example.androidkmm.utils

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
