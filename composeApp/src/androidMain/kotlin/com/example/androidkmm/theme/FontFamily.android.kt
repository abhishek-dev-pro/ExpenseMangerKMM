package com.example.androidkmm.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.Composable

@Composable
actual fun getPlatformFontFamily(): FontFamily {
    // Using actual SF Pro font files for Android
    val context = LocalContext.current
    return FontFamily(
        Font(resId = context.resources.getIdentifier("sf_pro_display_regular", "font", context.packageName), weight = FontWeight.Normal),
        Font(resId = context.resources.getIdentifier("sf_pro_display_medium", "font", context.packageName), weight = FontWeight.Medium),
        Font(resId = context.resources.getIdentifier("sf_pro_display_semibold", "font", context.packageName), weight = FontWeight.SemiBold),
        Font(resId = context.resources.getIdentifier("sf_pro_display_bold", "font", context.packageName), weight = FontWeight.Bold)
    )
}
