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
import com.example.androidkmm.design.AppStyleDesignSystem

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
                shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM)
            )
            .border(
                width = AppStyleDesignSystem.Sizes.BORDER_NORMAL,
                color = when (type) {
                    ErrorType.ERROR -> Color(0xFFD32F2F)
                    ErrorType.WARNING -> Color(0xFFFF9800)
                    ErrorType.INFO -> Color(0xFF2196F3)
                },
                shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM)
            )
            .padding(AppStyleDesignSystem.Padding.MEDIUM),
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
            modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_SMALL)
        )
        
        Spacer(modifier = Modifier.width(AppStyleDesignSystem.Padding.SMALL))
        
        Text(
            text = message,
            color = when (type) {
                ErrorType.ERROR -> Color(0xFFD32F2F)
                ErrorType.WARNING -> Color(0xFFE65100)
                ErrorType.INFO -> Color(0xFF1976D2)
            },
            style = AppStyleDesignSystem.Typography.CAPTION_1.copy(
                fontWeight = AppStyleDesignSystem.iOSFontWeights.medium
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
        style = AppStyleDesignSystem.Typography.CAPTION_1.copy(
            fontWeight = AppStyleDesignSystem.iOSFontWeights.medium
        ),
        modifier = modifier.padding(start = AppStyleDesignSystem.Padding.XS, top = AppStyleDesignSystem.Padding.XS)
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
                shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.SMALL)
            )
            .padding(horizontal = AppStyleDesignSystem.Padding.SMALL, vertical = AppStyleDesignSystem.Padding.XS),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            tint = Color(0xFFD32F2F),
            modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_TINY)
        )
        
        Spacer(modifier = Modifier.width(AppStyleDesignSystem.Padding.XS))
        
        Text(
            text = message,
            color = Color(0xFFD32F2F),
            style = AppStyleDesignSystem.Typography.CAPTION_2.copy(
                fontWeight = AppStyleDesignSystem.iOSFontWeights.medium
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
                shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM)
            )
            .border(
                width = AppStyleDesignSystem.Sizes.BORDER_NORMAL,
                color = Color(0xFF4CAF50),
                shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM)
            )
            .padding(AppStyleDesignSystem.Padding.MEDIUM),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Color(0xFF4CAF50),
            modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_SMALL)
        )
        
        Spacer(modifier = Modifier.width(AppStyleDesignSystem.Padding.SMALL))
        
        Text(
            text = message,
            color = Color(0xFF2E7D32),
            style = AppStyleDesignSystem.Typography.CAPTION_1.copy(
                fontWeight = AppStyleDesignSystem.iOSFontWeights.medium
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
            verticalArrangement = Arrangement.spacedBy(AppStyleDesignSystem.Padding.XS)
        ) {
            Text(
                text = "Please fix the following errors:",
                color = Color(0xFFD32F2F),
                style = AppStyleDesignSystem.Typography.CALL_OUT.copy(
                    fontWeight = AppStyleDesignSystem.iOSFontWeights.bold
                )
            )
            
            errors.forEach { (field, message) ->
                Text(
                    text = "• $message",
                    color = Color(0xFFD32F2F),
                    style = AppStyleDesignSystem.Typography.CAPTION_1,
                    modifier = Modifier.padding(start = AppStyleDesignSystem.Padding.SMALL)
                )
            }
        }
    }
}
