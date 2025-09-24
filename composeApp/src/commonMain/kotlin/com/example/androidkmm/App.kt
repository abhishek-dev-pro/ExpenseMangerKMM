package com.example.androidkmm

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.example.androidkmm.database.rememberSQLiteSettingsDatabase
import com.example.androidkmm.screens.MainScreen
import com.example.androidkmm.screens.UserSetupScreen
import com.example.androidkmm.theme.AppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview


@Composable
@Preview
fun App() {
    CompositionLocalProvider(
        LocalDensity provides Density(
            density = LocalDensity.current.density,
            fontScale = 1f // 🚀 ignores phone font size setting
        )
    ) {
        AppTheme {
            val settingsDatabase = rememberSQLiteSettingsDatabase()
            val appSettings by settingsDatabase.getAppSettings().collectAsState(initial = com.example.androidkmm.models.AppSettings())


            // Check if user has completed setup (name is not empty and not default "User")
            val isUserSetupComplete = appSettings.userName.isNotBlank() && appSettings.userName != "User"

            if (isUserSetupComplete) {
                MainScreen()
            } else {
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


}