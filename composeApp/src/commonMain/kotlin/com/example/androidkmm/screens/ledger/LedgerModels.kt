package com.example.androidkmm.screens.ledger

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

// LedgerModels.kt
data class LedgerPerson(
    val id: String,
    val name: String,
    val avatarColor: Color,
    val balance: Double,
    val transactionCount: Int,
    val lastTransactionDate: String
)

data class LedgerTransaction(
    val id: String,
    val amount: Double,
    val description: String,
    val date: String,
    val time: String,
    val type: TransactionType,
    val account: String? = null
)

enum class TransactionType {
    SENT, RECEIVED
}

// Data classes for account selection
data class Account(
    val id: String,
    val name: String,
    val balance: String,
    val icon: ImageVector,
    val color: Color,
    val type: String
)
