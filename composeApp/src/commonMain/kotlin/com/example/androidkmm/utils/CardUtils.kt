package com.example.androidkmm.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.androidkmm.design.DesignSystem

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
                .clip(RoundedCornerShape(DesignSystem.CornerRadius.md))
                .border(
                    width = 0.5.dp,
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(DesignSystem.CornerRadius.md)
                ),
            color = Color(0xFF1A1A1A),
            tonalElevation = 2.dp,
            shadowElevation = 6.dp
        ) {
            content()
        }
    }

    /**
     * Creates a colored circular icon background
     */
    @Composable
    fun ColoredIconBackground(
        color: Color,
        icon: @Composable () -> Unit
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .size(DesignSystem.IconSize.avatar)
                .background(color, shape = androidx.compose.foundation.shape.CircleShape),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            icon()
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
                .clip(RoundedCornerShape(DesignSystem.CornerRadius.md))
                .border(
                    width = 0.5.dp, // very thin border
                    color = Color.White.copy(alpha = 0.2f), // subtle white
                    shape = RoundedCornerShape(DesignSystem.CornerRadius.md)
                ),
            color = Color(0xFF1A1A1A), // slightly lighter black background
            tonalElevation = 2.dp,
            shadowElevation = 6.dp
        ) {
            content()
        }
    }
}
