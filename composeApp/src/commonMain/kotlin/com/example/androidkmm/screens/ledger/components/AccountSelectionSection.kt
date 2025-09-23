package com.example.androidkmm.screens.ledger.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.example.androidkmm.design.AppStyleDesignSystem
import com.example.androidkmm.models.Account
import com.example.androidkmm.screens.ledger.LedgerTheme

@Composable
fun AccountSelectionSection(
    selectedAccount: Account?,
    onAccountSelectionClick: () -> Unit,
    validationErrors: Map<String, String>
) {
    Column {
        Text(
            text = "Account",
            fontSize = AppStyleDesignSystem.Typography.HEADLINE.fontSize,
            fontWeight = FontWeight.Medium,
            color = LedgerTheme.textPrimary()
        )

        Spacer(modifier = Modifier.height(AppStyleDesignSystem.Padding.ARRANGEMENT_SMALL))

        Column {
            Button(
                onClick = onAccountSelectionClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1F1F1F)
                ),
                shape = RoundedCornerShape(AppStyleDesignSystem.Padding.ARRANGEMENT_XL),
                contentPadding = PaddingValues(AppStyleDesignSystem.Padding.MEDIUM_LARGE)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Wallet,
                        contentDescription = null,
                        tint = LedgerTheme.textPrimary(),
                        modifier = Modifier.size(AppStyleDesignSystem.Padding.MEDIUM_LARGE)
                    )
                    Spacer(modifier = Modifier.width(AppStyleDesignSystem.Padding.ARRANGEMENT_SMALL))
                    Box(
                        modifier = Modifier
                            .size(AppStyleDesignSystem.Padding.ARRANGEMENT_SMALL)
                            .background(LedgerTheme.greenAmount, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(AppStyleDesignSystem.Padding.ARRANGEMENT_SMALL))
                    Text(
                        text = selectedAccount?.name ?: "Select account",
                        color = LedgerTheme.textPrimary(),
                        fontSize = AppStyleDesignSystem.Typography.CALL_OUT.fontSize,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Start
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = LedgerTheme.textSecondary(),
                        modifier = Modifier.size(AppStyleDesignSystem.Padding.MEDIUM_LARGE)
                    )
                }
            }
            
            // Show error message
            validationErrors["account"]?.let { error ->
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
