package com.example.androidkmm.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight

@Composable
fun AppTheme(
    content: @Composable () -> Unit
) {
    val platformFontFamily = getPlatformFontFamily()
    val typography = Typography().copy(
        displayLarge = Typography().displayLarge.copy(fontFamily = platformFontFamily),
        displayMedium = Typography().displayMedium.copy(fontFamily = platformFontFamily),
        displaySmall = Typography().displaySmall.copy(fontFamily = platformFontFamily),
        headlineLarge = Typography().headlineLarge.copy(fontFamily = platformFontFamily),
        headlineMedium = Typography().headlineMedium.copy(fontFamily = platformFontFamily),
        headlineSmall = Typography().headlineSmall.copy(fontFamily = platformFontFamily),
        titleLarge = Typography().titleLarge.copy(fontFamily = platformFontFamily),
        titleMedium = Typography().titleMedium.copy(fontFamily = platformFontFamily),
        titleSmall = Typography().titleSmall.copy(fontFamily = platformFontFamily),
        bodyLarge = Typography().bodyLarge.copy(fontFamily = platformFontFamily),
        bodyMedium = Typography().bodyMedium.copy(fontFamily = platformFontFamily),
        bodySmall = Typography().bodySmall.copy(fontFamily = platformFontFamily),
        labelLarge = Typography().labelLarge.copy(fontFamily = platformFontFamily),
        labelMedium = Typography().labelMedium.copy(fontFamily = platformFontFamily),
        labelSmall = Typography().labelSmall.copy(fontFamily = platformFontFamily)
    )
    
    val darkColorScheme = darkColorScheme(
        primary = androidx.compose.ui.graphics.Color(0xFF4C2EFF),
        secondary = androidx.compose.ui.graphics.Color(0xFF9F3DFF),
        background = androidx.compose.ui.graphics.Color.Black,
        surface = androidx.compose.ui.graphics.Color(0xFF1A1A1A),
        onPrimary = androidx.compose.ui.graphics.Color.White,
        onSecondary = androidx.compose.ui.graphics.Color.White,
        onBackground = androidx.compose.ui.graphics.Color.White,
        onSurface = androidx.compose.ui.graphics.Color.White,
    )

    MaterialTheme(
        colorScheme = darkColorScheme,
        typography = typography,
        content = content
    )
}
