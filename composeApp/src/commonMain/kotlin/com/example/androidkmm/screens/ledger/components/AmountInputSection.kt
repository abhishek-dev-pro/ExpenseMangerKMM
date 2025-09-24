package com.example.androidkmm.screens.ledger.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import com.example.androidkmm.design.AppStyleDesignSystem
import com.example.androidkmm.screens.ledger.LedgerTheme

@Composable
fun AmountInputSection(
    amount: String,
    onAmountChanged: (String) -> String,
    validationErrors: Map<String, String>
) {
    Column {
        Text(
            text = "Amount",
            fontSize = AppStyleDesignSystem.Typography.HEADLINE.fontSize,
            fontWeight = FontWeight.Medium,
            color = LedgerTheme.textPrimary()
        )

        Spacer(modifier = Modifier.height(AppStyleDesignSystem.Padding.ARRANGEMENT_SMALL))

        Column {
            TextField(
                value = amount,
                onValueChange = { newValue ->
                    // Allow only numbers and one decimal point, max 2 decimal places
                    val filtered = newValue.filter { char ->
                        char.isDigit() || char == '.'
                    }
                    
                    // Limit to maximum 10 digits (excluding decimal point)
                    val digitsOnly = filtered.filter { char -> char.isDigit() }
                    val decimalCount = filtered.count { char -> char == '.' }
                    
                    // Check if it's a valid decimal format and within digit limit
                    if (filtered.matches(Regex("^\\d*\\.?\\d{0,2}$")) && 
                        digitsOnly.length <= 10 && 
                        decimalCount <= 1) {
                        onAmountChanged(filtered)
                    }
                },
                placeholder = {
                    Text(
                        text = "Enter amount",
                        color = LedgerTheme.textSecondary()
                    )
                },
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedContainerColor = Color(0xFF1F1F1F),
                    unfocusedTextColor = LedgerTheme.textPrimary(),
                    focusedTextColor = LedgerTheme.textPrimary(),
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(AppStyleDesignSystem.Padding.ARRANGEMENT_XL),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = AppStyleDesignSystem.Sizes.BORDER_NORMAL,
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(AppStyleDesignSystem.Padding.ARRANGEMENT_XL)
                    ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            
            // Show error message
            validationErrors["amount"]?.let { error ->
                Text(
                    text = error,
                    color = LedgerTheme.redAmount,
                    fontSize = AppStyleDesignSystem.Typography.FOOTNOTE.fontSize,
                    modifier = Modifier.padding(top = AppStyleDesignSystem.Padding.ARRANGEMENT_TINY, start = AppStyleDesignSystem.Padding.MEDIUM_LARGE)
                )
            }
        }
    }
}
