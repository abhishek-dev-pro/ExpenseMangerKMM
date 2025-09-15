package com.example.androidkmm.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidkmm.design.DesignSystem
import com.example.androidkmm.database.rememberSQLiteGroupDatabase
import com.example.androidkmm.database.rememberSQLiteCategoryDatabase
import com.example.androidkmm.database.SQLiteGroupDatabase
import com.example.androidkmm.models.Group
import com.example.androidkmm.models.GroupMember
import com.example.androidkmm.utils.formatDouble
import kotlinx.coroutines.delay
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsScreen(
    onNavigateToCreateGroup: () -> Unit = {}
) {
    val groupDatabaseManager = rememberSQLiteGroupDatabase()
    val categoryDatabaseManager = rememberSQLiteCategoryDatabase()
    val allGroups by groupDatabaseManager.getAllGroups().collectAsState(initial = emptyList())
    val allMembers by groupDatabaseManager.getAllGroupMembers().collectAsState(initial = emptyList())
    
    // Calculate total owed and owe from all groups
    val totalOwed = remember(allMembers) {
        val total = allMembers.sumOf { if (it.balance > 0) it.balance else 0.0 }
        "+$${formatDouble(total)}"
    }
    
    val totalOwe = remember(allMembers) {
        val total = allMembers.sumOf { if (it.balance < 0) kotlin.math.abs(it.balance) else 0.0 }
        "-$${formatDouble(total)}"
    }
    
    var selectedGroup by remember { mutableStateOf<Group?>(null) }
    var showGroupDetails by remember { mutableStateOf(false) }
    var showAddGroupExpense by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Groups",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Track group expenses & splits",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }

            IconButton(
                onClick = { onNavigateToCreateGroup() },
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        MaterialTheme.colorScheme.onBackground,
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Summary Cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // You are owed Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(100.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(
                        width = 0.5.dp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(16.dp)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF0F2419) // Keep green for "To Send"
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = "You are owed",
                            tint = Color(0xFF16A34A),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "You are owed",
                            color = Color(0xFF16A34A),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Column {
                        Text(
                            text = totalOwed,
                            color = Color(0xFF16A34A),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "0 people",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // You owe Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(100.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(
                        width = 0.5.dp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(16.dp)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2D1B1B) // Keep red for "To Receive"
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = "You owe",
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "You owe",
                            color = Color(0xFFDC2626),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Column {
                        Text(
                            text = totalOwe,
                            color = Color(0xFFDC2626),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "0 people",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Groups list
        LazyColumn(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }

            itemsIndexed(allGroups) { index, group ->
                var visible by remember { mutableStateOf(false) }
                val groupMembers by groupDatabaseManager.getGroupMembersByGroup(group.id).collectAsState(initial = emptyList())

                LaunchedEffect(Unit) {
                    delay(index * 100L) // stagger each item by 100ms
                    visible = true
                }

                AnimatedVisibility(
                    visible = visible,
                    enter = slideInVertically(
                        initialOffsetY = { it } // slide in from bottom
                    ) + fadeIn()
                ) {
                    GroupCard(
                        group = group,
                        members = groupMembers,
                        groupDatabaseManager = groupDatabaseManager,
                        onClick = {
                            selectedGroup = group
                            showGroupDetails = true
                        }
                    )
                }
            }
        }

    }
    
    // Show Group Details Screen
    if (showGroupDetails && selectedGroup != null) {
        GroupDetailsScreen(
            group = selectedGroup!!,
            onBack = {
                showGroupDetails = false
                selectedGroup = null
            },
            onAddExpense = {
                showAddGroupExpense = true
            }
        )
    }
    
    // Show Add Group Expense Screen
    if (showAddGroupExpense && selectedGroup != null) {
        val groupMembers by groupDatabaseManager.getGroupMembersByGroup(selectedGroup!!.id).collectAsState(initial = emptyList())
        AddGroupExpenseScreen(
            group = selectedGroup!!,
            members = groupMembers,
            onBack = {
                showAddGroupExpense = false
            },
            onExpenseAdded = {
                showAddGroupExpense = false
                // Refresh the group details if needed
            },
            groupDatabaseManager = groupDatabaseManager,
            categoryDatabaseManager = categoryDatabaseManager
        )
    }
}


@Composable
fun GroupCard(
    group: Group,
    members: List<GroupMember>,
    groupDatabaseManager: SQLiteGroupDatabase,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .clip(RoundedCornerShape(DesignSystem.CornerRadius.md))
            .border(
                width = 0.5.dp, // very thin border
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), // subtle white
                shape = RoundedCornerShape(DesignSystem.CornerRadius.md)
            ).padding(16.dp),

    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(group.color),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Groups,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = group.name,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Normal,
                        fontStyle = FontStyle.Normal,
                        fontSize = DesignSystem.Typography.balanceLabel,
                        lineHeight = DesignSystem.Typography.cardTitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${members.size} members • ${formatDateForGroup(group.createdAt)}",
                        color = Color.Gray,
                        fontStyle = FontStyle.Normal,
                        fontSize = DesignSystem.Typography.caption1,
                        lineHeight = DesignSystem.Typography.caption1, // tight spacing
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Avatars + total spent
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(-8.dp)) {
                members.take(5).forEach { member ->
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(member.avatarColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (member.name.isNotBlank()) {
                                member.name.split(" ").mapNotNull { if (it.isNotBlank()) it.first() else null }.joinToString("")
                            } else {
                                "?"
                            },
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Total spent",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    lineHeight = DesignSystem.Typography.caption1,
                )
                Text(
                    text = "$${formatDouble(group.totalSpent)}",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = DesignSystem.Typography.caption1,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Status row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val currentUserBalance = members.firstOrNull()?.balance ?: 0.0
            val balanceText = if (currentUserBalance < 0) "You owe" else "You get back"
            val balanceAmount = if (currentUserBalance < 0) "-$${formatDouble(kotlin.math.abs(currentUserBalance))}" else "+$${formatDouble(currentUserBalance)}"
            
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (currentUserBalance < 0) Color.DarkGray else MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = balanceText,
                    color = if (currentUserBalance < 0) Color.White else MaterialTheme.colorScheme.onSurface,
                    fontSize = 12.sp
                )
            }
            Text(
                text = balanceAmount,
                color = if (currentUserBalance < 0) Color(0xFFDC2626) else Color(0xFF16A34A),
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

// Helper function to format date for group display
fun formatDateForGroup(timestamp: Long): String {
    // For now, return a simple format. We can enhance this later
    val currentTime = System.currentTimeMillis() / 1000L
    val daysDiff = ((currentTime - timestamp) / (24L * 60L * 60L)).toInt()
    
    return when {
        daysDiff == 0 -> "Today"
        daysDiff == 1 -> "Yesterday"
        daysDiff < 7 -> "${daysDiff} days ago"
        else -> "Older"
    }
}


@Composable
fun MemberItem(name: String, email: String = "", isAdmin: Boolean, onRemove: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(DesignSystem.CornerRadius.md))
            .border(
                width = 0.5.dp, // very thin border
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), // subtle white
                shape = RoundedCornerShape(DesignSystem.CornerRadius.md)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isAdmin) Color(0xFF181818) else Color.Black
        ),
        shape = RoundedCornerShape(DesignSystem.CornerRadius.md)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(Color(0xFF262626), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name.take(2).uppercase(),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        if (isAdmin) "You" else name,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        style = TextStyle(
                            lineHeight = 18.sp
                        )
                    )
                    Text(
                        if (isAdmin) "Group admin" else (if (email.isNotEmpty()) email else "${name.lowercase().replace(" ", ".")}@example.com"),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        style = TextStyle(
                            lineHeight = 16.sp
                        )
                    )
                }
            }
            if (!isAdmin) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Remove member",
                    tint = Color.Red.copy(alpha = 0.5f),
                    modifier = Modifier.clickable { onRemove() }
                )
            }
        }
    }
}




@Composable
fun SuggestedContactItem(name: String, onAdd: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clip(RoundedCornerShape(DesignSystem.CornerRadius.md))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.3f),
                shape = RoundedCornerShape(DesignSystem.CornerRadius.md)
            ),
        shape = RoundedCornerShape(DesignSystem.CornerRadius.md),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black
        )
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(Color(0xFF262626), CircleShape),
                            contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name.split(" ").map { it.first() }.joinToString(""),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = name,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Normal,
                        style = TextStyle(
                            lineHeight = 18.sp // 👈 reduced line height
                        )
                    )
                    Text(
                        text = "${name.lowercase().replace(" ", ".")}@example.com",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        style = TextStyle(
                            lineHeight = 18.sp // 👈 match line height for tighter spacing
                        )
                    )
                }

            }
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add contact",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .size(16.dp)
                    .clickable { onAdd() }
            )
        }
    }
}




/**
 * Full-screen Create Group page
 */
@OptIn(ExperimentalMaterial3Api::class, kotlin.time.ExperimentalTime::class)
@Composable
fun CreateGroupScreen(
    onBack: () -> Unit,
    groupDatabaseManager: SQLiteGroupDatabase
) {
    var groupName by remember { mutableStateOf("") }
    var memberName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    // Members list: "You" is always present
    val members = remember { mutableStateListOf("You") }
    val memberEmails = remember { mutableStateOf(mutableMapOf<String, String>()) }

    val isButtonEnabled = groupName.isNotBlank() && members.size > 1

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Create Group",
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
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Group Name Section
            Text(
                text = "Group Name",
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            BasicTextField(
                value = groupName,
                onValueChange = { groupName = it },
                singleLine = true,
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 16.sp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(12.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = if (groupName.isNotEmpty()) 
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        else 
                            Color.White.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) { innerTextField ->
                if (groupName.isEmpty()) {
                    Text(
                        text = "Enter group name...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 16.sp
                    )
                }
                innerTextField()
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Members Section
            Text(
                text = "Members (${members.size})",
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Current Members
            members.forEach { member ->
                MemberItem(
                    name = member,
                    email = memberEmails.value[member] ?: "",
                    isAdmin = member == "You",
                    onRemove = { 
                        if (member != "You") {
                            members.remove(member)
                            memberEmails.value.remove(member)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Added Members Section (show in suggested contacts style)
            if (members.size > 1) {
                Text(
                    text = "Added Members",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                members.filter { it != "You" }.forEach { member ->
                    SuggestedContactItem(
                        name = member,
                        onAdd = { 
                            // This is just for display, no action needed
                        }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Add Member Section
            Text(
                text = "Add Member",
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            // Name Field
            BasicTextField(
                value = memberName,
                onValueChange = { memberName = it },
                singleLine = true,
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 16.sp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(12.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = if (memberName.isNotEmpty()) 
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        else 
                            Color.White.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) { innerTextField ->
                if (memberName.isEmpty()) {
                    Text(
                        text = "Enter member name...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 16.sp
                    )
                }
                innerTextField()
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Email Field
            BasicTextField(
                value = email,
                onValueChange = { email = it },
                singleLine = true,
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 16.sp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(12.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = if (email.isNotEmpty()) 
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        else 
                            Color.White.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) { innerTextField ->
                if (email.isEmpty()) {
                    Text(
                        text = "Enter email address...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 16.sp
                    )
                }
                innerTextField()
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Add Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            CircleShape
                        )
                        .clickable {
                            if (memberName.isNotBlank() && email.isNotBlank()) {
                                val memberDisplayName = memberName.trim()
                                if (!members.contains(memberDisplayName)) {
                                    members.add(memberDisplayName)
                                    memberEmails.value[memberDisplayName] = email.trim()
                                    memberName = ""
                                    email = ""
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add Member",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Suggested Contacts Section
            Text(
                text = "Suggested Contacts",
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            val contacts = listOf(
                "Sarah Miller" to "sarah.miller@example.com",
                "Mike Johnson" to "mike.johnson@example.com",
                "Lisa Wilson" to "lisa.wilson@example.com",
                "Alex Chen" to "alex.chen@example.com"
            )
            
            contacts.forEach { (name, email) ->
                if (!members.contains(name)) {
                    SuggestedContactItem(
                        name = name,
                        onAdd = { 
                            if (!members.contains(name)) {
                                members.add(name)
                                memberEmails.value[name] = email
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Create Group Button
            Button(
                onClick = {
                    // Create the group with members
                    val groupId = "group_${kotlin.time.Clock.System.now().toEpochMilliseconds()}"
                    val group = Group(
                        id = groupId,
                        name = groupName,
                        description = "",
                        color = Color(0xFF9333EA), // Default purple color
                        createdAt = kotlin.time.Clock.System.now().epochSeconds,
                        totalSpent = 0.0,
                        memberCount = members.size
                    )
                    
                    val groupMembers = members.mapIndexed { index, memberName ->
                        GroupMember(
                            id = "member_${groupId}_${index}",
                            groupId = groupId,
                            name = memberName,
                            email = if (memberName == "You") "" else (memberEmails.value[memberName] ?: ""),
                            phone = "",
                            avatarColor = Color(0xFF2196F3), // Default blue color
                            balance = 0.0,
                            totalPaid = 0.0,
                            totalOwed = 0.0
                        )
                    }
                    
                    // Save to database
                    kotlinx.coroutines.GlobalScope.launch {
                        groupDatabaseManager.addGroupWithMembers(group, groupMembers)
                    }
                    
                    onBack()
                },
                enabled = isButtonEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Create Group",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
