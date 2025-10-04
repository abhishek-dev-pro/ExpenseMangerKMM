package com.example.androidkmm.screens.ledger

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TransactionItem(
    transaction: LedgerTransaction,
    balanceAtTransaction: Double,
    modifier: Modifier = Modifier,
    onDelete: () -> Unit = {},
    onEdit: () -> Unit = {},
    isHighlighted: Boolean = false
) {
    val isSent = transaction.type == TransactionType.SENT
    val amountColor = if (isSent) Color(0xFFFF1744) else Color(0xFF00C853)
    val bgColor = Color(0xFF1A1A1A)

    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent // No background
        ),    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left colored square with amount
            if (transaction.type == TransactionType.RECEIVED) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(amountColor.copy(alpha = 0.15f))
                        .border(
                            width = 1.dp,                // thin border
                            color = Color.LightGray.copy(alpha = 0.15f),     // light color border
                            shape = RoundedCornerShape(4.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (transaction.type == TransactionType.RECEIVED) {
                        Text(
                            text = "${transaction.amount.toInt()}",
                            color = amountColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                )
            }
            Spacer(Modifier.width(4.dp))


            // Middle content - FIXED LAYOUT
            Column(
                modifier = Modifier
                    .size(72.dp)
                    .weight(1f)
                    .clip(RoundedCornerShape(4.dp)) // rounded corners
                    .background(Color.Black)         // black background
                    .border(
                        width = 1.dp,                // thin border
                        color = Color.LightGray.copy(alpha = 0.15f),     // light color border
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                // First row: Description and Balance
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Description with limited width
                    Text(
                        text = "${transaction.description} - ${if (isSent) "Sent" else "Received"}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Balance with fixed width
                    Text(
                        text = "Bal: ${balanceAtTransaction.toInt()}",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        maxLines = 1
                    )
                }


                // Second row: Date and Account
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Date/Time
                    Text(
                        text = "${transaction.date} | ${transaction.time}",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Account
                    Text(
                        text = transaction.account ?: "Cash",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        maxLines = 1
                    )
                }
            }
            Spacer(Modifier.width(4.dp))


            // Right colored square with amount
            if (transaction.type == TransactionType.SENT) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(amountColor.copy(alpha = 0.15f))
                    .border(
                        width = 1.dp,                // thin border
                        color = Color.LightGray.copy(alpha = 0.15f),     // light color border
                        shape = RoundedCornerShape(4.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (transaction.type == TransactionType.SENT) {

                    Text(
                        text = "${transaction.amount.toInt()}",
                        color = amountColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }else {
        Box(
            modifier = Modifier
                .size(72.dp))
    }
        }
    }
}

