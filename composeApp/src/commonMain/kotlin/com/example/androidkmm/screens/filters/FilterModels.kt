package com.example.androidkmm.screens.filters

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.androidkmm.models.TransactionType

/**
 * Data classes for filter functionality
 * 
 * These models represent the filter options available in the transaction filter bottom sheet.
 * They provide a structured way to handle different types of filters including transaction type,
 * categories, accounts, date ranges, and amount ranges.
 */

/**
 * Main filter options container
 * 
 * @property transactionType The selected transaction type filter (null means all types)
 * @property selectedCategories Set of selected category names for filtering
 * @property selectedAccounts Set of selected account names for filtering
 * @property dateRange Optional date range filter
 * @property amountRange Optional amount range filter
 */
data class FilterOptions(
    val transactionType: TransactionType? = null,
    val selectedCategories: Set<String> = emptySet(),
    val selectedAccounts: Set<String> = emptySet(),
    val dateRange: DateRange? = null,
    val amountRange: AmountRange? = null
)

/**
 * Date range filter configuration
 * 
 * @property from Start date in YYYY-MM-DD format
 * @property to End date in YYYY-MM-DD format
 * @property predefined Optional predefined date range selection
 */
data class DateRange(
    val from: String = "",
    val to: String = "",
    val predefined: PredefinedDateRange? = null
)

/**
 * Predefined date range options for quick selection
 */
enum class PredefinedDateRange {
    TODAY, THIS_WEEK, THIS_MONTH, LAST_3_MONTHS
}

/**
 * Amount range filter configuration
 * 
 * @property min Minimum amount (null means no minimum)
 * @property max Maximum amount (null means no maximum)
 * @property predefined Optional predefined amount range selection
 */
data class AmountRange(
    val min: Double? = null,
    val max: Double? = null,
    val predefined: PredefinedAmountRange? = null
)

/**
 * Predefined amount range options for quick selection
 */
enum class PredefinedAmountRange {
    UNDER_25, BETWEEN_25_100, BETWEEN_100_500, OVER_500
}

/**
 * Color definitions for filter UI components
 * 
 * Provides consistent color scheme for filter interface elements
 */
object FilterColors {
    val selectedBorder = Color(0xFFFFFFFF)
    val unselectedBackground = Color(0xFF2C2C2E)
    val income = Color(0xFF10B981)
    val expense = Color(0xFFEF4444)
    val transfer = Color(0xFF3B82F6)
}

/**
 * Filter option card data
 * 
 * @property icon Icon to display for the filter option
 * @property iconColor Color for the icon
 * @property title Display title for the option
 * @property subtitle Display subtitle for the option
 * @property isSelected Whether this option is currently selected
 * @property onClick Callback when the option is clicked
 */
data class FilterOptionData(
    val icon: ImageVector,
    val iconColor: Color,
    val title: String,
    val subtitle: String,
    val isSelected: Boolean,
    val onClick: () -> Unit
)