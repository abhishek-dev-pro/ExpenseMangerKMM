package com.example.androidkmm.database

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.example.androidkmm.models.AppSetting
import com.example.androidkmm.models.AppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.Dispatchers

class SQLiteSettingsDatabase(private val database: CategoryDatabase) {
    
    fun getAllSettings(): Flow<List<AppSetting>> {
        return database.categoryDatabaseQueries.selectAllSettings().asFlow().mapToList(Dispatchers.Default).map { rows ->
            println("Database Debug - Retrieved ${rows.size} settings from database")
            rows.forEach { row ->
                println("Database Debug - Setting: key='${row.key}', value='${row.value_}'")
            }
            rows.map { row ->
                AppSetting(
                    key = row.key,
                    value = row.value_,
                    updatedAt = row.updated_at
                )
            }
        }
    }
    
    fun getSetting(key: String): Flow<AppSetting?> {
        return database.categoryDatabaseQueries.selectSettingByKey(key).asFlow().mapToOneOrNull(Dispatchers.Default).map { row ->
            row?.let {
                AppSetting(
                    key = it.key,
                    value = it.value_,
                    updatedAt = it.updated_at
                )
            }
        }
    }
    
    suspend fun updateSetting(key: String, value: String) {
        println("Database Debug - Updating setting: key='$key', value='$value'")
        database.categoryDatabaseQueries.upsertSetting(key, value)
        println("Database Debug - Setting updated successfully")
    }
    
    fun getAppSettings(): Flow<AppSettings> {
        return getAllSettings().map { settings ->
            println("SQLiteSettingsDatabase - Loading settings, found ${settings.size} settings")
            settings.forEach { setting ->
                println("SQLiteSettingsDatabase - Setting: ${setting.key} = '${setting.value}'")
            }
            
            val settingsMap = settings.associate { it.key to it.value }
            
            // Check if required settings exist, if not, insert them
            val needsCarryForward = !settingsMap.containsKey("carry_forward_enabled")
            val needsNegativeBalanceWarning = !settingsMap.containsKey("negative_balance_warning_enabled")
            val needsCurrencySymbol = !settingsMap.containsKey("currency_symbol")
            val needsDateFormat = !settingsMap.containsKey("date_format")
            val needsUserName = !settingsMap.containsKey("user_name")
            val needsUserEmail = !settingsMap.containsKey("user_email")
            
            if (needsCarryForward || needsNegativeBalanceWarning || needsCurrencySymbol || needsDateFormat || needsUserName || needsUserEmail) {
                println("SQLiteSettingsDatabase - Missing required settings, will insert defaults")
                // Note: We can't call suspend functions here, so we'll handle this in the UI layer
            }
            
            val carryForwardValue = settingsMap["carry_forward_enabled"]
            val carryForwardEnabled = when (carryForwardValue) {
                "1" -> true
                "0" -> false
                null -> {
                    println("SQLiteSettingsDatabase - WARNING: carry_forward_enabled not found in database, defaulting to true")
                    true // Default to true if not found
                }
                else -> {
                    println("SQLiteSettingsDatabase - WARNING: carry_forward_enabled has unexpected value: '$carryForwardValue', defaulting to true")
                    true
                }
            }
            
            val negativeBalanceWarningValue = settingsMap["negative_balance_warning_enabled"]
            val negativeBalanceWarningEnabled = when (negativeBalanceWarningValue) {
                "1" -> true
                "0" -> false
                null -> {
                    println("SQLiteSettingsDatabase - WARNING: negative_balance_warning_enabled not found in database, defaulting to true")
                    true // Default to true if not found
                }
                else -> {
                    println("SQLiteSettingsDatabase - WARNING: negative_balance_warning_enabled has unexpected value: '$negativeBalanceWarningValue', defaulting to true")
                    true
                }
            }
            
            println("SQLiteSettingsDatabase - carry_forward_enabled: '$carryForwardValue', parsed: $carryForwardEnabled")
            println("SQLiteSettingsDatabase - negative_balance_warning_enabled: '$negativeBalanceWarningValue', parsed: $negativeBalanceWarningEnabled")
            println("SQLiteSettingsDatabase - settingsMap: $settingsMap")
            println("SQLiteSettingsDatabase - user_name from map: '${settingsMap["user_name"]}'")
            println("SQLiteSettingsDatabase - user_email from map: '${settingsMap["user_email"]}'")
            
            val appSettings = AppSettings(
                carryForwardEnabled = carryForwardEnabled,
                currencySymbol = settingsMap["currency_symbol"] ?: "$",
                dateFormat = settingsMap["date_format"] ?: "MMM dd, yyyy",
                userName = settingsMap["user_name"] ?: "",
                userEmail = settingsMap["user_email"] ?: "",
                negativeBalanceWarningEnabled = negativeBalanceWarningEnabled
            )
            
            println("SQLiteSettingsDatabase - Final AppSettings: userName='${appSettings.userName}', userEmail='${appSettings.userEmail}', carryForwardEnabled='${appSettings.carryForwardEnabled}', negativeBalanceWarningEnabled='${appSettings.negativeBalanceWarningEnabled}'")
            appSettings
        }
    }
    
    suspend fun updateCarryForwardEnabled(enabled: Boolean) {
        val value = if (enabled) "1" else "0"
        println("Updating carry_forward_enabled to: $value")
        updateSetting("carry_forward_enabled", value)
    }
    
    suspend fun updateCurrencySymbol(symbol: String) {
        updateSetting("currency_symbol", symbol)
    }
    
    suspend fun updateDateFormat(format: String) {
        updateSetting("date_format", format)
    }
    
    suspend fun updateUserName(name: String) {
        updateSetting("user_name", name)
    }
    
    suspend fun updateUserEmail(email: String) {
        updateSetting("user_email", email)
    }
    
    suspend fun updateNegativeBalanceWarningEnabled(enabled: Boolean) {
        val value = if (enabled) "1" else "0"
        println("Updating negative_balance_warning_enabled to: $value")
        updateSetting("negative_balance_warning_enabled", value)
    }
    
    // Debug function to check database state
    suspend fun debugDatabaseState() {
        try {
            val settings = database.categoryDatabaseQueries.selectAllSettings().executeAsList()
            println("Database Debug - Total settings in database: ${settings.size}")
            settings.forEach { setting ->
                println("Database Debug - Setting: ${setting.key} = '${setting.value_}'")
            }
        } catch (e: Exception) {
            println("Database Debug - Error reading database: ${e.message}")
            e.printStackTrace()
        }
    }
    
    // Force initialize default settings if they don't exist
    suspend fun ensureDefaultSettings() {
        try {
            val settings = database.categoryDatabaseQueries.selectAllSettings().executeAsList()
            val settingsMap = settings.associate { it.key to it.value_ }
            
            println("Database Debug - Current settings in database: $settingsMap")
            
            if (!settingsMap.containsKey("carry_forward_enabled")) {
                println("Database Debug - carry_forward_enabled not found, inserting default value")
                updateSetting("carry_forward_enabled", "1")
            } else {
                println("Database Debug - carry_forward_enabled already exists: ${settingsMap["carry_forward_enabled"]}")
            }
            
            if (!settingsMap.containsKey("negative_balance_warning_enabled")) {
                println("Database Debug - negative_balance_warning_enabled not found, inserting default value")
                updateSetting("negative_balance_warning_enabled", "1")
            } else {
                println("Database Debug - negative_balance_warning_enabled already exists: ${settingsMap["negative_balance_warning_enabled"]}")
            }
            
            if (!settingsMap.containsKey("currency_symbol")) {
                println("Database Debug - currency_symbol not found, inserting default value")
                updateSetting("currency_symbol", "₹")
            } else {
                println("Database Debug - currency_symbol already exists: ${settingsMap["currency_symbol"]}")
            }
            
            if (!settingsMap.containsKey("date_format")) {
                println("Database Debug - date_format not found, inserting default value")
                updateSetting("date_format", "MMM dd, yyyy")
            } else {
                println("Database Debug - date_format already exists: ${settingsMap["date_format"]}")
            }
            
            if (!settingsMap.containsKey("user_name")) {
                println("Database Debug - user_name not found, inserting default value")
                updateSetting("user_name", "")
            } else {
                println("Database Debug - user_name already exists: ${settingsMap["user_name"]}")
            }
            
            if (!settingsMap.containsKey("user_email")) {
                println("Database Debug - user_email not found, inserting default value")
                updateSetting("user_email", "")
            } else {
                println("Database Debug - user_email already exists: ${settingsMap["user_email"]}")
            }
            
        } catch (e: Exception) {
            println("Database Debug - Error ensuring default settings: ${e.message}")
            e.printStackTrace()
        }
    }
}

@Composable
fun rememberSQLiteSettingsDatabase(): SQLiteSettingsDatabase {
    println("Database Debug - Creating SQLiteSettingsDatabase")
    val driverFactory = rememberDatabaseDriverFactory()
    println("Database Debug - Driver factory created")
    val database = remember { 
        println("Database Debug - Creating CategoryDatabase")
        CategoryDatabase(driverFactory.createDriver())
    }
    println("Database Debug - CategoryDatabase created")
    return remember { 
        println("Database Debug - Creating SQLiteSettingsDatabase instance")
        SQLiteSettingsDatabase(database)
    }
}
