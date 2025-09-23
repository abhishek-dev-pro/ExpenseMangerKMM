package com.example.androidkmm.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.androidkmm.design.AppStyleDesignSystem
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
            fontSize = AppStyleDesignSystem.Typography.TITLE_2.fontSize,
            fontWeight = AppStyleDesignSystem.iOSFontWeights.bold
        )
    }
}
