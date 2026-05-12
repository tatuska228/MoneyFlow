package com.example.moneyflow.model

import android.content.Context
import java.util.Calendar

class TransactionRepository(context: Context) {

    private val db = DatabaseHelper(context)

    // ── Period helpers ────────────────────────────────────────────

    fun getPeriodRange(filter: PeriodFilter): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        // End = end of today
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val end = cal.timeInMillis

        // Start depends on filter
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        when (filter) {
            PeriodFilter.WEEK -> {
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
            }
            PeriodFilter.MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
            }
            PeriodFilter.YEAR -> {
                cal.set(Calendar.DAY_OF_YEAR, 1)
            }
        }
        return cal.timeInMillis to end
    }

    fun getPeriodLabel(filter: PeriodFilter): String {
        val (start, end) = getPeriodRange(filter)
        val calStart = Calendar.getInstance().also { it.timeInMillis = start }
        val calEnd = Calendar.getInstance().also { it.timeInMillis = end }
        val months = listOf("янв", "фев", "мар", "апр", "май", "июн",
            "июл", "авг", "сен", "окт", "ноя", "дек")
        return when (filter) {
            PeriodFilter.WEEK -> {
                val s = "${calStart.get(Calendar.DAY_OF_MONTH)} ${months[calStart.get(Calendar.MONTH)]}"
                val e = "${calEnd.get(Calendar.DAY_OF_MONTH)} ${months[calEnd.get(Calendar.MONTH)]}"
                "$s - $e"
            }
            PeriodFilter.MONTH -> {
                months[calStart.get(Calendar.MONTH)].replaceFirstChar { it.uppercase() } +
                        " ${calStart.get(Calendar.YEAR)}"
            }
            PeriodFilter.YEAR -> calStart.get(Calendar.YEAR).toString()
        }
    }

    // ── Public API ────────────────────────────────────────────────

    fun getPeriodSummary(filter: PeriodFilter, mode: TransactionType): PeriodSummary {
        val (start, end) = getPeriodRange(filter)
        val totalExpenses = db.getTotalByPeriod(TransactionType.EXPENSE, start, end)
        val totalIncome = db.getTotalByPeriod(TransactionType.INCOME, start, end)
        val categorySummaries = db.getCategorySummaries(mode, start, end)

        // Budget calculations: sum of category budget limits for the period type
        val categories = db.getCategories(mode)
        val plannedTotal = categories.sumOf { it.budgetLimit }
        val actual = if (mode == TransactionType.EXPENSE) totalExpenses else totalIncome
        val remaining = (plannedTotal - actual).coerceAtLeast(0.0)

        return PeriodSummary(
            totalExpenses = totalExpenses,
            totalIncome = totalIncome,
            plannedExpenses = plannedTotal,
            remainingExpenses = if (mode == TransactionType.EXPENSE) remaining else 0.0,
            plannedIncome = plannedTotal,
            remainingIncome = if (mode == TransactionType.INCOME) remaining else 0.0,
            categorySummaries = categorySummaries,
            periodLabel = getPeriodLabel(filter)
        )
    }

    fun getTransactions(type: TransactionType, filter: PeriodFilter): List<Transaction> {
        val (start, end) = getPeriodRange(filter)
        return db.getTransactionsByPeriod(type, start, end)
    }

    fun addTransaction(tx: Transaction): Long = db.insertTransaction(tx)
    fun updateTransaction(tx: Transaction) = db.updateTransaction(tx)
    fun deleteTransaction(id: Long) = db.deleteTransaction(id)

    fun getCategories(type: TransactionType): List<Category> = db.getCategories(type)
    fun addCategory(category: Category): Long = db.insertCategory(category)
    fun updateCategory(category: Category) = db.updateCategory(category)
    fun deleteCategory(id: Long) = db.deleteCategory(id)
}