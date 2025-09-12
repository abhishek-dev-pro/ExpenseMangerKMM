package com.example.androidkmm.screens.ledger

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.androidkmm.utils.formatDouble

@Composable
fun PersonLedgerItem(
    person: LedgerPerson,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A1A)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
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
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Person Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = person.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = LedgerTheme.textPrimary
                )
                Text(
                    text = "${person.lastTransactionDate} • ${person.transactionCount} transaction${if (person.transactionCount > 1) "s" else ""}",
                    fontSize = 14.sp,
                    color = LedgerTheme.textSecondary
                )
            }

            // Amount and Arrow
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = if (person.balance > 0) "$${formatDouble(person.balance)}" else "$${formatDouble(-person.balance)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (person.balance > 0) LedgerTheme.greenAmount else LedgerTheme.redAmount
                )
                Text(
                    text = if (person.balance > 0) "owes you" else "you owe",
                    fontSize = 12.sp,
                    color = LedgerTheme.textSecondary
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View details",
                tint = LedgerTheme.textSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
