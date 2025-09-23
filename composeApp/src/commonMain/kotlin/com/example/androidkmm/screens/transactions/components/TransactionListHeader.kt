package com.example.androidkmm.screens.transactions.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.androidkmm.design.AppStyleDesignSystem
import com.example.androidkmm.utils.TextUtils

/**
 * Header component for transaction list screen
 * 
 * Features:
 * - Search functionality
 * - Filter options
 * - Add transaction button
 * - Date range selection
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListHeader(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    onAddTransactionClick: () -> Unit,
    onDateRangeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                TextUtils.StandardText(
                    text = "Transactions",
                    fontSize = AppStyleDesignSystem.Typography.TITLE_2.fontSize,
                    fontWeight = AppStyleDesignSystem.iOSFontWeights.bold,
                    color = Color.White
                )
            },
            actions = {
                IconButton(onClick = onFilterClick) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter",
                        tint = Color.White
                    )
                }
                IconButton(onClick = onAddTransactionClick) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Transaction",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )
        
        // Search Bar
        SearchBar(
            query = searchQuery,
            onQueryChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppStyleDesignSystem.Padding.MEDIUM_LARGE)
        )
        
        // Date Range Selector
        DateRangeSelector(
            onDateRangeClick = onDateRangeClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppStyleDesignSystem.Padding.MEDIUM_LARGE, vertical = AppStyleDesignSystem.Padding.SMALL)
        )
    }
}

/**
 * Search bar component
 */
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            TextUtils.StandardText(
                text = "Search transactions...",
                fontSize = AppStyleDesignSystem.Typography.BODY.fontSize,
                color = Color.Gray
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color.Gray
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear",
                        tint = Color.Gray
                    )
                }
            }
        },
        modifier = modifier,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Blue,
            unfocusedBorderColor = Color.Gray
        )
    )
}

/**
 * Date range selector component
 */
@Composable
private fun DateRangeSelector(
    onDateRangeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onDateRangeClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color.Gray
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppStyleDesignSystem.Padding.MEDIUM_LARGE),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextUtils.StandardText(
                text = "This Month",
                fontSize = AppStyleDesignSystem.Typography.BODY.fontSize,
                fontWeight = AppStyleDesignSystem.iOSFontWeights.medium,
                color = Color.White
            )
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = "Date Range",
                tint = Color.Gray
            )
        }
    }
}
