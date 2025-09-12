package com.example.androidkmm.models

data class AppSetting(
    val key: String,
    val value: String,
    val updatedAt: Long
)

data class AppSettings(
    val carryForwardEnabled: Boolean = false,
    val currencySymbol: String = "$",
    val dateFormat: String = "MMM dd, yyyy"
)
