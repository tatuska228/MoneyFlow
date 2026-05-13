package com.example.moneyflow.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    displayLarge  = TextStyle(fontWeight = FontWeight.Bold,      fontSize = 32.sp, color = TextPrimary),
    headlineLarge = TextStyle(fontWeight = FontWeight.Bold,      fontSize = 24.sp, color = TextPrimary),
    headlineMedium= TextStyle(fontWeight = FontWeight.SemiBold,  fontSize = 20.sp, color = TextPrimary),
    titleLarge    = TextStyle(fontWeight = FontWeight.SemiBold,  fontSize = 16.sp, color = TextPrimary),
    titleMedium   = TextStyle(fontWeight = FontWeight.Medium,    fontSize = 14.sp, color = TextPrimary),
    bodyLarge     = TextStyle(fontWeight = FontWeight.Normal,    fontSize = 16.sp, color = TextPrimary),
    bodyMedium    = TextStyle(fontWeight = FontWeight.Normal,    fontSize = 14.sp, color = TextSecondary),
    bodySmall     = TextStyle(fontWeight = FontWeight.Normal,    fontSize = 12.sp, color = TextHint),
    labelLarge    = TextStyle(fontWeight = FontWeight.Medium,    fontSize = 14.sp, color = TextPrimary),
    labelMedium   = TextStyle(fontWeight = FontWeight.Medium,    fontSize = 12.sp, color = TextSecondary),
)