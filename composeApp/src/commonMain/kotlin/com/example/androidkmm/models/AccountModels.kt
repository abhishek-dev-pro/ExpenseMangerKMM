package com.example.androidkmm.models

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class Account(
    val id: String = "",
    val name: String,
    val balance: String,
    val icon: ImageVector,
    val color: Color,
    val type: String,
    val isCustom: Boolean = false,
    val isArchived: Boolean = false
)

enum class AccountType {
    CASH,
    BANK_ACCOUNT,
    CREDIT_CARD,
    DEBIT_CARD,
    DIGITAL_WALLET,
    SAVINGS,
    CURRENT,
    SHARED
}
