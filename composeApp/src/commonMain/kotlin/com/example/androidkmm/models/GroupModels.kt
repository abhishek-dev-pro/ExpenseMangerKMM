package com.example.androidkmm.models

import androidx.compose.ui.graphics.Color

// Group Models for Split-wise functionality
data class Group(
    val id: String,
    val name: String,
    val description: String = "",
    val color: Color,
    val createdAt: Long,
    val totalSpent: Double = 0.0,
    val memberCount: Int = 0
)

data class GroupMember(
    val id: String,
    val groupId: String,
    val name: String,
    val email: String = "",
    val phone: String = "",
    val avatarColor: Color,
    val balance: Double = 0.0, // Positive = owes you, Negative = you owe
    val totalPaid: Double = 0.0,
    val totalOwed: Double = 0.0
)

data class GroupExpense(
    val id: String,
    val groupId: String,
    val paidBy: String, // Member ID who paid
    val amount: Double,
    val description: String,
    val category: String = "General",
    val date: String,
    val time: String,
    val splitType: SplitType = SplitType.EQUAL,
    val splitDetails: String = "", // JSON string for custom splits
    val createdAt: Long
)

enum class SplitType {
    EQUAL,          // Split equally among all members
    EXACT_AMOUNT,   // Each member pays exact amount
    PERCENTAGE,     // Split by percentage
    SHARES          // Split by shares (e.g., 2:1:1)
}

data class GroupExpenseSplit(
    val id: String,
    val expenseId: String,
    val memberId: String,
    val amount: Double,
    val isPaid: Boolean = false
)
