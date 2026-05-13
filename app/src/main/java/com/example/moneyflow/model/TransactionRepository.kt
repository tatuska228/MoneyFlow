package com.example.moneyflow.model

import android.content.Context
import java.util.Calendar

class TransactionRepository(context: Context) {

    val db = MoneyFlowDatabaseHelper(context)

    // ── Period helpers ────────────────────────────────────────────

    fun getPeriodRange(filter: PeriodFilter): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59); cal.set(Calendar.MILLISECOND, 999)
        val end = cal.timeInMillis

        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        when (filter) {
            PeriodFilter.WEEK  -> cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
            PeriodFilter.MONTH -> cal.set(Calendar.DAY_OF_MONTH, 1)
            PeriodFilter.YEAR  -> cal.set(Calendar.DAY_OF_YEAR, 1)
        }
        return cal.timeInMillis to end
    }

    fun getPeriodLabel(filter: PeriodFilter): String {
        val (start, end) = getPeriodRange(filter)
        val calS = Calendar.getInstance().also { it.timeInMillis = start }
        val calE = Calendar.getInstance().also { it.timeInMillis = end }
        val months = listOf("янв","фев","мар","апр","май","июн",
            "июл","авг","сен","окт","ноя","дек")
        return when (filter) {
            PeriodFilter.WEEK -> {
                val s = "${calS.get(Calendar.DAY_OF_MONTH)} ${months[calS.get(Calendar.MONTH)]}"
                val e = "${calE.get(Calendar.DAY_OF_MONTH)} ${months[calE.get(Calendar.MONTH)]}"
                "$s - $e"
            }
            PeriodFilter.MONTH ->
                months[calS.get(Calendar.MONTH)].replaceFirstChar { it.uppercase() } +
                        " ${calS.get(Calendar.YEAR)}"
            PeriodFilter.YEAR  -> calS.get(Calendar.YEAR).toString()
        }
    }

    // ── Summary ───────────────────────────────────────────────────

    fun getPeriodSummary(userId: Long, filter: PeriodFilter, mode: TransactionType): PeriodSummary {
        val (start, end) = getPeriodRange(filter)
        val transactions = db.getTransactionsByPeriod(userId, mode, start, end)
        val totalAmount  = transactions.sumOf { it.amount }
        val grandTotal   = totalAmount.takeIf { it > 0 } ?: 1.0

        val summaries = transactions.map { tx ->
            TransactionSummary(
                transaction = tx,
                percentage  = (tx.amount / grandTotal * 100).toFloat()
            )
        }

        val planned   = db.getPlannedBudget(userId)
        // Для расходов: остаток = план - фактические расходы (может быть отрицательным)
        // Для доходов:  остаток = план - фактические доходы
        val remainder = planned - totalAmount

        return PeriodSummary(
            totalAmount          = totalAmount,
            plannedBudget        = planned,
            remainder            = remainder,
            transactionSummaries = summaries,
            periodLabel          = getPeriodLabel(filter)
        )
    }

    // ── Transactions ──────────────────────────────────────────────

    fun addTransaction(tx: Transaction): Long    = db.insertTransaction(tx)
    fun updateTransaction(tx: Transaction)       = db.updateTransaction(tx)
    fun deleteTransaction(id: Long, userId: Long) = db.deleteTransaction(id, userId)

    // ── Categories ────────────────────────────────────────────────

    fun getCategories(type: TransactionType, userId: Long): List<Category> =
        db.getCategories(type, userId)

    // ── Planned budget ────────────────────────────────────────────

    fun getPlannedBudget(userId: Long): Double    = db.getPlannedBudget(userId)
    fun setPlannedBudget(userId: Long, amount: Double) = db.setPlannedBudget(userId, amount)

    // ── User ──────────────────────────────────────────────────────

    fun getUserById(userId: Long): User? = db.getUserById(userId)
}