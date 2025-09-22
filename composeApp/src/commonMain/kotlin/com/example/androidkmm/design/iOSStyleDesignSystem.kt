package com.example.androidkmm.design

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * iOS-Inspired Design System for Android KMM
 * 
 * Provides consistent typography and spacing throughout the app
 * following iOS design principles adapted for Android.
 */

object iOSStyleDesignSystem {
    // iOS-inspired Typography Styles
    object Typography {
        val LARGE_TITLE = TextStyle(
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 34.sp
        )
        
        val TITLE_1 = TextStyle(
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 30.sp
        )
        
        val TITLE_2 = TextStyle(
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 26.sp
        )
        
        val TITLE_3 = TextStyle(
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 24.sp
        )
        
        val HEADLINE = TextStyle(
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 22.sp
        )
        
        val BODY = TextStyle(
            fontSize = 15.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 20.sp
        )
        
        val CALL_OUT = TextStyle(
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 19.sp
        )
        
        val SUBHEAD = TextStyle(
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 18.sp
        )
        
        val FOOTNOTE = TextStyle(
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 16.sp
        )
        
        val CAPTION_1 = TextStyle(
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 15.sp
        )
        
        val CAPTION_2 = TextStyle(
            fontSize = 10.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 14.sp
        )
    }

    // iOS-inspired Padding System
    object Padding {
        val XXS = 1.dp
        val XS = 3.dp
        val SMALL = 6.dp
        val SMALL_MEDIUM = 9.dp
        val MEDIUM = 12.dp
        val MEDIUM_LARGE = 16.dp
        val LARGE = 18.dp
        val XL = 24.dp
        val XXL = 32.dp
        val XXXL = 48.dp

        // Screen Padding
        val SCREEN_HORIZONTAL = 12.dp
        val SCREEN_VERTICAL = 16.dp

        // Card Padding
        val CARD_PADDING = 12.dp
        val CARD_MARGIN = 12.dp

        // Component-specific padding
        val BUTTON_VERTICAL = 8.dp
        val BUTTON_HORIZONTAL = 16.dp
        val LIST_ITEM_VERTICAL = 8.dp
        val LIST_ITEM_HORIZONTAL = 12.dp
        val SECTION_SPACING = 24.dp
        val CONTENT_SPACING = 18.dp
        
        // Arrangement Spacing
        val ARRANGEMENT_TINY = 4.dp
        val ARRANGEMENT_SMALL = 8.dp
        val ARRANGEMENT_MEDIUM = 12.dp
        val ARRANGEMENT_LARGE = 16.dp
        val ARRANGEMENT_XL = 20.dp
        val ARRANGEMENT_XXL = 24.dp
    }

    // iOS-inspired Component Sizes
    object Sizes {
        val BUTTON_HEIGHT = 44.dp
        val INPUT_HEIGHT = 44.dp
        
        // Icon Sizes
        val ICON_SIZE_TINY = 12.dp
        val ICON_SIZE_SMALL = 16.dp
        val ICON_SIZE_MEDIUM = 20.dp
        val ICON_SIZE_LARGE = 24.dp
        val ICON_SIZE_XL = 32.dp
        val ICON_SIZE_XXL = 40.dp
        val ICON_SIZE_XXXL = 48.dp
        val ICON_SIZE_HUGE = 64.dp
        val ICON_SIZE_MASSIVE = 80.dp
        val ICON_SIZE_GIANT = 120.dp
        
        // Avatar Sizes
        val AVATAR_SMALL = 32.dp
        val AVATAR_MEDIUM = 40.dp
        val AVATAR_LARGE = 48.dp
        val AVATAR_XL = 60.dp
        
        // Border Widths
        val BORDER_THIN = 0.5.dp
        val BORDER_NORMAL = 1.dp
        val BORDER_THICK = 2.dp
        
        // Spacer Sizes
        val SPACER_TINY = 4.dp
        val SPACER_SMALL = 8.dp
        val SPACER_MEDIUM = 16.dp
        val SPACER_LARGE = 20.dp
        val SPACER_XL = 24.dp
        val SPACER_XXL = 32.dp
    }

    // iOS-inspired Border Radius
    object CornerRadius {
        val SMALL = 8.dp
        val MEDIUM = 10.dp
        val LARGE = 12.dp
        val XL = 16.dp
    }

    // iOS-inspired Font Weights
    object iOSFontWeights {
        val ultraLight = FontWeight.W100
        val thin = FontWeight.W200
        val light = FontWeight.W300
        val regular = FontWeight.W400
        val medium = FontWeight.W500
        val semibold = FontWeight.W600
        val bold = FontWeight.W700
        val heavy = FontWeight.W800
        val black = FontWeight.W900
    }

    // iOS-inspired Colors
    object Colors {
        val primary = Color(0xFF007AFF)
        val secondary = Color(0xFF5856D6)
        val success = Color(0xFF34C759)
        val warning = Color(0xFFFF9500)
        val error = Color(0xFFFF3B30)
        val background = Color(0xFFF2F2F7)
        val surface = Color.White
        val onSurface = Color(0xFF1C1C1E)
        val onSurfaceVariant = Color(0xFF3C3C43)
    }

    // iOS-inspired Shapes
    object Shapes {
        val small = RoundedCornerShape(8.dp)
        val medium = RoundedCornerShape(10.dp)
        val large = RoundedCornerShape(12.dp)
        val xl = RoundedCornerShape(16.dp)
    }
}

// iOS-style component examples
@Composable
fun iOSStyleCard(
    title: String,
    subtitle: String,
    body: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = iOSStyleDesignSystem.Padding.CARD_MARGIN),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp) // iOS subtle shadows
    ) {
        Column(
            modifier = Modifier.padding(iOSStyleDesignSystem.Padding.CARD_PADDING)
        ) {
            Text(
                text = title,
                style = iOSStyleDesignSystem.Typography.HEADLINE,
                fontWeight = iOSStyleDesignSystem.iOSFontWeights.semibold // iOS uses semibold frequently
            )
            
            Spacer(modifier = Modifier.height(iOSStyleDesignSystem.Padding.XS))
            
            Text(
                text = subtitle,
                style = iOSStyleDesignSystem.Typography.SUBHEAD,
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(iOSStyleDesignSystem.Padding.MEDIUM))
            
            Text(
                text = body,
                style = iOSStyleDesignSystem.Typography.BODY,
                lineHeight = 20.sp // Compact iOS line height
            )
        }
    }
}

@Composable
fun iOSStyleButton(
    text: String,
    onClick: () -> Unit,
    isPrimary: Boolean = true,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(iOSStyleDesignSystem.Sizes.BUTTON_HEIGHT), // Compact iOS button height
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isPrimary) iOSStyleDesignSystem.Colors.primary else Color.Transparent // iOS blue
        ),
        shape = RoundedCornerShape(iOSStyleDesignSystem.CornerRadius.MEDIUM) // iOS rounded corners
    ) {
        Text(
            text = text,
            style = iOSStyleDesignSystem.Typography.BODY,
            fontWeight = iOSStyleDesignSystem.iOSFontWeights.semibold
        )
    }
}

@Composable
fun iOSStyleListItem(
    title: String,
    subtitle: String? = null,
    trailing: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = iOSStyleDesignSystem.Padding.LIST_ITEM_HORIZONTAL,
                vertical = iOSStyleDesignSystem.Padding.LIST_ITEM_VERTICAL
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = iOSStyleDesignSystem.Typography.BODY,
                fontWeight = iOSStyleDesignSystem.iOSFontWeights.regular
            )
            
            subtitle?.let {
                Spacer(modifier = Modifier.height(iOSStyleDesignSystem.Padding.XXS))
                Text(
                    text = it,
                    style = iOSStyleDesignSystem.Typography.FOOTNOTE,
                    color = Color.Gray
                )
            }
        }
        
        trailing?.invoke()
    }
}

// Screen layout example
@Composable
fun iOSStyleScreen(
    title: String,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = iOSStyleDesignSystem.Padding.SCREEN_HORIZONTAL)
    ) {
        Spacer(modifier = Modifier.height(iOSStyleDesignSystem.Padding.SCREEN_VERTICAL))
        
        // Large title
        Text(
            text = title,
            style = iOSStyleDesignSystem.Typography.LARGE_TITLE,
            fontWeight = iOSStyleDesignSystem.iOSFontWeights.bold
        )
        
        Spacer(modifier = Modifier.height(iOSStyleDesignSystem.Padding.CONTENT_SPACING))
        
        content()
    }
}