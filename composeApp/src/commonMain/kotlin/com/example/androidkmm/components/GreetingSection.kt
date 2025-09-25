package com.example.androidkmm.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import com.example.androidkmm.database.rememberSQLiteSettingsDatabase
import com.example.androidkmm.design.AppStyleDesignSystem
import com.example.androidkmm.utils.GreetingUtils
import com.example.androidkmm.utils.TextUtils
import kotlinx.coroutines.delay

/**
 * Greeting section component with dynamic user name and time-based greeting
 */
@Composable
fun GreetingSection() {
    val settingsDatabase = rememberSQLiteSettingsDatabase()
    val appSettings by settingsDatabase.getAppSettings().collectAsState(initial = com.example.androidkmm.models.AppSettings())
    
    val userName = appSettings.userName
    val greeting = GreetingUtils.getTimeBasedGreeting()
    var showToast by remember { mutableStateOf(false) }
    
    Box {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
               Text(
                    text = "Hi $userName 👋",
                    style = AppStyleDesignSystem.Typography.MAIN_PAGE_HEADING_TITLE
                )
                Text(
                    text = greeting,
                    style = AppStyleDesignSystem.Typography.MAIN_PAGE_SUBHEADING_TITLE
                )
            }
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = "Notifications",
                tint = Color.White,
                modifier = Modifier
                    .size(AppStyleDesignSystem.Sizes.ICON_SIZE_LARGE)
                    .clickable {
                        showToast = true
                    }
            )
        }
        
        // Toast message
        if (showToast) {
            ToastMessage(
                message = "hola, wait a bit. once completed we'll be first to notify type text.",
                onDismiss = { showToast = false }
            )
        }
    }
}

/**
 * Customized toast notification component
 */
@Composable
private fun ToastMessage(
    message: String,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1A1A1A)
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Notification icon
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            Color(0xFF4CAF50),
                            RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Message text
                Text(
                    text = message,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                    lineHeight = 20.sp,
                    modifier = Modifier.weight(1f)
                )
                
                // Close button
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
    
    // Auto-dismiss after 4 seconds
    androidx.compose.runtime.LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(4000)
        onDismiss()
    }
}
