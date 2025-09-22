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
import com.example.androidkmm.design.iOSStyleDesignSystem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayerSelectionScreen(
    members: List<GroupMember>,
    selectedPayer: GroupMember?,
    onBack: () -> Unit,
    onPayerSelected: (GroupMember) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Who paid?",
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(Modifier.height(24.dp)) }
            
            items(members) { member ->
                PayerSelectionItem(
                    member = member,
                    isSelected = selectedPayer?.id == member.id,
                    onClick = { onPayerSelected(member) }
                )
            }
            
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun PayerSelectionItem(
    member: GroupMember,
    isSelected: Boolean,
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
                            width = iOSStyleDesignSystem.Sizes.BORDER_THICK,
                            color = Color(0xFFE5E7EB),
                            shape = CircleShape
                        )
                )
            }
        }
    }
}
