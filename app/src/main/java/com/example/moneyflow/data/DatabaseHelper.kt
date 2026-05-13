package com.example.moneyflow.data.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(
    context, DATABASE_NAME, null, DATABASE_VERSION
) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_USERS)
        db.execSQL(SQL_CREATE_CATEGORIES)
        db.execSQL(SQL_CREATE_TRANSACTIONS)
        db.execSQL(SQL_CREATE_BUDGETS)
        db.execSQL(SQL_CREATE_REGULAR_PAYMENTS)
        // Insert default user
        db.execSQL("INSERT INTO $TABLE_USERS (login, password_hash) VALUES ('user', '${hashPassword("1234")}')")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CATEGORIES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TRANSACTIONS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_BUDGETS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_REGULAR_PAYMENTS")
        onCreate(db)
    }

    companion object {
        const val DATABASE_NAME = "moneyflow.db"
        const val DATABASE_VERSION = 1

        // Tables
        const val TABLE_USERS = "users"
        const val TABLE_CATEGORIES = "categories"
        const val TABLE_TRANSACTIONS = "transactions"
        const val TABLE_BUDGETS = "budgets"
        const val TABLE_REGULAR_PAYMENTS = "regular_payments"

        // Users columns
        const val COL_USER_ID = "id"
        const val COL_USER_LOGIN = "login"
        const val COL_USER_PASSWORD_HASH = "password_hash"

        // Categories columns
        const val COL_CATEGORY_ID = "id"
        const val COL_CATEGORY_NAME = "name"
        const val COL_CATEGORY_TYPE = "type" // "income" | "expense"
        const val COL_CATEGORY_ICON = "icon"
        const val COL_CATEGORY_COLOR = "color"

        // Transactions columns
        const val COL_TRANSACTION_ID = "id"
        const val COL_TRANSACTION_AMOUNT = "amount"
        const val COL_TRANSACTION_TYPE = "type" // "income" | "expense"
        const val COL_TRANSACTION_CATEGORY_ID = "category_id"
        const val COL_TRANSACTION_DATE = "date"
        const val COL_TRANSACTION_NOTE = "note"
        const val COL_TRANSACTION_USER_ID = "user_id"

        // Budgets columns
        const val COL_BUDGET_ID = "id"
        const val COL_BUDGET_CATEGORY_ID = "category_id"
        const val COL_BUDGET_LIMIT = "budget_limit"
        const val COL_BUDGET_MONTH = "month" // "YYYY-MM"
        const val COL_BUDGET_USER_ID = "user_id"

        // Regular payments columns
        const val COL_REGULAR_ID = "id"
        const val COL_REGULAR_NAME = "name"
        const val COL_REGULAR_AMOUNT = "amount"
        const val COL_REGULAR_CATEGORY_ID = "category_id"
        const val COL_REGULAR_INTERVAL_DAYS = "interval_days"
        const val COL_REGULAR_NEXT_DATE = "next_date"
        const val COL_REGULAR_USER_ID = "user_id"

        private val SQL_CREATE_USERS = """
            CREATE TABLE $TABLE_USERS (
                $COL_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_USER_LOGIN TEXT NOT NULL UNIQUE,
                $COL_USER_PASSWORD_HASH TEXT NOT NULL
            )
        """.trimIndent()

        private val SQL_CREATE_CATEGORIES = """
            CREATE TABLE $TABLE_CATEGORIES (
                $COL_CATEGORY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_CATEGORY_NAME TEXT NOT NULL,
                $COL_CATEGORY_TYPE TEXT NOT NULL,
                $COL_CATEGORY_ICON TEXT,
                $COL_CATEGORY_COLOR TEXT
            )
        """.trimIndent()

        private val SQL_CREATE_TRANSACTIONS = """
            CREATE TABLE $TABLE_TRANSACTIONS (
                $COL_TRANSACTION_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_TRANSACTION_AMOUNT REAL NOT NULL,
                $COL_TRANSACTION_TYPE TEXT NOT NULL,
                $COL_TRANSACTION_CATEGORY_ID INTEGER,
                $COL_TRANSACTION_DATE TEXT NOT NULL,
                $COL_TRANSACTION_NOTE TEXT,
                $COL_TRANSACTION_USER_ID INTEGER NOT NULL,
                FOREIGN KEY ($COL_TRANSACTION_CATEGORY_ID) REFERENCES $TABLE_CATEGORIES($COL_CATEGORY_ID),
                FOREIGN KEY ($COL_TRANSACTION_USER_ID) REFERENCES $TABLE_USERS($COL_USER_ID)
            )
        """.trimIndent()

        private val SQL_CREATE_BUDGETS = """
            CREATE TABLE $TABLE_BUDGETS (
                $COL_BUDGET_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_BUDGET_CATEGORY_ID INTEGER NOT NULL,
                $COL_BUDGET_LIMIT REAL NOT NULL,
                $COL_BUDGET_MONTH TEXT NOT NULL,
                $COL_BUDGET_USER_ID INTEGER NOT NULL,
                FOREIGN KEY ($COL_BUDGET_CATEGORY_ID) REFERENCES $TABLE_CATEGORIES($COL_CATEGORY_ID),
                FOREIGN KEY ($COL_BUDGET_USER_ID) REFERENCES $TABLE_USERS($COL_USER_ID)
            )
        """.trimIndent()

        private val SQL_CREATE_REGULAR_PAYMENTS = """
            CREATE TABLE $TABLE_REGULAR_PAYMENTS (
                $COL_REGULAR_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_REGULAR_NAME TEXT NOT NULL,
                $COL_REGULAR_AMOUNT REAL NOT NULL,
                $COL_REGULAR_CATEGORY_ID INTEGER,
                $COL_REGULAR_INTERVAL_DAYS INTEGER NOT NULL,
                $COL_REGULAR_NEXT_DATE TEXT NOT NULL,
                $COL_REGULAR_USER_ID INTEGER NOT NULL,
                FOREIGN KEY ($COL_REGULAR_CATEGORY_ID) REFERENCES $TABLE_CATEGORIES($COL_CATEGORY_ID),
                FOREIGN KEY ($COL_REGULAR_USER_ID) REFERENCES $TABLE_USERS($COL_USER_ID)
            )
        """.trimIndent()

        fun hashPassword(password: String): String {
            val md = java.security.MessageDigest.getInstance("SHA-256")
            val digest = md.digest(password.toByteArray())
            return digest.joinToString("") { "%02x".format(it) }
        }
    }
}
