package com.example.androidkmm.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.androidkmm.design.DesignSystem
import com.example.androidkmm.utils.TextUtils

/**
 * Progress card component showing achievement or progress information
 */
@Composable
fun ProgressCard(
    title: String = "Great progress! 🎉",
    subtitle: String = "You spent 20% less on Food this month"
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color(0xFFDAFFF2),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(DesignSystem.CornerRadius.md)
            )
            .padding(DesignSystem.Spacing.cardPadding)
    ) {
        Column {
            TextUtils.StandardText(
                text = title,
                color = Color.Black,
                fontSize = DesignSystem.Typography.cardTitle,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(DesignSystem.Spacing.xs))
            TextUtils.StandardText(
                text = subtitle,
                color = Color.DarkGray,
                fontSize = DesignSystem.Typography.cardSubtitle,
                maxLines = 2
            )
        }
    }
}
