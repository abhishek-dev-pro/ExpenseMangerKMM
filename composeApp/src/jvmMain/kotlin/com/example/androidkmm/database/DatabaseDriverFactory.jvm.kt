package com.example.androidkmm.database

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.sqlite.JdbcSqliteDriver
import java.io.File

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        val databasePath = File(System.getProperty("user.home"), ".androidkmm")
        if (!databasePath.exists()) {
            databasePath.mkdirs()
        }
        
        val driver = JdbcSqliteDriver("jdbc:sqlite:${databasePath.absolutePath}/app_database.db")
        CategoryDatabase.Schema.create(driver)
        return driver
    }
}

@Composable
actual fun rememberDatabaseDriverFactory(): DatabaseDriverFactory {
    return remember { DatabaseDriverFactory() }
}

actual fun parseColorHex(hexString: String): Color {
    // Simple hex color parser for JVM
    val cleanHex = hexString.removePrefix("#")
    val color = when (cleanHex.length) {
        6 -> java.awt.Color.decode("#$cleanHex")
        8 -> {
            val alpha = cleanHex.substring(0, 2).toInt(16)
            val rgb = cleanHex.substring(2).toInt(16)
            java.awt.Color(rgb, alpha != 0)
        }
        else -> java.awt.Color.BLACK
    }
    return Color(
        red = color.red / 255f,
        green = color.green / 255f,
        blue = color.blue / 255f,
        alpha = color.alpha / 255f
    )
}
