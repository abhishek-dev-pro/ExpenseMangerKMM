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
    val personId: String,
    val amount: Double,
    val title: String,
    val description: String,
    val date: String,
    val time: String,
    val type: TransactionType,
    val account: String? = null,
    val balanceAtTime: Double = 0.0
)

enum class TransactionType {
    SENT, RECEIVED
}

