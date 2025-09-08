package com.example.androidkmm.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.androidkmm.components.*
import com.example.androidkmm.transactions.TransactionsScreen

/**
 * Main screen with bottom navigation and content switching
 */
@Composable
fun MainScreen() {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selected = selectedTab,
                onSelect = { selectedTab = it }
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selectedTab) {
                0 -> HomeScreenContent()
                1 -> TransactionsScreen()
                2 -> GroupsScreen()
                3 -> PlaceholderScreen("Insights")
                4 -> PlaceholderScreen("Profile")
            }
        }
    }
}