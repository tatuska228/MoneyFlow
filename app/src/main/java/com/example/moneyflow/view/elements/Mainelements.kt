package com.example.moneyflow.view.elements

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneyflow.model.Category
import com.example.moneyflow.model.TransactionSummary
import com.example.moneyflow.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

// ── Mode toggle button ────────────────────────────────────────────

@Composable
fun ModeToggleButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick  = onClick,
        shape    = RoundedCornerShape(20.dp),
        colors   = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) TabSelected else TabUnselected,
            contentColor   = if (isSelected) OnPrimary   else TextSecondary
        ),
        modifier = modifier.height(36.dp)
    ) {
        Text(
            text          = label,
            fontSize      = 12.sp,
            fontWeight    = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            letterSpacing = 0.8.sp
        )
    }
}

// ── Main screen action buttons ────────────────────────────────────

@Composable
fun SaveButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick  = onClick,
        shape    = RoundedCornerShape(10.dp),
        colors   = ButtonDefaults.buttonColors(containerColor = ButtonPrimary, contentColor = OnPrimary),
        modifier = modifier.height(48.dp)
    ) { Text("Сохранить", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) }
}

@Composable
fun DeleteButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick  = onClick,
        shape    = RoundedCornerShape(10.dp),
        colors   = ButtonDefaults.buttonColors(containerColor = ButtonDanger, contentColor = OnPrimary),
        modifier = modifier.height(48.dp)
    ) { Text("Удалить", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) }
}

@Composable
fun CancelButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick  = onClick,
        shape    = RoundedCornerShape(10.dp),
        colors   = ButtonDefaults.buttonColors(containerColor = ButtonCancel, contentColor = OnPrimary),
        modifier = modifier.height(48.dp)
    ) { Text("Отмена", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) }
}

// ── Donut chart ───────────────────────────────────────────────────

data class DonutSlice(val color: Color, val percentage: Float)

@Composable
fun DonutChart(
    slices: List<DonutSlice>,
    centerLabel: String,
    modifier: Modifier = Modifier,
    size: Dp = 180.dp,
    strokeWidth: Dp = 36.dp
) {
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            val stroke  = strokeWidth.toPx()
            val inset   = stroke / 2f
            val arcSize = Size(this.size.width - stroke, this.size.height - stroke)
            val topLeft = Offset(inset, inset)

            if (slices.isEmpty()) {
                drawArc(
                    color = BackgroundDialog.copy(alpha = 0.4f),
                    startAngle = 0f, sweepAngle = 360f,
                    useCenter = false, topLeft = topLeft, size = arcSize,
                    style = Stroke(width = stroke)
                )
            } else {
                var startAngle = -90f
                slices.forEach { slice ->
                    val sweep = 360f * (slice.percentage / 100f)
                    drawArc(
                        color = slice.color,
                        startAngle = startAngle, sweepAngle = sweep,
                        useCenter = false, topLeft = topLeft, size = arcSize,
                        style = Stroke(width = stroke)
                    )
                    startAngle += sweep
                }
            }
        }
        Text(text = centerLabel, color = OnPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

fun transactionSlices(summaries: List<TransactionSummary>): List<DonutSlice> =
    summaries.map { DonutSlice(Color(it.transaction.categoryColor), it.percentage) }

// ── Transaction row ───────────────────────────────────────────────

@Composable
fun TransactionRow(txSummary: TransactionSummary, onClick: () -> Unit) {
    val tx = txSummary.transaction
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, DividerColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(14.dp).clip(CircleShape)
                .background(Color(tx.categoryColor))
        )
        Spacer(Modifier.width(10.dp))
        Text(text = tx.categoryName, color = OnPrimary, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Text(text = "${txSummary.percentage.toInt()}%", color = TextSecondary, fontSize = 13.sp)
        Spacer(Modifier.width(12.dp))
        Text(text = "${tx.amount.toInt()} ₽", color = OnPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

// ── Transaction add/edit bottom sheet ────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDialog(
    isVisible: Boolean,
    isEditing: Boolean,
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
                modifier = Modifier.padding(vertical = 12.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(OnPrimary.copy(alpha = 0.3f))
            )
        },
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 20.dp).padding(bottom = 32.dp)
        ) {
            // Date
            DialogFieldLabel("Дата")
            Spacer(Modifier.height(8.dp))
            DialogDropdownBox(text = dateFormatter.format(Date(selectedDateMs)), onClick = {})

            Spacer(Modifier.height(16.dp))

            // Category
            DialogFieldLabel("Категория")
            Spacer(Modifier.height(8.dp))
            Box {
                DialogDropdownBox(text = selectedCategoryName, onClick = { showCategoryDropdown = true })
                DropdownMenu(
                    expanded         = showCategoryDropdown,
                    onDismissRequest = { showCategoryDropdown = false },
                    modifier         = Modifier.background(BackgroundDialog)
                ) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text    = { Text(cat.name, color = OnPrimary, fontSize = 14.sp) },
                            onClick = { onCategoryChange(cat.id); showCategoryDropdown = false }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Amount
            DialogFieldLabel("Сумма, ₽")
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value           = amount,
                onValueChange   = { onAmountChange(it.filter { c -> c.isDigit() || c == '.' }) },
                modifier        = Modifier.fillMaxWidth(),
                placeholder     = { Text("0", color = TextHint, fontSize = 15.sp) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine      = true,
                shape           = RoundedCornerShape(10.dp),
                colors          = dialogTextFieldColors()
            )

            Spacer(Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SaveButton(onClick = onSave, modifier = Modifier.weight(1f))
                if (isEditing) DeleteButton(onClick = onDelete, modifier = Modifier.weight(1f))
                else           CancelButton(onClick = onDismiss, modifier = Modifier.weight(1f))
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
        title  = { Text("Сумма по плану", color = OnPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text   = {
            OutlinedTextField(
                value           = currentValue,
                onValueChange   = { onValueChange(it.filter { c -> c.isDigit() || c == '.' }) },
                placeholder     = { Text("0", color = TextHint) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine      = true,
                shape           = RoundedCornerShape(10.dp),
                colors          = dialogTextFieldColors(),
                modifier        = Modifier.fillMaxWidth()
            )
        },
        confirmButton = { TextButton(onClick = onSave)    { Text("Сохранить", color = Primary, fontWeight = FontWeight.SemiBold) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена",    color = TextSecondary) } }
    )
}

// ── Private helpers ───────────────────────────────────────────────

@Composable
private fun DialogFieldLabel(text: String) {
    Text(text = text, color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
}

@Composable
private fun DialogDropdownBox(text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(BackgroundField)
            .border(1.dp, DividerColor, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = text, color = OnPrimary, fontSize = 15.sp)
        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = TextSecondary)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun dialogTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor   = BackgroundField,
    unfocusedContainerColor = BackgroundField,
    focusedBorderColor      = Primary,
    unfocusedBorderColor    = DividerColor,
    focusedTextColor        = OnPrimary,
    unfocusedTextColor      = OnPrimary,
    cursorColor             = Primary
)