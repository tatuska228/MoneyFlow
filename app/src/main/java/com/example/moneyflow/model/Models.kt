package com.example.moneyflow.model

// ── Auth ──────────────────────────────────────────────────────────

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

sealed class BiometricResult {
    object Success   : BiometricResult()
    object Cancelled : BiometricResult()
    data class Error(val message: String) : BiometricResult()
}

// ── Transactions ──────────────────────────────────────────────────

enum class TransactionType(val dbValue: String) {
    INCOME("income"),
    EXPENSE("expense");

    companion object {
        fun fromDb(value: String) = entries.first { it.dbValue == value }
    }
}

enum class PeriodFilter { WEEK, MONTH, YEAR }

data class Transaction(
    val id: Long = 0,
    val userId: Long,
    val amount: Double,
    val type: TransactionType,
    val categoryId: Long,
    val categoryName: String,
    val categoryColor: Long,   // ARGB as Long for SQLite
    val date: Long,            // Unix ms
    val note: String = ""
)

data class Category(
    val id: Long = 0,
    val userId: Long?,         // null = default/shared category
    val name: String,
    val colorArgb: Long,
    val type: TransactionType,
    val isDefault: Boolean = false
)

// ── Summaries ─────────────────────────────────────────────────────

data class TransactionSummary(
    val transaction: Transaction,
    val percentage: Float
)

data class PeriodSummary(
    val totalAmount: Double,
    val plannedBudget: Double,
    val remainder: Double,     // plannedBudget - totalAmount (can be negative)
    val transactionSummaries: List<TransactionSummary>,
    val periodLabel: String
)