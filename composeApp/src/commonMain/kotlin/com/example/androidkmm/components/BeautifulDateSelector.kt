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
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Enhanced Month Navigation with beautiful header
        BeautifulMonthHeader(
            currentMonth = currentMonth,
            onMonthChange = { currentMonth = it },
            maxDate = maxDate,
            selectedDate = selectedDate
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Enhanced Calendar Grid with better styling
        BeautifulCalendarGrid(
            month = currentMonth,
            selectedDate = selectedDate,
            onDateSelected = onDateSelected,
            maxDate = maxDate
        )
    }
}

@Composable
private fun BeautifulMonthHeader(
    currentMonth: LocalDate,
    onMonthChange: (LocalDate) -> Unit,
    maxDate: LocalDate,
    selectedDate: LocalDate
) {
    val today = DateTimeUtils.getCurrentDate()
    val isCurrentMonth = currentMonth.year == today.year && currentMonth.monthNumber == today.monthNumber
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentMonth) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Month and Year with beautiful styling
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Previous month button
                IconButton(
                    onClick = {
                        val newMonth = DateTimeUtils.addDays(currentMonth, -30)
                        if (!DateTimeUtils.isDateAfter(newMonth, maxDate)) {
                            onMonthChange(newMonth)
                        }
                    },
                    enabled = !DateTimeUtils.isDateAfter(DateTimeUtils.addDays(currentMonth, -30), maxDate),
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Previous Month",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                // Month and Year with gradient text
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = getMonthName(currentMonth.monthNumber),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isCurrentMonth) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                    Text(
                        text = currentMonth.year.toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = if (isCurrentMonth) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
                
                // Next month button
                IconButton(
                    onClick = {
                        val newMonth = DateTimeUtils.addDays(currentMonth, 30)
                        onMonthChange(newMonth)
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Next Month",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Selected date info
            if (selectedDate.year == currentMonth.year && selectedDate.monthNumber == currentMonth.monthNumber) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Selected: ${DateTimeUtils.formatDate(selectedDate)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun BeautifulCalendarGrid(
    month: LocalDate,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    maxDate: LocalDate
) {
    val today = DateTimeUtils.getCurrentDate()
    val firstDayOfMonth = LocalDate(month.year, month.monthNumber, 1)
    val lastDayOfMonth = firstDayOfMonth.plus(DatePeriod(months = 1)).minus(DatePeriod(days = 1))
    
    // Generate calendar days with enhanced information
    val calendarDays = remember(month) {
        val days = mutableListOf<EnhancedCalendarDay>()
        
        // Add days from previous month
        val startDate = firstDayOfMonth.minus(DatePeriod(days = firstDayOfMonth.dayOfWeek.ordinal))
        for (i in 0 until firstDayOfMonth.dayOfWeek.ordinal) {
            val date = startDate.plus(DatePeriod(days = i))
            val isPast = DateTimeUtils.isDateAfter(today, date)
            val isSelectable = !DateTimeUtils.isDateAfter(date, maxDate)
            days.add(EnhancedCalendarDay(date, false, isSelectable, isPast, false))
        }
        
        // Add days of current month
        for (day in 1..lastDayOfMonth.dayOfMonth) {
            val date = LocalDate(month.year, month.monthNumber, day)
            val isToday = date == today
            val isPast = DateTimeUtils.isDateAfter(today, date)
            val isSelectable = !DateTimeUtils.isDateAfter(date, maxDate)
            val isCurrentMonth = true
            days.add(EnhancedCalendarDay(date, isToday, isSelectable, isPast, isCurrentMonth))
        }
        
        // Add days from next month to complete the grid
        val remainingDays = 42 - days.size
        for (i in 1..remainingDays) {
            val date = lastDayOfMonth.plus(DatePeriod(days = i))
            val isPast = DateTimeUtils.isDateAfter(today, date)
            val isSelectable = !DateTimeUtils.isDateAfter(date, maxDate)
            days.add(EnhancedCalendarDay(date, false, isSelectable, isPast, false))
        }
        
        days
    }
    
    Column {
        // Beautiful day headers with enhanced styling
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(vertical = 12.dp)
        ) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Enhanced calendar grid
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(6) { week ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (dayIndex in 0..6) {
                        val day = calendarDays[week * 7 + dayIndex]
                        BeautifulCalendarDayItem(
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
private fun BeautifulCalendarDayItem(
    day: EnhancedCalendarDay,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val animatedScale by animateFloatAsState(
        targetValue = if (isSelected) 1.15f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "day_scale"
    )
    
    val animatedElevation by animateDpAsState(
        targetValue = if (isSelected) 8.dp else 2.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "day_elevation"
    )
    
    Card(
        modifier = Modifier
            .size(44.dp)
            .scale(animatedScale)
            .clickable(enabled = day.isSelectable) { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> MaterialTheme.colorScheme.primary
                day.isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                day.isPast && day.isCurrentMonth -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                !day.isCurrentMonth -> MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
                else -> Color.Transparent
            }
        ),
        shape = CircleShape,
        elevation = CardDefaults.cardElevation(defaultElevation = animatedElevation),
        border = when {
            isSelected -> BorderStroke(3.dp, MaterialTheme.colorScheme.primary)
            day.isToday && !isSelected -> BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
            day.isPast && day.isCurrentMonth -> BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            else -> null
        }
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = day.date.dayOfMonth.toString(),
                    color = when {
                        isSelected -> MaterialTheme.colorScheme.onPrimary
                        !day.isSelectable -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        day.isToday -> MaterialTheme.colorScheme.primary
                        day.isPast && day.isCurrentMonth -> MaterialTheme.colorScheme.onSurfaceVariant
                        !day.isCurrentMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                    fontWeight = when {
                        isSelected -> FontWeight.Bold
                        day.isToday -> FontWeight.SemiBold
                        day.isPast && day.isCurrentMonth -> FontWeight.Medium
                        else -> FontWeight.Normal
                    },
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
                
                // Add a small indicator for today
                if (day.isToday && !isSelected) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            )
                    )
                }
            }
        }
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

private data class EnhancedCalendarDay(
    val date: LocalDate,
    val isToday: Boolean,
    val isSelectable: Boolean,
    val isPast: Boolean,
    val isCurrentMonth: Boolean
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
