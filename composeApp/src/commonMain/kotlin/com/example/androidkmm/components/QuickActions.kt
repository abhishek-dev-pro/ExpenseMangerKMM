package com.example.androidkmm.components

import androidx.compose.foundation.layout.*
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
            fontSize = 20.sp, // Slightly larger title
            fontWeight = FontWeight.Bold, // Bolder title
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Spacer(modifier = Modifier.height(16.dp)) // More spacing after title
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp), // Better spacing between buttons
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
    Button(
        onClick = onClick,
        modifier = modifier
            .height(48.dp), // Reduced height for better proportions
        colors = ButtonDefaults.buttonColors(
            containerColor = color.copy(alpha = 0.15f), // Subtle background tint
            contentColor = color // Bright action color for content
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp), // More rounded corners
        border = androidx.compose.foundation.BorderStroke(
            width = 1.5.dp,
            color = color.copy(alpha = 0.3f) // More visible border
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 6.dp,
            pressedElevation = 3.dp,
            hoveredElevation = 8.dp
        )
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp) // Slightly smaller icon
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = text,
                fontSize = 16.sp, // Much larger text for better readability
                fontWeight = FontWeight.SemiBold, // Bolder text
                color = color
            )
        }
    }
}
