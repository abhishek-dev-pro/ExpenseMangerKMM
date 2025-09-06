package com.example.androidkmm.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.androidkmm.design.DesignSystem
import com.example.androidkmm.utils.CardUtils

/**
 * Bill item component for displaying upcoming bills
 */
@Composable
fun BillItem(
    title: String,
    subtitle: String,
    amount: String,
    color: Color
) {
    CardUtils.ItemCardSurface {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = DesignSystem.Spacing.md,
                    vertical = DesignSystem.Spacing.sm
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side (icon + texts)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f).padding(vertical = 4.dp)
            ) {
                // Colored circular icon background
                Box(
                    modifier = Modifier
                        .size(DesignSystem.IconSize.avatar)
                        .background(color, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(DesignSystem.IconSize.md)
                    )
                }

                Spacer(Modifier.width(DesignSystem.Spacing.md))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontWeight = FontWeight.Normal,
                        fontStyle = FontStyle.Normal,
                        fontSize = DesignSystem.Typography.balanceLabel,
                        lineHeight = DesignSystem.Typography.cardTitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitle,
                        color = Color.Gray,
                        fontStyle = FontStyle.Normal,
                        fontSize = DesignSystem.Typography.caption1,
                        lineHeight = DesignSystem.Typography.caption1, // tight spacing
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Right side (amount)
            Text(
                text = amount,
                color = Color.Red,
                fontSize = DesignSystem.Typography.balanceLabel,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
