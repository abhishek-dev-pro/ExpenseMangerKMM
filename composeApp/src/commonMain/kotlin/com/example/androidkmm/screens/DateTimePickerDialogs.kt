package com.example.androidkmm.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.util.*

@Composable
fun DatePickerDialog(
    onDismissRequest: () -> Unit,
    onDateSelected: (Date) -> Unit
) {
    var selectedDate by remember { mutableStateOf(Date()) }
    
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1A1A1A)
            ),
            shape = RoundedCornerShape(16.dp)
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
                        text = "Select Date",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Simple date picker (you can enhance this with a proper date picker)
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DateOption(
                        label = "Today",
                        date = Date(),
                        isSelected = selectedDate == Date(),
                        onClick = { selectedDate = Date() }
                    )
                    
                    DateOption(
                        label = "Yesterday",
                        date = Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000),
                        isSelected = false,
                        onClick = { selectedDate = Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000) }
                    )
                    
                    DateOption(
                        label = "This Week",
                        date = Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000),
                        isSelected = false,
                        onClick = { selectedDate = Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000) }
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismissRequest,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2A2A2A)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Cancel",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                    
                    Button(
                        onClick = {
                            onDateSelected(selectedDate)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Select",
                            color = Color.Black,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DateOption(
    label: String,
    date: Date,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isSelected) Color.White.copy(alpha = 0.1f) else Color.Transparent,
                RoundedCornerShape(8.dp)
            )
            .padding(12.dp)
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 14.sp
        )
    }
}

@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    onTimeSelected: (Date) -> Unit
) {
    var selectedHour by remember { mutableStateOf(12) }
    var selectedMinute by remember { mutableStateOf(0) }
    var isAM by remember { mutableStateOf(true) }
    
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1A1A1A)
            ),
            shape = RoundedCornerShape(16.dp)
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
                        text = "Select Time",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Time picker
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Hour
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Hour",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TimeNumberPicker(
                            value = selectedHour,
                            onValueChange = { selectedHour = it },
                            range = 1..12
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(20.dp))
                    
                    Text(
                        text = ":",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.width(20.dp))
                    
                    // Minute
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Minute",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TimeNumberPicker(
                            value = selectedMinute,
                            onValueChange = { selectedMinute = it },
                            range = 0..59,
                            step = 5
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(20.dp))
                    
                    // AM/PM
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Period",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TimeNumberPicker(
                            value = if (isAM) 0 else 1,
                            onValueChange = { isAM = it == 0 },
                            range = 0..1,
                            labels = listOf("AM", "PM")
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismissRequest,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2A2A2A)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Cancel",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                    
                    Button(
                        onClick = {
                            val calendar = Calendar.getInstance()
                            calendar.set(Calendar.HOUR, selectedHour)
                            calendar.set(Calendar.MINUTE, selectedMinute)
                            calendar.set(Calendar.AM_PM, if (isAM) Calendar.AM else Calendar.PM)
                            onTimeSelected(calendar.time)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Select",
                            color = Color.Black,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeNumberPicker(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    step: Int = 1,
    labels: List<String>? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Up arrow
        Icon(
            imageVector = Icons.Default.KeyboardArrowUp,
            contentDescription = "Increase",
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
        
        // Value
        Text(
            text = labels?.getOrNull(value) ?: value.toString().padStart(2, '0'),
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
        
        // Down arrow
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = "Decrease",
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun CategoryPickerDialog(
    categories: List<com.example.androidkmm.models.TransactionCategory>,
    onDismissRequest: () -> Unit,
    onCategorySelected: (com.example.androidkmm.models.TransactionCategory) -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1A1A1A)
            ),
            shape = RoundedCornerShape(16.dp)
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
                        text = "Select Category",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Categories list
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        CategoryOption(
                            category = category,
                            onClick = {
                                onCategorySelected(category)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryOption(
    category: com.example.androidkmm.models.TransactionCategory,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color(0xFF2A2A2A),
                RoundedCornerShape(8.dp)
            )
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(category.color, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Category,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = category.name,
                color = Color.White,
                fontSize = 14.sp
            )
        }
    }
}

// Helper function to get icon for category
private fun getIconForCategory(iconName: String) = when (iconName) {
    "Restaurant" -> Icons.Default.Restaurant
    "ShoppingCart" -> Icons.Default.ShoppingCart
    "DirectionsCar" -> Icons.Default.DirectionsCar
    "Home" -> Icons.Default.Home
    "AttachMoney" -> Icons.Default.AttachMoney
    else -> Icons.Default.Category
}
