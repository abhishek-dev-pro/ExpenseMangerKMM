package com.example.androidkmm.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Responsive Design Utilities
 * 
 * Provides consistent responsive design across all screen sizes.
 * 
 * Features:
 * - Automatic screen size detection (Small, Medium, Large)
 * - Responsive dimensions for all UI elements
 * - Consistent spacing, typography, and sizing
 * - Screen-specific optimizations
 * - Easy-to-use composable functions
 * 
 * Screen Categories:
 * - Small: < 600dp height (iPhone SE, Pixel 4a)
 * - Medium: 600-700dp height (Standard phones)
 * - Large: > 700dp height (iPhone Pro Max, large tablets)
 * 
 * Benefits:
 * - Consistent UI across all devices
 * - Optimized for different screen sizes
 * - Easy maintenance and updates
 * - Better user experience
 * 
 * Usage:
 * ```kotlin
 * val dimensions = ResponsiveDesign.getResponsiveDimensions()
 * val spacing = ResponsiveDesign.getSpacing()
 * val isSmall = ResponsiveDesign.isSmallScreen()
 * ```
 */
object ResponsiveDesign {
    
    /**
     * Screen size categories
     */
    enum class ScreenSize {
        SMALL,    // < 600dp height
        MEDIUM,   // 600-700dp height
        LARGE     // > 700dp height
    }
    
    /**
     * Responsive dimensions data class
     */
    data class ResponsiveDimensions(
        val spacing: Dp,
        val titleFontSize: TextUnit,
        val labelFontSize: TextUnit,
        val inputHeight: Dp,
        val buttonHeight: Dp,
        val buttonPadding: Dp,
        val amountFontSize: TextUnit,
        val receiptHeight: Dp,
        val cardPadding: Dp,
        val iconSize: Dp
    )
    
    /**
     * Get screen size category based on height
     */
    @Composable
    fun getScreenSize(): ScreenSize {
        val configuration = LocalConfiguration.current
        val screenHeight = configuration.screenHeightDp
        
        return when {
            screenHeight < 600 -> ScreenSize.SMALL
            screenHeight < 700 -> ScreenSize.MEDIUM
            else -> ScreenSize.LARGE
        }
    }
    
    /**
     * Get responsive dimensions for current screen size
     */
    @Composable
    fun getResponsiveDimensions(): ResponsiveDimensions {
        val screenSize = getScreenSize()
        
        return when (screenSize) {
            ScreenSize.SMALL -> ResponsiveDimensions(
                spacing = 12.dp,
                titleFontSize = 14.sp,
                labelFontSize = 12.sp,
                inputHeight = 40.dp,
                buttonHeight = 48.dp,
                buttonPadding = 8.dp,
                amountFontSize = 32.sp,
                receiptHeight = 40.dp,
                cardPadding = 12.dp,
                iconSize = 20.dp
            )
            ScreenSize.MEDIUM -> ResponsiveDimensions(
                spacing = 16.dp,
                titleFontSize = 16.sp,
                labelFontSize = 14.sp,
                inputHeight = 48.dp,
                buttonHeight = 52.dp,
                buttonPadding = 12.dp,
                amountFontSize = 36.sp,
                receiptHeight = 48.dp,
                cardPadding = 16.dp,
                iconSize = 24.dp
            )
            ScreenSize.LARGE -> ResponsiveDimensions(
                spacing = 20.dp,
                titleFontSize = 18.sp,
                labelFontSize = 16.sp,
                inputHeight = 56.dp,
                buttonHeight = 56.dp,
                buttonPadding = 16.dp,
                amountFontSize = 40.sp,
                receiptHeight = 56.dp,
                cardPadding = 20.dp,
                iconSize = 28.dp
            )
        }
    }
    
    /**
     * Get responsive spacing
     */
    @Composable
    fun getSpacing(): Dp {
        return getResponsiveDimensions().spacing
    }
    
    /**
     * Get responsive title font size
     */
    @Composable
    fun getTitleFontSize(): TextUnit {
        return getResponsiveDimensions().titleFontSize
    }
    
    /**
     * Get responsive label font size
     */
    @Composable
    fun getLabelFontSize(): TextUnit {
        return getResponsiveDimensions().labelFontSize
    }
    
    /**
     * Get responsive input height
     */
    @Composable
    fun getInputHeight(): Dp {
        return getResponsiveDimensions().inputHeight
    }
    
    /**
     * Get responsive button height
     */
    @Composable
    fun getButtonHeight(): Dp {
        return getResponsiveDimensions().buttonHeight
    }
    
    /**
     * Get responsive button padding
     */
    @Composable
    fun getButtonPadding(): Dp {
        return getResponsiveDimensions().buttonPadding
    }
    
    /**
     * Get responsive amount font size
     */
    @Composable
    fun getAmountFontSize(): TextUnit {
        return getResponsiveDimensions().amountFontSize
    }
    
    /**
     * Get responsive receipt height
     */
    @Composable
    fun getReceiptHeight(): Dp {
        return getResponsiveDimensions().receiptHeight
    }
    
    /**
     * Get responsive card padding
     */
    @Composable
    fun getCardPadding(): Dp {
        return getResponsiveDimensions().cardPadding
    }
    
    /**
     * Get responsive icon size
     */
    @Composable
    fun getIconSize(): Dp {
        return getResponsiveDimensions().iconSize
    }
    
    /**
     * Check if screen is small
     */
    @Composable
    fun isSmallScreen(): Boolean {
        return getScreenSize() == ScreenSize.SMALL
    }
    
    /**
     * Check if screen is medium
     */
    @Composable
    fun isMediumScreen(): Boolean {
        return getScreenSize() == ScreenSize.MEDIUM
    }
    
    /**
     * Check if screen is large
     */
    @Composable
    fun isLargeScreen(): Boolean {
        return getScreenSize() == ScreenSize.LARGE
    }
    
    /**
     * Get responsive column spacing
     */
    @Composable
    fun getColumnSpacing(): Dp {
        return when (getScreenSize()) {
            ScreenSize.SMALL -> 8.dp
            ScreenSize.MEDIUM -> 12.dp
            ScreenSize.LARGE -> 16.dp
        }
    }
    
    /**
     * Get responsive row spacing
     */
    @Composable
    fun getRowSpacing(): Dp {
        return when (getScreenSize()) {
            ScreenSize.SMALL -> 6.dp
            ScreenSize.MEDIUM -> 8.dp
            ScreenSize.LARGE -> 12.dp
        }
    }
    
    /**
     * Get responsive elevation
     */
    @Composable
    fun getElevation(): Dp {
        return when (getScreenSize()) {
            ScreenSize.SMALL -> 2.dp
            ScreenSize.MEDIUM -> 4.dp
            ScreenSize.LARGE -> 6.dp
        }
    }
    
    /**
     * Get responsive corner radius
     */
    @Composable
    fun getCornerRadius(): Dp {
        return when (getScreenSize()) {
            ScreenSize.SMALL -> 8.dp
            ScreenSize.MEDIUM -> 12.dp
            ScreenSize.LARGE -> 16.dp
        }
    }
}
