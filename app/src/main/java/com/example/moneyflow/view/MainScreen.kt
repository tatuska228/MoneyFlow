package com.example.moneyflow.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneyflow.model.PeriodFilter
import com.example.moneyflow.model.TransactionType
import com.example.moneyflow.ui.theme.*
import com.example.moneyflow.view.elements.*
import com.example.moneyflow.viewmodel.MainScreenViewModel

@Composable
fun MainScreen(
    viewModel: MainScreenViewModel,
    onProfileClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()
        .background(Background)
        .padding(top = 20.dp, bottom = 20.dp)) {

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
        ) {

            // ── Top bar ────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text       = uiState.userLogin.ifEmpty { "MoneyFlow" },
                    color      = OnPrimary,
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Box(
                    modifier = Modifier.size(38.dp).clip(CircleShape)
                        .background(BackgroundDialog)
                        .clickable { onProfileClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector        = Icons.Default.Person,
                        contentDescription = "Профиль",
                        tint               = OnPrimary,
                        modifier           = Modifier.size(22.dp)
                    )
                }
            }

            // ── Mode toggle ────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ModeToggleButton(
                    label      = "РАСХОДЫ",
                    isSelected = uiState.selectedMode == TransactionType.EXPENSE,
                    onClick    = { viewModel.selectMode(TransactionType.EXPENSE) },
                    modifier   = Modifier.weight(1f)
                )
                ModeToggleButton(
                    label      = "ДОХОДЫ",
                    isSelected = uiState.selectedMode == TransactionType.INCOME,
                    onClick    = { viewModel.selectMode(TransactionType.INCOME) },
                    modifier   = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Budget summary ─────────────────────────────────────
            val summary = uiState.summary
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Bottom
            ) {
                Column(modifier = Modifier.clickable { viewModel.openBudgetDialog() }) {
                    Text("по плану", color = TextSecondary, fontSize = 12.sp)
                    Text(
                        text       = "${(summary?.plannedBudget ?: 0.0).toInt()} ₽",
                        color      = OnPrimary,
                        fontSize   = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("остаток", color = TextSecondary, fontSize = 12.sp)
                    val rem = summary?.remainder ?: 0.0
                    Text(
                        text       = "${rem.toInt()} ₽",
                        color      = if (rem < 0) Color(0xFFFF6B6B) else OnPrimary,
                        fontSize   = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Chart card ─────────────────────────────────────────
            Card(
                modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape     = RoundedCornerShape(20.dp),
                colors    = CardDefaults.cardColors(containerColor = BackgroundDialog),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier            = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Period tabs
                    PeriodTabsRow(
                        selectedPeriod   = uiState.selectedPeriod,
                        onPeriodSelected = { viewModel.selectPeriod(it) }
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text      = summary?.periodLabel ?: "",
                        color     = TextSecondary,
                        fontSize  = 13.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(16.dp))

                    val slices     = transactionSlices(summary?.transactionSummaries ?: emptyList())
                    val totalLabel = "${(summary?.totalAmount ?: 0.0).toInt()} ₽"

                    DonutChart(slices = slices, centerLabel = totalLabel, size = 180.dp, strokeWidth = 38.dp)

                    Spacer(Modifier.height(16.dp))

                    val txList = summary?.transactionSummaries ?: emptyList()
                    if (txList.isEmpty()) {
                        Text(
                            text     = "Нет операций за период",
                            color    = TextHint,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        txList.forEach { txSummary ->
                            TransactionRow(
                                txSummary = txSummary,
                                onClick   = { viewModel.openEditDialog(txSummary.transaction) }
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(88.dp))
        }

        // ── FAB ────────────────────────────────────────────────────
        FloatingActionButton(
            onClick        = { viewModel.openAddDialog() },
            containerColor = BackgroundDialog,
            contentColor   = OnPrimary,
            shape          = CircleShape,
            modifier       = Modifier.align(Alignment.BottomEnd).padding(24.dp).size(56.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Добавить операцию", modifier = Modifier.size(28.dp))
        }
    }

    // ── Loading ────────────────────────────────────────────────────
    if (uiState.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = OnPrimary)
        }
    }

    // ── Error snackbar ─────────────────────────────────────────────
    uiState.error?.let { errorMsg ->
        val snackState = remember { SnackbarHostState() }
        LaunchedEffect(errorMsg) {
            snackState.showSnackbar(errorMsg)
            viewModel.loadData() // clear error by reloading
        }
        Box(Modifier.fillMaxSize()) {
            SnackbarHost(snackState, modifier = Modifier.align(Alignment.BottomCenter))
        }
    }

    // ── Transaction dialog ─────────────────────────────────────────
    TransactionDialog(
        isVisible          = uiState.showEditDialog,
        isEditing          = uiState.isEditing,
        categories         = uiState.categories,
        selectedCategoryId = uiState.editCategoryId,
        selectedDateMs     = uiState.editDateMs,
        amount             = uiState.editAmount,
        note               = uiState.editNote,
        onCategoryChange   = viewModel::onDialogCategoryChange,
        onDateChange       = viewModel::onDialogDateChange,
        onAmountChange     = viewModel::onDialogAmountChange,
        onNoteChange       = viewModel::onDialogNoteChange,
        onSave             = viewModel::saveTransaction,
        onDelete           = viewModel::deleteTransaction,
        onDismiss          = viewModel::dismissDialog
    )

    // ── Budget dialog ──────────────────────────────────────────────
    PlannedBudgetDialog(
        isVisible    = uiState.showBudgetDialog,
        currentValue = uiState.editPlannedBudget,
        onValueChange = viewModel::onBudgetAmountChange,
        onSave       = viewModel::savePlannedBudget,
        onDismiss    = viewModel::dismissBudgetDialog
    )
}

// ── Period tabs ────────────────────────────────────────────────────

@Composable
private fun PeriodTabsRow(selectedPeriod: PeriodFilter, onPeriodSelected: (PeriodFilter) -> Unit) {
    val tabs = listOf("НЕД" to PeriodFilter.WEEK, "МЕСЯЦ" to PeriodFilter.MONTH, "ГОД" to PeriodFilter.YEAR)
    Row(modifier = Modifier.fillMaxWidth()) {
        tabs.forEach { (label, filter) ->
            val isSelected = selectedPeriod == filter
            Text(
                text       = label,
                color      = if (isSelected) OnPrimary else TextSecondary,
                fontSize   = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                textAlign  = TextAlign.Center,
                modifier   = Modifier.weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onPeriodSelected(filter) }
                    .padding(vertical = 6.dp)
            )
        }
    }
}