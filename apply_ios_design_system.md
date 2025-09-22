# iOS-Style Design System Application Guide

## Overview
This guide documents the systematic application of the iOS-inspired design system across the Android KMM app.

## Design System Components

### Typography System
- **Large Titles**: 28sp (LARGE_TITLE), 24sp (TITLE_1), 20sp (TITLE_2), 18sp (TITLE_3)
- **Headlines**: 16sp (HEADLINE), 17sp (HEADLINE_LARGE)
- **Body Text**: 15sp (BODY), 16sp (BODY_LARGE), 14sp (BODY_SMALL)
- **Callout & Subheadlines**: 14sp (CALLOUT), 13sp (SUBHEADLINE)
- **Footnotes & Captions**: 12sp (FOOTNOTE), 11sp (CAPTION_1), 10sp (CAPTION_2)
- **Labels**: 15sp (LABEL_LARGE), 13sp (LABEL_MEDIUM), 12sp (LABEL_SMALL), 10sp (LABEL_TINY)

### Padding System
- **Micro**: 1dp (XXS), 3dp (XS)
- **Small**: 6dp (SMALL), 9dp (SMALL_MEDIUM)
- **Medium**: 12dp (MEDIUM), 16dp (MEDIUM_LARGE)
- **Large**: 18dp (LARGE), 24dp (XL), 32dp (XXL)
- **Component-specific**: 
  - Button: 8dp vertical, 16dp horizontal
  - Card: 12dp internal, 12dp external margin
  - List items: 8dp vertical, 12dp horizontal
  - Screen: 12dp horizontal, 16dp vertical
  - Sections: 24dp spacing, 18dp content spacing

### Font Weights
- **iOS Standard**: Regular (400), Medium (500), Semibold (600), Bold (700)
- **iOS Extended**: UltraLight (100), Thin (200), Light (300), Heavy (800), Black (900)

## Applied Components

### ✅ Completed
1. **iOSStyleDesignSystem.kt** - Core design system file
2. **OverviewTab.kt** - Updated spacing and layout
3. **MonthlySummaryCard.kt** - Updated typography and padding
4. **BalanceCard.kt** - Updated typography and padding
5. **TransactionListScreen.kt** - Updated screen padding

### 🔄 In Progress
6. **TransactionListContent.kt** - Update component spacing
7. **TransactionListHeader.kt** - Update header typography
8. **AddExpenseScreen.kt** - Update form spacing
9. **ProfileScreen.kt** - Update profile layout
10. **AccountsScreen.kt** - Update account list spacing

### 📋 Pending
11. **InsightsScreen.kt** - Update insights layout
12. **GroupScreen.kt** - Update group management
13. **LedgerMainScreen.kt** - Update ledger layout
14. **SearchTransactionsScreen.kt** - Update search layout
15. **FilterTransactionsBottomSheet.kt** - Update filter UI

## Implementation Pattern

### 1. Import the Design System
```kotlin
import com.example.androidkmm.design.iOSStyleDesignSystem
```

### 2. Replace Hardcoded Values
```kotlin
// Before
fontSize = 18.sp
padding = 16.dp
fontWeight = FontWeight.Bold

// After
fontSize = iOSStyleDesignSystem.Typography.HEADLINE
padding = iOSStyleDesignSystem.Padding.MEDIUM
fontWeight = iOSStyleDesignSystem.iOSFontWeights.semibold
```

### 3. Update Card Components
```kotlin
// Before
shape = RoundedCornerShape(DesignSystem.CornerRadius.md)
.padding(DesignSystem.Spacing.lg)

// After
shape = RoundedCornerShape(10.dp) // iOS rounded corners
.padding(iOSStyleDesignSystem.Padding.CARD_PADDING)
```

### 4. Update Screen Layouts
```kotlin
// Before
.padding(horizontal = 16.dp, vertical = 16.dp)

// After
.padding(
    horizontal = iOSStyleDesignSystem.Padding.SCREEN_HORIZONTAL,
    vertical = iOSStyleDesignSystem.Padding.SCREEN_VERTICAL
)
```

## Benefits

1. **Consistency**: Unified spacing and typography across the app
2. **iOS-like Feel**: Compact, clean design following iOS principles
3. **Maintainability**: Centralized design system for easy updates
4. **Accessibility**: Proper font sizes and spacing for readability
5. **Performance**: Optimized spacing reduces layout complexity

## Next Steps

1. Continue applying to remaining components
2. Test on different screen sizes
3. Validate accessibility compliance
4. Update documentation
5. Create component examples
