package com.example.moneyflow.model

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MoneyFlowDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_USERS_TABLE)
        db.execSQL(CREATE_CATEGORIES_TABLE)
        db.execSQL(CREATE_TRANSACTIONS_TABLE)
        db.execSQL(CREATE_BUDGETS_TABLE)
        db.execSQL(CREATE_REGULAR_PAYMENTS_TABLE)
        db.execSQL(CREATE_USER_SETTINGS_TABLE)
        seedDefaultCategories(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        listOf(
            "user_settings", "regular_payments", "budgets",
            "transactions", "categories", "users"
        ).forEach { db.execSQL("DROP TABLE IF EXISTS $it") }
        onCreate(db)
    }

    // ── Default categories seed ───────────────────────────────────

    private fun seedDefaultCategories(db: SQLiteDatabase) {
        val defaults = listOf(
            Triple("Продукты",     0xFF2196F3L, "expense"),
            Triple("Развлечения",  0xFFE53935L, "expense"),
            Triple("Транспорт",    0xFFFF9800L, "expense"),
            Triple("Здоровье",     0xFF9C27B0L, "expense"),
            Triple("Коммунальные", 0xFF009688L, "expense"),
            Triple("Одежда",       0xFFE91E63L, "expense"),
            Triple("Рестораны",    0xFFFFC107L, "expense"),
            Triple("Образование",  0xFF3F51B5L, "expense"),
            Triple("Зарплата",     0xFF4CAF50L, "income"),
            Triple("Фриланс",      0xFF009688L, "income"),
            Triple("Премия",       0xFFFF9800L, "income"),
            Triple("Подарок",      0xFFE91E63L, "income"),
            Triple("Инвестиции",   0xFF3F51B5L, "income")
        )
        defaults.forEach { (name, color, type) ->
            db.insert(TABLE_CATEGORIES, null, ContentValues().apply {
                putNull(COL_CAT_USER_ID)
                put(COL_CAT_NAME, name)
                put(COL_CAT_COLOR, color)
                put(COL_CAT_TYPE, type)
                put(COL_CAT_IS_DEFAULT, 1)
            })
        }
    }

    // ── Constants ─────────────────────────────────────────────────

    companion object {
        const val DATABASE_NAME    = "moneyflow.db"
        const val DATABASE_VERSION = 4  // bumped to force clean upgrade

        const val TABLE_USERS            = "users"
        const val COL_USER_ID            = "id"
        const val COL_USER_LOGIN         = "login"
        const val COL_USER_PASSWORD_HASH = "password_hash"
        const val COL_USER_BIOMETRIC     = "biometric_enabled"
        const val COL_USER_CREATED_AT    = "created_at"

        const val TABLE_CATEGORIES   = "categories"
        const val COL_CAT_ID         = "id"
        const val COL_CAT_USER_ID    = "user_id"
        const val COL_CAT_NAME       = "name"
        const val COL_CAT_COLOR      = "color_argb"
        const val COL_CAT_TYPE       = "type"
        const val COL_CAT_IS_DEFAULT = "is_default"

        const val TABLE_TRANSACTIONS    = "transactions"
        const val COL_TX_ID             = "id"
        const val COL_TX_USER_ID        = "user_id"
        const val COL_TX_CATEGORY_ID    = "category_id"
        const val COL_TX_CATEGORY_NAME  = "category_name"
        const val COL_TX_CATEGORY_COLOR = "category_color"
        const val COL_TX_AMOUNT         = "amount"
        const val COL_TX_TYPE           = "type"
        const val COL_TX_NOTE           = "note"
        const val COL_TX_DATE           = "date"

        const val TABLE_BUDGETS  = "budgets"
        const val TABLE_REGULAR  = "regular_payments"

        const val TABLE_SETTINGS          = "user_settings"
        const val COL_SET_USER_ID         = "user_id"
        const val COL_SET_PLANNED_BUDGET  = "planned_budget"

        // ── DDL ───────────────────────────────────────────────────

        const val CREATE_USERS_TABLE =
            "CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "login TEXT UNIQUE NOT NULL, " +
                    "password_hash TEXT NOT NULL, " +
                    "biometric_enabled INTEGER NOT NULL DEFAULT 0, " +
                    "created_at INTEGER NOT NULL DEFAULT (strftime('%s','now')))"

        const val CREATE_CATEGORIES_TABLE =
            "CREATE TABLE IF NOT EXISTS categories (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER, " +
                    "name TEXT NOT NULL, " +
                    "color_argb INTEGER NOT NULL DEFAULT 0, " +
                    "type TEXT NOT NULL CHECK(type IN ('income','expense')), " +
                    "is_default INTEGER NOT NULL DEFAULT 0)"

        const val CREATE_TRANSACTIONS_TABLE =
            "CREATE TABLE IF NOT EXISTS transactions (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER NOT NULL, " +
                    "category_id INTEGER NOT NULL DEFAULT 0, " +
                    "category_name TEXT NOT NULL DEFAULT '', " +
                    "category_color INTEGER NOT NULL DEFAULT 0, " +
                    "amount REAL NOT NULL, " +
                    "type TEXT NOT NULL CHECK(type IN ('income','expense')), " +
                    "note TEXT NOT NULL DEFAULT '', " +
                    "date INTEGER NOT NULL)"

        const val CREATE_BUDGETS_TABLE =
            "CREATE TABLE IF NOT EXISTS budgets (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER NOT NULL, " +
                    "category_id INTEGER NOT NULL, " +
                    "amount REAL NOT NULL, " +
                    "period TEXT NOT NULL)"

        const val CREATE_REGULAR_PAYMENTS_TABLE =
            "CREATE TABLE IF NOT EXISTS regular_payments (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER NOT NULL, " +
                    "category_id INTEGER, " +
                    "amount REAL NOT NULL, " +
                    "type TEXT NOT NULL, " +
                    "note TEXT, " +
                    "interval_days INTEGER NOT NULL, " +
                    "next_date INTEGER NOT NULL)"

        const val CREATE_USER_SETTINGS_TABLE =
            "CREATE TABLE IF NOT EXISTS user_settings (" +
                    "user_id INTEGER PRIMARY KEY, " +
                    "planned_budget REAL NOT NULL DEFAULT 0)"
    }

    // ── Users ─────────────────────────────────────────────────────

    fun insertUser(login: String, passwordHash: String, biometric: Boolean = false): Long =
        writableDatabase.insert(TABLE_USERS, null, ContentValues().apply {
            put(COL_USER_LOGIN, login)
            put(COL_USER_PASSWORD_HASH, passwordHash)
            put(COL_USER_BIOMETRIC, if (biometric) 1 else 0)
        })

    fun getUserByLoginAndHash(login: String, hash: String): User? =
        readableDatabase.query(
            TABLE_USERS,
            arrayOf(COL_USER_ID, COL_USER_LOGIN, COL_USER_PASSWORD_HASH, COL_USER_BIOMETRIC),
            "$COL_USER_LOGIN = ? AND $COL_USER_PASSWORD_HASH = ?",
            arrayOf(login, hash), null, null, null
        ).use { c ->
            if (c.moveToFirst()) User(c.getLong(0), c.getString(1), c.getString(2), c.getInt(3) == 1)
            else null
        }

    fun getUserById(userId: Long): User? =
        readableDatabase.query(
            TABLE_USERS,
            arrayOf(COL_USER_ID, COL_USER_LOGIN, COL_USER_PASSWORD_HASH, COL_USER_BIOMETRIC),
            "$COL_USER_ID = ?", arrayOf(userId.toString()), null, null, null
        ).use { c ->
            if (c.moveToFirst()) User(c.getLong(0), c.getString(1), c.getString(2), c.getInt(3) == 1)
            else null
        }

    fun getAllUsers(): List<User> {
        val list = mutableListOf<User>()
        readableDatabase.query(
            TABLE_USERS,
            arrayOf(COL_USER_ID, COL_USER_LOGIN, COL_USER_PASSWORD_HASH, COL_USER_BIOMETRIC),
            null, null, null, null, "$COL_USER_CREATED_AT ASC"
        ).use { c -> while (c.moveToNext()) list += User(c.getLong(0), c.getString(1), c.getString(2), c.getInt(3) == 1) }
        return list
    }

    fun setBiometricEnabled(userId: Long, enabled: Boolean) {
        writableDatabase.update(
            TABLE_USERS,
            ContentValues().apply { put(COL_USER_BIOMETRIC, if (enabled) 1 else 0) },
            "$COL_USER_ID = ?", arrayOf(userId.toString())
        )
    }

    // ── Categories ────────────────────────────────────────────────

    /** Returns default categories + categories created by this user */
    fun getCategories(type: TransactionType, userId: Long): List<Category> {
        val list = mutableListOf<Category>()
        readableDatabase.rawQuery(
            "SELECT $COL_CAT_ID, $COL_CAT_USER_ID, $COL_CAT_NAME, $COL_CAT_COLOR, $COL_CAT_TYPE, $COL_CAT_IS_DEFAULT " +
                    "FROM $TABLE_CATEGORIES " +
                    "WHERE $COL_CAT_TYPE = ? AND ($COL_CAT_IS_DEFAULT = 1 OR $COL_CAT_USER_ID = ?) " +
                    "ORDER BY $COL_CAT_IS_DEFAULT DESC, $COL_CAT_NAME ASC",
            arrayOf(type.dbValue, userId.toString())
        ).use { c ->
            while (c.moveToNext()) list += Category(
                id        = c.getLong(0),
                userId    = if (c.isNull(1)) null else c.getLong(1),
                name      = c.getString(2),
                colorArgb = c.getLong(3),
                type      = TransactionType.fromDb(c.getString(4)),
                isDefault = c.getInt(5) == 1
            )
        }
        return list
    }

    // ── Transactions ──────────────────────────────────────────────

    fun insertTransaction(tx: Transaction): Long =
        writableDatabase.insert(TABLE_TRANSACTIONS, null, ContentValues().apply {
            put(COL_TX_USER_ID,        tx.userId)
            put(COL_TX_CATEGORY_ID,    tx.categoryId)
            put(COL_TX_CATEGORY_NAME,  tx.categoryName)
            put(COL_TX_CATEGORY_COLOR, tx.categoryColor)
            put(COL_TX_AMOUNT,         tx.amount)
            put(COL_TX_TYPE,           tx.type.dbValue)
            put(COL_TX_NOTE,           tx.note)
            put(COL_TX_DATE,           tx.date)
        })

    fun updateTransaction(tx: Transaction) {
        writableDatabase.update(
            TABLE_TRANSACTIONS,
            ContentValues().apply {
                put(COL_TX_CATEGORY_ID,    tx.categoryId)
                put(COL_TX_CATEGORY_NAME,  tx.categoryName)
                put(COL_TX_CATEGORY_COLOR, tx.categoryColor)
                put(COL_TX_AMOUNT,         tx.amount)
                put(COL_TX_TYPE,           tx.type.dbValue)
                put(COL_TX_NOTE,           tx.note)
                put(COL_TX_DATE,           tx.date)
            },
            "$COL_TX_ID = ? AND $COL_TX_USER_ID = ?",
            arrayOf(tx.id.toString(), tx.userId.toString())
        )
    }

    fun deleteTransaction(txId: Long, userId: Long) {
        writableDatabase.delete(
            TABLE_TRANSACTIONS,
            "$COL_TX_ID = ? AND $COL_TX_USER_ID = ?",
            arrayOf(txId.toString(), userId.toString())
        )
    }

    fun getTransactionsByPeriod(
        userId: Long, type: TransactionType, startMs: Long, endMs: Long
    ): List<Transaction> {
        val list = mutableListOf<Transaction>()
        readableDatabase.rawQuery(
            "SELECT $COL_TX_ID, $COL_TX_USER_ID, $COL_TX_AMOUNT, $COL_TX_TYPE, " +
                    "$COL_TX_CATEGORY_ID, $COL_TX_CATEGORY_NAME, $COL_TX_CATEGORY_COLOR, $COL_TX_DATE, $COL_TX_NOTE " +
                    "FROM $TABLE_TRANSACTIONS " +
                    "WHERE $COL_TX_USER_ID = ? AND $COL_TX_TYPE = ? AND $COL_TX_DATE >= ? AND $COL_TX_DATE <= ? " +
                    "ORDER BY $COL_TX_DATE DESC",
            arrayOf(userId.toString(), type.dbValue, startMs.toString(), endMs.toString())
        ).use { c ->
            while (c.moveToNext()) list += Transaction(
                id            = c.getLong(0),
                userId        = c.getLong(1),
                amount        = c.getDouble(2),
                type          = TransactionType.fromDb(c.getString(3)),
                categoryId    = c.getLong(4),
                categoryName  = c.getString(5),
                categoryColor = c.getLong(6),
                date          = c.getLong(7),
                note          = c.getString(8)
            )
        }
        return list
    }

    fun getTotalByPeriod(userId: Long, type: TransactionType, startMs: Long, endMs: Long): Double =
        readableDatabase.rawQuery(
            "SELECT COALESCE(SUM($COL_TX_AMOUNT), 0) FROM $TABLE_TRANSACTIONS " +
                    "WHERE $COL_TX_USER_ID = ? AND $COL_TX_TYPE = ? AND $COL_TX_DATE >= ? AND $COL_TX_DATE <= ?",
            arrayOf(userId.toString(), type.dbValue, startMs.toString(), endMs.toString())
        ).use { c -> if (c.moveToFirst()) c.getDouble(0) else 0.0 }

    // ── User settings ─────────────────────────────────────────────

    fun getPlannedBudget(userId: Long): Double =
        readableDatabase.query(
            TABLE_SETTINGS, arrayOf(COL_SET_PLANNED_BUDGET),
            "$COL_SET_USER_ID = ?", arrayOf(userId.toString()), null, null, null
        ).use { c -> if (c.moveToFirst()) c.getDouble(0) else 0.0 }

    fun setPlannedBudget(userId: Long, amount: Double) {
        writableDatabase.insertWithOnConflict(
            TABLE_SETTINGS,
            null,
            ContentValues().apply {
                put(COL_SET_USER_ID, userId)
                put(COL_SET_PLANNED_BUDGET, amount)
            },
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }
}