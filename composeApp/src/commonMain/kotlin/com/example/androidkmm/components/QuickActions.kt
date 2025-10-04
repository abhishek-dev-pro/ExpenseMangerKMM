package com.example.androidkmm.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidkmm.design.AppStyleDesignSystem
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
        Text(
            text = "Quick Actions",
            color = Color.White,
            style = AppStyleDesignSystem.Typography.MAIN_PAGE_SUBHEADING_TITLE,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Spacer(modifier = Modifier.height(8.dp)) // Reduced spacing after title
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp), // Increased spacing between buttons
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
        Spacer(modifier = Modifier.height(4.dp))
    }
}

/**
 * Individual quick action card - styled as an attractive, compact button
 */
@Composable
private fun QuickActionCard(
    text: String,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = modifier
            .height(48.dp) // Reduced height for more compact buttons
            .clickable { onClick() },
        color = color.copy(alpha = 0.15f),
        contentColor = color,
        shape = RoundedCornerShape(16.dp), // Slightly more rounded corners
        border = BorderStroke(2.dp, color.copy(alpha = 0.4f)) // Slightly thicker border
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp) // Reduced vertical padding
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp) // Larger icon
            )
            Spacer(modifier = Modifier.width(8.dp)) // More space between icon and text
            Text(
                text = text,
                fontSize = 16.sp, // Larger text
                fontWeight = FontWeight.Bold, // Bolder text
                color = color
            )
        }
    }
}
