package com.example.androidkmm.models

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard

data class Transaction(
    val id: String = "",
    val title: String,
    val amount: Double,
    val category: String,
    val categoryIcon: ImageVector,
    val categoryColor: Color,
    val account: String,
    val transferTo: String? = null,
    val time: String,
    val type: TransactionType,
    val description: String = "",
    val date: String = "",
    val accountIcon: ImageVector = Icons.Default.CreditCard,
    val accountColor: Color = Color.Blue
)

enum class TransactionType {
    INCOME, EXPENSE, TRANSFER
}

data class DayGroup(
    val date: String,
    val displayDate: String,
    val transactions: List<Transaction>,
    val income: Double,
    val expense: Double
)

data class TransactionFormData(
    val amount: String = "",
    val title: String = "",
    val category: TransactionCategory? = null,
    val account: Account? = null,
    val toAccount: Account? = null,
    val date: String = "",
    val time: String = "",
    val description: String = "",
    val type: TransactionType = TransactionType.EXPENSE
)

data class TransactionCategory(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val color: Color
)

data class InsufficientBalanceInfo(
    val accountName: String,
    val currentBalance: Double,
    val requiredAmount: Double
)
