package com.example.androidkmm.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.Font
import androidx.compose.runtime.Composable

@Composable
actual fun getPlatformFontFamily(): FontFamily {
    // Using actual SF Pro font files for iOS
    return FontFamily(
        Font(
            identity = "SF Pro Display Regular",
            getData = { loadFontData("fonts/SF-Pro-Display-Regular.otf") },
            weight = androidx.compose.ui.text.font.FontWeight.Normal
        ),
        Font(
            identity = "SF Pro Display Medium",
            getData = { loadFontData("fonts/SF-Pro-Display-Medium.otf") },
            weight = androidx.compose.ui.text.font.FontWeight.Medium
        ),
        Font(
            identity = "SF Pro Display Semibold",
            getData = { loadFontData("fonts/SF-Pro-Display-Semibold.otf") },
            weight = androidx.compose.ui.text.font.FontWeight.SemiBold
        ),
        Font(
            identity = "SF Pro Display Bold",
            getData = { loadFontData("fonts/SF-Pro-Display-Bold.otf") },
            weight = androidx.compose.ui.text.font.FontWeight.Bold
        )
    )
}

private fun loadFontData(path: String): ByteArray {
    // For now, return empty array - in a real implementation, you would load the font data
    // This would typically involve reading the font file from the iOS bundle
    return byteArrayOf()
}
