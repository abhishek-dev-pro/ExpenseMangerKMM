package com.example.androidkmm.screens.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidkmm.models.Goal
import com.example.androidkmm.models.GoalIcon
import com.example.androidkmm.models.GoalPriority
import com.example.androidkmm.utils.DateFormatUtils
import com.example.androidkmm.components.BeautifulDateSelector
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.toJavaLocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGoalScreen(
    onClose: () -> Unit,
    onGoalCreated: (Goal) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var targetAmount by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf<LocalDate?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var isRecurring by remember { mutableStateOf(false) }
    var monthlyAmount by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf(GoalIcon.HOUSE) }
    var selectedPriority by remember { mutableStateOf(GoalPriority.HIGH) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1A))
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Create Goal",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Set your financial target and track progress",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
            
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.Transparent, CircleShape)
            ) {
                Text(
                    text = "✕",
                    color = Color.White,
                    fontSize = 20.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Target Amount
        Text(
            text = "Target Amount",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        BasicTextField(
            value = targetAmount,
            onValueChange = { newValue ->
                val filtered = newValue.filter { it.isDigit() || it == '.' }
                val parts = filtered.split('.')
                if (parts.size <= 2 && (parts.getOrNull(1)?.length ?: 0) <= 2) {
                    targetAmount = filtered
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color(0xFF2A2A2A), RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp),
            textStyle = androidx.compose.ui.text.TextStyle(
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            cursorBrush = Brush.linearGradient(
                colors = listOf(Color.White, Color.White)
            ),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (targetAmount.isEmpty()) {
                        Text(
                            text = "50000",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 18.sp
                        )
                    }
                    innerTextField()
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Title
        Text(
            text = "Goal Title",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        BasicTextField(
            value = title,
            onValueChange = { title = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color(0xFF2A2A2A), RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp),
            textStyle = androidx.compose.ui.text.TextStyle(
                color = Color.White,
                fontSize = 16.sp
            ),
            cursorBrush = Brush.linearGradient(
                colors = listOf(Color.White, Color.White)
            ),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (title.isEmpty()) {
                        Text(
                            text = "e.g., Buy a new laptop",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 16.sp
                        )
                    }
                    innerTextField()
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Description (Optional)
        Text(
            text = "Description (Optional)",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        BasicTextField(
            value = description,
            onValueChange = { description = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(Color(0xFF2A2A2A), RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            textStyle = androidx.compose.ui.text.TextStyle(
                color = Color.White,
                fontSize = 16.sp
            ),
            cursorBrush = Brush.linearGradient(
                colors = listOf(Color.White, Color.White)
            ),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopStart
                ) {
                    if (description.isEmpty()) {
                        Text(
                            text = "Add a description for your goal...",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 16.sp
                        )
                    }
                    innerTextField()
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Deadline (Optional)
        Text(
            text = "Deadline (Optional)",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color.White, RoundedCornerShape(12.dp))
                .clickable { showDatePicker = true }
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📅",
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = deadline?.let { 
                    it.toJavaLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                } ?: "dd/mm/yyyy",
                color = if (deadline != null) Color.Black else Color.Gray,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "📅",
                fontSize = 20.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Recurring Goal
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Recurring Goal",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    Text(
                        text = "Set monthly savings target",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
                
                Switch(
                    checked = isRecurring,
                    onCheckedChange = { isRecurring = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF4CAF50),
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFF2A2A2A)
                    )
                )
            }
            
            // Monthly Amount Input (only show when recurring is enabled)
            if (isRecurring) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Monthly amount",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                BasicTextField(
                    value = monthlyAmount,
                    onValueChange = { newValue ->
                        val filtered = newValue.filter { it.isDigit() || it == '.' }
                        val parts = filtered.split('.')
                        if (parts.size <= 2 && (parts.getOrNull(1)?.length ?: 0) <= 2) {
                            monthlyAmount = filtered
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(Color(0xFF2A2A2A), RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = Color.White,
                        fontSize = 16.sp
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    cursorBrush = Brush.linearGradient(
                        colors = listOf(Color.White, Color.White)
                    ),
                    decorationBox = { innerTextField ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = "₹",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(modifier = Modifier.weight(1f)) {
                                if (monthlyAmount.isEmpty()) {
                                    Text(
                                        text = "Monthly amount",
                                        color = Color.White.copy(alpha = 0.5f),
                                        fontSize = 16.sp
                                    )
                                }
                                innerTextField()
                            }
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Icon Selection
        Text(
            text = "Icon",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(6),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.height(120.dp)
        ) {
            items(GoalIcon.values().take(12)) { icon ->
                IconSelectionItem(
                    icon = icon,
                    isSelected = selectedIcon == icon,
                    onClick = { selectedIcon = icon }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Priority Selection
        Text(
            text = "Priority",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GoalPriority.values().forEach { priority ->
                PrioritySelectionItem(
                    priority = priority,
                    isSelected = selectedPriority == priority,
                    onClick = { selectedPriority = priority },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Create Goal Button
        Button(
            onClick = {
                if (title.isNotEmpty() && targetAmount.isNotEmpty()) {
                    val goal = Goal(
                        title = title,
                        description = description.ifEmpty { null },
                        targetAmount = targetAmount.toDoubleOrNull() ?: 0.0,
                        deadline = deadline,
                        isRecurring = isRecurring,
                        monthlyAmount = if (isRecurring && monthlyAmount.isNotEmpty()) monthlyAmount.toDoubleOrNull() else null,
                        icon = selectedIcon.iconName,
                        color = "blue", // Default color
                        priority = selectedPriority.priorityName
                    )
                    onGoalCreated(goal)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = title.isNotEmpty() && targetAmount.isNotEmpty()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "✓",
                    color = Color.Black,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Create Goal",
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
    
    // Simple Date Picker Dialog
    if (showDatePicker) {
        SimpleDatePicker(
            selectedDate = deadline,
            onDateSelected = { selectedDate ->
                deadline = selectedDate
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

@Composable
fun IconSelectionItem(
    icon: GoalIcon,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(
                if (isSelected) Color.White else Color.Transparent,
                CircleShape
            )
            .border(
                width = 1.dp,
                color = if (isSelected) Color.Gray else Color.Gray.copy(alpha = 0.3f),
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = getIconEmoji(icon),
            fontSize = 20.sp
        )
    }
}


@Composable
fun PrioritySelectionItem(
    priority: GoalPriority,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                Color(0xFF2A2A2A),
                RoundedCornerShape(12.dp)
            )
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = Color.White,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Colored dot
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    Color(android.graphics.Color.parseColor(priority.dotColor)),
                    CircleShape
                )
        )
        
        Spacer(modifier = Modifier.height(6.dp))
        
        Text(
            text = priority.displayName,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun getIconEmoji(icon: GoalIcon): String {
    return when (icon) {
        GoalIcon.TARGET -> "🎯"
        GoalIcon.DOLLAR -> "💰"
        GoalIcon.EXCLAMATION -> "⚠️"
        GoalIcon.GAME_CONTROLLER -> "🎮"
        GoalIcon.SHOPPING_CART -> "🛒"
        GoalIcon.CAR -> "🚗"
        GoalIcon.HOUSE -> "🏠"
        GoalIcon.PAPER_AIRPLANE -> "✈️"
        GoalIcon.HEART -> "❤️"
        GoalIcon.GRADUATION_CAP -> "🎓"
        GoalIcon.MOBILE_PHONE -> "📱"
        GoalIcon.COFFEE_CUP -> "☕"
        GoalIcon.GIFT_BOX -> "🎁"
    }
}

@Composable
fun SimpleDatePicker(
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    var currentMonth by remember { mutableStateOf(12) }
    var currentYear by remember { mutableStateOf(2024) }
    val today = LocalDate(2024, 12, 17) // Current date for reference
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clickable { /* Prevent click through */ },
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select Deadline",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Month Navigation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (currentMonth > 1) {
                                currentMonth--
                            } else {
                                currentMonth = 12
                                currentYear--
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.KeyboardArrowLeft,
                            contentDescription = "Previous Month",
                            tint = Color.White
                        )
                    }
                    
                    Text(
                        text = "${getMonthName(currentMonth)} $currentYear",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    
                    IconButton(
                        onClick = {
                            if (currentMonth < 12) {
                                currentMonth++
                            } else {
                                currentMonth = 1
                                currentYear++
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.KeyboardArrowRight,
                            contentDescription = "Next Month",
                            tint = Color.White
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Days of week
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                        Text(
                            text = day,
                            color = Color.Gray,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Calendar Grid - Simple approach
                val daysInMonth = getDaysInMonth(currentMonth, currentYear)
                val firstDayOfWeek = getFirstDayOfWeek(currentMonth, currentYear)
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.height(200.dp)
                ) {
                    // Empty cells for days before the first day of the month
                    items(firstDayOfWeek) {
                        Box(modifier = Modifier.height(40.dp))
                    }
                    
                    // Days of the month
                    items(daysInMonth) { day ->
                        val date = LocalDate(currentYear, currentMonth, day + 1)
                        val isToday = date == today
                        val isPast = date < today
                        val isSelected = selectedDate == date
                        
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    when {
                                        isSelected -> Color(0xFF6A4C93)
                                        isToday -> Color(0xFF4CAF50)
                                        else -> Color.Transparent
                                    },
                                    CircleShape
                                )
                                .clickable(enabled = !isPast) {
                                    if (!isPast) {
                                        onDateSelected(date)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${day + 1}",
                                color = when {
                                    isSelected -> Color.White
                                    isToday -> Color.White
                                    isPast -> Color.Gray
                                    else -> Color.White
                                },
                                fontSize = 14.sp,
                                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Selected date info
                if (selectedDate != null) {
                    Text(
                        text = "Selected: ${selectedDate.dayOfMonth}/${selectedDate.monthNumber}/${selectedDate.year}",
                        color = Color(0xFF6A4C93),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

private fun getMonthName(month: Int): String {
    return when (month) {
        1 -> "January"
        2 -> "February"
        3 -> "March"
        4 -> "April"
        5 -> "May"
        6 -> "June"
        7 -> "July"
        8 -> "August"
        9 -> "September"
        10 -> "October"
        11 -> "November"
        12 -> "December"
        else -> "Unknown"
    }
}

private fun getDaysInMonth(month: Int, year: Int): Int {
    return when (month) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
        else -> 30
    }
}

private fun getFirstDayOfWeek(month: Int, year: Int): Int {
    // Simple calculation - this is a basic implementation
    // For a more accurate implementation, you'd use proper date libraries
    return 0 // Start from Sunday for simplicity
}
