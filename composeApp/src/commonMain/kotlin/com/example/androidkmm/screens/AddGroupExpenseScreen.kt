package com.example.androidkmm.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidkmm.database.SQLiteGroupDatabase
import com.example.androidkmm.database.SQLiteCategoryDatabase
import com.example.androidkmm.models.Group
import com.example.androidkmm.models.GroupMember
import com.example.androidkmm.models.Category
import com.example.androidkmm.models.CategoryType
import androidx.compose.runtime.collectAsState
import kotlin.time.ExperimentalTime
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, kotlin.time.ExperimentalTime::class)
@Composable
fun AddGroupExpenseScreen(
    group: Group,
    members: List<GroupMember>,
    onBack: () -> Unit,
    onExpenseAdded: () -> Unit,
    groupDatabaseManager: SQLiteGroupDatabase,
    categoryDatabaseManager: SQLiteCategoryDatabase
) {
    val allCategories by categoryDatabaseManager.getCategoriesByType(CategoryType.EXPENSE).collectAsState(initial = emptyList())
    
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var selectedPayer by remember { mutableStateOf<GroupMember?>(members.firstOrNull()) }
    var selectedMembers by remember { mutableStateOf(setOf<String>()) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showPayerSelection by remember { mutableStateOf(false) }
    var showSplitSelection by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    
    // Get current date and time
    val currentDate = remember {
        val now = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        "${now.date.year}-${now.date.monthNumber.toString().padStart(2, '0')}-${now.date.dayOfMonth.toString().padStart(2, '0')}"
    }
    val currentTime = remember {
        val now = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        "${now.time.hour.toString().padStart(2, '0')}:${now.time.minute.toString().padStart(2, '0')}"
    }
    
    // Initialize selected members with all members
    LaunchedEffect(members) {
        selectedMembers = members.map { it.id }.toSet()
    }
    
    val isFormValid = description.isNotBlank() && amount.isNotBlank() && selectedCategory != null && selectedPayer != null && selectedMembers.isNotEmpty()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Add Expense",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(24.dp))

            // Group Context Bar
            GroupContextBar(group = group, members = members)
            
            Spacer(Modifier.height(32.dp))

            // Expense Description Section
            ExpenseDescriptionSection(
                description = description,
                onDescriptionChange = { description = it },
                selectedCategory = selectedCategory,
                onCategoryClick = { showCategoryPicker = true }
            )
            
            Spacer(Modifier.height(24.dp))

            // Amount Section
            AmountSection(
                amount = amount,
                onAmountChange = { amount = it }
            )
            
            Spacer(Modifier.height(32.dp))

            // Payment and Split Section
            PaymentSplitSection(
                selectedPayer = selectedPayer,
                selectedMembers = selectedMembers,
                members = members,
                onPayerClick = { showPayerSelection = true },
                onSplitClick = { showSplitSelection = true }
            )

            Spacer(Modifier.height(48.dp))

            // Save Button
            Button(
                onClick = {
                    if (isFormValid) {
                        val expenseId = "expense_${kotlin.time.Clock.System.now().toEpochMilliseconds()}"
                        val expenseAmount = amount.toDoubleOrNull() ?: 0.0
                        val splitAmount = expenseAmount / selectedMembers.size
                        
                        val groupExpense = com.example.androidkmm.models.GroupExpense(
                            id = expenseId,
                            groupId = group.id,
                            paidBy = selectedPayer!!.id,
                            amount = expenseAmount,
                            description = description,
                            category = selectedCategory!!.name,
                            date = currentDate,
                            time = currentTime,
                            splitType = com.example.androidkmm.models.SplitType.EQUAL,
                            splitDetails = "",
                            createdAt = kotlin.time.Clock.System.now().toEpochMilliseconds()
                        )
                        
                        coroutineScope.launch {
                            groupDatabaseManager.insertGroupExpense(groupExpense)
                            
                            selectedMembers.forEach { memberId ->
                                val member = members.find { it.id == memberId }
                                if (member != null) {
                                    val newBalance = if (memberId == selectedPayer!!.id) {
                                        member.balance + (expenseAmount - splitAmount)
                                    } else {
                                        member.balance - splitAmount
                                    }
                                    val updatedMember = member.copy(balance = newBalance)
                                    groupDatabaseManager.updateGroupMember(updatedMember)
                                }
                            }
                            
                            onExpenseAdded()
                        }
                    }
                },
                enabled = isFormValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF10B981), // Green color for success
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Add Expense", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    // Category Picker Bottom Sheet
    if (showCategoryPicker) {
        CategorySelectionBottomSheet(
            onDismiss = { showCategoryPicker = false },
            onCategorySelected = { category ->
                selectedCategory = category
                showCategoryPicker = false
            },
            categoryDatabaseManager = categoryDatabaseManager
        )
    }

    // Payer Selection Screen
    if (showPayerSelection) {
        PayerSelectionScreen(
            members = members,
            selectedPayer = selectedPayer,
            onBack = { showPayerSelection = false },
            onPayerSelected = { payer ->
                selectedPayer = payer
                showPayerSelection = false
            }
        )
    }

    // Split Selection Screen
    if (showSplitSelection) {
        SplitSelectionScreen(
            members = members,
            selectedMembers = selectedMembers,
            onBack = { 
                println("AddGroupExpenseScreen: Split screen back button clicked")
                showSplitSelection = false 
            },
            onMembersSelected = { members, splitType, customAmounts ->
                println("AddGroupExpenseScreen: Split screen save button clicked")
                println("Selected members: $members")
                println("Split type: $splitType")
                println("Custom amounts: $customAmounts")
                selectedMembers = members
                // TODO: Store splitType and customAmounts for later use in expense creation
                showSplitSelection = false
            }
        )
    }
}

@Composable
private fun GroupContextBar(
    group: Group,
    members: List<GroupMember>
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color(0xFFF3F4F6),
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
                    Text(
            text = "With you and:",
            color = Color(0xFF6B7280),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    
        Spacer(Modifier.width(8.dp))
        
        // Group Icon
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                .background(group.color),
                                    contentAlignment = Alignment.Center
                                ) {
            Text(
                text = group.name.take(2).uppercase(),
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(Modifier.width(8.dp))
                                
                                Text(
            text = group.name,
            color = Color(0xFF374151),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(Modifier.width(4.dp))
        
        Text(
            text = "😊",
            fontSize = 16.sp
        )
    }
}

@Composable
private fun ExpenseDescriptionSection(
    description: String,
    onDescriptionChange: (String) -> Unit,
    selectedCategory: Category?,
    onCategoryClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Category Icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    selectedCategory?.color ?: Color(0xFFE5E7EB)
                )
                .clickable { onCategoryClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = selectedCategory?.icon ?: Icons.Default.Category,
                contentDescription = null,
                tint = if (selectedCategory != null) Color.White else Color(0xFF9CA3AF),
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(Modifier.width(12.dp))
        
        // Description Input
        BasicTextField(
            value = description,
            onValueChange = onDescriptionChange,
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 8.dp)
        ) { innerTextField ->
            if (description.isEmpty()) {
                    Text(
                    text = "What did you spend on?",
                    color = Color(0xFF9CA3AF),
                    fontSize = 18.sp
                )
            }
            innerTextField()
        }
    }
    
    // Underline
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
            .height(1.dp)
            .background(Color(0xFFE5E7EB))
    )
}

@Composable
private fun AmountSection(
    amount: String,
    onAmountChange: (String) -> Unit
                    ) {
                        Row(
        modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
        // Dollar Icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF3F4F6)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$",
                color = Color(0xFF6B7280),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(Modifier.width(12.dp))
        
        // Amount Input
        BasicTextField(
            value = amount,
            onValueChange = onAmountChange,
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 8.dp)
        ) { innerTextField ->
            if (amount.isEmpty()) {
                            Text(
                    text = "0.00",
                    color = Color(0xFF9CA3AF),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            innerTextField()
        }
    }
    
    // Underline
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
            .height(1.dp)
            .background(Color(0xFFE5E7EB))
    )
}

@Composable
private fun PaymentSplitSection(
    selectedPayer: GroupMember?,
    selectedMembers: Set<String>,
    members: List<GroupMember>,
    onPayerClick: () -> Unit,
    onSplitClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Paid by Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Paid by",
                color = Color(0xFF6B7280),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(Modifier.width(8.dp))
            
            // Payer Tag
            PayerTag(
                payer = selectedPayer,
                onClick = onPayerClick
            )
            
            Spacer(Modifier.weight(1f))
            
            // Tick Button for Paid by
            TickButton(
                isSelected = selectedPayer != null,
                onClick = onPayerClick
            )
        }
        
        // Split between Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Split between",
                color = Color(0xFF6B7280),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(Modifier.width(8.dp))
            
            // Split Tag
            SplitTag(
                selectedCount = selectedMembers.size,
                totalCount = members.size,
                onClick = onSplitClick
            )
            
            Spacer(Modifier.weight(1f))
            
            // Tick Button for Split between
            TickButton(
                isSelected = selectedMembers.isNotEmpty(),
                onClick = onSplitClick
            )
        }
    }
}

@Composable
private fun PayerTag(
    payer: GroupMember?,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF3F4F6))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
                    Text(
            text = payer?.name ?: "Select payer",
            color = if (payer != null) Color(0xFF374151) else Color(0xFF9CA3AF),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
    }
}

@Composable
private fun SplitTag(
    selectedCount: Int,
    totalCount: Int,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF3F4F6))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = if (selectedCount == totalCount) "equally" else "between $selectedCount",
            color = Color(0xFF374151),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun TickButton(
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(
                if (isSelected) Color(0xFF10B981) else Color(0xFFE5E7EB)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        } else {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
                tint = Color(0xFF6B7280),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategorySelectionBottomSheet(
    onDismiss: () -> Unit,
    onCategorySelected: (Category) -> Unit,
    categoryDatabaseManager: SQLiteCategoryDatabase
) {
    val categories = categoryDatabaseManager.getCategoriesByType(CategoryType.EXPENSE).collectAsState(initial = emptyList())

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
                        modifier = Modifier
                            .fillMaxWidth()
                .padding(24.dp)
            ) {
                Text(
                text = "Select Category",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(16.dp))
            
            categories.value.forEach { category ->
                CategoryItem(
                    category = category,
                    onClick = { onCategorySelected(category) }
                )
                Spacer(Modifier.height(8.dp))
            }
            
            Spacer(Modifier.height(24.dp))
        }
    }
}


@Composable
private fun CategoryItem(
    category: Category,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
            Box(
                modifier = Modifier
                .size(40.dp)
                    .clip(CircleShape)
                .background(category.color),
                contentAlignment = Alignment.Center
            ) {
            Icon(
                imageVector = category.icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(16.dp))
        Text(
            text = category.name,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
