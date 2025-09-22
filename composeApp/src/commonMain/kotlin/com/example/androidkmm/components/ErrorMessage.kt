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
import com.example.androidkmm.design.iOSStyleDesignSystem

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
                shape = RoundedCornerShape(iOSStyleDesignSystem.CornerRadius.MEDIUM)
            )
            .border(
                width = iOSStyleDesignSystem.Sizes.BORDER_NORMAL,
                color = when (type) {
                    ErrorType.ERROR -> Color(0xFFD32F2F)
                    ErrorType.WARNING -> Color(0xFFFF9800)
                    ErrorType.INFO -> Color(0xFF2196F3)
                },
                shape = RoundedCornerShape(iOSStyleDesignSystem.CornerRadius.MEDIUM)
            )
            .padding(iOSStyleDesignSystem.Padding.MEDIUM),
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
            modifier = Modifier.size(iOSStyleDesignSystem.Sizes.ICON_SIZE_SMALL)
        )
        
        Spacer(modifier = Modifier.width(iOSStyleDesignSystem.Padding.SMALL))
        
        Text(
            text = message,
            color = when (type) {
                ErrorType.ERROR -> Color(0xFFD32F2F)
                ErrorType.WARNING -> Color(0xFFE65100)
                ErrorType.INFO -> Color(0xFF1976D2)
            },
            style = iOSStyleDesignSystem.Typography.CAPTION_1.copy(
                fontWeight = iOSStyleDesignSystem.iOSFontWeights.medium
            )
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
        style = iOSStyleDesignSystem.Typography.CAPTION_1.copy(
            fontWeight = iOSStyleDesignSystem.iOSFontWeights.medium
        ),
        modifier = modifier.padding(start = iOSStyleDesignSystem.Padding.XS, top = iOSStyleDesignSystem.Padding.XS)
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
                shape = RoundedCornerShape(iOSStyleDesignSystem.CornerRadius.SMALL)
            )
            .padding(horizontal = iOSStyleDesignSystem.Padding.SMALL, vertical = iOSStyleDesignSystem.Padding.XS),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            tint = Color(0xFFD32F2F),
            modifier = Modifier.size(iOSStyleDesignSystem.Sizes.ICON_SIZE_TINY)
        )
        
        Spacer(modifier = Modifier.width(iOSStyleDesignSystem.Padding.XS))
        
        Text(
            text = message,
            color = Color(0xFFD32F2F),
            style = iOSStyleDesignSystem.Typography.CAPTION_2.copy(
                fontWeight = iOSStyleDesignSystem.iOSFontWeights.medium
            )
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
                shape = RoundedCornerShape(iOSStyleDesignSystem.CornerRadius.MEDIUM)
            )
            .border(
                width = iOSStyleDesignSystem.Sizes.BORDER_NORMAL,
                color = Color(0xFF4CAF50),
                shape = RoundedCornerShape(iOSStyleDesignSystem.CornerRadius.MEDIUM)
            )
            .padding(iOSStyleDesignSystem.Padding.MEDIUM),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Color(0xFF4CAF50),
            modifier = Modifier.size(iOSStyleDesignSystem.Sizes.ICON_SIZE_SMALL)
        )
        
        Spacer(modifier = Modifier.width(iOSStyleDesignSystem.Padding.SMALL))
        
        Text(
            text = message,
            color = Color(0xFF2E7D32),
            style = iOSStyleDesignSystem.Typography.CAPTION_1.copy(
                fontWeight = iOSStyleDesignSystem.iOSFontWeights.medium
            )
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
            verticalArrangement = Arrangement.spacedBy(iOSStyleDesignSystem.Padding.XS)
        ) {
            Text(
                text = "Please fix the following errors:",
                color = Color(0xFFD32F2F),
                style = iOSStyleDesignSystem.Typography.CALL_OUT.copy(
                    fontWeight = iOSStyleDesignSystem.iOSFontWeights.bold
                )
            )
            
            errors.forEach { (field, message) ->
                Text(
                    text = "• $message",
                    color = Color(0xFFD32F2F),
                    style = iOSStyleDesignSystem.Typography.CAPTION_1,
                    modifier = Modifier.padding(start = iOSStyleDesignSystem.Padding.SMALL)
                )
            }
        }
    }
}
