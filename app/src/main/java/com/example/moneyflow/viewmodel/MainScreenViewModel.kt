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
import com.example.moneyflow.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class MainUiState(
    val isLoading: Boolean = true,
    val userId: Long = -1L,
    val userLogin: String = "",
    val selectedMode: TransactionType = TransactionType.EXPENSE,
    val selectedPeriod: PeriodFilter = PeriodFilter.WEEK,
    val summary: PeriodSummary? = null,
    val categories: List<Category> = emptyList(),
    // Edit/Add dialog
    val showEditDialog: Boolean = false,
    val isEditing: Boolean = false,
    val editTx: Transaction? = null,
    val editCategoryId: Long = -1L,
    val editDateMs: Long = System.currentTimeMillis(),
    val editAmount: String = "",
    val editNote: String = "",
    // Planned budget dialog
    val showBudgetDialog: Boolean = false,
    val editPlannedBudget: String = "",
    val error: String? = null
)

class MainScreenViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TransactionRepository(application)

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    // ── Init with userId ─────────────────────────────────────────

    fun init(userId: Long) {
        if (_uiState.value.userId == userId) return
        _uiState.value = _uiState.value.copy(userId = userId)
        viewModelScope.launch {
            val user = withContext(Dispatchers.IO) { repository.getUserById(userId) }
            _uiState.value = _uiState.value.copy(userLogin = user?.login ?: "")
            loadData()
        }
    }

    // ── Data loading ─────────────────────────────────────────────

    fun loadData() {
        val userId = _uiState.value.userId
        if (userId == -1L) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val state = _uiState.value
                val summary = withContext(Dispatchers.IO) {
                    repository.getPeriodSummary(userId, state.selectedPeriod, state.selectedMode)
                }
                val categories = withContext(Dispatchers.IO) {
                    repository.getCategories(state.selectedMode, userId)
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    summary = summary,
                    categories = categories,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    // ── Mode / period ────────────────────────────────────────────

    fun selectMode(mode: TransactionType) {
        _uiState.value = _uiState.value.copy(selectedMode = mode)
        loadData()
    }

    fun selectPeriod(period: PeriodFilter) {
        _uiState.value = _uiState.value.copy(selectedPeriod = period)
        loadData()
    }

    // ── Planned budget dialog ────────────────────────────────────

    fun openBudgetDialog() {
        val current = _uiState.value.summary?.plannedBudget ?: 0.0
        _uiState.value = _uiState.value.copy(
            showBudgetDialog = true,
            editPlannedBudget = if (current > 0) current.toInt().toString() else ""
        )
    }

    fun onBudgetAmountChange(v: String) {
        _uiState.value = _uiState.value.copy(editPlannedBudget = v)
    }

    fun savePlannedBudget() {
        val amount = _uiState.value.editPlannedBudget.toDoubleOrNull() ?: return
        val userId = _uiState.value.userId
        viewModelScope.launch {
            withContext(Dispatchers.IO) { repository.setPlannedBudget(userId, amount) }
            _uiState.value = _uiState.value.copy(showBudgetDialog = false)
            loadData()
        }
    }

    fun dismissBudgetDialog() {
        _uiState.value = _uiState.value.copy(showBudgetDialog = false)
    }

    // ── Transaction add dialog ───────────────────────────────────

    fun openAddDialog() {
        val cats = _uiState.value.categories
        _uiState.value = _uiState.value.copy(
            showEditDialog = true,
            isEditing = false,
            editTx = null,
            editCategoryId = cats.firstOrNull()?.id ?: -1L,
            editDateMs = System.currentTimeMillis(),
            editAmount = "",
            editNote = ""
        )
    }

    // ── Transaction edit dialog ──────────────────────────────────

    fun openEditDialog(tx: Transaction) {
        _uiState.value = _uiState.value.copy(
            showEditDialog = true,
            isEditing = true,
            editTx = tx,
            editCategoryId = tx.categoryId,
            editDateMs = tx.date,
            editAmount = tx.amount.toInt().toString(),
            editNote = tx.note
        )
    }

    fun dismissDialog() {
        _uiState.value = _uiState.value.copy(showEditDialog = false)
    }

    fun onDialogCategoryChange(id: Long) {
        _uiState.value = _uiState.value.copy(editCategoryId = id)
    }

    fun onDialogDateChange(ms: Long) {
        _uiState.value = _uiState.value.copy(editDateMs = ms)
    }

    fun onDialogAmountChange(v: String) {
        _uiState.value = _uiState.value.copy(editAmount = v)
    }

    fun onDialogNoteChange(v: String) {
        _uiState.value = _uiState.value.copy(editNote = v)
    }

    // ── Save transaction ─────────────────────────────────────────

    fun saveTransaction() {
        viewModelScope.launch {
            val state = _uiState.value
            val amount = state.editAmount.toDoubleOrNull() ?: return@launch
            val category = state.categories.find { it.id == state.editCategoryId } ?: return@launch
            val userId = state.userId

            withContext(Dispatchers.IO) {
                val tx = Transaction(
                    id = state.editTx?.id ?: 0L,
                    userId = userId,
                    amount = amount,
                    type = state.selectedMode,
                    categoryId = category.id,
                    categoryName = category.name,
                    categoryColor = category.colorArgb,
                    date = state.editDateMs,
                    note = state.editNote
                )
                if (state.isEditing) repository.updateTransaction(tx)
                else repository.addTransaction(tx)
            }
            dismissDialog()
            loadData()
        }
    }

    // ── Delete transaction ───────────────────────────────────────

    fun deleteTransaction() {
        viewModelScope.launch {
            val state = _uiState.value
            val txId = state.editTx?.id ?: return@launch
            withContext(Dispatchers.IO) {
                repository.deleteTransaction(txId, state.userId)
            }
            dismissDialog()
            loadData()
        }
    }
}