package com.example.androidkmm.screens

import ProfileMainScreen
import TransactionsScreen
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.androidkmm.components.*
import com.example.androidkmm.database.InitializeDatabase

/**
 * Main screen with bottom navigation and content switching
 */
@Composable
fun MainScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    
    // Initialize database
    InitializeDatabase()

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
                3 -> LedgerMainScreen()
                4 -> ProfileMainScreen()
            }
        }
    }
}