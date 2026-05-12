package com.example.moneyflow.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneyflow.model.Category
import com.example.moneyflow.model.PeriodFilter
import com.example.moneyflow.model.PeriodSummary
import com.example.moneyflow.model.Transaction
import com.example.moneyflow.model.TransactionRepository
import com.example.moneyflow.model.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class MainUiState(
    val isLoading: Boolean = true,
    val selectedMode: TransactionType = TransactionType.EXPENSE,
    val selectedPeriod: PeriodFilter = PeriodFilter.WEEK,
    val summary: PeriodSummary? = null,
    val categories: List<Category> = emptyList(),
    val selectedTransaction: Transaction? = null,
    val showEditDialog: Boolean = false,
    val editDialogCategoryId: Long = -1L,
    val editDialogDate: Long = System.currentTimeMillis(),
    val editDialogAmount: String = "",
    val error: String? = null
)

class MainScreenViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TransactionRepository(application)

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    // ── Data loading ──────────────────────────────────────────────

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val state = _uiState.value
                val summary = withContext(Dispatchers.IO) {
                    repository.getPeriodSummary(state.selectedPeriod, state.selectedMode)
                }
                val categories = withContext(Dispatchers.IO) {
                    repository.getCategories(state.selectedMode)
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    summary = summary,
                    categories = categories,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    // ── Mode / period selection ───────────────────────────────────

    fun selectMode(mode: TransactionType) {
        _uiState.value = _uiState.value.copy(selectedMode = mode)
        loadData()
    }

    fun selectPeriod(period: PeriodFilter) {
        _uiState.value = _uiState.value.copy(selectedPeriod = period)
        loadData()
    }

    // ── Edit dialog ───────────────────────────────────────────────

    fun openEditDialog(transaction: Transaction) {
        _uiState.value = _uiState.value.copy(
            selectedTransaction = transaction,
            showEditDialog = true,
            editDialogCategoryId = transaction.categoryId,
            editDialogDate = transaction.date,
            editDialogAmount = transaction.amount.toInt().toString()
        )
    }

    fun openAddDialog() {
        val categories = _uiState.value.categories
        _uiState.value = _uiState.value.copy(
            selectedTransaction = null,
            showEditDialog = true,
            editDialogCategoryId = categories.firstOrNull()?.id ?: -1L,
            editDialogDate = System.currentTimeMillis(),
            editDialogAmount = ""
        )
    }

    fun dismissDialog() {
        _uiState.value = _uiState.value.copy(showEditDialog = false)
    }

    fun onDialogCategoryChange(categoryId: Long) {
        _uiState.value = _uiState.value.copy(editDialogCategoryId = categoryId)
    }

    fun onDialogDateChange(dateMs: Long) {
        _uiState.value = _uiState.value.copy(editDialogDate = dateMs)
    }

    fun onDialogAmountChange(amount: String) {
        _uiState.value = _uiState.value.copy(editDialogAmount = amount)
    }

    fun saveTransaction() {
        viewModelScope.launch {
            val state = _uiState.value
            val amount = state.editDialogAmount.toDoubleOrNull() ?: return@launch
            val category = state.categories.find { it.id == state.editDialogCategoryId }
                ?: return@launch

            withContext(Dispatchers.IO) {
                val tx = Transaction(
                    id = state.selectedTransaction?.id ?: 0,
                    amount = amount,
                    type = state.selectedMode,
                    categoryId = category.id,
                    categoryName = category.name,
                    categoryColor = category.colorArgb,
                    date = state.editDialogDate
                )
                if (state.selectedTransaction == null) {
                    repository.addTransaction(tx)
                } else {
                    repository.updateTransaction(tx)
                }
            }
            dismissDialog()
            loadData()
        }
    }

    fun deleteTransaction() {
        viewModelScope.launch {
            val txId = _uiState.value.selectedTransaction?.id ?: return@launch
            withContext(Dispatchers.IO) { repository.deleteTransaction(txId) }
            dismissDialog()
            loadData()
        }
    }
}