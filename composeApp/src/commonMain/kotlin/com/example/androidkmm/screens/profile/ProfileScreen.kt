package com.example.androidkmm.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidkmm.database.rememberSQLiteSettingsDatabase
import com.example.androidkmm.database.rememberSQLiteAccountDatabase
import com.example.androidkmm.database.rememberSQLiteGroupDatabase
import com.example.androidkmm.design.DesignSystem
import com.example.androidkmm.models.AppSettings
import com.example.androidkmm.utils.Logger

/**
 * Profile screen for user settings and account management
 * 
 * Displays user profile information and provides access to:
 * - User settings and preferences
 * - Account management
 * - Group management
 * - App settings and configuration
 * 
 * @param onNavigateToGroups Callback for navigating to groups management
 */
@Composable
fun ProfileMainScreen(
    onNavigateToGroups: () -> Unit = {}
) {
    Logger.debug("ProfileMainScreen initialized", "ProfileScreen")
    
    // Get user settings and data
    val settingsDatabaseManager = rememberSQLiteSettingsDatabase()
    val accountDatabaseManager = rememberSQLiteAccountDatabase()
    val groupDatabaseManager = rememberSQLiteGroupDatabase()
    
    val appSettings by settingsDatabaseManager.getAppSettings().collectAsState(initial = AppSettings())
    val accounts by accountDatabaseManager.getAllAccounts().collectAsState(initial = emptyList())
    val groups by groupDatabaseManager.getAllGroups().collectAsState(initial = emptyList())
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = DesignSystem.Spacing.safeAreaPadding)
    ) {
        item { Spacer(Modifier.height(DesignSystem.Spacing.lg)) }
        
        // Profile Header
        item {
            ProfileHeader(
                userName = appSettings.userName,
                userEmail = appSettings.userEmail
            )
        }
        
        item { Spacer(Modifier.height(DesignSystem.Spacing.lg)) }
        
        // Account Summary
        item {
            AccountSummaryCard(
                accountCount = accounts.size,
                groupCount = groups.size
            )
        }
        
        item { Spacer(Modifier.height(DesignSystem.Spacing.lg)) }
        
        // Settings Section
        item {
            SettingsSection(
                currencySymbol = appSettings.currencySymbol,
                onNavigateToGroups = onNavigateToGroups
            )
        }
        
        item { Spacer(Modifier.height(DesignSystem.Spacing.lg)) }
        
        // App Info Section
        item {
            AppInfoSection()
        }
        
        item { Spacer(Modifier.height(DesignSystem.Spacing.lg)) }
    }
}

@Composable
private fun ProfileHeader(
    userName: String,
    userEmail: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Avatar
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = userName.take(1).uppercase(),
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(Modifier.width(16.dp))
            
            // User Info
            Column {
                Text(
                    text = userName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = userEmail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AccountSummaryCard(
    accountCount: Int,
    groupCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Account Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Accounts Count
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = accountCount.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Accounts",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Groups Count
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = groupCount.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Groups",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    currencySymbol: String,
    onNavigateToGroups: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(Modifier.height(16.dp))
            
            // Currency Setting
            SettingItem(
                icon = Icons.Default.AttachMoney,
                title = "Currency",
                subtitle = "Current: $currencySymbol",
                onClick = { /* TODO: Implement currency change */ }
            )
            
            Spacer(Modifier.height(8.dp))
            
            // Groups Management
            SettingItem(
                icon = Icons.Default.Group,
                title = "Manage Groups",
                subtitle = "View and manage your groups",
                onClick = onNavigateToGroups
            )
            
            Spacer(Modifier.height(8.dp))
            
            // App Settings
            SettingItem(
                icon = Icons.Default.Settings,
                title = "App Settings",
                subtitle = "Configure app preferences",
                onClick = { /* TODO: Implement app settings */ }
            )
        }
    }
}

@Composable
private fun SettingItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(Modifier.width(16.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AppInfoSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "MoneyMate",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                text = "Version 1.0.0",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                text = "Your personal finance companion",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
