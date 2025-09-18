package com.example.androidkmm.screens.filters.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.androidkmm.database.rememberSQLiteAccountDatabase
import com.example.androidkmm.database.rememberSQLiteCategoryDatabase
import com.example.androidkmm.models.Account
import com.example.androidkmm.models.Category
import com.example.androidkmm.screens.filters.AmountRange
import com.example.androidkmm.screens.filters.DateRange
import com.example.androidkmm.screens.filters.FilterOptions

/**
 * Main filter content component
 * 
 * Orchestrates all filter sections and handles the overall filter state.
 * Provides a clean interface for managing complex filter configurations
 * with multiple filter types (transaction type, categories, accounts, dates, amounts).
 * 
 * @param filterOptions Current filter configuration
 * @param onFilterOptionsChange Callback when filter options change
 * @param onApplyFilters Callback when filters are applied
 * @param onDismiss Callback when filter is dismissed
 */
@Composable
fun FilterContent(
    filterOptions: FilterOptions,
    onFilterOptionsChange: (FilterOptions) -> Unit,
    onApplyFilters: () -> Unit,
    onDismiss: () -> Unit
) {
    val categoryDatabaseManager = rememberSQLiteCategoryDatabase()
    val accountDatabaseManager = rememberSQLiteAccountDatabase()
    
    val categoriesState = categoryDatabaseManager.getAllCategories().collectAsState(initial = emptyList<Category>())
    val accountsState = accountDatabaseManager.getAllAccounts().collectAsState(initial = emptyList<Account>())
    
    // Date picker states
    var showFromDatePicker by remember { mutableStateOf(false) }
    var showToDatePicker by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        // Header
        FilterHeader(onDismiss = onDismiss)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Transaction Type Section
            item {
                TransactionTypeSection(
                    selectedType = filterOptions.transactionType,
                    onTypeSelected = { type ->
                        onFilterOptionsChange(filterOptions.copy(transactionType = type))
                    }
                )
            }
            
            // Categories Section
            item {
                CategoriesSection(
                    categories = categoriesState.value,
                    selectedCategories = filterOptions.selectedCategories,
                    onCategoryToggle = { categoryName ->
                        val newSelected = if (filterOptions.selectedCategories.contains(categoryName)) {
                            filterOptions.selectedCategories - categoryName
                        } else {
                            filterOptions.selectedCategories + categoryName
                        }
                        onFilterOptionsChange(filterOptions.copy(selectedCategories = newSelected))
                    }
                )
            }
            
            // Accounts Section
            item {
                AccountsSection(
                    accounts = accountsState.value,
                    selectedAccounts = filterOptions.selectedAccounts,
                    onAccountToggle = { accountName ->
                        val newSelected = if (filterOptions.selectedAccounts.contains(accountName)) {
                            filterOptions.selectedAccounts - accountName
                        } else {
                            filterOptions.selectedAccounts + accountName
                        }
                        onFilterOptionsChange(filterOptions.copy(selectedAccounts = newSelected))
                    }
                )
            }
            
            // Date Range Section
            item {
                DateRangeSection(
                    dateRange = filterOptions.dateRange ?: DateRange(),
                    onDateRangeChange = { newDateRange ->
                        onFilterOptionsChange(filterOptions.copy(dateRange = newDateRange))
                    },
                    onShowFromDatePicker = { showFromDatePicker = true },
                    onShowToDatePicker = { showToDatePicker = true }
                )
            }
            
            // Amount Range Section
            item {
                AmountRangeSection(
                    amountRange = filterOptions.amountRange ?: AmountRange(),
                    onAmountRangeChange = { newAmountRange ->
                        onFilterOptionsChange(filterOptions.copy(amountRange = newAmountRange))
                    }
                )
            }
            
            // Action Buttons
            item {
                ActionButtons(
                    onApplyFilters = onApplyFilters,
                    onCancel = onDismiss
                )
            }
        }
    }
    
    // Date Picker Dialogs
    if (showFromDatePicker) {
        FilterDatePickerDialog(
            onDismiss = { showFromDatePicker = false },
            onDateSelected = { selectedDate ->
                onFilterOptionsChange(
                    filterOptions.copy(
                        dateRange = filterOptions.dateRange?.copy(
                            from = selectedDate,
                            predefined = null
                        ) ?: DateRange(from = selectedDate)
                    )
                )
                showFromDatePicker = false
            },
            initialDate = filterOptions.dateRange?.from ?: ""
        )
    }
    
    if (showToDatePicker) {
        FilterDatePickerDialog(
            onDismiss = { showToDatePicker = false },
            onDateSelected = { selectedDate ->
                onFilterOptionsChange(
                    filterOptions.copy(
                        dateRange = filterOptions.dateRange?.copy(
                            to = selectedDate,
                            predefined = null
                        ) ?: DateRange(to = selectedDate)
                    )
                )
                showToDatePicker = false
            },
            initialDate = filterOptions.dateRange?.to ?: ""
        )
    }
}
