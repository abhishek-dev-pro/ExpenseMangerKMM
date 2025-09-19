package com.example.androidkmm.screens.filters.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidkmm.design.DesignSystem
import com.example.androidkmm.screens.filters.AmountRange
import com.example.androidkmm.screens.filters.FilterColors
import com.example.androidkmm.screens.filters.PredefinedAmountRange
import com.example.androidkmm.database.rememberSQLiteSettingsDatabase
import com.example.androidkmm.models.AppSettings
import androidx.compose.runtime.collectAsState

/**
 * Amount range filter section
 * 
 * Provides predefined amount range options and custom amount inputs.
 * Allows users to filter transactions by amount range with both quick selections
 * and custom amount inputs.
 * 
 * @param amountRange Current amount range configuration
 * @param onAmountRangeChange Callback when amount range changes
 */
@Composable
fun AmountRangeSection(
    amountRange: AmountRange,
    onAmountRangeChange: (AmountRange) -> Unit
) {
    // Get currency symbol from settings
    val settingsDatabaseManager = rememberSQLiteSettingsDatabase()
    val appSettings = settingsDatabaseManager.getAppSettings().collectAsState(initial = AppSettings())
    val currencySymbol = appSettings.value.currencySymbol
    Column {
        Text(
            text = "Amount Range",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Predefined Amount Ranges
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(PredefinedAmountRange.values().toList()) { predefinedRange ->
                AmountRangeChip(
                    label = when (predefinedRange) {
                        PredefinedAmountRange.UNDER_25 -> "Under $currencySymbol${25}"
                        PredefinedAmountRange.BETWEEN_25_100 -> "$currencySymbol${25} - $currencySymbol${100}"
                        PredefinedAmountRange.BETWEEN_100_500 -> "$currencySymbol${100} - $currencySymbol${500}"
                        PredefinedAmountRange.OVER_500 -> "Over $currencySymbol${500}"
                    },
                    isSelected = amountRange.predefined == predefinedRange,
                    onClick = {
                        onAmountRangeChange(amountRange.copy(predefined = predefinedRange))
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Custom Amount Range
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Min Amount
            OutlinedTextField(
                value = amountRange.min?.toString() ?: "",
                onValueChange = { newMin ->
                    val minValue = newMin.toDoubleOrNull()
                    onAmountRangeChange(amountRange.copy(min = minValue, predefined = null))
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "Min Amount",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                leadingIcon = {
                    Text(
                        text = currencySymbol,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = FilterColors.unselectedBackground,
                    unfocusedContainerColor = FilterColors.unselectedBackground,
                    focusedBorderColor = MaterialTheme.colorScheme.outline,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                ),
                singleLine = true
            )
            
            // Max Amount
            OutlinedTextField(
                value = amountRange.max?.toString() ?: "",
                onValueChange = { newMax ->
                    val maxValue = newMax.toDoubleOrNull()
                    onAmountRangeChange(amountRange.copy(max = maxValue, predefined = null))
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "Max Amount",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                leadingIcon = {
                    Text(
                        text = currencySymbol,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = FilterColors.unselectedBackground,
                    unfocusedContainerColor = FilterColors.unselectedBackground,
                    focusedBorderColor = MaterialTheme.colorScheme.outline,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
                singleLine = true
            )
        }
    }
}

/**
 * Amount range chip component
 * 
 * Displays a predefined amount range option with selection state.
 * 
 * @param label Display text for the amount range
 * @param isSelected Whether this amount range is selected
 * @param onClick Callback when the chip is clicked
 */
@Composable
private fun AmountRangeChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .clickable { onClick() }
            .clip(RoundedCornerShape(DesignSystem.CornerRadius.md))
            .border(
                width = 0.5.dp,
                color = Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(DesignSystem.CornerRadius.md)
            ),
        shape = RoundedCornerShape(DesignSystem.CornerRadius.md),
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
