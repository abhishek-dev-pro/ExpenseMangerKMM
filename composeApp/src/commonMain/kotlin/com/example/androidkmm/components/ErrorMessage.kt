package com.example.androidkmm.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidkmm.design.DesignSystem

/**
 * Standardized error message component
 */
@Composable
fun ErrorMessage(
    message: String,
    modifier: Modifier = Modifier,
    type: ErrorType = ErrorType.ERROR
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = when (type) {
                    ErrorType.ERROR -> Color(0xFFFFEBEE)
                    ErrorType.WARNING -> Color(0xFFFFF3E0)
                    ErrorType.INFO -> Color(0xFFE3F2FD)
                },
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                color = when (type) {
                    ErrorType.ERROR -> Color(0xFFD32F2F)
                    ErrorType.WARNING -> Color(0xFFFF9800)
                    ErrorType.INFO -> Color(0xFF2196F3)
                },
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = when (type) {
                ErrorType.ERROR -> Icons.Default.Error
                ErrorType.WARNING -> Icons.Default.Warning
                ErrorType.INFO -> Icons.Default.Info
            },
            contentDescription = null,
            tint = when (type) {
                ErrorType.ERROR -> Color(0xFFD32F2F)
                ErrorType.WARNING -> Color(0xFFFF9800)
                ErrorType.INFO -> Color(0xFF2196F3)
            },
            modifier = Modifier.size(16.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = message,
            color = when (type) {
                ErrorType.ERROR -> Color(0xFFD32F2F)
                ErrorType.WARNING -> Color(0xFFE65100)
                ErrorType.INFO -> Color(0xFF1976D2)
            },
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Error types
 */
enum class ErrorType {
    ERROR,
    WARNING,
    INFO
}

/**
 * Field error message component
 */
@Composable
fun FieldErrorMessage(
    message: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = message,
        color = Color(0xFFD32F2F),
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        modifier = modifier.padding(start = 4.dp, top = 4.dp)
    )
}

/**
 * Inline error message for form fields
 */
@Composable
fun InlineErrorMessage(
    message: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFFFFEBEE),
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            tint = Color(0xFFD32F2F),
            modifier = Modifier.size(12.dp)
        )
        
        Spacer(modifier = Modifier.width(4.dp))
        
        Text(
            text = message,
            color = Color(0xFFD32F2F),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Success message component
 */
@Composable
fun SuccessMessage(
    message: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFFE8F5E8),
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                color = Color(0xFF4CAF50),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Color(0xFF4CAF50),
            modifier = Modifier.size(16.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = message,
            color = Color(0xFF2E7D32),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Form validation summary component
 */
@Composable
fun ValidationSummary(
    errors: Map<String, String>,
    modifier: Modifier = Modifier
) {
    if (errors.isNotEmpty()) {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Please fix the following errors:",
                color = Color(0xFFD32F2F),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            
            errors.forEach { (field, message) ->
                Text(
                    text = "• $message",
                    color = Color(0xFFD32F2F),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}
