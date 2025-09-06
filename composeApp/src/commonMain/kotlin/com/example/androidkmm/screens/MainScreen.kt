// MainScreen.kt

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidkmm.design.DesignSystem

@Composable
fun MainScreen() {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = { BottomNavigationBar(selectedTab) { selectedTab = it } },
        containerColor = Color.Black
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selectedTab) {
                0 -> HomeScreenContent()
                1 -> PlaceholderScreen("Transactions")
                2 -> PlaceholderScreen("Groups")
                3 -> PlaceholderScreen("Insights")
                4 -> PlaceholderScreen("Profile")
            }
        }
    }
}

@Composable
private fun HomeScreenContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = DesignSystem.Spacing.safeAreaPadding)
    ) {
        Spacer(Modifier.height(DesignSystem.Spacing.lg))
        GreetingSection()
        Spacer(Modifier.height(DesignSystem.Spacing.lg))
        BalanceCard()
        Spacer(Modifier.height(DesignSystem.Spacing.lg))
        QuickActions()
        Spacer(Modifier.height(DesignSystem.Spacing.lg))
        ProgressCard()
        Spacer(Modifier.height(DesignSystem.Spacing.sectionSpacing))
        UpcomingBills()
        Spacer(Modifier.height(DesignSystem.Spacing.sectionSpacing))
        GroupHighlights()
        Spacer(Modifier.height(DesignSystem.Spacing.bottomNavHeight)) // bottom padding for navigation
    }
}

@Composable
private fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title, 
            color = Color.White, 
            fontSize = DesignSystem.Typography.title2,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun GreetingSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f) // Take available space
        ) {
            Text(
                text = "Hi Abhishek 👋",
                color = Color.White, 
                fontSize = DesignSystem.Typography.title3,
                fontWeight = FontWeight.Normal,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Text(
                text = "Good morning", 
                color = Color.Gray, 
                fontSize = DesignSystem.Typography.footnote,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
        Icon(
            imageVector = Icons.Outlined.Notifications, 
            contentDescription = null, 
            tint = Color.White, 
            modifier = Modifier.size(DesignSystem.IconSize.lg)
        )
    }
}

@Composable
private fun BalanceCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(
                brush = Brush.linearGradient(
                    listOf(Color(0xFF4C2EFF), Color(0xFF9F3DFF))
                ),
                shape = RoundedCornerShape(DesignSystem.CornerRadius.xl)
            )
            .padding(DesignSystem.Spacing.cardPadding)
    ) {
        Column {
            Text(
                text = "Total Balance", 
                color = Color.White.copy(alpha = 0.8f), 
                fontSize = DesignSystem.Typography.balanceLabel,
                fontWeight = FontWeight.Normal
            )
            Spacer(Modifier.height(DesignSystem.Spacing.xl))
            Text(
                text = "$2,847.5", 
                color = Color.White, 
                fontSize = DesignSystem.Typography.balanceAmount, 
                fontWeight = FontWeight.ExtraLight
            )
            Spacer(Modifier.height(DesignSystem.Spacing.xs))
            Text(
                text = "+\$234.8 this month", 
                color = Color(0xFF9FFFA5), 
                fontSize = DesignSystem.Typography.balanceSubtext
            )
        }
        Row(
            modifier = Modifier.align(Alignment.TopEnd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.VisibilityOff, 
                contentDescription = null, 
                tint = Color.White, 
                modifier = Modifier.size(DesignSystem.IconSize.md)
            )
        }
    }
}

@Composable
private fun QuickActions() {
    Column {
        Text(
            "Quick Actions",
            color = Color.White,
            fontSize = DesignSystem.Typography.footnote,
            fontWeight = FontWeight.ExtraLight
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.sm),
            modifier = Modifier.fillMaxWidth()
        ) {
            QuickActionCard(
                text = "Expense",
                color = Color.Red,
                icon = Icons.Default.Remove,
                modifier = Modifier.weight(1f)
            )
            QuickActionCard(
                text = "Income",
                color = Color.Green,
                icon = Icons.Default.Add,
                modifier = Modifier.weight(1f)
            )
            QuickActionCard(
                text = "Group",
                color = Color(0xFF9F3DFF),
                icon = Icons.Outlined.Group,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuickActionCard(
    text: String,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = modifier
            .padding(top = 4.dp)
            .clip(RoundedCornerShape(DesignSystem.CornerRadius.md))
            .border(
                width = 0.5.dp, // very thin border
                color = Color.White.copy(alpha = 0.2f), // subtle white
                shape = RoundedCornerShape(DesignSystem.CornerRadius.md)
            )
            .clickable(onClick = onClick),
        color = Color(0xFF1A1A1A), // slightly lighter black
        tonalElevation = 2.dp,
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 4.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(DesignSystem.IconSize.sm)
            )
            Spacer(Modifier.width(DesignSystem.Spacing.xs))
            Text(
                text = text,
                fontSize = DesignSystem.Typography.buttonText,
                color = color,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}





@Composable
private fun ProgressCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFDAFFF2), shape = RoundedCornerShape(DesignSystem.CornerRadius.md))
            .padding(DesignSystem.Spacing.cardPadding)
    ) {
        Column {
            Text(
                text = "Great progress! 🎉", 
                color = Color.Black, 
                fontWeight = FontWeight.Bold, 
                fontSize = DesignSystem.Typography.cardTitle,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(DesignSystem.Spacing.xs))
            Text(
                text = "You spent 20% less on Food this month", 
                color = Color.DarkGray, 
                fontSize = DesignSystem.Typography.cardSubtitle,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun UpcomingBills() {
    SectionHeader("Upcoming Bills")
    Spacer(Modifier.height(DesignSystem.Spacing.sm))
    val bills = listOf(
        Triple("Netflix", "Today", "-\$15.99"),
        Triple("Spotify", "Tomorrow", "-\$9.99"),
        Triple("Internet", "Sep 8", "-\$79.99"),
        Triple("Electricity", "Sep 10", "-\$45.50"),
        Triple("Gym Membership", "Sep 12", "-\$25.00"),
        Triple("Amazon Prime", "Sep 15", "-\$12.99"),
    )
    bills.forEachIndexed { index, (title, subtitle, amount) ->
        val color = when (index % 4) {
            0 -> Color.Red
            1 -> Color.Green
            2 -> Color.Blue
            else -> Color.Magenta
        }
        BillItem(title, subtitle, amount, color)
        Spacer(Modifier.height(DesignSystem.Spacing.sm))

    }
}

@Composable
fun BillItem(
    title: String,
    subtitle: String,
    amount: String,
    color: Color
) {
    Surface(
        modifier = Modifier
            .padding(top = 4.dp)
            .clip(RoundedCornerShape(DesignSystem.CornerRadius.md))
            .border(
                width = 0.5.dp, // very thin border
                color = Color.White.copy(alpha = 0.2f), // subtle white
                shape = RoundedCornerShape(DesignSystem.CornerRadius.md)
            ),
        color = Color(0xFF1A1A1A), // slightly lighter black background
        tonalElevation = 2.dp,
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = DesignSystem.Spacing.md,
                    vertical = DesignSystem.Spacing.sm
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side (icon + texts)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f).padding(vertical = 4.dp)
            ) {
                // Colored circular icon background
                Box(
                    modifier = Modifier
                        .size(DesignSystem.IconSize.avatar)
                        .background(color, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(DesignSystem.IconSize.md)
                    )
                }

                Spacer(Modifier.width(DesignSystem.Spacing.md))


                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontWeight = FontWeight.Normal,
                        fontStyle = FontStyle.Normal,
                        fontSize = DesignSystem.Typography.balanceLabel,
                        lineHeight = DesignSystem.Typography.cardTitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitle,
                        color = Color.Gray,
                        fontStyle = FontStyle.Normal,
                        fontSize = DesignSystem.Typography.caption1,
                        lineHeight = DesignSystem.Typography.caption1, // tight spacing
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Right side (amount)
            Text(
                text = amount,
                color = Color.Red,
                fontSize = DesignSystem.Typography.balanceLabel,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}


@Composable
private fun GroupHighlights() {
    SectionHeader("Group Highlights")
    Spacer(Modifier.height(DesignSystem.Spacing.sm))
    val groups = listOf(
        GroupData("Vacation Trip", "+\$156.40", "You get", Color(0xFF9F3DFF), true, "4 members"),
        GroupData("Office Lunch", "\$23.50", "You owe", Color(0xFFFF6A00), false, "6 members"),
        GroupData("Birthday Party", "+\$75.20", "You get", Color(0xFF00BCD4), true, "5 members"),
        GroupData("Movie Night", "\$12.00", "You owe", Color(0xFF4CAF50), false, "3 members"),
        GroupData("Road Trip", "+\$220.00", "You get", Color(0xFF673AB7), true, "7 members"),
    )
    groups.forEach { g ->
        GroupItem(g.title, g.amount, g.chip, g.color, g.positive, g.members)
    }
}

data class GroupData(
    val title: String,
    val amount: String,
    val chip: String,
    val color: Color,
    val positive: Boolean,
    val members: String
)

@Composable
private fun GroupItem(
    title: String,
    amount: String,
    chip: String,
    color: Color,
    positive: Boolean,
    members: String
) {
    Surface(
        modifier = Modifier
            .padding(top = 4.dp)
            .clip(RoundedCornerShape(DesignSystem.CornerRadius.md))
            .border(
                width = 0.5.dp, // very thin border
                color = Color.White.copy(alpha = 0.2f), // subtle white border
                shape = RoundedCornerShape(DesignSystem.CornerRadius.md)
            ),
        color = Color(0xFF1A1A1A), // slightly lighter black (like BillItem)
        tonalElevation = 2.dp,
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = DesignSystem.Spacing.md,
                    vertical = DesignSystem.Spacing.md
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side (icon + text)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f).padding(vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(DesignSystem.IconSize.avatar)
                        .background(color, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(DesignSystem.IconSize.md)
                    )
                }
                Spacer(Modifier.width(DesignSystem.Spacing.md))
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontStyle = FontStyle.Normal,
                        fontSize = DesignSystem.Typography.cardTitle,
                        lineHeight = DesignSystem.Typography.cardTitle, // tight spacing
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = members,
                        color = Color.Gray,
                        fontStyle = FontStyle.Normal,
                        fontSize = DesignSystem.Typography.caption1,
                        lineHeight = DesignSystem.Typography.caption1, // tight spacing
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Right side (amount + chip)
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = amount,
                    color = if (positive) Color.Green else Color.Red,
                    fontSize = DesignSystem.Typography.balanceLabel,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = DesignSystem.Typography.cardTitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(DesignSystem.Spacing.xs))
                Box(
                    modifier = Modifier
                        .background(
                            if (positive) Color.Gray else Color.White,
                            shape = RoundedCornerShape(DesignSystem.CornerRadius.sm)
                        )
                        .padding(
                            horizontal = DesignSystem.Spacing.xs,
                        )
                ) {
                    Text(
                        text = chip,
                        color = if (positive) Color.White else Color.Black,
                        fontSize = DesignSystem.Typography.caption1,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            color = Color.White,
            fontWeight = FontWeight.Normal,
            fontStyle = FontStyle.Normal,
            fontSize = DesignSystem.Typography.balanceLabel,
            lineHeight = DesignSystem.Typography.cardTitle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "View all >",
            color = Color.White,
            fontSize = DesignSystem.Typography.caption2,
            fontWeight = FontWeight.ExtraLight,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun BottomNavigationBar(selected: Int, onSelect: (Int) -> Unit) {
    NavigationBar(containerColor = Color.Black) {
        val items = listOf(
            "Pal" to Icons.Default.Home,
            "Transactions" to Icons.Default.AttachMoney,
            "Groups" to Icons.Default.Group,
            "Insights" to Icons.Default.ShowChart,
            "Profile" to Icons.Default.Person
        )
        items.forEachIndexed { index, (label, icon) ->
            NavigationBarItem(
                selected = selected == index,
                onClick = { onSelect(index) },
                icon = { 
                    Icon(
                        imageVector = icon, 
                        contentDescription = label, 
                        modifier = Modifier.size(DesignSystem.IconSize.lg)
                    ) 
                },
                label = { 
                    Text(
                        text = label, 
                        fontSize = DesignSystem.Typography.tabLabel,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    ) 
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = Color.White,
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}
