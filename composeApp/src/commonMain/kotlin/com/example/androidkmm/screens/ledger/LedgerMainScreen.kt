package com.example.androidkmm.screens.ledger

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidkmm.utils.CurrencyUtils.formatDouble
import com.example.androidkmm.database.rememberSQLiteLedgerDatabase
import com.example.androidkmm.design.AppStyleDesignSystem
import androidx.compose.runtime.collectAsState
import kotlin.time.ExperimentalTime

// LedgerMainScreen.kt
@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun LedgerMainScreen(
    navigateToPerson: String? = null,
    navigateToTransaction: String? = null,
    onPersonNavigated: () -> Unit = {},
    onBottomSheetVisibilityChange: (Boolean) -> Unit = {}
) {
    val ledgerDatabaseManager = rememberSQLiteLedgerDatabase()
    val peopleState = ledgerDatabaseManager.getAllLedgerPersons().collectAsState(initial = emptyList<LedgerPerson>())
    val allPeople = peopleState.value
    
    var selectedPerson by remember { mutableStateOf<LedgerPerson?>(null) }
    var showAddBottomSheet by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    
    // Track when any sheet is visible to hide bottom navigation
    val isAnySheetVisible = showAddBottomSheet
    
    // Notify parent when sheet visibility changes
    LaunchedEffect(isAnySheetVisible) {
        onBottomSheetVisibilityChange(isAnySheetVisible)
    }
    
    // Handle navigation to specific person
    LaunchedEffect(navigateToPerson, allPeople) {
        if (navigateToPerson != null && allPeople.isNotEmpty()) {
            println("LedgerMainScreen - Looking for person: '$navigateToPerson'")
            println("LedgerMainScreen - Available people: ${allPeople.map { it.name }}")
            
            // Try exact match first
            var person = allPeople.find { it.name.equals(navigateToPerson, ignoreCase = true) }
            
            // If no exact match, try partial match
            if (person == null) {
                person = allPeople.find { 
                    it.name.contains(navigateToPerson, ignoreCase = true) || 
                    navigateToPerson.contains(it.name, ignoreCase = true)
                }
            }
            
            if (person != null) {
                println("LedgerMainScreen - Found person: '${person.name}', selecting...")
                selectedPerson = person
                onPersonNavigated() // Clear the navigation request
            } else {
                println("LedgerMainScreen - No person found matching: '$navigateToPerson'")
            }
        }
    }
    
    // Filter options
    val filterOptions = listOf(
        "All" to "All contacts",
        "Get Back" to "Money people will return to me",
        "Give Back" to "Money I have to return"
    )
    
    // Filter people based on search text and filter option
    val people = remember(allPeople, searchText, selectedFilter) {
        var filteredPeople = allPeople
        
        // Apply search filter
        if (searchText.isNotBlank()) {
            filteredPeople = filteredPeople.filter { person ->
                person.name.contains(searchText, ignoreCase = true)
            }
        }
        
        // Apply balance filter
        when (selectedFilter) {
            "Get Back" -> filteredPeople = filteredPeople.filter { it.balance < 0 } // You owe them (red/negative balance)
            "Give Back" -> filteredPeople = filteredPeople.filter { it.balance > 0 } // They owe you (green/positive balance)
            // "All" -> no additional filtering
        }
        
        filteredPeople
    }
    
    // Calculate real-time amounts
    val toReceiveAmount = remember(allPeople) {
        allPeople.filter { it.balance > 0 }.sumOf { it.balance }
    }
    
    val toSendAmount = remember(allPeople) {
        allPeople.filter { it.balance < 0 }.sumOf { kotlin.math.abs(it.balance) }
    }
    
    val toReceiveCount = remember(allPeople) {
        allPeople.count { it.balance > 0 }
    }
    
    val toSendCount = remember(allPeople) {
        allPeople.count { it.balance < 0 }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LedgerTheme.backgroundColor())
            .padding(horizontal = AppStyleDesignSystem.Padding.SCREEN_HORIZONTAL)
            .padding(top = AppStyleDesignSystem.Padding.SCREEN_VERTICAL),
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Ledger",
                    style = AppStyleDesignSystem.Typography.MAIN_PAGE_HEADING_TITLE
                )
                Text(
                    text = "Track borrowed & lent money",
                    style = AppStyleDesignSystem.Typography.MAIN_PAGE_SUBHEADING_TITLE
                )
            }

            IconButton(
                onClick = { showAddBottomSheet = true },
                modifier = Modifier
                    .size(AppStyleDesignSystem.Sizes.ICON_BUTTON_SIZE)
                    .background(
                        Color.White,
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = Color.Black,
                    modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON__SIZE)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        // Summary Cards
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // To Receive Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(120.dp) // Slightly increased height from 100dp to 120dp
                    .clip(RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM))
                    .border(
                        width = 0.5.dp, // very thin border
                        color = Color.White.copy(alpha = 0.2f), // subtle white
                        shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF0F2419) // Keep green background
                ),
                shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding( 16.dp),
                    verticalArrangement = Arrangement.Top
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = null,
                            tint = LedgerTheme.greenAmount,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "To Send",
                            fontSize = 14.sp,
                            color = LedgerTheme.textSecondary()
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Column {
                        Text(
                            text = formatDouble(toReceiveAmount),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = LedgerTheme.redAmount
                        )
                        Text(
                            text = if (toReceiveCount == 1) "1 person" else "$toReceiveCount people",
                            fontSize = 12.sp,
                            color = LedgerTheme.textSecondary()
                        )
                    }
                }
            }

            // To Send Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(120.dp) // Slightly increased height from 100dp to 120dp
                    .clip(RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM))
                    .border(
                        width = 0.5.dp, // very thin border
                        color = Color.White.copy(alpha = 0.2f), // subtle white
                        shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2A1919) // Keep red background
                ),
                shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Top
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = LedgerTheme.redAmount,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "To Receive",
                            fontSize = 14.sp,
                            color = LedgerTheme.textSecondary()
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Column {
                        Text(
                            text = formatDouble(toSendAmount),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = LedgerTheme.greenAmount
                        )
                        Text(
                            text = if (toSendCount == 1) "1 person" else "$toSendCount people",
                            fontSize = 12.sp,
                            color = LedgerTheme.textSecondary()
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Search Bar and Filter
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Search Field
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                placeholder = {
                    Text(
                        text = "Search people",
                        color = LedgerTheme.textSecondary(),
                        fontSize = 14.sp,
                        maxLines = 1,
                        modifier = Modifier.padding(vertical = 0.5.dp)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = LedgerTheme.textSecondary(),
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (searchText.isNotEmpty()) {
                        IconButton(onClick = { searchText = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = LedgerTheme.textSecondary(),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 14.sp,
                    color = LedgerTheme.textPrimary()
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = LedgerTheme.textPrimary(),
                    unfocusedTextColor = LedgerTheme.textPrimary(),
                    cursorColor = Color.Blue
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.weight(1f)
            )


            // Filter Dropdown
            var expanded by remember { mutableStateOf(false) }
            Box {
                OutlinedButton(
                    onClick = { expanded = true },
                    modifier = Modifier
                        .height(56.dp)
                        .width(100.dp)
                        .border(
                            width = AppStyleDesignSystem.Sizes.BORDER_NORMAL,
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = LedgerTheme.textPrimary()
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(0.dp, Color.Transparent)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = selectedFilter,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = LedgerTheme.textSecondary(),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                        .width(260.dp)
                ) {
                    filterOptions.forEach { (option, description) ->
                        DropdownMenuItem(
                            text = {
                                Column(
                                    modifier = Modifier.padding(vertical = 2.dp)
                                ) {
                                    Text(
                                        text = option,
                                        color = if (option == selectedFilter) LedgerTheme.greenAmount else LedgerTheme.textPrimary(),
                                        fontWeight = if (option == selectedFilter) FontWeight.Bold else FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = description,
                                        color = LedgerTheme.textSecondary(),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Normal
                                    )
                                }
                            },
                            onClick = {
                                selectedFilter = option
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Filter and People Section
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // People Header
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "People",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                fontStyle = FontStyle.Normal,
                color = LedgerTheme.textPrimary(),
                style = androidx.compose.ui.text.TextStyle(
                    fontStyle = FontStyle.Normal,
                    fontWeight = FontWeight.SemiBold
                )
            )

            Box(
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (people.size == 1) "1 contact" else "${people.size} contacts",
                    fontSize = 12.sp,
                    color = LedgerTheme.textSecondary()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // People List or Empty State
        if (people.isEmpty()) {
            // Check if this is a search with no results or truly empty
            val isSearching = searchText.isNotBlank() || selectedFilter != "All"
            
            if (isSearching) {
                // No results found for search/filter
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Search Icon
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(80.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // "No results found" text
                    Text(
                        text = "No results found",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // "Try adjusting your search" text
                    Text(
                        text = "Try adjusting your search or filter",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Add Ledger Entry Button
                    Button(
                        onClick = { showAddBottomSheet = true },
                        modifier = Modifier
                            .width(240.dp)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Add Ledger Entry",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )
                        }
                    }
                }
            } else {
                // Empty State (no people at all)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Receipt Icon
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(80.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // "No ledger entries yet" text
                    Text(
                        text = "No ledger entries yet",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // "No ledger entries found for this month" text
                    Text(
                        text = "No ledger entries found for this month",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Add Ledger Entry Button
                    Button(
                        onClick = { showAddBottomSheet = true },
                        modifier = Modifier
                            .width(240.dp)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Add Ledger Entry",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        } else {
            // People List
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(people) { person ->
                    PersonLedgerItem(
                        person = person,
                        onClick = { selectedPerson = person }
                    )
                    if (person != people.last()) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
    
    
    // Show person detail screen when a person is selected
    selectedPerson?.let { person ->
        println("LedgerMainScreen - Passing highlightTransactionId: '$navigateToTransaction' to PersonLedgerDetailScreen for person: '${person.name}'")
        println("LedgerMainScreen - navigateToPerson: '$navigateToPerson', navigateToTransaction: '$navigateToTransaction'")
        PersonLedgerDetailScreen(
            person = person,
            onBack = { selectedPerson = null },
            onAddTransaction = { showAddBottomSheet = true },
            highlightTransactionId = navigateToTransaction
        )
    }
    
    // Show add bottom sheet when needed
    if (showAddBottomSheet) {
        AddLedgerEntryBottomSheet(
            onDismiss = { showAddBottomSheet = false },
            person = selectedPerson
        )
    }
}
