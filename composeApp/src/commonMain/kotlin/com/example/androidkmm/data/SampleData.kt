package com.example.androidkmm.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Group
import androidx.compose.ui.graphics.Color

/**
 * Sample data for the finance app
 */
object SampleData {

    val bills = listOf(
        BillData("Netflix", "Today", "-$15.99", Color.Red),
        BillData("Spotify", "Tomorrow", "-$9.99", Color.Green),
        BillData("Internet", "Sep 8", "-$79.99", Color.Blue),
        BillData("Electricity", "Sep 10", "-$45.50", Color.Magenta),
        BillData("Gym Membership", "Sep 12", "-$25.00", Color.Red),
        BillData("Amazon Prime", "Sep 15", "-$12.99", Color.Green),
    )

    val groups = listOf(
        GroupData("Vacation Trip", "+$156.40", "You get", Color(0xFF9F3DFF), true, "4 members"),
        GroupData("Office Lunch", "$23.50", "You owe", Color(0xFFFF6A00), false, "6 members"),
        GroupData("Birthday Party", "+$75.20", "You get", Color(0xFF00BCD4), true, "5 members"),
        GroupData("Movie Night", "$12.00", "You owe", Color(0xFF4CAF50), false, "3 members"),
        GroupData("Road Trip", "+$220.00", "You get", Color(0xFF673AB7), true, "7 members"),
    )

    val quickActions = listOf(
        QuickActionData("Expense", Color.Red, Icons.Default.Remove),
        QuickActionData("Income", Color.Green, Icons.Default.Add),
        QuickActionData("Group", Color(0xFF9F3DFF), Icons.Outlined.Group),
    )

    val navigationItems = listOf(
        NavigationItem("Pal", Icons.Default.Home),
        NavigationItem("Transactions", Icons.Default.AttachMoney),
        NavigationItem("Groups", Icons.Default.Group),
        NavigationItem("Insights", Icons.Default.ShowChart),
        NavigationItem("Profile", Icons.Default.Person)
    )
}
