package com.example.androidkmm.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.androidkmm.design.AppStyleDesignSystem
import com.example.androidkmm.utils.TextUtils

/**
 * Progress card component showing achievement or progress information
 */
@Composable
fun ProgressCard(
    title: String = "Great progress! 🎉",
    subtitle: String = "You spent 20% less on Food this month"
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color(0xFFDAFFF2),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM)
            )
            .padding(AppStyleDesignSystem.Padding.CARD_PADDING)
    ) {
        Column {
            TextUtils.StandardText(
                text = title,
                color = Color.Black,
                fontSize = AppStyleDesignSystem.Typography.HEADLINE.fontSize,
                fontWeight = AppStyleDesignSystem.iOSFontWeights.bold
            )
            Spacer(Modifier.height(AppStyleDesignSystem.Padding.XS))
            TextUtils.StandardText(
                text = subtitle,
                color = Color.DarkGray,
                fontSize = AppStyleDesignSystem.Typography.CALL_OUT.fontSize,
                maxLines = 2
            )
        }
    }
}
