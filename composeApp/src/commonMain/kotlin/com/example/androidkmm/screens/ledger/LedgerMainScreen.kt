package com.example.androidkmm.screens.ledger

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Filter
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidkmm.utils.formatDouble

// LedgerMainScreen.kt
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerMainScreen() {
    var selectedPerson by remember { mutableStateOf<LedgerPerson?>(null) }
    var showAddBottomSheet by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }

    val people = remember {
        listOf(
            LedgerPerson(
                id = "1",
                name = "Sarah Chen",
                avatarColor = LedgerTheme.avatarBlue,
                balance = -40.00,
                transactionCount = 1,
                lastTransactionDate = "Sep 6"
            ),
            LedgerPerson(
                id = "2",
                name = "Alex Johnson",
                avatarColor = LedgerTheme.avatarBlue,
                balance = 15.50,
                transactionCount = 2,
                lastTransactionDate = "Yesterday"
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LedgerTheme.backgroundColor)
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
                    text = "Ledger",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = LedgerTheme.textPrimary
                )
                Text(
                    text = "Track borrowed & lent money",
                    fontSize = 16.sp,
                    color = LedgerTheme.textSecondary
                )
            }

            IconButton(
                onClick = { showAddBottomSheet = true },
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        Color.White,
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = Color.Black,
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
            // To Receive Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(100.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF0F2419)
                ),
                shape = RoundedCornerShape(20.dp)
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
                            contentDescription = null,
                            tint = LedgerTheme.greenAmount,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "To Receive",
                            fontSize = 14.sp,
                            color = LedgerTheme.textSecondary
                        )
                    }

                    Column {
                        Text(
                            text = "$15.50",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = LedgerTheme.greenAmount
                        )
                        Text(
                            text = "1 person",
                            fontSize = 12.sp,
                            color = LedgerTheme.textSecondary
                        )
                    }
                }
            }

            // To Send Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(100.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2A1919)
                ),
                shape = RoundedCornerShape(20.dp)
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
                            contentDescription = null,
                            tint = LedgerTheme.redAmount,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "To Send",
                            fontSize = 14.sp,
                            color = LedgerTheme.textSecondary
                        )
                    }

                    Column {
                        Text(
                            text = "$40.00",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = LedgerTheme.redAmount
                        )
                        Text(
                            text = "1 person",
                            fontSize = 12.sp,
                            color = LedgerTheme.textSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Search Bar
        TextField(
            value = searchText,
            onValueChange = { searchText = it },
            placeholder = {
                Text(
                    text = "Search",
                    color = LedgerTheme.textSecondary
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = LedgerTheme.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
            },
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = LedgerTheme.searchBackground,
                focusedContainerColor = LedgerTheme.searchBackground,
                unfocusedTextColor = LedgerTheme.textPrimary,
                focusedTextColor = LedgerTheme.textPrimary,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Filter and People Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Filter,
                    contentDescription = "Filter",
                    tint = LedgerTheme.textSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "All People",
                    color = LedgerTheme.textPrimary,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = null,
                    tint = LedgerTheme.textSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // People Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "People",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = LedgerTheme.textPrimary
            )

            Box(
                modifier = Modifier
                    .background(
                        Color(0xFF1F1F1F),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "2 contacts",
                    fontSize = 12.sp,
                    color = LedgerTheme.textSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // People List
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 24.dp)
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
    
    // Show person detail screen when a person is selected
    selectedPerson?.let { person ->
        PersonLedgerDetailScreen(
            person = person,
            onBack = { selectedPerson = null },
            onAddTransaction = { showAddBottomSheet = true }
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
