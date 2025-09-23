package com.example.androidkmm.utils

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.androidkmm.design.AppStyleDesignSystem

/**
 * Utility functions for creating consistent card components
 */
object CardUtils {

    /**
     * Creates a standard card surface with consistent styling
     */
    @Composable
    fun StandardCard(
        modifier: Modifier = Modifier,
        onClick: (() -> Unit)? = null,
        content: @Composable () -> Unit
    ) {
        val clickableModifier = if (onClick != null) {
            modifier.clickable { onClick() }
        } else {
            modifier
        }

        Surface(
            modifier = clickableModifier
                .padding(top = 4.dp)
                .clip(RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM))
                .border(
                    width = AppStyleDesignSystem.Sizes.BORDER_THIN,
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM)
                ),
            color = Color(0xFF1A1A1A),
            tonalElevation = 2.dp,
            shadowElevation = 6.dp
        ) {
            content()
        }
    }


    /**
     * Creates a standard item card surface (used by BillItem and GroupItem)
     */
    @Composable
    fun ItemCardSurface(
        content: @Composable () -> Unit
    ) {
        Surface(
            modifier = Modifier
                .padding(top = 4.dp)
                .clip(RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM))
                .border(
                    width = AppStyleDesignSystem.Sizes.BORDER_THIN, // very thin border
                    color = Color.White.copy(alpha = 0.2f), // subtle white
                    shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM)
                ),
            color = Color(0xFF1A1A1A), // slightly lighter black background
            tonalElevation = 2.dp,
            shadowElevation = 6.dp
        ) {
            content()
        }
    }
}
