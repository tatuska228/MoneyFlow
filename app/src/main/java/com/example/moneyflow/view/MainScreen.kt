package com.example.moneyflow.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneyflow.model.CategorySummary
import com.example.moneyflow.model.PeriodFilter
import com.example.moneyflow.model.TransactionType
import com.example.moneyflow.ui.theme.BackgroundCard
import com.example.moneyflow.ui.theme.BackgroundDark
import com.example.moneyflow.ui.theme.DividerColor
import com.example.moneyflow.ui.theme.PrimaryGreen
import com.example.moneyflow.ui.theme.TextHint
import com.example.moneyflow.ui.theme.TextPrimary
import com.example.moneyflow.ui.theme.TextSecondary
import com.example.moneyflow.viewmodel.MainScreenViewModel

@Composable
fun MainScreen(viewModel: MainScreenViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = BackgroundDark,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddDialog() },
                containerColor = PrimaryGreen,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Добавить",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { innerPadding ->

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryGreen)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // ── Mode toggle (РАСХОДЫ / ДОХОДЫ) ──────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ModeToggleButton(
                    label = "РАСХОДЫ",
                    isSelected = uiState.selectedMode == TransactionType.EXPENSE,
                    onClick = { viewModel.selectMode(TransactionType.EXPENSE) },
                    modifier = Modifier.weight(1f)
                )
                ModeToggleButton(
                    label = "ДОХОДЫ",
                    isSelected = uiState.selectedMode == TransactionType.INCOME,
                    onClick = { viewModel.selectMode(TransactionType.INCOME) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Budget summary row ───────────────────────────────
            val summary = uiState.summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "по плану",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    val planned = if (uiState.selectedMode == TransactionType.EXPENSE)
                        summary?.plannedExpenses ?: 0.0
                    else
                        summary?.plannedIncome ?: 0.0
                    Text(
                        text = "${planned.toInt()} ₽",
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "остаток",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    val remaining = if (uiState.selectedMode == TransactionType.EXPENSE)
                        summary?.remainingExpenses ?: 0.0
                    else
                        summary?.remainingIncome ?: 0.0
                    Text(
                        text = "${remaining.toInt()} ₽",
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Chart card ───────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BackgroundCard)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Period tabs
                    PeriodTabsRow(
                        selectedPeriod = uiState.selectedPeriod,
                        onPeriodSelected = { viewModel.selectPeriod(it) }
                    )

                    Spacer(Modifier.height(8.dp))

                    // Period label
                    Text(
                        text = summary?.periodLabel ?: "",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )

                    Spacer(Modifier.height(16.dp))

                    // Donut chart
                    val slices = categorySlices(summary?.categorySummaries ?: emptyList())
                    val totalAmount = if (uiState.selectedMode == TransactionType.EXPENSE)
                        summary?.totalExpenses ?: 0.0
                    else
                        summary?.totalIncome ?: 0.0

                    DonutChart(
                        slices = slices,
                        centerLabel = "${totalAmount.toInt()} ₽",
                        size = 180.dp,
                        strokeWidth = 36.dp
                    )

                    Spacer(Modifier.height(16.dp))

                    // Category legend list
                    val categories = summary?.categorySummaries ?: emptyList()
                    if (categories.isEmpty()) {
                        Text(
                            text = "Нет данных за период",
                            color = TextHint,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        categories.forEach { categorySummary ->
                            CategoryRow(
                                categorySummary = categorySummary,
                                onClick = {
                                    // For now open edit dialog with a representative transaction
                                    // In a real flow you'd navigate to category transactions
                                }
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(80.dp)) // FAB clearance
        }
    }

    // ── Edit/Add transaction dialog ──────────────────────────────
    TransactionEditDialog(
        isVisible = uiState.showEditDialog,
        isEditing = uiState.selectedTransaction != null,
        categories = uiState.categories,
        selectedCategoryId = uiState.editDialogCategoryId,
        selectedDateMs = uiState.editDialogDate,
        amount = uiState.editDialogAmount,
        onCategoryChange = viewModel::onDialogCategoryChange,
        onDateChange = viewModel::onDialogDateChange,
        onAmountChange = viewModel::onDialogAmountChange,
        onSave = viewModel::saveTransaction,
        onDelete = viewModel::deleteTransaction,
        onDismiss = viewModel::dismissDialog
    )
}

// ── Period tabs row ───────────────────────────────────────────────

@Composable
private fun PeriodTabsRow(
    selectedPeriod: PeriodFilter,
    onPeriodSelected: (PeriodFilter) -> Unit
) {
    val tabs = listOf("НЕД" to PeriodFilter.WEEK, "МЕСЯЦ" to PeriodFilter.MONTH, "ГОД" to PeriodFilter.YEAR)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        tabs.forEach { (label, filter) ->
            val isSelected = selectedPeriod == filter
            Text(
                text = label,
                color = if (isSelected) TextPrimary else TextSecondary,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onPeriodSelected(filter) }
                    .padding(vertical = 6.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

// ── Category row ──────────────────────────────────────────────────

@Composable
fun CategoryRow(
    categorySummary: CategorySummary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, DividerColor, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Color dot
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(Color(categorySummary.category.colorArgb))
        )

        Spacer(Modifier.width(10.dp))

        Text(
            text = categorySummary.category.name,
            color = TextPrimary,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = "${categorySummary.percentage.toInt()}%",
            color = TextSecondary,
            fontSize = 13.sp
        )

        Spacer(Modifier.width(12.dp))

        Text(
            text = "${categorySummary.totalAmount.toInt()} ₽",
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}