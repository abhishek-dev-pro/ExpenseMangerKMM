package com.example.androidkmm.screens.filters.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.androidkmm.design.AppStyleDesignSystem

/**
 * Header component for the filter bottom sheet
 * 
 * Displays the filter icon, title, subtitle, and close button.
 * Provides a consistent header design across all filter interfaces.
 * 
 * @param onDismiss Callback when the close button is clicked
 */
@Composable
fun FilterHeader(onDismiss: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Filter Icon
        Box(
            modifier = Modifier
                .size(AppStyleDesignSystem.Sizes.AVATAR_MEDIUM)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = "Filter",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .size(AppStyleDesignSystem.Sizes.ICON_SIZE_MEDIUM)
                    .align(Alignment.Center)
            )
        }
        
        // Title and Subtitle
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Filter Transactions",
                color = MaterialTheme.colorScheme.onSurface,
                style = AppStyleDesignSystem.Typography.TITLE_2
            )
            
            Text(
                text = "Customize your transaction view",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = AppStyleDesignSystem.Typography.CALL_OUT
            )
        }
        
        // Close Icon
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .size(AppStyleDesignSystem.Sizes.AVATAR_MEDIUM)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
