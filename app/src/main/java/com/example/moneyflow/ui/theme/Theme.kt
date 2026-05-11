package com.example.moneyflow.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val MoneyFlowColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryDark,
    onPrimaryContainer = OnPrimary,
    secondary = PrimaryLight,
    onSecondary = OnPrimary,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = TextSecondary,
    error = Expense,
    onError = OnPrimary,
)

@Composable
fun MoneyFlowTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MoneyFlowColorScheme,
        typography = AppTypography,
        content = content
    )
}