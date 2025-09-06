package com.example.androidkmm.utils

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.example.androidkmm.design.DesignSystem

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
        fontSize: androidx.compose.ui.unit.TextUnit = DesignSystem.Typography.body,
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

    /**
     * Creates a title text with consistent styling
     */
    @Composable
    fun TitleText(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = Color.White,
        fontSize: androidx.compose.ui.unit.TextUnit = DesignSystem.Typography.title3,
        fontWeight: FontWeight = FontWeight.Normal
    ) {
        StandardText(
            text = text,
            modifier = modifier,
            color = color,
            fontSize = fontSize,
            fontWeight = fontWeight
        )
    }

    /**
     * Creates a subtitle text with consistent styling
     */
    @Composable
    fun SubtitleText(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = Color.Gray,
        fontSize: androidx.compose.ui.unit.TextUnit = DesignSystem.Typography.caption1
    ) {
        StandardText(
            text = text,
            modifier = modifier,
            color = color,
            fontSize = fontSize,
            fontWeight = FontWeight.Normal
        )
    }

    /**
     * Creates an amount text with consistent styling
     */
    @Composable
    fun AmountText(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = Color.White,
        fontSize: androidx.compose.ui.unit.TextUnit = DesignSystem.Typography.balanceLabel,
        fontWeight: FontWeight = FontWeight.SemiBold
    ) {
        StandardText(
            text = text,
            modifier = modifier,
            color = color,
            fontSize = fontSize,
            fontWeight = fontWeight
        )
    }
}
