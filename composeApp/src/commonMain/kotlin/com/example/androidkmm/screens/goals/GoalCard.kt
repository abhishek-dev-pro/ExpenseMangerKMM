package com.example.androidkmm.screens.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidkmm.models.Goal
import com.example.androidkmm.utils.DateFormatUtils
import com.example.androidkmm.design.AppStyleDesignSystem

@Composable
fun GoalCard(
    goal: Goal,
    currencySymbol: String = "$",
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp, vertical = 8.dp)
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 0.dp, bottomEnd = 0.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black
        ),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 0.dp, bottomEnd = 0.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Top Section: Icon, Title, Priority
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Goal Icon
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            Color(0xFF6A4C93),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = getIconEmoji(goal.icon),
                        style = AppStyleDesignSystem.Typography.BODY
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Title and Description - takes remaining space
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = goal.title,
                        color = Color.White,
                        style = AppStyleDesignSystem.Typography.HEADLINE
                    )
                    if (!goal.description.isNullOrEmpty()) {
                        Text(
                            text = goal.description,
                            color = Color.Gray,
                            style = AppStyleDesignSystem.Typography.CALL_OUT,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Status Badge
                StatusBadge(status = goal.status)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Middle Section: Progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side - Current and Target amounts grouped together
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Current Amount - large and bold
                    Text(
                        text = "$currencySymbol${goal.currentAmount}",
                        color = Color.White,
                        style = AppStyleDesignSystem.Typography.TITLE_2
                    )
                    
                    // Target Amount - smaller and gray
                    Text(
                        text = " of $currencySymbol${goal.targetAmount}",
                        color = Color.Gray,
                        style = AppStyleDesignSystem.Typography.BODY
                    )
                }
                
                // Right side - Percentage
                Text(
                    text = "${goal.progressPercentage}%",
                    color = Color.White,
                    style = AppStyleDesignSystem.Typography.TITLE_2
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Progress Bar
            LinearProgressIndicator(
                progress = goal.progressPercentage / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = Color(0xFF6A4C93), // Blue color to match goal cards
                trackColor = Color(0xFF2A2A2A)
            )
            
            
        }
    }
}

@Composable
fun GoalActionBar(
    onAddClick: () -> Unit,
    onWithdrawClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp, vertical = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2A2A2A)
        ),
        shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp, topStart = 0.dp, topEnd = 0.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Add Button
            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
            ) {
                Text(
                    text = "Add",
                    color = Color.White,
                    style = AppStyleDesignSystem.Typography.FOOTNOTE
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Withdraw Button
            Button(
                onClick = onWithdrawClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF9800)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
            ) {
                Text(
                    text = "Withdraw",
                    color = Color.White,
                    style = AppStyleDesignSystem.Typography.FOOTNOTE
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Delete Icon
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier
                    .height(40.dp)
                    .width(40.dp)
                    .background(
                        Color(0xFFD32F2F),
                        RoundedCornerShape(8.dp)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Goal",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun PriorityBadge(priority: String) {
    val dotColor = when (priority.lowercase()) {
        "high" -> Color(0xFFD32F2F)
        "medium" -> Color(0xFFFF9800)
        "low" -> Color(0xFF4CAF50)
        else -> Color(0xFFFF9800)
    }
    
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(dotColor, CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = priority,
            color = Color.Gray,
            style = AppStyleDesignSystem.Typography.FOOTNOTE
        )
    }
}

@Composable
private fun StatusBadge(status: String) {
    Text(
        text = status,
        color = Color(0xFF6A4C93), // Blue color to match goal cards
        style = AppStyleDesignSystem.Typography.FOOTNOTE
    )
}

@Composable
private fun MonthlyBadge(amount: Double, currencySymbol: String = "$") {
    Text(
        text = "$currencySymbol${amount.toInt()}/mo",
        color = Color(0xFF6A4C93), // Blue color to match goal cards
        style = AppStyleDesignSystem.Typography.FOOTNOTE
    )
}

private fun getIconEmoji(icon: String): String {
    return when (icon.lowercase()) {
        "target" -> "🎯"
        "dollar" -> "💰"
        "house" -> "🏠"
        "car" -> "🚗"
        "plane" -> "✈️"
        "heart" -> "❤️"
        "star" -> "⭐"
        "book" -> "📚"
        "gift" -> "🎁"
        "phone" -> "📱"
        "laptop" -> "💻"
        "camera" -> "📷"
        "game" -> "🎮"
        "music" -> "🎵"
        "sport" -> "⚽"
        "travel" -> "🧳"
        "education" -> "🎓"
        "health" -> "🏥"
        "shopping" -> "🛍️"
        "food" -> "🍕"
        else -> "🎯"
    }
}
