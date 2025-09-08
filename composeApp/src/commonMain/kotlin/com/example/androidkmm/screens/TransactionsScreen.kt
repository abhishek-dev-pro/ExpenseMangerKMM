package com.example.androidkmm.transactions

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.androidkmm.screens.AddTransactionSheet
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.pow
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

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

@OptIn(ExperimentalMaterial3Api::class)
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

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

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
            onAddClick = { showSheet = true }
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

        if (showSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSheet = false },
                sheetState = sheetState,
                containerColor = Color.Black,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            ) {
                AddTransactionSheet(onClose = { showSheet = false })
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

@Composable
fun InputRow(title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1C1C1C))
            .padding(horizontal = 12.dp, vertical = 14.dp)
    ) {
        Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(2.dp))
        Text(subtitle, color = Color.Gray, fontSize = 13.sp)
    }
}

@Composable
fun SelectCategoryOrPaymentBox(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color = Color.Gray,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black)
            .drawBehind {
                val strokeWidth = 1.5.dp.toPx()
                val dashWidth = 12.dp.toPx()
                val dashGap = 8.dp.toPx()

                drawRoundRect(
                    color = Color.Gray,
                    style = Stroke(
                        width = strokeWidth,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashWidth, dashGap))
                    ),
                    cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
                )
            }
            .clickable(onClick = onClick) // 👈 clickable row
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Icon circle background
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(iconColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon, // 👈 passed from caller
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column {
                Text(
                    title,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    subtitle,
                    color = Color.Gray,
                    fontSize = 13.sp
                )
            }
        }

        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.Gray
        )
    }
}






/* ---------------- Helper Composables ---------------- */










@Composable
fun UploadReceiptCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(16.dp))
            .drawBehind {
                val strokeWidth = 1.dp.toPx()
                val dashWidth = 10.dp.toPx()
                val dashGap = 6.dp.toPx()

                drawRoundRect(
                    color = Color.Gray,
                    style = Stroke(
                        width = strokeWidth,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashWidth, dashGap))
                    ),
                    cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
                )
            }
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Attachment,
                contentDescription = "Upload",
                tint = Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Upload Receipt",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}



@OptIn(ExperimentalTime::class)
@Composable
fun TransactionDateTimePicker() {
    val currentDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

    var selectedDay by remember { mutableStateOf(currentDateTime.dayOfMonth) }
    var selectedMonth by remember { mutableStateOf(currentDateTime.monthNumber) }
    var selectedYear by remember { mutableStateOf(currentDateTime.year) }

    var selectedHour by remember { mutableStateOf(if (currentDateTime.hour % 12 == 0) 12 else currentDateTime.hour % 12) }
    var selectedMinute by remember { mutableStateOf(currentDateTime.minute) }
    var selectedAmPm by remember { mutableStateOf(if (currentDateTime.hour < 12) "AM" else "PM") }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val monthNames = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )

    fun formatDate(): String = "${selectedDay.toString().padStart(2,'0')} ${monthNames[selectedMonth-1]} $selectedYear"
    fun formatTime(): String = "${selectedHour.toString().padStart(2,'0')}:${selectedMinute.toString().padStart(2,'0')} $selectedAmPm"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1C1C1C))
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF2C2C2E))
                .clickable { showDatePicker = true }
                .padding(vertical = 12.dp, horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DateRange, contentDescription = "Date", tint = Color.White, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(text = formatDate(), color = Color.White, fontSize = 14.sp)
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF2C2C2E))
                .clickable { showTimePicker = true }
                .padding(vertical = 12.dp, horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccessTime, contentDescription = "Time", tint = Color.White, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(text = formatTime(), color = Color.White, fontSize = 14.sp)
            }
        }
    }

    // --- Date Picker ---
    if (showDatePicker) {
        Dialog(onDismissRequest = { showDatePicker = false }) {
            Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFF1C1C1C)) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Select Date", color = Color.White, fontSize = 18.sp)
                    Spacer(Modifier.height(16.dp))

                    // LazyListStates
                    val dayState = rememberLazyListState(initialFirstVisibleItemIndex = selectedDay - 1)
                    val monthState = rememberLazyListState(initialFirstVisibleItemIndex = selectedMonth - 1)
                    val years = (currentDateTime.year-50..currentDateTime.year+50).toList()
                    val yearState = rememberLazyListState(initialFirstVisibleItemIndex = years.indexOf(selectedYear))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        LazyColumn(state = dayState, modifier = Modifier.height(150.dp).width(60.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            items((1..31).toList()) { day ->
                                Text(
                                    text = day.toString(),
                                    color = if(day==selectedDay) Color.Yellow else Color.White,
                                    fontSize = if(day==selectedDay) 20.sp else 16.sp,
                                    modifier = Modifier.padding(vertical = 4.dp).clickable { selectedDay = day }
                                )
                            }
                        }

                        LazyColumn(state = monthState, modifier = Modifier.height(150.dp).width(100.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            items(monthNames) { month ->
                                val monthIndex = monthNames.indexOf(month)+1
                                Text(
                                    text = month,
                                    color = if(monthIndex==selectedMonth) Color.Yellow else Color.White,
                                    fontSize = if(monthIndex==selectedMonth) 20.sp else 16.sp,
                                    modifier = Modifier.padding(vertical = 4.dp).clickable { selectedMonth = monthIndex }
                                )
                            }
                        }

                        LazyColumn(state = yearState, modifier = Modifier.height(150.dp).width(80.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            items(years) { year ->
                                Text(
                                    text = year.toString(),
                                    color = if(year==selectedYear) Color.Yellow else Color.White,
                                    fontSize = if(year==selectedYear) 20.sp else 16.sp,
                                    modifier = Modifier.padding(vertical = 4.dp).clickable { selectedYear = year }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { showDatePicker = false }) { Text("Done") }
                }
            }
        }
    }

    // --- Time Picker ---
    if (showTimePicker) {
        Dialog(onDismissRequest = { showTimePicker = false }) {
            Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFF1C1C1C)) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Select Time", color = Color.White, fontSize = 18.sp)
                    Spacer(Modifier.height(16.dp))

                    val hourState = rememberLazyListState(initialFirstVisibleItemIndex = selectedHour - 1)
                    val minuteState = rememberLazyListState(initialFirstVisibleItemIndex = selectedMinute)
                    val amPmOptions = listOf("AM","PM")
                    val amPmState = rememberLazyListState(initialFirstVisibleItemIndex = amPmOptions.indexOf(selectedAmPm))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        LazyColumn(state = hourState, modifier = Modifier.height(150.dp).width(60.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            items((1..12).toList()) { hour ->
                                Text(
                                    text = hour.toString(),
                                    color = if(hour==selectedHour) Color.Yellow else Color.White,
                                    fontSize = if(hour==selectedHour) 20.sp else 16.sp,
                                    modifier = Modifier.padding(vertical = 4.dp).clickable { selectedHour = hour }
                                )
                            }
                        }

                        LazyColumn(state = minuteState, modifier = Modifier.height(150.dp).width(60.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            items((0..59).toList()) { minute ->
                                Text(
                                    text = minute.toString().padStart(2,'0'),
                                    color = if(minute==selectedMinute) Color.Yellow else Color.White,
                                    fontSize = if(minute==selectedMinute) 20.sp else 16.sp,
                                    modifier = Modifier.padding(vertical = 2.dp).clickable { selectedMinute = minute }
                                )
                            }
                        }

                        LazyColumn(state = amPmState, modifier = Modifier.height(150.dp).width(60.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            items(amPmOptions) { ap ->
                                Text(
                                    text = ap,
                                    color = if(ap==selectedAmPm) Color.Yellow else Color.White,
                                    fontSize = if(ap==selectedAmPm) 20.sp else 16.sp,
                                    modifier = Modifier.padding(vertical = 4.dp).clickable { selectedAmPm = ap }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { showTimePicker = false }) { Text("Done") }
                }
            }
        }
    }
}




@Composable
fun TransactionsHeader(
    transactionCount: Int,
    netAmount: Double,
    onAddClick: () -> Unit
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
        // Add button
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(Color.White, shape = CircleShape)
                .clickable { onAddClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add transaction",
                tint = Color.Black
            )
        }
    }
}



