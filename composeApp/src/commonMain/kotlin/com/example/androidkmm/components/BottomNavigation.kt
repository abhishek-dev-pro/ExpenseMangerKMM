package com.example.androidkmm.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.androidkmm.design.iOSStyleDesignSystem
import com.example.androidkmm.utils.TextUtils

/**
 * Bottom navigation bar component
 */
@Composable
fun BottomNavigationBar(
    selected: Int,
    onSelect: (Int) -> Unit,
    items: List<com.example.androidkmm.data.NavigationItem> = com.example.androidkmm.data.SampleData.navigationItems
) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selected == index,
                onClick = { onSelect(index) },
                icon = { 
                    Icon(
                        imageVector = item.icon, 
                        contentDescription = item.label, 
                        modifier = Modifier.size(iOSStyleDesignSystem.Sizes.ICON_SIZE_LARGE)
                    ) 
                },
                label = { 
                    TextUtils.StandardText(
                        text = item.label,
                        fontSize = iOSStyleDesignSystem.Typography.CAPTION_1.fontSize
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}
