package com.example.androidkmm.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.androidkmm.design.DesignSystem
import com.example.androidkmm.utils.TextUtils

/**
 * Balance card component showing total balance and monthly change
 */
@Composable
fun BalanceCard(
    totalBalance: String = "$2,847.5",
    monthlyChange: String = "+$234.8 this month",
    isVisible: Boolean = true
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(
                brush = Brush.linearGradient(
                    listOf(Color(0xFF4C2EFF), Color(0xFF9F3DFF))
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(DesignSystem.CornerRadius.xl)
            )
            .padding(DesignSystem.Spacing.cardPadding)
    ) {
        Column {
            TextUtils.StandardText(
                text = "Total Balance",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = DesignSystem.Typography.balanceLabel,
                fontWeight = FontWeight.Normal
            )
            Spacer(Modifier.height(DesignSystem.Spacing.xl))
            TextUtils.StandardText(
                text = if (isVisible) totalBalance else "••••••",
                color = Color.White,
                fontSize = DesignSystem.Typography.balanceAmount,
                fontWeight = FontWeight.ExtraLight
            )
            Spacer(Modifier.height(DesignSystem.Spacing.xs))
            TextUtils.StandardText(
                text = if (isVisible) monthlyChange else "••••••••••••••••",
                color = Color(0xFF9FFFA5),
                fontSize = DesignSystem.Typography.balanceSubtext
            )
        }
        Row(
            modifier = Modifier.align(Alignment.TopEnd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.VisibilityOff,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(DesignSystem.IconSize.md)
            )
        }
    }
}
