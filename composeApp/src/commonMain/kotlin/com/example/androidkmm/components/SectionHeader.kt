package com.example.androidkmm.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.androidkmm.design.DesignSystem
import com.example.androidkmm.utils.TextUtils

/**
 * Section header component with title and "View all" link
 */
@Composable
fun SectionHeader(
    title: String,
    showViewAll: Boolean = true,
    onViewAllClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TextUtils.StandardText(
            text = title,
            color = Color.White,
            fontSize = DesignSystem.Typography.balanceLabel,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
        if (showViewAll) {
            TextUtils.StandardText(
                text = "View all >",
                color = Color.White,
                fontSize = DesignSystem.Typography.caption2,
                fontWeight = FontWeight.ExtraLight
            )
        }
    }
}
