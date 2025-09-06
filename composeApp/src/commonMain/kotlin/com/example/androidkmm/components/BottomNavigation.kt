package com.example.androidkmm.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import com.example.androidkmm.design.DesignSystem
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
    NavigationBar(containerColor = Color.Black) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selected == index,
                onClick = { onSelect(index) },
                icon = { 
                    Icon(
                        imageVector = item.icon, 
                        contentDescription = item.label, 
                        modifier = Modifier.size(DesignSystem.IconSize.lg)
                    ) 
                },
                label = { 
                    TextUtils.StandardText(
                        text = item.label,
                        fontSize = DesignSystem.Typography.tabLabel
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = Color.White,
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}
