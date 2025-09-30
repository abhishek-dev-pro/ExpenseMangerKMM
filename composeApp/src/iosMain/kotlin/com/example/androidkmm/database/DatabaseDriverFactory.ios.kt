package com.example.androidkmm.database

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(CategoryDatabase.Schema, "app_database_v13.db")
    }
}

@Composable
actual fun rememberDatabaseDriverFactory(): DatabaseDriverFactory {
    return remember { DatabaseDriverFactory() }
}

actual fun parseColorHex(hexString: String): Color {
    return try {
        // iOS-specific color parsing implementation
        val cleanHex = hexString.removePrefix("#")
        val color = when (cleanHex.length) {
            6 -> {
                val r = cleanHex.substring(0, 2).toInt(16) / 255f
                val g = cleanHex.substring(2, 4).toInt(16) / 255f
                val b = cleanHex.substring(4, 6).toInt(16) / 255f
                Color(r, g, b, 1f)
            }
            8 -> {
                val a = cleanHex.substring(0, 2).toInt(16) / 255f
                val r = cleanHex.substring(2, 4).toInt(16) / 255f
                val g = cleanHex.substring(4, 6).toInt(16) / 255f
                val b = cleanHex.substring(6, 8).toInt(16) / 255f
                Color(r, g, b, a)
            }
            else -> Color(0xFF607D8B) // Default gray color
        }
        color
    } catch (e: Exception) {
        println("Error parsing color hex '$hexString': ${e.message}")
        Color(0xFF607D8B) // Default gray color
    }
}

