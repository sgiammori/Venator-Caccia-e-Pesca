package it.mygroup.org.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class WidthClass {
    Compact,
    Medium,
    Expanded
}

data class ResponsiveUiSpec(
    val widthClass: WidthClass,
    val fontScale: Float,
    val isLargeText: Boolean,
    val screenHorizontalPadding: Dp,
    val listItemSpacing: Dp,
    val chipHeight: Dp,
    val chipTextSize: TextUnit,
    val inputTextSize: TextUnit,
    val actionButtonSize: Dp,
    val chatBubbleMaxWidth: Dp,
    val resultCardMaxWidth: Dp
)

@Composable
fun rememberResponsiveUiSpec(): ResponsiveUiSpec {
    val configuration = LocalConfiguration.current
    val fontScale = LocalDensity.current.fontScale

    val widthClass = when {
        configuration.screenWidthDp >= 840 -> WidthClass.Expanded
        configuration.screenWidthDp >= 600 -> WidthClass.Medium
        else -> WidthClass.Compact
    }

    val isLargeText = fontScale >= 1.15f

    return remember(configuration.screenWidthDp, fontScale) {
        val baseBubbleMax = when (widthClass) {
            WidthClass.Compact -> 320.dp
            WidthClass.Medium -> 400.dp
            WidthClass.Expanded -> 500.dp
        }

        val baseResultMax = when (widthClass) {
            WidthClass.Compact -> 340.dp
            WidthClass.Medium -> 440.dp
            WidthClass.Expanded -> 560.dp
        }

        ResponsiveUiSpec(
            widthClass = widthClass,
            fontScale = fontScale,
            isLargeText = isLargeText,
            screenHorizontalPadding = when (widthClass) {
                WidthClass.Compact -> 12.dp
                WidthClass.Medium -> 16.dp
                WidthClass.Expanded -> 20.dp
            },
            listItemSpacing = if (isLargeText) 14.dp else 12.dp,
            chipHeight = if (isLargeText) 48.dp else 44.dp,
            chipTextSize = if (isLargeText) 13.sp else 12.sp,
            inputTextSize = if (isLargeText) 15.sp else 14.sp,
            actionButtonSize = if (isLargeText) 50.dp else 46.dp,
            chatBubbleMaxWidth = if (isLargeText) baseBubbleMax + 20.dp else baseBubbleMax,
            resultCardMaxWidth = if (isLargeText) baseResultMax + 20.dp else baseResultMax
        )
    }
}

