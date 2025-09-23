package com.example.androidkmm.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle

// Theme state management
object AppTheme {
    var isDarkMode by mutableStateOf(true)
        private set

    fun toggleTheme() {
        isDarkMode = !isDarkMode
    }

    fun updateDarkMode(isDark: Boolean) {
        isDarkMode = isDark
    }
}

// Color schemes
val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF2196F3),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1976D2),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF03DAC6),
    onSecondary = Color.Black,
    tertiary = Color(0xFFBB86FC),
    onTertiary = Color.Black,
    error = Color(0xFFCF6679),
    onError = Color.Black,
    background = Color(0xFF000000), // True black
    onBackground = Color.White,
    surface = Color(0xFF000000), // True black for cards
    onSurface = Color.White,
    surfaceVariant = Color(0xFF000000), // True black for variants
    onSurfaceVariant = Color.White,
    outline = Color(0xFF1A1A1A), // Very subtle gray for borders only
    outlineVariant = Color(0xFF000000),
    scrim = Color.Black.copy(alpha = 0.5f),
    inverseSurface = Color.White,
    inverseOnSurface = Color.Black,
    inversePrimary = Color(0xFF1976D2)
)

val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1976D2),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE3F2FD),
    onPrimaryContainer = Color(0xFF0D47A1),
    secondary = Color(0xFF03DAC6),
    onSecondary = Color.Black,
    tertiary = Color(0xFFBB86FC),
    onTertiary = Color.Black,
    error = Color(0xFFB00020),
    onError = Color.White,
    background = Color(0xFFF5F5F5), // Light gray background
    onBackground = Color(0xFF1C1B1F), // Dark text on light background
    surface = Color(0xFFFFFFFF), // White surface for cards
    onSurface = Color(0xFF1C1B1F), // Dark text on white surface
    surfaceVariant = Color(0xFFF8F8F8), // Very light gray for variants
    onSurfaceVariant = Color(0xFF49454F), // Dark gray text for variants
    outline = Color(0xFFE0E0E0), // Light gray for borders
    outlineVariant = Color(0xFFE7E0EC),
    scrim = Color.Black.copy(alpha = 0.5f),
    inverseSurface = Color(0xFF313033),
    inverseOnSurface = Color(0xFFF4EFF4),
    inversePrimary = Color(0xFF90CAF9)
)

// Custom colors for the app
object AppColors {
    val Success = Color(0xFF4CAF50)
    val Warning = Color(0xFFFF9800)
    val Error = Color(0xFFF44336)
    val Info = Color(0xFF2196F3)
    
    // Transaction colors
    val Income = Color(0xFF4CAF50)
    val Expense = Color(0xFFF44336)
    val Transfer = Color(0xFF2196F3)
    
    // Account type colors
    val BankAccount = Color(0xFF4285F4)
    val CreditCard = Color(0xFF34A853)
    val Cash = Color(0xFFFF6D01)
    val DigitalWallet = Color(0xFF9C27B0)
}

@Composable
fun AppTheme(
    content: @Composable () -> Unit
) {
    val isDarkMode = AppTheme.isDarkMode
    val colorScheme = if (isDarkMode) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Typography
val Typography = androidx.compose.material3.Typography(
    displayLarge = androidx.compose.material3.Typography().displayLarge.copy(
        fontStyle = FontStyle.Normal
    ),
    displayMedium = androidx.compose.material3.Typography().displayMedium.copy(
        fontStyle = FontStyle.Normal
    ),
    displaySmall = androidx.compose.material3.Typography().displaySmall.copy(
        fontStyle = FontStyle.Normal
    ),
    headlineLarge = androidx.compose.material3.Typography().headlineLarge.copy(
        fontStyle = FontStyle.Normal
    ),
    headlineMedium = androidx.compose.material3.Typography().headlineMedium.copy(
        fontStyle = FontStyle.Normal
    ),
    headlineSmall = androidx.compose.material3.Typography().headlineSmall.copy(
        fontStyle = FontStyle.Normal
    ),
    titleLarge = androidx.compose.material3.Typography().titleLarge.copy(
        fontStyle = FontStyle.Normal
    ),
    titleMedium = androidx.compose.material3.Typography().titleMedium.copy(
        fontStyle = FontStyle.Normal
    ),
    titleSmall = androidx.compose.material3.Typography().titleSmall.copy(
        fontStyle = FontStyle.Normal
    ),
    bodyLarge = androidx.compose.material3.Typography().bodyLarge.copy(
        fontStyle = FontStyle.Normal
    ),
    bodyMedium = androidx.compose.material3.Typography().bodyMedium.copy(
        fontStyle = FontStyle.Normal
    ),
    bodySmall = androidx.compose.material3.Typography().bodySmall.copy(
        fontStyle = FontStyle.Normal
    ),
    labelLarge = androidx.compose.material3.Typography().labelLarge.copy(
        fontStyle = FontStyle.Normal
    ),
    labelMedium = androidx.compose.material3.Typography().labelMedium.copy(
        fontStyle = FontStyle.Normal
    ),
    labelSmall = androidx.compose.material3.Typography().labelSmall.copy(
        fontStyle = FontStyle.Normal
    )
)
