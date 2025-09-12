package com.example.androidkmm.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidkmm.database.rememberSQLiteCategoryDatabase
import com.example.androidkmm.models.Group
import com.example.androidkmm.models.GroupMember
import com.example.androidkmm.models.TransactionCategory
import com.example.androidkmm.utils.formatDouble
import androidx.compose.runtime.collectAsState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGroupExpenseScreen(
    group: Group,
    members: List<GroupMember>,
    onBack: () -> Unit,
    onExpenseAdded: () -> Unit
) {
    val categoryDatabaseManager = rememberSQLiteCategoryDatabase()
    val allCategories by categoryDatabaseManager.getAllCategories().collectAsState(initial = emptyList())
    
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<TransactionCategory?>(null) }
    var selectedPayer by remember { mutableStateOf<GroupMember?>(members.firstOrNull()) }
    var selectedMembers by remember { mutableStateOf(setOf<String>()) }
    var selectedDate by remember { mutableStateOf(Date()) }
    var selectedTime by remember { mutableStateOf(Date()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    
    // Initialize selected members with all members
    LaunchedEffect(members) {
        selectedMembers = members.map { it.id }.toSet()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Add Group Expense",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = Color.White,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onBack() }
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Total Amount Section
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$",
                        color = Color.Gray,
                        fontSize = 24.sp
                    )
                    
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                    
                    Text(
                        text = "Total Amount",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
            
            // Description Section
            item {
                Column {
                    Text(
                        text = "Description",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 14.sp
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = "What was this expense for?",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                            focusedContainerColor = Color(0xFF1A1A1A),
                            unfocusedContainerColor = Color(0xFF1A1A1A)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
            
            // Category Section
            item {
                Column {
                    Text(
                        text = "Category",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Color(0xFF1A1A1A),
                                RoundedCornerShape(8.dp)
                            )
                            .border(
                                1.dp,
                                Color.Gray.copy(alpha = 0.3f),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { showCategoryPicker = true }
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (selectedCategory != null) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(selectedCategory!!.color),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Category,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Text(
                                    text = selectedCategory!!.name,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            } else {
                                Text(
                                    text = "Select Category",
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                            }
                            
                            Spacer(modifier = Modifier.weight(1f))
                            
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
            
            // Paid By Section
            item {
                Column {
                    Text(
                        text = "Paid by",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(members) { member ->
                            MemberSelectionItem(
                                member = member,
                                isSelected = selectedPayer?.id == member.id,
                                onSelect = { selectedPayer = member }
                            )
                        }
                    }
                }
            }
            
            // Split Between Section
            item {
                Column {
                    Text(
                        text = "Split between",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(members) { member ->
                            MemberCheckboxItem(
                                member = member,
                                isSelected = selectedMembers.contains(member.id),
                                onToggle = { isSelected ->
                                    selectedMembers = if (isSelected) {
                                        selectedMembers + member.id
                                    } else {
                                        selectedMembers - member.id
                                    }
                                }
                            )
                        }
                    }
                }
            }
            
            // Date Section
            item {
                Column {
                    Text(
                        text = "Date",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Color(0xFF1A1A1A),
                                RoundedCornerShape(8.dp)
                            )
                            .border(
                                1.dp,
                                Color.Gray.copy(alpha = 0.3f),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { showDatePicker = true }
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault()).format(selectedDate),
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
            
            // Time Section
            item {
                Column {
                    Text(
                        text = "Time",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Color(0xFF1A1A1A),
                                RoundedCornerShape(8.dp)
                            )
                            .border(
                                1.dp,
                                Color.Gray.copy(alpha = 0.3f),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { showTimePicker = true }
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(selectedTime),
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
            
            // Notes Section
            item {
                Column {
                    Text(
                        text = "Notes (Optional)",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = "",
                        onValueChange = { },
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 14.sp
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        placeholder = {
                            Text(
                                text = "Add any additional notes...",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                            focusedContainerColor = Color(0xFF1A1A1A),
                            unfocusedContainerColor = Color(0xFF1A1A1A)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
            
            // Add Expense Button
            item {
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = {
                        // TODO: Save expense to database
                        onExpenseAdded()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1A1A1A)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "Add Expense",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
    
    // Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            onDateSelected = { date ->
                selectedDate = date
                showDatePicker = false
            }
        )
    }
    
    // Time Picker Dialog
    if (showTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            onTimeSelected = { time ->
                selectedTime = time
                showTimePicker = false
            }
        )
    }
    
    // Category Picker Dialog
    if (showCategoryPicker) {
        CategoryPickerDialog(
            categories = allCategories.map { 
                com.example.androidkmm.models.TransactionCategory(
                    id = it.id,
                    name = it.name,
                    icon = it.icon,
                    color = it.color
                )
            },
            onDismissRequest = { showCategoryPicker = false },
            onCategorySelected = { category ->
                selectedCategory = category
                showCategoryPicker = false
            }
        )
    }
}

@Composable
private fun MemberSelectionItem(
    member: GroupMember,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isSelected) Color.White.copy(alpha = 0.1f) else Color(0xFF1A1A1A),
                RoundedCornerShape(8.dp)
            )
            .border(
                width = if (isSelected) 1.dp else 0.dp,
                color = if (isSelected) Color.White else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onSelect() }
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(member.avatarColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = member.name.split(" ").mapNotNull { if (it.isNotBlank()) it.first() else null }.joinToString(""),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = member.name,
                color = Color.White,
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun MemberCheckboxItem(
    member: GroupMember,
    isSelected: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color(0xFF1A1A1A),
                RoundedCornerShape(8.dp)
            )
            .clickable { onToggle(!isSelected) }
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = onToggle,
                colors = CheckboxDefaults.colors(
                    checkedColor = Color.White,
                    uncheckedColor = Color.Gray
                )
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(member.avatarColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = member.name.split(" ").mapNotNull { if (it.isNotBlank()) it.first() else null }.joinToString(""),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = member.name,
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
