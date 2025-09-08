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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidkmm.transactions.SelectCategoryOrPaymentBox
import com.example.androidkmm.transactions.Transaction

@Composable
fun TransactionDetailSheet(
    transaction: Transaction,
    onDismiss: () -> Unit,
    onSave: (Transaction) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }

    // Editable state
    var editedAmount by remember { mutableStateOf(transaction.amount.toString()) }
    var editedCategory by remember { mutableStateOf(transaction.category) }
    var editedMethod by remember { mutableStateOf(transaction.method) }
    var editedDate by remember { mutableStateOf(transaction.date) }
    var editedDescription by remember { mutableStateOf(transaction.subtitle) }

    // Bottom sheet state
    var showCategorySheet by remember { mutableStateOf(false) }
    var showCustomCategorySheet by remember { mutableStateOf(false) }
    var showPaymentSheet by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        TransactionDetailHeader(
            isEditing = isEditing,
            onEditToggle = { isEditing = !isEditing },
            onClose = onDismiss
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (!isEditing) {
            // ---------------- VIEW MODE ----------------
            TransactionDetailRow(
                title = "Amount",
                subtitle = "₹${transaction.amount}"
            )
            Spacer(modifier = Modifier.height(16.dp))

            TransactionDetailRow(title = "Category", subtitle = transaction.category)
            Spacer(modifier = Modifier.height(16.dp))
            TransactionDetailRow(title = "Payment Method", subtitle = transaction.method)
            Spacer(modifier = Modifier.height(16.dp))

            TransactionDetailRow(title = "Date", subtitle = transaction.date.toString())
            if (transaction.subtitle.isNotBlank()) {
                TransactionDetailRow(title = "Description", subtitle = transaction.subtitle)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { isEditing = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Edit Transaction")
            }
        } else {
            // ---------------- EDIT MODE ----------------
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Amount
                SectionLabel("Amount")
                EditTextField(
                    value = editedAmount,
                    onValueChange = { editedAmount = it },
                    placeholder = "Amount",
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Category
                SectionLabel("Category")
                SelectCategoryOrPaymentBox(
                    title = editedCategory,
                    subtitle = "Choose from categories",
                    icon = Icons.Default.Category,
                    iconColor = transaction.categoryBg,
                    onClick = { showCategorySheet = true }
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Payment Method
                SectionLabel("Payment Method")
                SelectCategoryOrPaymentBox(
                    title = editedMethod,
                    subtitle = "Choose payment method",
                    icon = Icons.Default.CreditCard,
                    iconColor = Color.Gray,
                    onClick = { showPaymentSheet = true }
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Date
                SectionLabel("Date")
                EditTextField(
                    value = editedDate.toString(),
                    onValueChange = { /* open date picker instead */ },
                    placeholder = "Date"
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Description
                SectionLabel("Description")
                EditTextField(
                    value = editedDescription,
                    onValueChange = {it -> editedDescription = it },
                    placeholder = "Description"
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { isEditing = false },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            val updated = transaction.copy(
                                amount = editedAmount.toDoubleOrNull() ?: transaction.amount,
                                category = editedCategory,
                                method = editedMethod,
                                date = editedDate,
                                subtitle = editedDescription
                            )
                            onSave(updated)
                            isEditing = false
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save Changes")
                    }
                }
            }
        }
    }


    // ---------------- Bottom Sheets ----------------
    if (showCategorySheet) {
        SelectCategoryBottomSheet(
            onDismiss = { showCategorySheet = false },
            onCategorySelected = {
                editedCategory = it.name
                showCategorySheet = false
            },
            onAddCustomCategory = {
                showCategorySheet = false
                showCustomCategorySheet = true
            }
        )
    }

    if (showCustomCategorySheet) {
        AddCustomCategoryBottomSheet(
            onDismiss = { showCustomCategorySheet = false },
            onCategoryAdded = {
                editedCategory = it.name
                showCustomCategorySheet = false
            }
        )
    }

    if (showPaymentSheet) {
        SelectPaymentMethodBottomSheet(
            onDismiss = { showPaymentSheet = false },
            selectedPaymentMethod = null,
            onPaymentSelected = {
                editedMethod = it.name
                showPaymentSheet = false
            }
        )
    }
}

@Composable
fun EditTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black, RoundedCornerShape(12.dp))
            .border(
                width = 0.5.dp,
                color = if (value.isNotEmpty()) Color.Gray else Color.LightGray.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) { innerTextField ->
        if (value.isEmpty()) {
            Text(
                text = placeholder,
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
        innerTextField()
    }

    Spacer(modifier = Modifier.height(12.dp))
}
// ---------------- Reusable UI ----------------

@Composable
fun TransactionDetailHeader(
    isEditing: Boolean,
    onEditToggle: () -> Unit,
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = if (isEditing) "Edit Transaction" else "Transaction Details",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Toggle Edit / Cancel
            Icon(
                imageVector = if (isEditing) Icons.Default.Close else Icons.Default.Edit,
                contentDescription = if (isEditing) "Cancel Editing" else "Edit Transaction",
                tint = Color.White,
                modifier = Modifier
                    .size(22.dp)
                    .clickable { onEditToggle() }
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Close Sheet
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = Color.White,
                modifier = Modifier
                    .size(22.dp)
                    .clickable { onClose() }
            )
        }
    }
}

@Composable
fun TransactionDetailRow(
    title: String,
    subtitle: String,
    bgColor: Color = Color(0xFF2C2C2E)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1C1C1C))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {


        Column {
            Text(title, color = Color.Gray, fontSize = 13.sp)
            Text(subtitle, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }
    }
}
