package com.example.androidkmm.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidkmm.models.GroupMember
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.BorderStroke

enum class SplitType {
    EQUAL, PERCENTAGE, FIXED_AMOUNT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitSelectionScreen(
    members: List<GroupMember>,
    selectedMembers: Set<String>,
    onBack: () -> Unit,
    onMembersSelected: (Set<String>, SplitType, Map<String, Double>) -> Unit
) {
    var localSelectedMembers by remember { mutableStateOf(selectedMembers) }
    var splitType by remember { mutableStateOf(SplitType.EQUAL) }
    var customAmounts by remember { mutableStateOf(mutableMapOf<String, Double>()) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Split between",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = {
                        println("SplitSelectionScreen: Back button clicked")
                        onBack()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            },
            actions = {
                // Save Button - Prominent and always visible
                Button(
                    onClick = {
                        println("SplitSelectionScreen: TOP BAR SAVE BUTTON CLICKED!")
                        println("Selected members: $localSelectedMembers")
                        println("Split type: $splitType")
                        println("Custom amounts: $customAmounts")
                        println("Is valid split: ${isValidSplit(splitType, customAmounts, localSelectedMembers)}")
                        
                        if (localSelectedMembers.isNotEmpty() && isValidSplit(splitType, customAmounts, localSelectedMembers)) {
                            println("SplitSelectionScreen: Calling onMembersSelected callback")
                            onMembersSelected(localSelectedMembers, splitType, customAmounts)
                        } else {
                            println("SplitSelectionScreen: Save button clicked but validation failed")
                        }
                    },
                    enabled = localSelectedMembers.isNotEmpty() && isValidSplit(splitType, customAmounts, localSelectedMembers),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.height(36.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Icon(
                        Icons.Default.Check, 
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "SAVE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(Modifier.width(8.dp))
                
                // Select All / Deselect All button
                TextButton(
                    onClick = {
                        localSelectedMembers = if (localSelectedMembers.size == members.size) {
                            emptySet()
                        } else {
                            members.map { it.id }.toSet()
                        }
                    }
                ) {
                    Text(
                        text = if (localSelectedMembers.size == members.size) "Deselect All" else "Select All",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        // Summary Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF0FDF4)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "${localSelectedMembers.size} of ${members.size} members selected",
                    fontSize = 14.sp,
                    color = Color(0xFF10B981),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Split Type Selection
        if (localSelectedMembers.size > 1) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "How to split?",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(12.dp))
                    
                    // Split Type Options
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SplitTypeButton(
                            text = "Equal",
                            isSelected = splitType == SplitType.EQUAL,
                            onClick = { splitType = SplitType.EQUAL }
                        )
                        SplitTypeButton(
                            text = "Percentage",
                            isSelected = splitType == SplitType.PERCENTAGE,
                            onClick = { splitType = SplitType.PERCENTAGE }
                        )
                        SplitTypeButton(
                            text = "Fixed Amount",
                            isSelected = splitType == SplitType.FIXED_AMOUNT,
                            onClick = { splitType = SplitType.FIXED_AMOUNT }
                        )
                    }
                }
            }
        }

        // Content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(members) { member ->
                SplitSelectionItem(
                    member = member,
                    isSelected = localSelectedMembers.contains(member.id),
                    splitType = splitType,
                    customAmount = customAmounts[member.id] ?: 0.0,
                    onAmountChange = { amount ->
                        customAmounts[member.id] = amount
                    },
                    onClick = { 
                        localSelectedMembers = if (localSelectedMembers.contains(member.id)) {
                            localSelectedMembers - member.id
                        } else {
                            localSelectedMembers + member.id
                        }
                    }
                )
            }
            
            item { Spacer(Modifier.height(24.dp)) }
        }
        
        // Bottom spacing
        Spacer(modifier = Modifier.height(24.dp))
    }
}

private fun isValidSplit(splitType: SplitType, customAmounts: Map<String, Double>, selectedMembers: Set<String>): Boolean {
    return when (splitType) {
        SplitType.EQUAL -> true
        SplitType.PERCENTAGE -> {
            val totalPercentage = selectedMembers.sumOf { customAmounts[it] ?: 0.0 }
            kotlin.math.abs(totalPercentage - 100.0) < 0.01
        }
        SplitType.FIXED_AMOUNT -> {
            selectedMembers.all { customAmounts[it] != null && customAmounts[it]!! > 0 }
        }
    }
}

@Composable
private fun SplitTypeButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) Color(0xFF10B981) else Color(0xFFF3F4F6)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) Color.White else Color(0xFF6B7280)
        )
    }
}

@Composable
private fun SplitSelectionItem(
    member: GroupMember,
    isSelected: Boolean,
    splitType: SplitType,
    customAmount: Double,
    onAmountChange: (Double) -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                Color(0xFF10B981).copy(alpha = 0.1f) 
            else 
                MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Member Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(member.avatarColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = member.name.take(2).uppercase(),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(Modifier.width(16.dp))
            
            // Member Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = member.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (member.balance >= 0) 
                        "Balance: +$${String.format("%.2f", member.balance)}" 
                    else 
                        "Balance: -$${String.format("%.2f", kotlin.math.abs(member.balance))}",
                    fontSize = 14.sp,
                    color = if (member.balance >= 0) 
                        Color(0xFF10B981) 
                    else 
                        Color(0xFFEF4444)
                )
                
                // Custom Amount Input (only for selected members and non-equal splits)
                if (isSelected && splitType != SplitType.EQUAL) {
                    Spacer(Modifier.height(8.dp))
                    CustomAmountInput(
                        splitType = splitType,
                        amount = customAmount,
                        onAmountChange = onAmountChange
                    )
                }
            }
            
            // Selection Indicator
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .border(
                            width = 2.dp,
                            color = Color(0xFFE5E7EB),
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

@Composable
private fun CustomAmountInput(
    splitType: SplitType,
    amount: Double,
    onAmountChange: (Double) -> Unit
) {
    val amountText = remember(amount) { 
        if (amount == 0.0) "" else amount.toString() 
    }
    var text by remember { mutableStateOf(amountText) }
    
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = when (splitType) {
                SplitType.PERCENTAGE -> "%"
                SplitType.FIXED_AMOUNT -> "$"
                else -> ""
            },
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 4.dp)
        )
        
        BasicTextField(
            value = text,
            onValueChange = { newText ->
                text = newText
                val newAmount = newText.toDoubleOrNull() ?: 0.0
                onAmountChange(newAmount)
            },
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier
                .width(80.dp)
                .background(
                    Color(0xFFF3F4F6),
                    RoundedCornerShape(6.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) { innerTextField ->
            if (text.isEmpty()) {
                Text(
                    text = when (splitType) {
                        SplitType.PERCENTAGE -> "0"
                        SplitType.FIXED_AMOUNT -> "0.00"
                        else -> "0"
                    },
                    color = Color(0xFF9CA3AF),
                    fontSize = 14.sp
                )
            }
            innerTextField()
        }
    }
}
