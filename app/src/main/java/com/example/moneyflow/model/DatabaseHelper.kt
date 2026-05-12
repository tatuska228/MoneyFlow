package com.example.moneyflow.model

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.moneyflow.ui.theme.CategoryBlue
import com.example.moneyflow.ui.theme.CategoryOrange
import com.example.moneyflow.ui.theme.CategoryPurple
import com.example.moneyflow.ui.theme.CategoryRed
import com.example.moneyflow.ui.theme.CategoryTeal
import com.example.moneyflow.ui.theme.PrimaryGreen

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "moneyflow.db"
        const val DATABASE_VERSION = 1

        // Transactions table
        const val TABLE_TRANSACTIONS = "transactions"
        const val COL_ID = "id"
        const val COL_AMOUNT = "amount"
        const val COL_TYPE = "type"
        const val COL_CATEGORY_ID = "category_id"
        const val COL_CATEGORY_NAME = "category_name"
        const val COL_CATEGORY_COLOR = "category_color"
        const val COL_DATE = "date"
        const val COL_NOTE = "note"

        // Categories table
        const val TABLE_CATEGORIES = "categories"
        const val COL_CAT_ID = "id"
        const val COL_CAT_NAME = "name"
        const val COL_CAT_COLOR = "color_argb"
        const val COL_CAT_TYPE = "type"
        const val COL_CAT_BUDGET = "budget_limit"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_CATEGORIES (
                $COL_CAT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_CAT_NAME TEXT NOT NULL,
                $COL_CAT_COLOR INTEGER NOT NULL,
                $COL_CAT_TYPE TEXT NOT NULL,
                $COL_CAT_BUDGET REAL DEFAULT 0
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE $TABLE_TRANSACTIONS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_AMOUNT REAL NOT NULL,
                $COL_TYPE TEXT NOT NULL,
                $COL_CATEGORY_ID INTEGER NOT NULL,
                $COL_CATEGORY_NAME TEXT NOT NULL,
                $COL_CATEGORY_COLOR INTEGER NOT NULL,
                $COL_DATE INTEGER NOT NULL,
                $COL_NOTE TEXT DEFAULT '',
                FOREIGN KEY ($COL_CATEGORY_ID) REFERENCES $TABLE_CATEGORIES($COL_CAT_ID)
            )
            """.trimIndent()
        )

        // Seed default expense categories
        val expenseCategories = listOf(
            Triple("Продукты", CategoryBlue.value.toInt(), TransactionType.EXPENSE),
            Triple("Развлечения", CategoryRed.value.toInt(), TransactionType.EXPENSE),
            Triple("Транспорт", CategoryOrange.value.toInt(), TransactionType.EXPENSE),
            Triple("Здоровье", CategoryPurple.value.toInt(), TransactionType.EXPENSE),
            Triple("Коммунальные", CategoryTeal.value.toInt(), TransactionType.EXPENSE)
        )
        val incomeCategories = listOf(
            Triple("Зарплата", PrimaryGreen.value.toInt(), TransactionType.INCOME),
            Triple("Фриланс", CategoryTeal.value.toInt(), TransactionType.INCOME)
        )
        (expenseCategories + incomeCategories).forEach { (name, color, type) ->
            val cv = ContentValues().apply {
                put(COL_CAT_NAME, name)
                put(COL_CAT_COLOR, color)
                put(COL_CAT_TYPE, type.name)
                put(COL_CAT_BUDGET, 0.0)
            }
            db.insert(TABLE_CATEGORIES, null, cv)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TRANSACTIONS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CATEGORIES")
        onCreate(db)
    }

    // ── Categories ────────────────────────────────────────────────

    fun getCategories(type: TransactionType): List<Category> {
        val list = mutableListOf<Category>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_CATEGORIES, null,
            "$COL_CAT_TYPE = ?", arrayOf(type.name),
            null, null, "$COL_CAT_NAME ASC"
        )
        cursor.use {
            while (it.moveToNext()) {
                list += Category(
                    id = it.getLong(it.getColumnIndexOrThrow(COL_CAT_ID)),
                    name = it.getString(it.getColumnIndexOrThrow(COL_CAT_NAME)),
                    colorArgb = it.getInt(it.getColumnIndexOrThrow(COL_CAT_COLOR)),
                    type = TransactionType.valueOf(it.getString(it.getColumnIndexOrThrow(COL_CAT_TYPE))),
                    budgetLimit = it.getDouble(it.getColumnIndexOrThrow(COL_CAT_BUDGET))
                )
            }
        }
        return list
    }

    fun insertCategory(category: Category): Long {
        val cv = ContentValues().apply {
            put(COL_CAT_NAME, category.name)
            put(COL_CAT_COLOR, category.colorArgb)
            put(COL_CAT_TYPE, category.type.name)
            put(COL_CAT_BUDGET, category.budgetLimit)
        }
        return writableDatabase.insert(TABLE_CATEGORIES, null, cv)
    }

    fun updateCategory(category: Category) {
        val cv = ContentValues().apply {
            put(COL_CAT_NAME, category.name)
            put(COL_CAT_COLOR, category.colorArgb)
            put(COL_CAT_BUDGET, category.budgetLimit)
        }
        writableDatabase.update(
            TABLE_CATEGORIES, cv,
            "$COL_CAT_ID = ?", arrayOf(category.id.toString())
        )
    }

    fun deleteCategory(categoryId: Long) {
        writableDatabase.delete(
            TABLE_CATEGORIES,
            "$COL_CAT_ID = ?",
            arrayOf(categoryId.toString())
        )
    }

    // ── Transactions ──────────────────────────────────────────────

    fun insertTransaction(tx: Transaction): Long {
        val cv = ContentValues().apply {
            put(COL_AMOUNT, tx.amount)
            put(COL_TYPE, tx.type.name)
            put(COL_CATEGORY_ID, tx.categoryId)
            put(COL_CATEGORY_NAME, tx.categoryName)
            put(COL_CATEGORY_COLOR, tx.categoryColor)
            put(COL_DATE, tx.date)
            put(COL_NOTE, tx.note)
        }
        return writableDatabase.insert(TABLE_TRANSACTIONS, null, cv)
    }

    fun updateTransaction(tx: Transaction) {
        val cv = ContentValues().apply {
            put(COL_AMOUNT, tx.amount)
            put(COL_TYPE, tx.type.name)
            put(COL_CATEGORY_ID, tx.categoryId)
            put(COL_CATEGORY_NAME, tx.categoryName)
            put(COL_CATEGORY_COLOR, tx.categoryColor)
            put(COL_DATE, tx.date)
            put(COL_NOTE, tx.note)
        }
        writableDatabase.update(
            TABLE_TRANSACTIONS, cv,
            "$COL_ID = ?", arrayOf(tx.id.toString())
        )
    }

    fun deleteTransaction(transactionId: Long) {
        writableDatabase.delete(
            TABLE_TRANSACTIONS,
            "$COL_ID = ?",
            arrayOf(transactionId.toString())
        )
    }

    fun getTransactionsByPeriod(
        type: TransactionType,
        startMs: Long,
        endMs: Long
    ): List<Transaction> {
        val list = mutableListOf<Transaction>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_TRANSACTIONS, null,
            "$COL_TYPE = ? AND $COL_DATE >= ? AND $COL_DATE <= ?",
            arrayOf(type.name, startMs.toString(), endMs.toString()),
            null, null, "$COL_DATE DESC"
        )
        cursor.use {
            while (it.moveToNext()) {
                list += Transaction(
                    id = it.getLong(it.getColumnIndexOrThrow(COL_ID)),
                    amount = it.getDouble(it.getColumnIndexOrThrow(COL_AMOUNT)),
                    type = TransactionType.valueOf(it.getString(it.getColumnIndexOrThrow(COL_TYPE))),
                    categoryId = it.getLong(it.getColumnIndexOrThrow(COL_CATEGORY_ID)),
                    categoryName = it.getString(it.getColumnIndexOrThrow(COL_CATEGORY_NAME)),
                    categoryColor = it.getInt(it.getColumnIndexOrThrow(COL_CATEGORY_COLOR)),
                    date = it.getLong(it.getColumnIndexOrThrow(COL_DATE)),
                    note = it.getString(it.getColumnIndexOrThrow(COL_NOTE))
                )
            }
        }
        return list
    }

    fun getCategorySummaries(
        type: TransactionType,
        startMs: Long,
        endMs: Long
    ): List<CategorySummary> {
        val db = readableDatabase
        val cursor = db.rawQuery(
            """
            SELECT t.$COL_CATEGORY_ID, t.$COL_CATEGORY_NAME, t.$COL_CATEGORY_COLOR,
                   SUM(t.$COL_AMOUNT) as total,
                   c.$COL_CAT_BUDGET
            FROM $TABLE_TRANSACTIONS t
            LEFT JOIN $TABLE_CATEGORIES c ON t.$COL_CATEGORY_ID = c.$COL_CAT_ID
            WHERE t.$COL_TYPE = ? AND t.$COL_DATE >= ? AND t.$COL_DATE <= ?
            GROUP BY t.$COL_CATEGORY_ID
            ORDER BY total DESC
            """.trimIndent(),
            arrayOf(type.name, startMs.toString(), endMs.toString())
        )
        val rawList = mutableListOf<Pair<Category, Double>>()
        cursor.use {
            while (it.moveToNext()) {
                val cat = Category(
                    id = it.getLong(0),
                    name = it.getString(1),
                    colorArgb = it.getInt(2),
                    type = type,
                    budgetLimit = it.getDouble(4)
                )
                rawList += cat to it.getDouble(3)
            }
        }
        val grandTotal = rawList.sumOf { it.second }.takeIf { it > 0 } ?: 1.0
        return rawList.map { (cat, total) ->
            CategorySummary(
                category = cat,
                totalAmount = total,
                percentage = (total / grandTotal * 100).toFloat()
            )
        }
    }

    fun getTotalByPeriod(type: TransactionType, startMs: Long, endMs: Long): Double {
        val cursor = readableDatabase.rawQuery(
            "SELECT SUM($COL_AMOUNT) FROM $TABLE_TRANSACTIONS WHERE $COL_TYPE=? AND $COL_DATE>=? AND $COL_DATE<=?",
            arrayOf(type.name, startMs.toString(), endMs.toString())
        )
        return cursor.use { if (it.moveToFirst()) it.getDouble(0) else 0.0 }
    }
}