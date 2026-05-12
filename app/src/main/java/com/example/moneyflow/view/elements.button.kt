package com.example.moneyflow.view

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneyflow.ui.theme.ButtonDanger
import com.example.moneyflow.ui.theme.ButtonPrimary
import com.example.moneyflow.ui.theme.TabSelected
import com.example.moneyflow.ui.theme.TabUnselected
import com.example.moneyflow.ui.theme.TextSecondary

@Composable
fun ModeToggleButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) TabSelected else TabUnselected,
            contentColor = if (isSelected) Color.White else TextSecondary
        ),
        modifier = modifier.height(36.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            letterSpacing = 0.5.sp
        )
    }
}


@Composable
fun SaveButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = ButtonPrimary,
            contentColor = Color.White
        ),
        modifier = modifier.height(48.dp)
    ) {
        Text(
            text = "Сохранить",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun DeleteButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = ButtonDanger,
            contentColor = Color.White
        ),
        modifier = modifier.height(48.dp)
    ) {
        Text(
            text = "Удалить",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}