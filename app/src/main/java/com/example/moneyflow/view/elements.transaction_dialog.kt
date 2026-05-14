package com.example.moneyflow.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneyflow.model.Category
import com.example.moneyflow.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDialog(
    isVisible: Boolean,
    isEditing: Boolean,            // true = редактирование, false = добавление
    categories: List<Category>,
    selectedCategoryId: Long,
    selectedDateMs: Long,
    amount: String,
    note: String,
    onCategoryChange: (Long) -> Unit,
    onDateChange: (Long) -> Unit,
    onAmountChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
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
        sheetState       = sheetState,
        containerColor   = BackgroundDialog,
        dragHandle       = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.3f))
            )
        },
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // ── Date ─────────────────────────────────────────────
            DialogLabel("Дата")
            Spacer(Modifier.height(8.dp))
            DialogDropdownBox(
                text  = dateFormatter.format(Date(selectedDateMs)),
                onClick = { /* date picker can be added later */ }
            )

            Spacer(Modifier.height(16.dp))

            // ── Category ─────────────────────────────────────────
            DialogLabel("Категория")
            Spacer(Modifier.height(8.dp))
            Box {
                DialogDropdownBox(
                    text    = selectedCategoryName,
                    onClick = { showCategoryDropdown = true }
                )
                DropdownMenu(
                    expanded        = showCategoryDropdown,
                    onDismissRequest = { showCategoryDropdown = false },
                    modifier = Modifier.background(BackgroundDialog)
                ) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text    = { Text(cat.name, color = TextPrimary, fontSize = 14.sp) },
                            onClick = { onCategoryChange(cat.id); showCategoryDropdown = false }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Amount ───────────────────────────────────────────
            DialogLabel("Сумма, ₽")
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value       = amount,
                onValueChange = { onAmountChange(it.filter { c -> c.isDigit() || c == '.' }) },
                modifier    = Modifier.fillMaxWidth(),
                placeholder = { Text("0", color = TextHint, fontSize = 15.sp) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine  = true,
                shape       = RoundedCornerShape(10.dp),
                colors      = dialogTextFieldColors()
            )

            Spacer(Modifier.height(24.dp))

            // ── Action buttons ────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SaveButton(onClick = onSave, modifier = Modifier.weight(1f))
                if (isEditing) {
                    DeleteButton(onClick = onDelete, modifier = Modifier.weight(1f))
                } else {
                    CancelButton(onClick = onDismiss, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

// ── Planned budget dialog ─────────────────────────────────────────

@Composable
fun PlannedBudgetDialog(
    isVisible: Boolean,
    currentValue: String,
    onValueChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!isVisible) return

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = BackgroundDialog,
        title = {
            Text("Сумма по плану", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            OutlinedTextField(
                value         = currentValue,
                onValueChange = { onValueChange(it.filter { c -> c.isDigit() || c == '.' }) },
                placeholder   = { Text("0", color = TextHint) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine    = true,
                shape         = RoundedCornerShape(10.dp),
                colors        = dialogTextFieldColors(),
                modifier      = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = onSave) {
                Text("Сохранить", color = Color.Green, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = TextSecondary)
            }
        }
    )
}

// ── Helpers ───────────────────────────────────────────────────────

@Composable
private fun DialogLabel(text: String) {
    Text(text = text, color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
}

@Composable
private fun DialogDropdownBox(text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(BackgroundField)
            .border(1.dp, DividerColor, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = text, color = TextPrimary, fontSize = 15.sp)
        Icon(
            imageVector = Icons.Default.ArrowDropDown,
            contentDescription = null,
            tint = TextSecondary
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun dialogTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor   = BackgroundField,
    unfocusedContainerColor = BackgroundField,
    focusedBorderColor      = Color.Green,
    unfocusedBorderColor    = DividerColor,
    focusedTextColor        = TextPrimary,
    unfocusedTextColor      = TextPrimary,
    cursorColor             = Color.Green
)