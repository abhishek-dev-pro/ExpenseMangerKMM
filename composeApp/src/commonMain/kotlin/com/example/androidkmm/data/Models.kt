package com.example.androidkmm.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Data models for the finance app
 */

data class BillData(
    val title: String,
    val subtitle: String,
    val amount: String,
    val color: Color
)

data class GroupData(
    val title: String,
    val amount: String,
    val chip: String,
    val color: Color,
    val positive: Boolean,
    val members: String
)

data class QuickActionData(
    val text: String,
    val color: Color,
    val icon: ImageVector
)

data class NavigationItem(
    val label: String,
    val icon: ImageVector
)
