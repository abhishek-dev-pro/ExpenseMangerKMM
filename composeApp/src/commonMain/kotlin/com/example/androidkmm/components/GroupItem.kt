package com.example.androidkmm.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidkmm.design.iOSStyleDesignSystem

/**
 * Group item component for displaying group highlights
 */
@Composable
fun GroupItem(
    title: String,
    amount: String,
    chip: String,
    color: Color,
    positive: Boolean,
    members: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(iOSStyleDesignSystem.CornerRadius.LARGE)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(iOSStyleDesignSystem.Padding.SMALL),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Group icon - same style as transaction category icon
            Box(
                modifier = Modifier
                    .size(iOSStyleDesignSystem.Sizes.AVATAR_LARGE)
                    .clip(RoundedCornerShape(iOSStyleDesignSystem.CornerRadius.LARGE))
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(iOSStyleDesignSystem.Sizes.ICON_SIZE_LARGE)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Group details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = iOSStyleDesignSystem.Typography.BODY.copy(
                        fontWeight = iOSStyleDesignSystem.iOSFontWeights.medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = members,
                    style = iOSStyleDesignSystem.Typography.CALL_OUT,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Amount and chip
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = amount,
                    style = iOSStyleDesignSystem.Typography.BODY.copy(
                        fontWeight = iOSStyleDesignSystem.iOSFontWeights.semibold
                    ),
                    color = if (positive) Color(0xFF00A63E) else Color(0xFFEF4444)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = chip,
                    style = iOSStyleDesignSystem.Typography.CAPTION_1,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
