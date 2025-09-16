package com.example.androidkmm.utils

import com.example.androidkmm.database.rememberSQLiteSettingsDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Utility functions for database reset and testing
 */
object DatabaseResetUtils {
    
    /**
     * Reset user setup to show UserSetupScreen again
     * This is useful for testing the setup flow
     */
    suspend fun resetUserSetup(settingsDatabase: com.example.androidkmm.database.SQLiteSettingsDatabase) {
        settingsDatabase.updateSetting("user_name", "")
        settingsDatabase.updateSetting("user_email", "")
    }
    
    /**
     * Reset user setup using coroutine scope
     */
    fun resetUserSetupAsync(
        settingsDatabase: com.example.androidkmm.database.SQLiteSettingsDatabase,
        coroutineScope: CoroutineScope
    ) {
        coroutineScope.launch {
            resetUserSetup(settingsDatabase)
        }
    }
}
