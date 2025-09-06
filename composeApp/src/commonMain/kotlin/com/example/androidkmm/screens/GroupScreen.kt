package com.example.androidkmm.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidkmm.design.DesignSystem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsScreen(
    totalOwed: String = "+$245.70",
    totalOwe: String = "-$23.50",
    groups: List<GroupItem> = sampleGroups()
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSheet by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        // Header row: Title + plus icon
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Groups",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Normal
            )
            Box(
                modifier = Modifier
                    .size(32.dp) // circle size
                    .background(Color.White, shape = CircleShape)
                    .clickable { showSheet = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add group",
                    tint = Color.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Top cards row: equal width




        // Groups list
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item{
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryCard(
                        amount = totalOwed,
                        label = "You are owed",
                        bgColor = Color(0xFFDCFCE7),
                        textColor = Color(0xFF16A34A),
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCard(
                        amount = totalOwe,
                        label = "You owe",
                        bgColor = Color(0xFFFEE2E2),
                        textColor = Color(0xFFDC2626),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item{
                Spacer(modifier = Modifier.height(20.dp))
            }
            items(groups) { group ->
                GroupCard(group)
            }
        }
        if (showSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSheet = false },
                sheetState = sheetState,
                containerColor = Color.Black,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                CreateGroupContent(onClose = { showSheet = false })
            }
        }

    }
}

@Composable
fun SummaryCard(
    amount: String,
    label: String,
    bgColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(vertical = 20.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = amount,
            color = textColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.Thin
        )
        Spacer(Modifier.height(DesignSystem.Spacing.sm))
        Text(
            text = label,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraLight
        )
    }
}

@Composable
fun GroupCard(group: GroupItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()

            .clip(RoundedCornerShape(DesignSystem.CornerRadius.md))
            .border(
                width = 0.5.dp, // very thin border
                color = Color.White.copy(alpha = 0.2f), // subtle white
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
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = group.name,
                        color = Color.White,
                        fontWeight = FontWeight.Normal,
                        fontStyle = FontStyle.Normal,
                        fontSize = DesignSystem.Typography.balanceLabel,
                        lineHeight = DesignSystem.Typography.cardTitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${group.membersCount} members • ${group.date}",
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
                group.members.take(5).forEach {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color.DarkGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = it,
                            color = Color.White,
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
                    text = "$${group.totalSpent}",
                    color = Color.White,
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
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.1f))
                    .padding(horizontal = 4.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (group.balance.startsWith("-")) "You owe" else "You get back",
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
            Text(
                text = group.balance,
                color = if (group.balance.startsWith("-")) Color(0xFFDC2626) else Color(0xFF16A34A),
                fontSize = 14.sp,
                fontWeight = FontWeight.Thin
            )
        }
    }
}

// Data model
data class GroupItem(
    val name: String,
    val membersCount: Int,
    val date: String,
    val members: List<String>,
    val totalSpent: String,
    val balance: String,
    val color: Color
)

// Sample
fun sampleGroups(): List<GroupItem> = listOf(
    GroupItem(
        name = "Vacation Trip",
        membersCount = 4,
        date = "Today",
        members = listOf("AB", "SM", "MJ", "LW"),
        totalSpent = "1247.50",
        balance = "+156.40",
        color = Color(0xFF9333EA) // Purple
    ),
    GroupItem(
        name = "Office Lunch",
        membersCount = 6,
        date = "Yesterday",
        members = listOf("AB", "JD", "EW", "DL", "+2"),
        totalSpent = "234.80",
        balance = "-23.50",
        color = Color(0xFFF97316) // Orange
    )
)

@Composable
fun CreateGroupContent(onClose: () -> Unit) {
    var groupName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    // Members list: "You" is always present
    val members = remember { mutableStateListOf("You") }

    val isButtonEnabled = groupName.isNotBlank() && members.size > 1

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()) // ✅ make sheet scrollable
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Create Group", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = Color.White,
                modifier = Modifier.clickable { onClose() }
            )
        }

        Spacer(Modifier.height(16.dp))

        // Group Name
        Text("Group Name", color = Color.White, fontWeight = FontWeight.Medium)
        OutlinedTextField(
            value = groupName,
            onValueChange = { groupName = it },
            placeholder = { Text("Enter group name...") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.Black,
                unfocusedContainerColor = Color.Black,
                focusedBorderColor = Color.Gray,
                unfocusedBorderColor = Color.Gray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedPlaceholderColor = Color.Gray,
                unfocusedPlaceholderColor = Color.Gray
            )
        )

        Spacer(Modifier.height(16.dp))

        // Members List
        Text("Members (${members.size})", color = Color.White, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))

        members.forEach { member ->
            MemberItem(
                name = member,
                isAdmin = member == "You",
                onRemove = { members.remove(member) }
            )
        }

        Spacer(Modifier.height(16.dp))

        // Add by Email
        Text("Add by Email", color = Color.White, fontWeight = FontWeight.Medium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("Enter email address...") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Black,
                    unfocusedContainerColor = Color.Black,
                    focusedBorderColor = Color.Gray,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedPlaceholderColor = Color.Gray,
                    unfocusedPlaceholderColor = Color.Gray
                )
            )
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.Gray, CircleShape)
                    .clickable {
                        if (email.isNotBlank()) {
                            members.add(email)
                            email = ""
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
            }
        }

        Spacer(Modifier.height(24.dp))

        // Suggested Contacts
        Text("Suggested Contacts", color = Color.White, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))

        val contacts = listOf("Sarah Miller", "Mike Johnson", "Lisa Wilson", "Alex Chen")
        contacts.forEach { name ->
            if (!members.contains(name)) {
                SuggestedContactItem(
                    name = name,
                    onAdd = { members.add(name) }
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // Create Group Button
        Button(
            onClick = { /* Create group */ },
            enabled = isButtonEnabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,   // background
                contentColor = Color.Black      // text/icon color
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Create Group")
        }
    }
}

@Composable
fun MemberItem(name: String, isAdmin: Boolean, onRemove: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.DarkGray),
        shape = RoundedCornerShape(12.dp)
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
                        .size(36.dp)
                        .background(Color.Gray, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name.take(2).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        if (isAdmin) "You" else name,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        if (isAdmin) "Group admin" else "${name.lowercase().replace(" ", ".")}@example.com",
                        color = Color.Gray
                    )
                }
            }
            if (!isAdmin) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove member",
                    tint = Color.Red,
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
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.DarkGray),
        shape = RoundedCornerShape(12.dp)
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
                        .size(36.dp)
                        .background(Color.Gray, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name.split(" ").map { it.first() }.joinToString(""),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(name, color = Color.White, fontWeight = FontWeight.Medium)
                    Text("${name.lowercase().replace(" ", ".")}@example.com", color = Color.Gray)
                }
            }
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add contact",
                tint = Color.White,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onAdd() }
            )
        }
    }
}


