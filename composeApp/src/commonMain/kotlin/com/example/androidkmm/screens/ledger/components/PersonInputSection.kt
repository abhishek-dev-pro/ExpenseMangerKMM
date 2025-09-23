package com.example.androidkmm.screens.ledger.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.androidkmm.design.AppStyleDesignSystem
import com.example.androidkmm.screens.ledger.LedgerPerson
import com.example.androidkmm.screens.ledger.LedgerTheme

@Composable
fun PersonInputSection(
    personName: String,
    onPersonNameChanged: (String) -> Unit,
    showSuggestions: Boolean,
    onShowSuggestionsChanged: (Boolean) -> Unit,
    suggestions: List<LedgerPerson>,
    validationErrors: Map<String, String>
) {
    Column {
        Text(
            text = "Person's Name",
            fontSize = AppStyleDesignSystem.Typography.HEADLINE.fontSize,
            fontWeight = FontWeight.Medium,
            color = LedgerTheme.textPrimary()
        )

        Spacer(modifier = Modifier.height(AppStyleDesignSystem.Padding.ARRANGEMENT_TINY))

        Column {
            TextField(
                value = personName,
                onValueChange = { newValue ->
                    // Limit to 20 characters
                    if (newValue.length <= 20) {
                        onPersonNameChanged(newValue)
                        onShowSuggestionsChanged(newValue.isNotBlank() && suggestions.isNotEmpty())
                    }
                },
                placeholder = {
                    Text(
                        text = "Enter name",
                        color = LedgerTheme.textSecondary()
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = LedgerTheme.textSecondary()
                    )
                },
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedTextColor = LedgerTheme.textPrimary(),
                    focusedTextColor = LedgerTheme.textPrimary(),
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(AppStyleDesignSystem.Padding.ARRANGEMENT_XL),
                modifier = Modifier.fillMaxWidth()
            )
            
            // Show suggestions dropdown
            if (showSuggestions && suggestions.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = AppStyleDesignSystem.Padding.ARRANGEMENT_TINY)
                        .clip(RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM))
                        .border(
                            width = AppStyleDesignSystem.Sizes.BORDER_THIN, // very thin border
                            color = Color.White.copy(alpha = 0.2f), // subtle white
                            shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM)
                ) {
                    Column {
                        suggestions.take(5).forEach { suggestion: LedgerPerson ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onPersonNameChanged(suggestion.name)
                                        onShowSuggestionsChanged(false)
                                    }
                                    .padding(AppStyleDesignSystem.Padding.ARRANGEMENT_MEDIUM),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = LedgerTheme.textSecondary(),
                                    modifier = Modifier.size(AppStyleDesignSystem.Padding.ARRANGEMENT_XL)
                                )
                                Spacer(modifier = Modifier.width(AppStyleDesignSystem.Padding.ARRANGEMENT_SMALL))
                                Text(
                                    text = suggestion.name,
                                    color = LedgerTheme.textPrimary(),
                                    fontSize = AppStyleDesignSystem.Typography.CALL_OUT.fontSize
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Show error message for person name
        validationErrors["personName"]?.let { error ->
            Text(
                text = error,
                color = LedgerTheme.redAmount,
                fontSize = AppStyleDesignSystem.Typography.FOOTNOTE.fontSize,
                modifier = Modifier.padding(top = AppStyleDesignSystem.Padding.ARRANGEMENT_TINY, start = AppStyleDesignSystem.Padding.MEDIUM_LARGE)
            )
        }
    }
}
