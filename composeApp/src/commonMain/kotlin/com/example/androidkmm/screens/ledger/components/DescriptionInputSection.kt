package com.example.androidkmm.screens.ledger.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.androidkmm.design.AppStyleDesignSystem
import com.example.androidkmm.screens.ledger.LedgerTheme

@Composable
fun DescriptionInputSection(
    description: String,
    onDescriptionChanged: (String) -> Unit,
    validationErrors: Map<String, String>
) {
    Column {
        Text(
            text = "Description (Optional)",
            fontSize = AppStyleDesignSystem.Typography.HEADLINE.fontSize,
            fontWeight = FontWeight.Medium,
            color = LedgerTheme.textPrimary()
        )

        Spacer(modifier = Modifier.height(AppStyleDesignSystem.Padding.ARRANGEMENT_SMALL))

        Column {
            TextField(
                value = description,
                onValueChange = onDescriptionChanged,
                placeholder = {
                    Text(
                        text = "e.g., Dinner split, Uber ride share (optional)",
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
                    .height(AppStyleDesignSystem.Sizes.ICON_SIZE_GIANT)
                    .border(
                        width = AppStyleDesignSystem.Sizes.BORDER_NORMAL,
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(AppStyleDesignSystem.Padding.ARRANGEMENT_XL)
                    ),
                maxLines = 3
            )
            
            // Show error message
            validationErrors["description"]?.let { error ->
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
