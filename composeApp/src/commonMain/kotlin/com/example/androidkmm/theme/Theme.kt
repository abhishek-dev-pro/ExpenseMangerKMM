package com.example.androidkmm.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle

@Composable
fun LegacyAppTheme(
    content: @Composable () -> Unit
) {
    val platformFontFamily = getPlatformFontFamily()
    val typography = Typography().copy(
        displayLarge = Typography().displayLarge.copy(fontFamily = platformFontFamily, fontStyle = FontStyle.Normal),
        displayMedium = Typography().displayMedium.copy(fontFamily = platformFontFamily, fontStyle = FontStyle.Normal),
        displaySmall = Typography().displaySmall.copy(fontFamily = platformFontFamily, fontStyle = FontStyle.Normal),
        headlineLarge = Typography().headlineLarge.copy(fontFamily = platformFontFamily, fontStyle = FontStyle.Normal),
        headlineMedium = Typography().headlineMedium.copy(fontFamily = platformFontFamily, fontStyle = FontStyle.Normal),
        headlineSmall = Typography().headlineSmall.copy(fontFamily = platformFontFamily, fontStyle = FontStyle.Normal),
        titleLarge = Typography().titleLarge.copy(fontFamily = platformFontFamily, fontStyle = FontStyle.Normal),
        titleMedium = Typography().titleMedium.copy(fontFamily = platformFontFamily, fontStyle = FontStyle.Normal),
        titleSmall = Typography().titleSmall.copy(fontFamily = platformFontFamily, fontStyle = FontStyle.Normal),
        bodyLarge = Typography().bodyLarge.copy(fontFamily = platformFontFamily, fontStyle = FontStyle.Normal),
        bodyMedium = Typography().bodyMedium.copy(fontFamily = platformFontFamily, fontStyle = FontStyle.Normal),
        bodySmall = Typography().bodySmall.copy(fontFamily = platformFontFamily, fontStyle = FontStyle.Normal),
        labelLarge = Typography().labelLarge.copy(fontFamily = platformFontFamily, fontStyle = FontStyle.Normal),
        labelMedium = Typography().labelMedium.copy(fontFamily = platformFontFamily, fontStyle = FontStyle.Normal),
        labelSmall = Typography().labelSmall.copy(fontFamily = platformFontFamily, fontStyle = FontStyle.Normal)
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
