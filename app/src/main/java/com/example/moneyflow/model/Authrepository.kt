package com.example.moneyflow.model

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// ── Result wrapper ──────────────────────────────────────────────────────────

sealed class AuthResult {
    data class Success(val userId: Long) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

// ── User model ──────────────────────────────────────────────────────────────

data class User(
    val id: Long,
    val login: String,
    val passwordHash: String
)

// ── DB Helper ───────────────────────────────────────────────────────────────

class MoneyFlowDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_USERS_TABLE)
        db.execSQL(CREATE_TRANSACTIONS_TABLE)
        db.execSQL(CREATE_CATEGORIES_TABLE)
        db.execSQL(CREATE_BUDGETS_TABLE)
        db.execSQL(CREATE_REGULAR_PAYMENTS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TRANSACTIONS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CATEGORIES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_BUDGETS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_REGULAR_PAYMENTS")
        onCreate(db)
    }

    companion object {
        const val DATABASE_NAME = "moneyflow.db"
        const val DATABASE_VERSION = 1

        // Tables
        const val TABLE_USERS = "users"
        const val TABLE_TRANSACTIONS = "transactions"
        const val TABLE_CATEGORIES = "categories"
        const val TABLE_BUDGETS = "budgets"
        const val TABLE_REGULAR_PAYMENTS = "regular_payments"

        // Users columns
        const val COL_USER_ID = "id"
        const val COL_USER_LOGIN = "login"
        const val COL_USER_PASSWORD_HASH = "password_hash"
        const val COL_USER_CREATED_AT = "created_at"

        private const val CREATE_USERS_TABLE = """
            CREATE TABLE IF NOT EXISTS $TABLE_USERS (
                $COL_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_USER_LOGIN TEXT UNIQUE NOT NULL,
                $COL_USER_PASSWORD_HASH TEXT NOT NULL,
                $COL_USER_CREATED_AT INTEGER NOT NULL DEFAULT (strftime('%s','now'))
            )
        """

        private const val CREATE_TRANSACTIONS_TABLE = """
            CREATE TABLE IF NOT EXISTS $TABLE_TRANSACTIONS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                category_id INTEGER,
                amount REAL NOT NULL,
                type TEXT NOT NULL CHECK(type IN ('income','expense')),
                note TEXT,
                date INTEGER NOT NULL DEFAULT (strftime('%s','now')),
                FOREIGN KEY(user_id) REFERENCES $TABLE_USERS(id),
                FOREIGN KEY(category_id) REFERENCES $TABLE_CATEGORIES(id)
            )
        """

        private const val CREATE_CATEGORIES_TABLE = """
            CREATE TABLE IF NOT EXISTS $TABLE_CATEGORIES (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER,
                name TEXT NOT NULL,
                icon TEXT,
                color TEXT,
                type TEXT NOT NULL CHECK(type IN ('income','expense','both')),
                is_default INTEGER NOT NULL DEFAULT 0,
                FOREIGN KEY(user_id) REFERENCES $TABLE_USERS(id)
            )
        """

        private const val CREATE_BUDGETS_TABLE = """
            CREATE TABLE IF NOT EXISTS $TABLE_BUDGETS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                category_id INTEGER NOT NULL,
                amount REAL NOT NULL,
                period TEXT NOT NULL CHECK(period IN ('daily','weekly','monthly','yearly')),
                FOREIGN KEY(user_id) REFERENCES $TABLE_USERS(id),
                FOREIGN KEY(category_id) REFERENCES $TABLE_CATEGORIES(id)
            )
        """

        private const val CREATE_REGULAR_PAYMENTS_TABLE = """
            CREATE TABLE IF NOT EXISTS $TABLE_REGULAR_PAYMENTS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                category_id INTEGER,
                amount REAL NOT NULL,
                type TEXT NOT NULL CHECK(type IN ('income','expense')),
                note TEXT,
                interval_days INTEGER NOT NULL,
                next_date INTEGER NOT NULL,
                FOREIGN KEY(user_id) REFERENCES $TABLE_USERS(id),
                FOREIGN KEY(category_id) REFERENCES $TABLE_CATEGORIES(id)
            )
        """
    }
}

// ── Repository ──────────────────────────────────────────────────────────────

class AuthRepository(private val dbHelper: MoneyFlowDatabaseHelper? = null) {

    /**
     * Простое хеширование пароля.
     * В production используйте BCrypt или аналог через JNI/библиотеку.
     */
    private fun hashPassword(password: String): String {
        val md = java.security.MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    suspend fun login(login: String, password: String): AuthResult {
        val db = dbHelper?.readableDatabase
            ?: return AuthResult.Error("База данных недоступна")

        val passwordHash = hashPassword(password)
        val cursor = db.query(
            MoneyFlowDatabaseHelper.TABLE_USERS,
            arrayOf(MoneyFlowDatabaseHelper.COL_USER_ID),
            "${MoneyFlowDatabaseHelper.COL_USER_LOGIN} = ? AND ${MoneyFlowDatabaseHelper.COL_USER_PASSWORD_HASH} = ?",
            arrayOf(login, passwordHash),
            null, null, null
        )

        return if (cursor.moveToFirst()) {
            val userId = cursor.getLong(0)
            cursor.close()
            AuthResult.Success(userId)
        } else {
            cursor.close()
            AuthResult.Error("Неверный логин или пароль")
        }
    }

    suspend fun register(login: String, password: String): AuthResult {
        val db = dbHelper?.writableDatabase
            ?: return AuthResult.Error("База данных недоступна")

        // Check if login already taken
        val checkCursor = db.query(
            MoneyFlowDatabaseHelper.TABLE_USERS,
            arrayOf(MoneyFlowDatabaseHelper.COL_USER_ID),
            "${MoneyFlowDatabaseHelper.COL_USER_LOGIN} = ?",
            arrayOf(login),
            null, null, null
        )
        if (checkCursor.moveToFirst()) {
            checkCursor.close()
            return AuthResult.Error("Пользователь с таким логином уже существует")
        }
        checkCursor.close()

        val passwordHash = hashPassword(password)
        val values = ContentValues().apply {
            put(MoneyFlowDatabaseHelper.COL_USER_LOGIN, login)
            put(MoneyFlowDatabaseHelper.COL_USER_PASSWORD_HASH, passwordHash)
        }

        val userId = db.insert(MoneyFlowDatabaseHelper.TABLE_USERS, null, values)
        return if (userId != -1L) {
            AuthResult.Success(userId)
        } else {
            AuthResult.Error("Ошибка при регистрации. Попробуйте ещё раз.")
        }
    }
}