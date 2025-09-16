package com.example.androidkmm.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import com.example.androidkmm.database.rememberSQLiteSettingsDatabase
import com.example.androidkmm.design.DesignSystem
import com.example.androidkmm.utils.GreetingUtils
import com.example.androidkmm.utils.TextUtils

/**
 * Greeting section component with dynamic user name and time-based greeting
 */
@Composable
fun GreetingSection() {
    val settingsDatabase = rememberSQLiteSettingsDatabase()
    val appSettings by settingsDatabase.getAppSettings().collectAsState(initial = com.example.androidkmm.models.AppSettings())
    
    val userName = appSettings.userName
    val greeting = GreetingUtils.getTimeBasedGreeting()
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
