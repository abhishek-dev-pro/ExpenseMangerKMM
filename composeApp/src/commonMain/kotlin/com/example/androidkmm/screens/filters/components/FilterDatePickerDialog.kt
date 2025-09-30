package com.example.androidkmm.screens.filters.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.example.androidkmm.utils.DateTimeUtils
import kotlinx.datetime.*
import kotlin.time.ExperimentalTime
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import com.example.androidkmm.utils.DateFormatUtils

/**
 * Date picker dialog for filter functionality
 * 
 * Provides a date picker interface for selecting custom date ranges in filters.
 * Handles date formatting and conversion between display and storage formats.
 * 
 * @param onDismiss Callback when dialog is dismissed
 * @param onDateSelected Callback when a date is selected
 * @param initialDate Initial date value in YYYY-MM-DD format
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun FilterDatePickerDialog(
    onDismiss: () -> Unit,
    onDateSelected: (String) -> Unit,
    initialDate: String = ""
) {
    val today = DateTimeUtils.getCurrentDate()
    val todayMillis = DateTimeUtils.getStartOfDay(today).toEpochMilliseconds()
    val tomorrowMillis = DateTimeUtils.getStartOfDay(today.plus(DatePeriod(days = 1))).toEpochMilliseconds()
    
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = if (initialDate.isNotEmpty()) {
            try {
                val parts = initialDate.split("-")
                if (parts.size == 3) {
                    val year = parts[0].toInt()
                    val month = parts[1].toInt()
                    val day = parts[2].toInt()
                    val selectedDate = DateTimeUtils.createDate(year, month, day)
                    // If the initial date is in the future, use today instead
                    if (selectedDate != null && DateTimeUtils.isDateAfter(selectedDate, today)) {
                        todayMillis
                    } else {
                        selectedDate?.let { date ->
                            DateTimeUtils.getStartOfDay(date).toEpochMilliseconds()
                        } ?: todayMillis
                    }
                } else null
            } catch (e: Exception) {
                null
            }
        } else null,
        selectableDates = object : androidx.compose.material3.SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                // Allow dates up to and including today
                return utcTimeMillis < tomorrowMillis
            }
            
            override fun isSelectableYear(year: Int): Boolean {
                // Only allow current year and previous years
                return year <= today.year
            }
        }
    )

    // Track selected date for display
    var selectedDateDisplay by remember { mutableStateOf("") }
    
    // Update display when date changes
    LaunchedEffect(datePickerState.selectedDateMillis) {
        datePickerState.selectedDateMillis?.let { millis ->
            val localDate = DateTimeUtils.instantToLocalDate(
                Instant.fromEpochMilliseconds(millis)
            )
            selectedDateDisplay = "${localDate.dayOfMonth} ${localDate.month.name.lowercase().replaceFirstChar { it.uppercase() }}, ${localDate.year}"
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.8f),
        title = {
            Column {
                Text(
                    text = "Select Date",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (selectedDateDisplay.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = selectedDateDisplay,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        text = {
            DatePicker(
                state = datePickerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(horizontal = 0.dp),
                colors = DatePickerDefaults.colors(
                    selectedYearContainerColor = Color(0xFF2196F3),
                    selectedDayContainerColor = Color(0xFF2196F3),
                    todayContentColor = Color(0xFF2196F3),
                    todayDateBorderColor = Color(0xFF2196F3),
                    dayInSelectionRangeContainerColor = Color(0xFF2196F3).copy(alpha = 0.3f)
                )
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val localDate = DateTimeUtils.instantToLocalDate(
                            Instant.fromEpochMilliseconds(millis)
                        )
                        val today = DateTimeUtils.getCurrentDate()
                        
                        // Check if selected date is in the future
                        if (DateTimeUtils.isDateAfter(localDate, today)) {
                            // Don't allow future dates - just close dialog without selecting
                            onDismiss()
                            return@Button
                        }
                        
                        val formattedDate = DateTimeUtils.formatDate(localDate)
                        onDateSelected(formattedDate)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3)
                )
            ) {
                Text(
                    text = "Select",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outline
                )
            ) {
                Text(
                    text = "Cancel",
                    fontSize = 14.sp
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurface
    )
}
