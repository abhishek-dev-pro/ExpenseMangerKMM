package com.example.androidkmm.screens.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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
            .padding(horizontal = 0.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A1A)
        ),
        shape = RoundedCornerShape(16.dp),
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
                        .size(48.dp)
                        .background(
                            Color(0xFF6A4C93),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = getIconEmoji(goal.icon),
                        style = AppStyleDesignSystem.Typography.TITLE_2
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
                
                // Priority Badge
                PriorityBadge(priority = goal.priority)
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
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Bottom Section: Remaining, Status, Monthly
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Remaining Amount - left side
                Text(
                    text = "$currencySymbol${goal.remainingAmount} to go",
                    color = Color.Gray,
                    style = AppStyleDesignSystem.Typography.CALL_OUT
                )
                
                // Spacer to push right content to the right
                Spacer(modifier = Modifier.weight(1f))
                
            // Right side - Status and Monthly as blue text
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Status as blue text
                StatusBadge(status = goal.status)

                // Monthly Amount as blue text
                if (goal.isRecurring && goal.monthlyAmount != null) {
                    MonthlyBadge(amount = goal.monthlyAmount!!, currencySymbol = currencySymbol)
                }
            }
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
