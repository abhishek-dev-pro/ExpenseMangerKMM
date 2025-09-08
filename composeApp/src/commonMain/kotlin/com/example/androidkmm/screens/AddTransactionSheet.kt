package com.example.androidkmm.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidkmm.transactions.SelectCategoryOrPaymentBox
import com.example.androidkmm.transactions.TransactionDateTimePicker
import com.example.androidkmm.transactions.UploadReceiptCard

// ------------------ Data --------------------
data class Category(val name: String, val color: Color, val icon: ImageVector)
data class PaymentMethod(val name: String, val color: Color, val icon: ImageVector)


// ------------------ Main Transaction Sheet --------------------
@Composable
fun AddTransactionSheet(onClose: () -> Unit) {
    var showCategorySheet by remember { mutableStateOf(false) }
    var showCustomCategorySheet by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }

    var showPaymentSheet by remember { mutableStateOf(false) }
    var selectedPaymentMethod by remember { mutableStateOf<PaymentMethod?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()) // 👈 enables scrolling
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        SheetHeader(title = "Add Transaction", onClose = onClose)

        Spacer(Modifier.height(16.dp))
        TransactionTypeToggle()

        Spacer(Modifier.height(24.dp))
        TransactionAmountInputSection()

        Spacer(Modifier.height(24.dp))
        SectionLabel("Category")
        SelectCategoryOrPaymentBox(
            title = selectedCategory?.name ?: "Select Category",
            subtitle = "Choose from categories",
            icon = selectedCategory?.icon ?: Icons.Default.CreditCard,
            iconColor = selectedCategory?.color ?: Color.Gray,
            onClick = { showCategorySheet = true }
        )

        Spacer(Modifier.height(16.dp))
        SectionLabel("Payment method")
        SelectCategoryOrPaymentBox(
            title = selectedPaymentMethod?.name ?: "Select Payment Method",
            subtitle = "Choose payment mode",
            icon = selectedPaymentMethod?.icon ?: Icons.Default.CreditCard,
            iconColor = selectedPaymentMethod?.color ?: Color.Gray,
            onClick = { showPaymentSheet = true }
        )

        Spacer(Modifier.height(16.dp))
        TransactionDateTimePicker()

        Spacer(Modifier.height(16.dp))
        NoteInput()

        UploadReceiptCard(
            modifier = Modifier.padding(top = 16.dp),
            onClick = { println("Upload receipt clicked!") }
        )

        Spacer(Modifier.height(24.dp))
        SaveTransactionButton(onSave = onClose)
        Spacer(Modifier.height(20.dp))
    }

    // Category Sheet
    if (showCategorySheet) {
        SelectCategoryBottomSheet(
            onDismiss = { showCategorySheet = false },
            onCategorySelected = {
                selectedCategory = it
                showCategorySheet = false
            },
            onAddCustomCategory = {
                showCategorySheet = false
                showCustomCategorySheet = true
            }
        )
    }

    // Custom Category Sheet
    if (showCustomCategorySheet) {
        AddCustomCategoryBottomSheet(
            onDismiss = { showCustomCategorySheet = false },
            onCategoryAdded = {
                selectedCategory = it
                showCustomCategorySheet = false
            }
        )
    }

    // Payment Method Sheet
    if (showPaymentSheet) {
        SelectPaymentMethodBottomSheet(
            onDismiss = { showPaymentSheet = false },
            selectedPaymentMethod = selectedPaymentMethod,
            onPaymentSelected = {
                selectedPaymentMethod = it
                showPaymentSheet = false
            }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectPaymentMethodBottomSheet(
    onDismiss: () -> Unit,
    selectedPaymentMethod: PaymentMethod?,
    onPaymentSelected: (PaymentMethod) -> Unit
) {
    val paymentMethods = listOf(
        PaymentMethod("Credit Card", Color(0xFF2979FF), Icons.Default.CreditCard),
        PaymentMethod("Debit Card", Color(0xFF00C853), Icons.Default.CreditCard),
        PaymentMethod("UPI", Color(0xFFAA00FF), Icons.Default.PhoneAndroid),
        PaymentMethod("Cash", Color(0xFFFF6D00), Icons.Default.AttachMoney),
        PaymentMethod("Bank Transfer", Color(0xFF304FFE), Icons.Default.AccountBalance),
        PaymentMethod("Digital Wallet", Color(0xFFD500F9), Icons.Default.AccountBalanceWallet),
    )

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color.Black) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Select Payment Method", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            Spacer(Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                paymentMethods.forEach { method ->
                    PaymentMethodCard(
                        method = method,
                        isSelected = method.name == selectedPaymentMethod?.name,
                        onClick = { onPaymentSelected(method) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PaymentMethodCard(
    method: PaymentMethod,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF121212))
            .border(
                width = if (isSelected) 1.dp else 0.dp,
                color = if (isSelected) Color.White else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(16.dp),
        contentAlignment = Alignment.CenterStart
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
                        .clip(CircleShape)
                        .background(method.color),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(method.icon, contentDescription = method.name, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(12.dp))
                Text(method.name, color = Color.White, fontSize = 16.sp)
            }
            if (isSelected) {
                Icon(Icons.Default.Check, contentDescription = "Selected", tint = Color.White)
            }
        }
    }
}



// ------------------ Category Bottom Sheet --------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectCategoryBottomSheet(
    onDismiss: () -> Unit,
    onCategorySelected: (Category) -> Unit,
    onAddCustomCategory: () -> Unit
) {
    val categories = listOf(
        Category("Salary", Color(0xFF00C853), Icons.Default.CreditCard),
        Category("Gift", Color(0xFFAA00FF), Icons.Default.CreditCard),
        Category("Business", Color(0xFF2962FF), Icons.Default.CreditCard),
        Category("Investment", Color(0xFF651FFF), Icons.Default.CreditCard),
        Category("Rental Income", Color(0xFF00C853), Icons.Default.CreditCard),
        Category("Bonus", Color(0xFFFFAB00), Icons.Default.CreditCard),
        Category("Side Hustle", Color(0xFFD500F9), Icons.Default.CreditCard),
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.Black
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Select Income Category", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Category Grid (2 per row)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                categories.chunked(2).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        row.forEach { category ->
                            CategoryCard(
                                name = category.name,
                                color = category.color,
                                icon = category.icon,
                                onClick = { onCategorySelected(category) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Add Custom Category
            AddCustomBox(label = "Add Custom Category", onClick = onAddCustomCategory)
        }
    }
}

// ------------------ Payment Bottom Sheet --------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectPaymentBottomSheet(
    onDismiss: () -> Unit,
    onPaymentSelected: (String) -> Unit,
    onAddCustomPayment: () -> Unit
) {
    val methods = listOf("UPI", "Cash", "Bank Transfer", "Digital Wallet", "Savings Account")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.Black
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Select Payment Method", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            Spacer(Modifier.height(16.dp))

            methods.forEach { method ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPaymentSelected(method) }
                        .padding(vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(method, color = Color.White, fontSize = 16.sp)
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
                }
            }

            Spacer(Modifier.height(20.dp))

            AddCustomBox(label = "Add Custom Payment Method", onClick = onAddCustomPayment)
        }
    }
}

// ------------------ Custom Category Bottom Sheet --------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustomCategoryBottomSheet(
    onDismiss: () -> Unit,
    onCategoryAdded: (Category) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(Color(0xFF2962FF)) }

    val availableColors = listOf(
        Color.Red, Color(0xFFFF6D00), Color(0xFFFFAB00), Color(0xFF00C853),
        Color(0xFF00B8D4), Color(0xFF2962FF), Color(0xFF651FFF),
        Color(0xFFD500F9), Color(0xFFAA00FF), Color(0xFF69F0AE)
    )

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color.Black) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Add Custom Category", fontSize = 18.sp, color = Color.White)
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Category Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))
            Text("Choose Color", color = Color.White)
            Spacer(Modifier.height(8.dp))

            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                availableColors.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                2.dp,
                                if (color == selectedColor) Color.White else Color.Transparent,
                                CircleShape
                            )
                            .clickable { selectedColor = color }
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onCategoryAdded(Category(name, selectedColor, icon = Icons.Default.CreditCard))
                        onDismiss()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
            ) {
                Text("Add Category", color = Color.White)
            }
        }
    }
}

// ------------------ Custom Payment Bottom Sheet --------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustomPaymentBottomSheet(
    onDismiss: () -> Unit,
    onPaymentAdded: (String) -> Unit
) {
    var customMethod by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.Black
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Add Custom Payment Method", fontSize = 18.sp, color = Color.White)
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = customMethod,
                onValueChange = { customMethod = it },
                label = { Text("Payment Method") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    if (customMethod.isNotBlank()) {
                        onPaymentAdded(customMethod)
                        onDismiss()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
            ) {
                Text("Add Payment Method", color = Color.White)
            }
        }
    }
}

// ------------------ Shared UI Components --------------------
@Composable
private fun AddCustomBox(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = Color.Gray,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.DarkGray),
                contentAlignment = Alignment.Center
            ) {
                Text("+", color = Color.White, fontSize = 20.sp)
            }
            Spacer(Modifier.width(12.dp))
            Text(label, color = Color.White, fontSize = 16.sp)
        }
    }
}

@Composable
private fun CategoryCard(
    name: String,
    color: Color,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, Color.Gray.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = name, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Text(name, color = Color.White, fontSize = 16.sp)
        }
    }
}

@Composable
private fun SheetHeader(title: String, onClose: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        IconButton(onClick = onClose) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
        }
    }
}

@Composable
private fun TransactionTypeToggle() {
    var selectedTab by remember { mutableStateOf("Expense") }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1C1C1C))
            .padding(4.dp)
    ) {
        listOf("Expense", "Income").forEach { tab ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selectedTab == tab) Color.DarkGray else Color.Transparent)
                    .clickable { selectedTab = tab }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(tab, color = Color.White, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun SectionLabel(text: String) {
    Text(text, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Normal)
    Spacer(Modifier.height(4.dp))
}

@Composable
private fun NoteInput() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1C1C1C))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text("Add a note...", color = Color.Gray, fontSize = 14.sp)
    }
}

@Composable
private fun SaveTransactionButton(onSave: () -> Unit) {
    Button(
        onClick = onSave,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C1C1C)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        Text("✓ Save Transaction", color = Color.White, fontSize = 16.sp)
    }
}

@Composable
fun TransactionAmountInputSection() {
    var amount by remember { mutableStateOf("") }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        val textColor = if (amount.isEmpty()) Color.LightGray else Color.White

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .weight(1f)
                .wrapContentWidth(Alignment.Start)
        ) {
            Column {
                Row {
                    Text(
                        text = "$",
                        color = textColor,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Medium
                    )
                    BasicTextField(
                        value = amount,
                        onValueChange = { input ->
                            if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                                amount = input
                            }
                        },
                        textStyle = TextStyle(
                            color = textColor,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Start
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(Color.White),
                        decorationBox = { innerTextField ->
                            if (amount.isEmpty()) {
                                Text(
                                    text = "0",
                                    color = Color.LightGray,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            innerTextField()
                        },
                        modifier = Modifier.widthIn(min = 40.dp)
                    )
                }
                Text("Enter amount", color = Color.Gray, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}
