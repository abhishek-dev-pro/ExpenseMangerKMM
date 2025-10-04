package com.example.androidkmm.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidkmm.utils.DateTimeUtils
import kotlinx.datetime.*
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun BeautifulDateSelector(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    maxDate: LocalDate = DateTimeUtils.getCurrentDate(),
    modifier: Modifier = Modifier
) {
    val today = DateTimeUtils.getCurrentDate()
    var selectedTab by remember { mutableStateOf(DateSelectorTab.QUICK) }
    
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Tab Selector
        DateSelectorTabs(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Content based on selected tab
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                slideInHorizontally(
                    initialOffsetX = { if (targetState.ordinal > initialState.ordinal) 300 else -300 }
                ) + fadeIn() togetherWith
                slideOutHorizontally(
                    targetOffsetX = { if (targetState.ordinal > initialState.ordinal) -300 else 300 }
                ) + fadeOut()
            },
            label = "date_selector_content"
        ) { tab ->
            when (tab) {
                DateSelectorTab.QUICK -> {
                    QuickDateSelector(
                        selectedDate = selectedDate,
                        onDateSelected = onDateSelected,
                        maxDate = maxDate
                    )
                }
                DateSelectorTab.CALENDAR -> {
                    CalendarDateSelector(
                        selectedDate = selectedDate,
                        onDateSelected = onDateSelected,
                        maxDate = maxDate
                    )
                }
            }
        }
    }
}

@Composable
private fun DateSelectorTabs(
    selectedTab: DateSelectorTab,
    onTabSelected: (DateSelectorTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(4.dp)
    ) {
        DateSelectorTab.values().forEach { tab ->
            val isSelected = selectedTab == tab
            val animatedScale by animateFloatAsState(
                targetValue = if (isSelected) 1.05f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "tab_scale"
            )
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .scale(animatedScale)
                    .clickable { onTabSelected(tab) }
                    .background(
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            Color.Transparent
                        },
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = null,
                        tint = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = tab.title,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickDateSelector(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    maxDate: LocalDate
) {
    val today = DateTimeUtils.getCurrentDate()
    
    // Generate quick date options
    val quickOptions = remember {
        val options = mutableListOf<QuickDateOption>()
        
        // Today
        options.add(QuickDateOption("Today", today, Icons.Default.Today, Color(0xFF4CAF50)))
        
        // Yesterday
        val yesterday = DateTimeUtils.addDays(today, -1)
        if (!DateTimeUtils.isDateAfter(yesterday, maxDate)) {
            options.add(QuickDateOption("Yesterday", yesterday, Icons.Default.Schedule, Color(0xFF2196F3)))
        }
        
        // This week
        for (i in 2..7) {
            val date = DateTimeUtils.addDays(today, -i)
            if (!DateTimeUtils.isDateAfter(date, maxDate)) {
                val (label, icon) = when (i) {
                    2 -> "2 days ago" to Icons.Default.Schedule
                    3 -> "3 days ago" to Icons.Default.Schedule
                    4 -> "4 days ago" to Icons.Default.Schedule
                    5 -> "5 days ago" to Icons.Default.Schedule
                    6 -> "6 days ago" to Icons.Default.Schedule
                    7 -> "1 week ago" to Icons.Default.DateRange
                    else -> "${i} days ago" to Icons.Default.Schedule
                }
                options.add(QuickDateOption(label, date, icon, Color(0xFF9C27B0)))
            }
        }
        
        // This month
        for (i in 8..30) {
            val date = DateTimeUtils.addDays(today, -i)
            if (!DateTimeUtils.isDateAfter(date, maxDate)) {
                val (label, icon) = when (i) {
                    8, 9, 10, 11, 12, 13, 14 -> "${i} days ago" to Icons.Default.Schedule
                    15, 16, 17, 18, 19, 20, 21 -> "${i} days ago" to Icons.Default.Schedule
                    22, 23, 24, 25, 26, 27, 28 -> "${i} days ago" to Icons.Default.Schedule
                    29 -> "29 days ago" to Icons.Default.Schedule
                    30 -> "1 month ago" to Icons.Default.CalendarMonth
                    else -> "${i} days ago" to Icons.Default.Schedule
                }
                options.add(QuickDateOption(label, date, icon, Color(0xFFFF9800)))
            }
        }
        
        options
    }
    
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(quickOptions) { option ->
            QuickDateOptionCard(
                option = option,
                isSelected = option.date == selectedDate,
                onClick = { onDateSelected(option.date) }
            )
        }
    }
}

@Composable
private fun QuickDateOptionCard(
    option: QuickDateOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val animatedScale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "card_scale"
    )
    
    val animatedElevation by animateDpAsState(
        targetValue = if (isSelected) 8.dp else 2.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "card_elevation"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(animatedScale)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                option.color.copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = animatedElevation),
        border = if (isSelected) {
            BorderStroke(2.dp, option.color.copy(alpha = 0.6f))
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Icon with gradient background
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    option.color.copy(alpha = 0.2f),
                                    option.color.copy(alpha = 0.1f)
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = option.icon,
                        contentDescription = null,
                        tint = option.color,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Column {
                    Text(
                        text = option.label,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        fontSize = 16.sp
                    )
                    Text(
                        text = DateTimeUtils.formatDate(option.date),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        },
                        fontSize = 14.sp
                    )
                }
            }
            
            // Selection indicator
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            color = option.color,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarDateSelector(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    maxDate: LocalDate
) {
    val today = DateTimeUtils.getCurrentDate()
    var currentMonth by remember { mutableStateOf(selectedDate) }
    
    Column {
        // Month Navigation
        MonthNavigation(
            currentMonth = currentMonth,
            onMonthChange = { currentMonth = it },
            maxDate = maxDate
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Calendar Grid
        CalendarGrid(
            month = currentMonth,
            selectedDate = selectedDate,
            onDateSelected = onDateSelected,
            maxDate = maxDate
        )
    }
}

@Composable
private fun MonthNavigation(
    currentMonth: LocalDate,
    onMonthChange: (LocalDate) -> Unit,
    maxDate: LocalDate
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = {
                val newMonth = DateTimeUtils.addDays(currentMonth, -30)
                if (!DateTimeUtils.isDateAfter(newMonth, maxDate)) {
                    onMonthChange(newMonth)
                }
            },
            enabled = !DateTimeUtils.isDateAfter(DateTimeUtils.addDays(currentMonth, -30), maxDate)
        ) {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = "Previous Month",
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        Text(
            text = "${currentMonth.year} ${getMonthName(currentMonth.monthNumber)}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        IconButton(
            onClick = {
                val newMonth = DateTimeUtils.addDays(currentMonth, 30)
                onMonthChange(newMonth)
            }
        ) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Next Month",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    month: LocalDate,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    maxDate: LocalDate
) {
    val today = DateTimeUtils.getCurrentDate()
    val firstDayOfMonth = LocalDate(month.year, month.monthNumber, 1)
    val lastDayOfMonth = firstDayOfMonth.plus(DatePeriod(months = 1)).minus(DatePeriod(days = 1))
    
    // Generate calendar days
    val calendarDays = remember(month) {
        val days = mutableListOf<CalendarDay>()
        
        // Add days from previous month
        val startDate = firstDayOfMonth.minus(DatePeriod(days = firstDayOfMonth.dayOfWeek.ordinal))
        for (i in 0 until firstDayOfMonth.dayOfWeek.ordinal) {
            val date = startDate.plus(DatePeriod(days = i))
            days.add(CalendarDay(date, false, false))
        }
        
        // Add days of current month
        for (day in 1..lastDayOfMonth.dayOfMonth) {
            val date = LocalDate(month.year, month.monthNumber, day)
            val isToday = date == today
            val isSelectable = !DateTimeUtils.isDateAfter(date, maxDate)
            days.add(CalendarDay(date, isToday, isSelectable))
        }
        
        // Add days from next month to complete the grid
        val remainingDays = 42 - days.size
        for (i in 1..remainingDays) {
            val date = lastDayOfMonth.plus(DatePeriod(days = i))
            days.add(CalendarDay(date, false, false))
        }
        
        days
    }
    
    Column {
        // Day headers
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Calendar grid
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(6) { week ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (dayIndex in 0..6) {
                        val day = calendarDays[week * 7 + dayIndex]
                        CalendarDayItem(
                            day = day,
                            isSelected = day.date == selectedDate,
                            onClick = { if (day.isSelectable) onDateSelected(day.date) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayItem(
    day: CalendarDay,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val animatedScale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "day_scale"
    )
    
    Box(
        modifier = Modifier
            .size(40.dp)
            .scale(animatedScale)
            .clickable(enabled = day.isSelectable) { onClick() }
            .background(
                color = when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    day.isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    else -> Color.Transparent
                },
                shape = CircleShape
            )
            .border(
                width = if (day.isToday && !isSelected) 2.dp else 0.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.date.dayOfMonth.toString(),
            color = when {
                isSelected -> MaterialTheme.colorScheme.onPrimary
                !day.isSelectable -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                day.isToday -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurface
            },
            fontWeight = if (isSelected || day.isToday) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}

// Data classes
private data class QuickDateOption(
    val label: String,
    val date: LocalDate,
    val icon: ImageVector,
    val color: Color
)

private data class CalendarDay(
    val date: LocalDate,
    val isToday: Boolean,
    val isSelectable: Boolean
)

private enum class DateSelectorTab(
    val title: String,
    val icon: ImageVector
) {
    QUICK("Quick", Icons.Default.FlashOn),
    CALENDAR("Calendar", Icons.Default.CalendarMonth)
}

// Helper functions
private fun getMonthName(monthNumber: Int): String {
    return when (monthNumber) {
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
