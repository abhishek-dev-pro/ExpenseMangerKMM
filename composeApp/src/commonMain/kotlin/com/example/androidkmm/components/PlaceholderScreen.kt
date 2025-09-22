package com.example.androidkmm.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.androidkmm.design.iOSStyleDesignSystem
import com.example.androidkmm.utils.TextUtils

/**
 * Placeholder screen component for unimplemented tabs
 */
@Composable
fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        TextUtils.StandardText(
            text = title,
            color = Color.White,
            fontSize = iOSStyleDesignSystem.Typography.TITLE_2.fontSize,
            fontWeight = iOSStyleDesignSystem.iOSFontWeights.bold
        )
    }
}
