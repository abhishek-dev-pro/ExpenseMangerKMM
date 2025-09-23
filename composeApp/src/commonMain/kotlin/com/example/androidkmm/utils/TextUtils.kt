package com.example.androidkmm.utils

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.example.androidkmm.design.AppStyleDesignSystem

/**
 * Utility functions for creating consistent text components
 */
object TextUtils {

    /**
     * Creates a standard text with ellipsis overflow
     */
    @Composable
    fun StandardText(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = Color.White,
        fontSize: androidx.compose.ui.unit.TextUnit = AppStyleDesignSystem.Typography.BODY.fontSize,
        fontWeight: FontWeight = FontWeight.Normal,
        fontStyle: FontStyle = FontStyle.Normal,
        maxLines: Int = 1
    ) {
        Text(
            text = text,
            modifier = modifier,
            color = color,
            fontSize = fontSize,
            fontWeight = fontWeight,
            fontStyle = fontStyle,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis
        )
    }

}
