package com.example.androidkmm.screens.ledger

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidkmm.utils.CurrencyUtils.formatDouble
import com.example.androidkmm.design.AppStyleDesignSystem
import com.example.androidkmm.database.rememberSQLiteSettingsDatabase
import com.example.androidkmm.models.AppSettings
import androidx.compose.runtime.collectAsState

@Composable
fun PersonLedgerItem(
    person: LedgerPerson,
    onClick: () -> Unit
) {
    // Get currency symbol from settings
    val settingsDatabaseManager = rememberSQLiteSettingsDatabase()
    val appSettings = settingsDatabaseManager.getAppSettings().collectAsState(initial = AppSettings())
    val currencySymbol = appSettings.value.currencySymbol
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .clip(RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM))
            .border(
                width = 0.5.dp, // very thin border
                color = Color.White.copy(alpha = 0.2f), // subtle white
                shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A1A)
        ),
        shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppStyleDesignSystem.Padding.MEDIUM_LARGE),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(AppStyleDesignSystem.Sizes.AVATAR_LARGE)
                    .background(
                        person.avatarColor,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (person.name.isNotBlank()) {
                        person.name.split(" ").mapNotNull { if (it.isNotBlank()) it.first() else null }.joinToString("")
                    } else {
                        "?"
                    },
                    color = Color.White,
                    style = AppStyleDesignSystem.Typography.BODY.copy(
                        fontWeight = AppStyleDesignSystem.iOSFontWeights.medium
                    )
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Person Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = person.name,
                    style = AppStyleDesignSystem.Typography.BODY.copy(
                        fontWeight = AppStyleDesignSystem.iOSFontWeights.medium
                    ),
                    color = LedgerTheme.textPrimary()
                )
                Text(
                    text = "${person.lastTransactionDate} • ${person.transactionCount} entr${if (person.transactionCount > 1) "ies" else "y"}",
                    style = AppStyleDesignSystem.Typography.CALL_OUT,
                    color = LedgerTheme.textSecondary()
                )
            }

            // Amount and Arrow
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = when {
                        person.balance == 0.0 -> "$currencySymbol${formatDouble(0.0)}"
                        person.balance > 0 -> "$currencySymbol${formatDouble(person.balance)}"
                        else -> "$currencySymbol${formatDouble(-person.balance)}"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = when {
                        person.balance == 0.0 -> Color(0xFF2196F3) // Blue for settled up
                        person.balance > 0 -> LedgerTheme.greenAmount
                        else -> LedgerTheme.redAmount
                    }
                )
                Text(
                    text = when {
                        person.balance == 0.0 -> "settled up"
                        person.balance > 0 -> "you will give"
                        else -> "you will get"
                    },
                    style = AppStyleDesignSystem.Typography.CAPTION_1,
                    color = LedgerTheme.textSecondary()
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View details",
                tint = LedgerTheme.textSecondary(),
                modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_MEDIUM)
            )
        }
    }
}
