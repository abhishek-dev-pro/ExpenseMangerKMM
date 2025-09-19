package com.example.androidkmm.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.androidkmm.design.DesignSystem
import com.example.androidkmm.utils.CardUtils
import com.example.androidkmm.utils.TextUtils

/**
 * Quick actions section with action cards
 */
@Composable
fun QuickActions(
    actions: List<com.example.androidkmm.data.QuickActionData> = com.example.androidkmm.data.SampleData.quickActions,
    onActionClick: (String) -> Unit = {}
) {
    Column {
        TextUtils.StandardText(
            text = "Quick Actions",
            color = Color.White,
            fontSize = DesignSystem.Typography.footnote,
            fontWeight = FontWeight.ExtraLight
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.sm),
            modifier = Modifier.fillMaxWidth()
        ) {
            actions.forEach { action ->
                QuickActionCard(
                    text = action.text,
                    color = action.color,
                    icon = action.icon,
                    modifier = Modifier.weight(1f),
                    onClick = { onActionClick(action.text) }
                )
            }
        }
    }
}

/**
 * Individual quick action card
 */
@Composable
private fun QuickActionCard(
    text: String,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    CardUtils.StandardCard(
        modifier = modifier,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 4.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
//            Icon(
//                imageVector = icon,
//                contentDescription = null,
//                tint = color,
//                modifier = Modifier.size(DesignSystem.IconSize.sm)
//            )
//            Spacer(Modifier.width(DesignSystem.Spacing.xs))
            TextUtils.StandardText(
                text = text,
                fontSize = DesignSystem.Typography.buttonText,
                color = color
            )
        }
    }
}
