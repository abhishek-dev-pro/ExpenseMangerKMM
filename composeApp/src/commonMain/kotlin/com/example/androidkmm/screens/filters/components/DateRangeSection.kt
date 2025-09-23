package com.example.androidkmm.screens.filters.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidkmm.design.AppStyleDesignSystem
import com.example.androidkmm.screens.filters.DateRange
import com.example.androidkmm.screens.filters.FilterColors
import com.example.androidkmm.screens.filters.PredefinedDateRange

/**
 * Date range filter section
 * 
 * Provides predefined date range options and custom date selection.
 * Allows users to filter transactions by date range with both quick selections
 * and custom date inputs.
 * 
 * @param dateRange Current date range configuration
 * @param onDateRangeChange Callback when date range changes
 * @param onShowFromDatePicker Callback to show from date picker
 * @param onShowToDatePicker Callback to show to date picker
 */
@Composable
fun DateRangeSection(
    dateRange: DateRange,
    onDateRangeChange: (DateRange) -> Unit,
    onShowFromDatePicker: () -> Unit,
    onShowToDatePicker: () -> Unit
) {
    Column {
        Text(
            text = "Date Range",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Predefined Date Ranges
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(PredefinedDateRange.values().toList()) { predefinedRange ->
                DateRangeChip(
                    label = when (predefinedRange) {
                        PredefinedDateRange.TODAY -> "Today"
                        PredefinedDateRange.THIS_WEEK -> "This Week"
                        PredefinedDateRange.THIS_MONTH -> "This Month"
                        PredefinedDateRange.LAST_3_MONTHS -> "Last 3 Months"
                    },
                    isSelected = dateRange.predefined == predefinedRange,
                    onClick = {
                        onDateRangeChange(dateRange.copy(predefined = predefinedRange))
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Custom Date Range
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // From Date - Clickable field
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onShowFromDatePicker() }
                    .background(
                        color = FilterColors.unselectedBackground,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(
                        width = AppStyleDesignSystem.Sizes.BORDER_NORMAL,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "From Date",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (dateRange.from.isNotEmpty()) {
                            // Format date for display (assuming YYYY-MM-DD format)
                            try {
                                val parts = dateRange.from.split("-")
                                if (parts.size == 3) {
                                    "${parts[2]}/${parts[1]}/${parts[0]}"
                                } else {
                                    dateRange.from
                                }
                            } catch (e: Exception) {
                                dateRange.from
                            }
                        } else {
                            "From"
                        },
                        color = if (dateRange.from.isNotEmpty()) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        fontSize = 14.sp
                    )
                }
            }
            
            // To Date - Clickable field
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onShowToDatePicker() }
                    .background(
                        color = FilterColors.unselectedBackground,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(
                        width = AppStyleDesignSystem.Sizes.BORDER_NORMAL,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "To Date",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (dateRange.to.isNotEmpty()) {
                            // Format date for display (assuming YYYY-MM-DD format)
                            try {
                                val parts = dateRange.to.split("-")
                                if (parts.size == 3) {
                                    "${parts[2]}/${parts[1]}/${parts[0]}"
                                } else {
                                    dateRange.to
                                }
                            } catch (e: Exception) {
                                dateRange.to
                            }
                        } else {
                            "To"
                        },
                        color = if (dateRange.to.isNotEmpty()) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

/**
 * Date range chip component
 * 
 * Displays a predefined date range option with selection state.
 * 
 * @param label Display text for the date range
 * @param isSelected Whether this date range is selected
 * @param onClick Callback when the chip is clicked
 */
@Composable
private fun DateRangeChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .clickable { onClick() }
            .clip(RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM))
            .border(
                width = 0.5.dp,
                color = Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM)
            ),
        shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.surfaceVariant else FilterColors.unselectedBackground
        )
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}
