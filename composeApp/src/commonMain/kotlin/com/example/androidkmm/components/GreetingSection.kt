package com.example.androidkmm.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import com.example.androidkmm.design.DesignSystem
import com.example.androidkmm.utils.TextUtils

/**
 * Greeting section component with user name and notification icon
 */
@Composable
fun GreetingSection(
    userName: String = "Abhishek",
    greeting: String = "Good morning"
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            TextUtils.StandardText(
                text = "Hi $userName 👋",
                color = Color.White,
                fontSize = DesignSystem.Typography.title3,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Normal
            )
            TextUtils.StandardText(
                text = greeting,
                color = Color.Gray,
                fontSize = DesignSystem.Typography.footnote
            )
        }
        Icon(
            imageVector = Icons.Outlined.Notifications,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(DesignSystem.IconSize.lg)
        )
    }
}
