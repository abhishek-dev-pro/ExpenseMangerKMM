package com.example.androidkmm.screens.ledger

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Theme.kt
object LedgerTheme {
    val cardBackgroundSent = Color(0xFF1C3B2A) // Keep green for "To Send"
    val cardBackgroundReceived = Color(0xFF3B1C1C) // Keep red for "To Receive"
    val greenAmount = Color(0xFF10B981)
    val redAmount = Color(0xFFEF4444)
    val avatarBlue = Color(0xFF3B82F6)
    
    @Composable
    fun backgroundColor() = MaterialTheme.colorScheme.background
    
    @Composable
    fun textPrimary() = MaterialTheme.colorScheme.onBackground
    
    @Composable
    fun textSecondary() = MaterialTheme.colorScheme.onSurfaceVariant
    
    @Composable
    fun searchBackground() = MaterialTheme.colorScheme.surfaceVariant
}
