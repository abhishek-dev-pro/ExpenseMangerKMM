package com.example.androidkmm.screens.ledger.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.example.androidkmm.utils.DateTimeUtils
import kotlinx.datetime.*
import kotlin.time.ExperimentalTime
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.androidkmm.design.AppStyleDesignSystem
import com.example.androidkmm.screens.ledger.LedgerTheme

@OptIn(ExperimentalTime::class)
@Composable
fun SimpleDatePickerDialog(
    onDismiss: () -> Unit,
    onDateSelected: (String) -> Unit,
    initialDate: String
) {
    val today = DateTimeUtils.getCurrentDate()
    
    // Parse initial date or use today
    val initialParsedDate = if (initialDate.isNotEmpty()) {
        DateTimeUtils.parseDate(initialDate) ?: today
    } else {
        today
    }
    
    var selectedDate by remember { mutableStateOf(initialParsedDate) }
    var showDialog by remember { mutableStateOf(true) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
            title = {
                Text(
                    text = "Select Date",
                    color = LedgerTheme.textPrimary(),
                    fontSize = AppStyleDesignSystem.Typography.MAIN_PAGE_SUBHEADING_TITLE.fontSize,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                CustomDatePicker(
                    selectedDate = selectedDate,
                    onDateSelected = { selectedDate = it },
                    maxDate = today
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Check if selected date is in the future
                        if (DateTimeUtils.isDateAfter(selectedDate, today)) {
                            // Don't allow future dates - just close dialog without selecting
                            showDialog = false
                            return@TextButton
                        }
                        
                        val dateString = DateTimeUtils.formatDate(selectedDate)
                        onDateSelected(dateString)
                        showDialog = false
                    }
                ) {
                    Text(
                        text = "OK",
                        color = LedgerTheme.textPrimary(),
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = "Cancel",
                        color = LedgerTheme.textSecondary()
                    )
                }
            },
            containerColor = Color(0xFF1F1F1F)
        )
    }
}

@Composable
private fun CustomDatePicker(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    maxDate: LocalDate
) {
    val today = DateTimeUtils.getCurrentDate()
    
    // Generate date options
    val dateOptions = remember {
        val options = mutableListOf<DateOption>()
        
        // Today
        options.add(DateOption("Today", today, today == selectedDate))
        
        // Yesterday
        val yesterday = DateTimeUtils.addDays(today, -1)
        if (!DateTimeUtils.isDateAfter(yesterday, maxDate)) {
            options.add(DateOption("Yesterday", yesterday, yesterday == selectedDate))
        }
        
        // This week (last 7 days)
        for (i in 2..7) {
            val date = DateTimeUtils.addDays(today, -i)
            if (!DateTimeUtils.isDateAfter(date, maxDate)) {
                val dayName = when (i) {
                    2 -> "2 days ago"
                    3 -> "3 days ago"
                    4 -> "4 days ago"
                    5 -> "5 days ago"
                    6 -> "6 days ago"
                    7 -> "1 week ago"
                    else -> "${i} days ago"
                }
                options.add(DateOption(dayName, date, date == selectedDate))
            }
        }
        
        // This month (last 30 days)
        for (i in 8..30) {
            val date = DateTimeUtils.addDays(today, -i)
            if (!DateTimeUtils.isDateAfter(date, maxDate)) {
                val dayName = when (i) {
                    8, 9, 10, 11, 12, 13, 14 -> "${i} days ago"
                    15, 16, 17, 18, 19, 20, 21 -> "${i} days ago"
                    22, 23, 24, 25, 26, 27, 28 -> "${i} days ago"
                    29 -> "29 days ago"
                    30 -> "1 month ago"
                    else -> "${i} days ago"
                }
                options.add(DateOption(dayName, date, date == selectedDate))
            }
        }
        
        options
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(dateOptions) { option ->
            DateOptionItem(
                option = option,
                onClick = { onDateSelected(option.date) }
            )
        }
    }
}

@Composable
private fun DateOptionItem(
    option: DateOption,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (option.isSelected) Color(0xFF2196F3) else Color(0xFF2A2A2A)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = option.label,
                    color = if (option.isSelected) Color.White else LedgerTheme.textPrimary(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = DateTimeUtils.formatDate(option.date),
                    color = if (option.isSelected) Color.White.copy(alpha = 0.8f) else LedgerTheme.textSecondary(),
                    fontSize = 14.sp
                )
            }
            
            if (option.isSelected) {
                Text(
                    text = "✓",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private data class DateOption(
    val label: String,
    val date: LocalDate,
    val isSelected: Boolean
)
