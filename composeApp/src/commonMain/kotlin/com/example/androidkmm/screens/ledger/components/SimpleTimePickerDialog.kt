package com.example.androidkmm.screens.ledger.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.DialogProperties
import com.example.androidkmm.design.AppStyleDesignSystem
import com.example.androidkmm.screens.ledger.LedgerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleTimePickerDialog(
    onDismiss: () -> Unit,
    onTimeSelected: (String) -> Unit,
    initialTime: String
) {
    val timePickerState = rememberTimePickerState(
        initialHour = 12,
        initialMinute = 0,
        is24Hour = false
    )
    var showDialog by remember { mutableStateOf(true) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            title = {
                Text(
                    text = "Select Time",
                    color = LedgerTheme.textPrimary(),
                    fontSize = AppStyleDesignSystem.Typography.TITLE_3.fontSize,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = Color(0xFF2A2A2A),
                        clockDialSelectedContentColor = Color.White,
                        clockDialUnselectedContentColor = LedgerTheme.textPrimary(),
                        selectorColor = Color(0xFF2196F3),
                        periodSelectorBorderColor = LedgerTheme.textSecondary(),
                        periodSelectorSelectedContainerColor = Color(0xFF2196F3),
                        periodSelectorUnselectedContainerColor = Color(0xFF2A2A2A),
                        periodSelectorSelectedContentColor = Color.White,
                        periodSelectorUnselectedContentColor = LedgerTheme.textPrimary(),
                        timeSelectorSelectedContainerColor = Color(0xFF2196F3),
                        timeSelectorUnselectedContainerColor = Color(0xFF2A2A2A),
                        timeSelectorSelectedContentColor = Color.White,
                        timeSelectorUnselectedContentColor = LedgerTheme.textPrimary()
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val hour = timePickerState.hour
                        val minute = timePickerState.minute
                        val timeString = String.format("%02d:%02d", hour, minute)
                        onTimeSelected(timeString)
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
