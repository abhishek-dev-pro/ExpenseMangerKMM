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
import com.example.androidkmm.database.rememberSQLiteSettingsDatabase
import com.example.androidkmm.design.AppStyleDesignSystem
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
                fontSize = AppStyleDesignSystem.Typography.TITLE_3.fontSize,
                fontWeight = AppStyleDesignSystem.iOSFontWeights.regular
            )
            TextUtils.StandardText(
                text = greeting,
                color = Color.Gray,
                fontSize = AppStyleDesignSystem.Typography.FOOTNOTE.fontSize
            )
        }
        Icon(
            imageVector = Icons.Outlined.Notifications,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_LARGE)
        )
    }
}
