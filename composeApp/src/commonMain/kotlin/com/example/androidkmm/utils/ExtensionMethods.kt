package com.example.androidkmm.utils

import kotlin.math.pow
import kotlin.math.round


// commonMain
fun Double.toMoney(digits: Int = 2): String {
    val factor = 10.0.pow(digits)
    return (kotlin.math.round(this * factor) / factor).toString()
}