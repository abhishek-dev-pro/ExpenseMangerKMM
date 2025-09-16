package com.example.androidkmm

import androidx.compose.runtime.*
import com.example.androidkmm.database.rememberSQLiteSettingsDatabase
import com.example.androidkmm.screens.MainScreen
import com.example.androidkmm.screens.UserSetupScreen
import com.example.androidkmm.theme.AppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview


@Composable
@Preview
fun App() {
    AppTheme {
        val settingsDatabase = rememberSQLiteSettingsDatabase()
        val appSettings by settingsDatabase.getAppSettings().collectAsState(initial = com.example.androidkmm.models.AppSettings())
        
        // Debug logging
        println("App Debug - userName: '${appSettings.userName}', userEmail: '${appSettings.userEmail}'")
        println("App Debug - userName.isNotBlank(): ${appSettings.userName.isNotBlank()}")
        println("App Debug - userName != 'User': ${appSettings.userName != "User"}")
        println("App Debug - userName length: ${appSettings.userName.length}")
        println("App Debug - userName isEmpty: ${appSettings.userName.isEmpty()}")
        
        // Check if user has completed setup (name is not empty and not default "User")
        val isUserSetupComplete = appSettings.userName.isNotBlank() && appSettings.userName != "User"
        
        println("App Debug - isUserSetupComplete: $isUserSetupComplete")
        println("App Debug - App recomposition triggered")
        
        if (isUserSetupComplete) {
            println("App Debug - Showing MainScreen")
            MainScreen()
        } else {
            println("App Debug - Showing UserSetupScreen")
            UserSetupScreen(
                settingsDatabase = settingsDatabase, // Pass the same database instance
                onSetupComplete = {
                    println("App Debug - onSetupComplete callback called")
                    // The state will automatically update when the database changes
                    // No need for additional logic here as the collectAsState will trigger recomposition
                }
            )
        }
    }
}