package com.example.androidkmm.screens.filters

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.androidkmm.screens.filters.components.FilterContent

/**
 * Filter transactions bottom sheet
 * 
 * Main entry point for the transaction filter functionality.
 * Provides a modal bottom sheet interface for configuring transaction filters
 * including type, categories, accounts, date ranges, and amount ranges.
 * 
 * @param isVisible Whether the bottom sheet is currently visible
 * @param onDismiss Callback when the bottom sheet is dismissed
 * @param onApplyFilters Callback when filters are applied with the selected options
 * @param initialFilters Initial filter configuration to pre-populate the form
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterTransactionsBottomSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onApplyFilters: (FilterOptions) -> Unit,
    initialFilters: FilterOptions = FilterOptions()
) {
    if (!isVisible) return
    
    var filterOptions by remember { mutableStateOf(initialFilters) }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
    ) {
        FilterContent(
            filterOptions = filterOptions,
            onFilterOptionsChange = { filterOptions = it },
            onApplyFilters = { onApplyFilters(filterOptions) },
            onDismiss = onDismiss
        )
    }
}
