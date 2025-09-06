package com.example.androidkmm.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.Font
import androidx.compose.runtime.Composable
import java.io.File

@Composable
actual fun getPlatformFontFamily(): FontFamily {
    // Using actual SF Pro font files for Desktop
    return FontFamily(
        Font(file = File("fonts/SF-Pro-Display-Regular.otf"), weight = androidx.compose.ui.text.font.FontWeight.Normal),
        Font(file = File("fonts/SF-Pro-Display-Medium.otf"), weight = androidx.compose.ui.text.font.FontWeight.Medium),
        Font(file = File("fonts/SF-Pro-Display-Semibold.otf"), weight = androidx.compose.ui.text.font.FontWeight.SemiBold),
        Font(file = File("fonts/SF-Pro-Display-Bold.otf"), weight = androidx.compose.ui.text.font.FontWeight.Bold)
    )
}
