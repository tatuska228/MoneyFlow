package com.example.moneyflow.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneyflow.model.Category
import com.example.moneyflow.model.Transaction
import com.example.moneyflow.ui.theme.BackgroundCard
import com.example.moneyflow.ui.theme.BackgroundCardLight
import com.example.moneyflow.ui.theme.BackgroundDark
import com.example.moneyflow.ui.theme.DividerColor
import com.example.moneyflow.ui.theme.PrimaryGreen
import com.example.moneyflow.ui.theme.TextPrimary
import com.example.moneyflow.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionEditDialog(
    isVisible: Boolean,
    isEditing: Boolean,
    categories: List<Category>,
    selectedCategoryId: Long,
    selectedDateMs: Long,
    amount: String,
    onCategoryChange: (Long) -> Unit,
    onDateChange: (Long) -> Unit,
    onAmountChange: (String) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!isVisible) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showCategoryDropdown by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }
    val selectedCategoryName = categories.find { it.id == selectedCategoryId }?.name ?: "Выберите"

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = BackgroundCard,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            // ── Date field ──────────────────────────────────────
            Text(
                text = "Дата",
                color = TextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(BackgroundCardLight)
                    .border(1.dp, DividerColor, RoundedCornerShape(10.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = dateFormatter.format(Date(selectedDateMs)),
                        color = TextPrimary,
                        fontSize = 15.sp
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = TextSecondary
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Category dropdown ───────────────────────────────
            Text(
                text = "Категория",
                color = TextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(8.dp))
            Box {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(BackgroundCardLight)
                        .border(1.dp, DividerColor, RoundedCornerShape(10.dp))
                        .clickable { showCategoryDropdown = true }
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = selectedCategoryName,
                            color = TextPrimary,
                            fontSize = 15.sp
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = TextSecondary
                        )
                    }
                }
                DropdownMenu(
                    expanded = showCategoryDropdown,
                    onDismissRequest = { showCategoryDropdown = false },
                    modifier = Modifier.background(BackgroundCard)
                ) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = cat.name,
                                    color = TextPrimary,
                                    fontSize = 14.sp
                                )
                            },
                            onClick = {
                                onCategoryChange(cat.id)
                                showCategoryDropdown = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Amount field ────────────────────────────────────
            Text(
                text = "Сумма, ₽",
                color = TextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = amount,
                onValueChange = { onAmountChange(it.filter { c -> c.isDigit() || c == '.' }) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("0", color = TextSecondary, fontSize = 15.sp)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = BackgroundCardLight,
                    unfocusedContainerColor = BackgroundCardLight,
                    focusedBorderColor = PrimaryGreen,
                    unfocusedBorderColor = DividerColor,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = PrimaryGreen
                )
            )

            Spacer(Modifier.height(24.dp))

            // ── Action buttons ──────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SaveButton(
                    onClick = onSave,
                    modifier = Modifier.weight(1f)
                )
                if (isEditing) {
                    DeleteButton(
                        onClick = onDelete,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}