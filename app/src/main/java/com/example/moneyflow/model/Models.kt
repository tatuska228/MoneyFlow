package com.example.moneyflow.model

// ── Transaction type ──────────────────────────────────────────────
enum class TransactionType(val dbValue: String) {
    INCOME("income"),
    EXPENSE("expense");

    companion object {
        fun fromDb(value: String) = entries.first { it.dbValue == value }
    }
}

// ── Period filter ─────────────────────────────────────────────────
enum class PeriodFilter { WEEK, MONTH, YEAR }

// ── Core models ───────────────────────────────────────────────────
data class Transaction(
    val id: Long = 0,
    val userId: Long,
    val amount: Double,
    val type: TransactionType,
    val categoryId: Long,
    val categoryName: String,
    val categoryColor: Long,   // ARGB stored as Long for SQLite
    val date: Long,            // Unix ms
    val note: String = ""
)

data class Category(
    val id: Long = 0,
    val userId: Long?,         // null = default/shared category
    val name: String,
    val colorArgb: Long,
    val type: TransactionType, // INCOME or EXPENSE
    val isDefault: Boolean = false
)

// ── Summary helpers ───────────────────────────────────────────────
data class TransactionSummary(
    val transaction: Transaction,
    val percentage: Float
)

data class PeriodSummary(
    val totalAmount: Double,       // расходы или доходы за период
    val plannedBudget: Double,     // «по плану» — устанавливает пользователь
    val remainder: Double,         // plannedBudget - totalExpenses (может быть < 0)
    val transactionSummaries: List<TransactionSummary>,
    val periodLabel: String
)

// ── Auth models (shared with AuthRepository) ──────────────────────
data class User(
    val id: Long,
    val login: String,
    val passwordHash: String,
    val biometricEnabled: Boolean = false
)

sealed class AuthResult {
    data class Success(val userId: Long) : AuthResult()
    data class Error(val message: String) : AuthResult()
}