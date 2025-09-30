package com.example.androidkmm.screens.ledger.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.example.androidkmm.utils.DateTimeUtils
import kotlinx.datetime.*
import kotlin.time.ExperimentalTime
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.DialogProperties
import com.example.androidkmm.design.AppStyleDesignSystem
import com.example.androidkmm.screens.ledger.LedgerTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun SimpleDatePickerDialog(
    onDismiss: () -> Unit,
    onDateSelected: (String) -> Unit,
    initialDate: String
) {
    val today = DateTimeUtils.getCurrentDate()
    val todayMillis = DateTimeUtils.getStartOfDay(today).toEpochMilliseconds()
    val tomorrowMillis = DateTimeUtils.getStartOfDay(today.plus(DatePeriod(days = 1))).toEpochMilliseconds()
    
    val datePickerState = rememberDatePickerState(
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
                DatePicker(
                    state = datePickerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(horizontal = AppStyleDesignSystem.Padding.XXS),
                    colors = DatePickerDefaults.colors(
                            containerColor = Color(0xFF1F1F1F),
                            titleContentColor = LedgerTheme.textPrimary(),
                            headlineContentColor = LedgerTheme.textPrimary(),
                            weekdayContentColor = LedgerTheme.textSecondary(),
                            subheadContentColor = LedgerTheme.textSecondary(),
                            yearContentColor = LedgerTheme.textPrimary(),
                            currentYearContentColor = LedgerTheme.textPrimary(),
                            selectedYearContentColor = Color.White,
                            selectedYearContainerColor = Color(0xFF2196F3),
                            dayContentColor = LedgerTheme.textPrimary(),
                            disabledDayContentColor = LedgerTheme.textSecondary(),
                            selectedDayContentColor = Color.White,
                            disabledSelectedDayContentColor = Color.White,
                            selectedDayContainerColor = Color(0xFF2196F3),
                            disabledSelectedDayContainerColor = LedgerTheme.textSecondary(),
                            todayContentColor = Color(0xFF2196F3),
                            todayDateBorderColor = Color(0xFF2196F3),
                            dayInSelectionRangeContentColor = LedgerTheme.textPrimary(),
                            dayInSelectionRangeContainerColor = Color(0xFF2196F3).copy(alpha = 0.3f)
                        )
                    )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = DateTimeUtils.instantToLocalDate(
                                Instant.fromEpochMilliseconds(millis)
                            )
                            val today = DateTimeUtils.getCurrentDate()
                            
                            // Check if selected date is in the future
                            if (DateTimeUtils.isDateAfter(date, today)) {
                                // Don't allow future dates - just close dialog without selecting
                                showDialog = false
                                return@TextButton
                            }
                            
                            val dateString = DateTimeUtils.formatDate(date)
                            onDateSelected(dateString)
                        }
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
