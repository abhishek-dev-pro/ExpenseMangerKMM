package com.example.androidkmm.transactions

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.LocalDate
import kotlin.math.pow

// ✅ Money formatting extension (from commonMain)
fun Double.toMoney(digits: Int = 2): String {
    val factor = 10.0.pow(digits)
    return (kotlin.math.round(this * factor) / factor).toString()
}

// ✅ Data class for transaction
data class Transaction(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: String,
    val amount: Double,
    val date: LocalDate,
    val method: String,
    val color: Color,
    val categoryBg: Color,
    val icon: String
)

@Composable
fun TransactionsScreen() {
    // Dummy transaction data
    val transactions: Map<String, List<Transaction>> = mapOf(
        "Today, Sep 8" to listOf(
            Transaction(
                id = "1",
                title = "Lunch at Subway",
                subtitle = "5:12 AM",
                category = "Food & Dining",
                amount = -24.50,
                date = LocalDate(2025, 9, 8),
                method = "Credit Card",
                color = Color(0xFFFF6F00),
                categoryBg = Color(0xFF2C2C2E),
                icon = "🍴"
            )
        ),
        "Yesterday, Sep 7" to listOf(
            Transaction(
                id = "2",
                title = "Monthly salary",
                subtitle = "5:12 AM",
                category = "Salary",
                amount = 2500.00,
                date = LocalDate(2025, 9, 7),
                method = "Bank Transfer",
                color = Color(0xFF00C853),
                categoryBg = Color(0xFF2C2C2E),
                icon = "💵"
            ),
            Transaction(
                id = "3",
                title = "Snacks",
                subtitle = "8:20 PM",
                category = "Food",
                amount = -10.0,
                date = LocalDate(2025, 9, 7),
                method = "Cash",
                color = Color(0xFFFF9800),
                categoryBg = Color(0xFF2C2C2E),
                icon = "🍪"
            )
        ),
        "Saturday, Sep 6" to listOf(
            Transaction(
                id = "4",
                title = "New headphones",
                subtitle = "5:12 AM",
                category = "Shopping",
                amount = -89.99,
                date = LocalDate(2025, 9, 6),
                method = "Debit Card",
                color = Color(0xFF9C27B0),
                categoryBg = Color(0xFF2C2C2E),
                icon = "🛒"
            )
        ),
        "Friday, Sep 5" to listOf(
            Transaction(
                id = "5",
                title = "Uber ride",
                subtitle = "5:12 AM",
                category = "Transportation",
                amount = -15.30,
                date = LocalDate(2025, 9, 5),
                method = "UPI",
                color = Color(0xFF1976D2),
                categoryBg = Color(0xFF2C2C2E),
                icon = "🚗"
            )
        )
    )

    val netAmount = transactions.values.flatten().sumOf { it.amount }
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        // Header
        TransactionsHeader(
            transactionCount = transactions.values.flatten().size,
            netAmount = netAmount,
            onCloseClick = { /* handle close */ }
        )
        Spacer(Modifier.height(16.dp))

        TransactionSearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            onFilterClick = { /* Open filter dialog */ }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Transactions list
        LazyColumn(
            verticalArrangement = Arrangement.Top
        ) {
            transactions.forEach { (sectionTitle, list) ->
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    TransactionSectionHeader(sectionTitle, list)
                    Spacer(modifier = Modifier.height(10.dp))
                }
                items(list.size) { index ->
                    val tx = list[index]

                    var visible by remember { mutableStateOf(false) }

                    // ✅ Staggered animation
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(index * 100L) // stagger by index
                        visible = true
                    }

                    AnimatedVisibility(
                        visible = visible,
                        enter = slideInVertically(
                            initialOffsetY = { it } // slide from bottom
                        ) + fadeIn()
                    ) {
                        Column {
                            TransactionItem(tx)
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
            }
        }

    }
}

@Composable
fun TransactionSectionHeader(title: String, list: List<Transaction>) {
    val totalAmount = list.sumOf { it.amount }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title.substringBefore(","),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                style = TextStyle(
                    lineHeight = 36.sp // 👈 match line height for tighter spacing
                )
            )
            Text(
                text = title.substringAfter(",").trim(),
                color = Color.Gray,
                fontSize = 13.sp,
                style = TextStyle(
                    lineHeight = 36.sp // 👈 match line height for tighter spacing
                )
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${list.size} transaction${if (list.size > 1) "s" else ""}",
                color = Color.Gray,
                fontSize = 13.sp,
                style = TextStyle(
                    lineHeight = 36.sp // 👈 match line height for tighter spacing
                )
            )
            Text(
                text = if (totalAmount >= 0) "$${totalAmount.toMoney()}"
                else "-$${(-totalAmount).toMoney()}",
                color = Color.Gray,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                style = TextStyle(
                    lineHeight = 36.sp // 👈 match line height for tighter spacing
                )
            )
        }
    }
}

@Composable
fun TransactionSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Search Box with BasicTextField
        Box(
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .background(Color(0xFF1C1C1C), shape = RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        color = Color.White,
                        fontSize = 16.sp
                    ),
                    cursorBrush = SolidColor(Color.White),
                    decorationBox = { innerTextField ->
                        if (query.isEmpty()) {
                            Text(
                                text = "Search transactions...",
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                        }
                        innerTextField()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Filter Button
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1C1C1C))
                .clickable { onFilterClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = "Filter",
                tint = Color.White
            )
        }
    }
}

@Composable
fun TransactionsHeader(
    transactionCount: Int,
    netAmount: Double,
    onCloseClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Transactions",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$transactionCount transactions • ${if (netAmount >= 0) "+" else ""}$${netAmount.toMoney()} net",
                color = if (netAmount >= 0) Color(0xFF00C853) else Color.Red,
                fontSize = 14.sp
            )
        }
        // Close (X) icon button
        Box(
            modifier = Modifier
                .size(32.dp) // circle size
                .background(Color.White, shape = CircleShape)
                .clickable { },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add group",
                tint = Color.Black
            )
        }
    }
}

@Composable
fun TransactionItem(tx: Transaction) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left side: icon + details
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(tx.color),
                contentAlignment = Alignment.Center
            ) {
                Text(tx.icon, fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = tx.title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    style = TextStyle(
                        lineHeight = 36.sp // 👈 match line height for tighter spacing
                    )
                )
                Text(
                    text = "${tx.subtitle} • ${tx.method}",
                    color = Color.Gray,
                    fontSize = 13.sp,
                    style = TextStyle(
                        lineHeight = 36.sp // 👈 match line height for tighter spacing
                    )
                )
            }
        }
        // Right side: category + amount
        Column(horizontalAlignment = Alignment.End) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(tx.categoryBg)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = tx.category,
                    fontSize = 12.sp,
                    color = Color.White
                )
            }
            Text(
                text = if (tx.amount >= 0) "+$${tx.amount.toMoney()}"
                else "-$${(-tx.amount).toMoney()}",
                color = if (tx.amount >= 0) Color(0xFF00C853) else Color.Red,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                
            )
        }
    }
}
