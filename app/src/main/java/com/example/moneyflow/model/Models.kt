package com.example.moneyflow.model

import androidx.compose.ui.graphics.Color

enum class TransactionType {
    INCOME, EXPENSE
}

data class Transaction(
    val id: Long = 0,
    val amount: Double,
    val type: TransactionType,
    val categoryId: Long,
    val categoryName: String,
    val categoryColor: Int, // stored as ARGB Int for SQLite
    val date: Long,         // Unix timestamp ms
    val note: String = ""
)

data class Category(
    val id: Long = 0,
    val name: String,
    val colorArgb: Int,
    val type: TransactionType,
    val budgetLimit: Double = 0.0
)

data class CategorySummary(
    val category: Category,
    val totalAmount: Double,
    val percentage: Float
)

enum class PeriodFilter {
    WEEK, MONTH, YEAR
}

data class PeriodSummary(
    val totalExpenses: Double,
    val totalIncome: Double,
    val plannedExpenses: Double,
    val remainingExpenses: Double,
    val plannedIncome: Double,
    val remainingIncome: Double,
    val categorySummaries: List<CategorySummary>,
    val periodLabel: String
)