package com.example.moneyflow.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AppColorScheme = darkColorScheme(
    primary          = PrimaryGreen,
    onPrimary        = Color.White,
    secondary        = PrimaryGreenLight,
    onSecondary      = Color.White,
    background       = BackgroundMain,
    onBackground     = TextPrimary,
    surface          = BackgroundCard,
    onSurface        = TextPrimary,
    surfaceVariant   = BackgroundField,
    onSurfaceVariant = TextSecondary,
    error            = ExpenseColor,
    onError          = Color.White,
    outline          = DividerColor
)

@Composable
fun MoneyFlowTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography  = Typography,
        content     = content
    )
}