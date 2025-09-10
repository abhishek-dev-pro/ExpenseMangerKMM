package com.example.androidkmm.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class Category(
    val id: String = "",
    val name: String,
    val icon: ImageVector,
    val color: Color,
    val type: CategoryType = CategoryType.EXPENSE,
    val isCustom: Boolean = false
)

enum class CategoryType {
    EXPENSE,
    INCOME
}

enum class CategoryTab {
    EXPENSE,
    INCOME
}
