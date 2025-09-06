package com.example.androidkmm.design

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * iOS-inspired Design System following Apple's Human Interface Guidelines
 * 
 * Typography sizes are based on iOS system font sizes:
 * - Large Title: 34sp (iOS 11+)
 * - Title 1: 28sp
 * - Title 2: 22sp
 * - Title 3: 20sp
 * - Headline: 17sp (semibold)
 * - Body: 17sp (regular)
 * - Callout: 16sp
 * - Subhead: 15sp
 * - Footnote: 13sp
 * - Caption 1: 12sp
 * - Caption 2: 11sp
 */

object DesignSystem {
    
    // MARK: - Typography
    object Typography {
        // Large titles for main headings
        val largeTitle = 34.sp
        val title1 = 28.sp
        val title2 = 22.sp
        val title3 = 20.sp
        
        // Body text
        val headline = 17.sp
        val body = 17.sp
        val callout = 16.sp
        val subhead = 15.sp
        
        // Small text
        val footnote = 13.sp
        val caption1 = 12.sp
        val caption2 = 11.sp
        
        // Custom sizes for specific use cases
        val balanceAmount = 28.sp
        val balanceLabel = 14.sp
        val balanceSubtext = 14.sp
        val cardTitle = 16.sp
        val cardSubtitle = 14.sp
        val buttonText = 14.sp
        val tabLabel = 12.sp
    }
    
    // MARK: - Spacing
    object Spacing {
        // Base spacing unit (8dp following iOS 8pt grid)
        private const val baseUnit = 8
        
        // Micro spacing
        val xs = (baseUnit * 0.5).dp    // 4dp
        val sm = baseUnit.dp            // 8dp
        val md = (baseUnit * 1.5).dp    // 12dp
        val lg = (baseUnit * 2).dp      // 16dp
        val xl = (baseUnit * 3).dp      // 24dp
        val xxl = (baseUnit * 4).dp     // 32dp
        val xxxl = (baseUnit * 6).dp    // 48dp
        
        // Specific spacing for common use cases
        val cardPadding = lg            // 16dp
        val sectionSpacing = xl         // 24dp
        val itemSpacing = md            // 12dp
        val buttonPadding = md          // 12dp
        val bottomNavHeight = 80.dp     // iOS standard
        val safeAreaPadding = lg        // 16dp
    }
    
    // MARK: - Corner Radius
    object CornerRadius {
        val xs = 4.dp
        val sm = 8.dp
        val md = 12.dp
        val lg = 16.dp
        val xl = 20.dp
        val xxl = 24.dp
        val round = 50.dp
    }
    
    // MARK: - Icon Sizes
    object IconSize {
        val xs = 12.dp
        val sm = 16.dp
        val md = 20.dp
        val lg = 24.dp
        val xl = 28.dp
        val xxl = 32.dp
        val avatar = 36.dp
        val largeAvatar = 48.dp
    }
    
    // MARK: - Component Heights
    object ComponentHeight {
        val button = 44.dp              // iOS standard button height
        val textField = 44.dp           // iOS standard text field height
        val listItem = 56.dp            // iOS standard list item height
        val card = 120.dp               // Custom card height
        val balanceCard = 120.dp        // Balance card height
        val quickActionButton = 48.dp   // Quick action button height
    }
    
    // MARK: - Elevation/Shadow
    object Elevation {
        val none = 0.dp
        val sm = 2.dp
        val md = 4.dp
        val lg = 8.dp
        val xl = 12.dp
    }
}
