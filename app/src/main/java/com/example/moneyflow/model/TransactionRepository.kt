package com.example.moneyflow.model

import android.content.Context
import java.util.Calendar

class TransactionRepository(val db: MoneyFlowDatabaseHelper) {

    /** Construct with Context — creates its own DB helper */
    constructor(context: Context) : this(db = MoneyFlowDatabaseHelper(context))

    // ── Period helpers ────────────────────────────────────────────

    fun getPeriodRange(filter: PeriodFilter): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59);      cal.set(Calendar.MILLISECOND, 999)
        val end = cal.timeInMillis

        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0);      cal.set(Calendar.MILLISECOND, 0)
        when (filter) {
            PeriodFilter.WEEK  -> cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
            PeriodFilter.MONTH -> cal.set(Calendar.DAY_OF_MONTH, 1)
            PeriodFilter.YEAR  -> cal.set(Calendar.DAY_OF_YEAR, 1)
        }
        return cal.timeInMillis to end
    }

    fun getPeriodLabel(filter: PeriodFilter): String {
        val (start, end) = getPeriodRange(filter)
        val s = Calendar.getInstance().also { it.timeInMillis = start }
        val e = Calendar.getInstance().also { it.timeInMillis = end }
        val months = listOf("янв","фев","мар","апр","май","июн",
            "июл","авг","сен","окт","ноя","дек")
        return when (filter) {
            PeriodFilter.WEEK ->
                "${s.get(Calendar.DAY_OF_MONTH)} ${months[s.get(Calendar.MONTH)]} - " +
                        "${e.get(Calendar.DAY_OF_MONTH)} ${months[e.get(Calendar.MONTH)]}"
            PeriodFilter.MONTH ->
                months[s.get(Calendar.MONTH)].replaceFirstChar { it.uppercase() } +
                        " ${s.get(Calendar.YEAR)}"
            PeriodFilter.YEAR -> s.get(Calendar.YEAR).toString()
        }
    }

    // ── Summary ───────────────────────────────────────────────────

    fun getPeriodSummary(userId: Long, filter: PeriodFilter, mode: TransactionType): PeriodSummary {
        val (start, end) = getPeriodRange(filter)
        val transactions = db.getTransactionsByPeriod(userId, mode, start, end)
        val totalAmount  = transactions.sumOf { it.amount }
        val grandTotal   = if (totalAmount > 0) totalAmount else 1.0

        val summaries = transactions.map { tx ->
            TransactionSummary(tx, (tx.amount / grandTotal * 100).toFloat())
        }

        val planned   = db.getPlannedBudget(userId)
        val remainder = planned - totalAmount   // intentionally can be negative

        return PeriodSummary(
            totalAmount          = totalAmount,
            plannedBudget        = planned,
            remainder            = remainder,
            transactionSummaries = summaries,
            periodLabel          = getPeriodLabel(filter)
        )
    }

    // ── CRUD ──────────────────────────────────────────────────────

    fun addTransaction(tx: Transaction): Long     = db.insertTransaction(tx)
    fun updateTransaction(tx: Transaction)        = db.updateTransaction(tx)
    fun deleteTransaction(id: Long, userId: Long) = db.deleteTransaction(id, userId)

    fun getCategories(type: TransactionType, userId: Long): List<Category> =
        db.getCategories(type, userId)

    fun getPlannedBudget(userId: Long): Double          = db.getPlannedBudget(userId)
    fun setPlannedBudget(userId: Long, amount: Double)  = db.setPlannedBudget(userId, amount)

    fun getUserById(userId: Long): User? = db.getUserById(userId)
}