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
    val passwordHash: String,
    val biometricEnabled: Boolean = false
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
        db.execSQL("DROP TABLE IF EXISTS users")
        db.execSQL("DROP TABLE IF EXISTS transactions")
        db.execSQL("DROP TABLE IF EXISTS categories")
        db.execSQL("DROP TABLE IF EXISTS budgets")
        db.execSQL("DROP TABLE IF EXISTS regular_payments")
        onCreate(db)
    }

    companion object {
        const val DATABASE_NAME = "moneyflow.db"
        const val DATABASE_VERSION = 2

        const val TABLE_USERS = "users"
        const val TABLE_TRANSACTIONS = "transactions"
        const val TABLE_CATEGORIES = "categories"
        const val TABLE_BUDGETS = "budgets"
        const val TABLE_REGULAR_PAYMENTS = "regular_payments"

        const val COL_USER_ID = "id"
        const val COL_USER_LOGIN = "login"
        const val COL_USER_PASSWORD_HASH = "password_hash"
        const val COL_USER_BIOMETRIC_ENABLED = "biometric_enabled"
        const val COL_USER_CREATED_AT = "created_at"

        // Строки без heredoc-отступов, чтобы SQLite не ругался
        const val CREATE_USERS_TABLE =
            "CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "login TEXT UNIQUE NOT NULL, " +
                    "password_hash TEXT NOT NULL, " +
                    "biometric_enabled INTEGER NOT NULL DEFAULT 0, " +
                    "created_at INTEGER NOT NULL DEFAULT (strftime('%s','now')))"

        const val CREATE_TRANSACTIONS_TABLE =
            "CREATE TABLE IF NOT EXISTS transactions (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER NOT NULL, " +
                    "category_id INTEGER, " +
                    "amount REAL NOT NULL, " +
                    "type TEXT NOT NULL CHECK(type IN ('income','expense')), " +
                    "note TEXT, " +
                    "date INTEGER NOT NULL DEFAULT (strftime('%s','now')), " +
                    "FOREIGN KEY(user_id) REFERENCES users(id), " +
                    "FOREIGN KEY(category_id) REFERENCES categories(id))"

        const val CREATE_CATEGORIES_TABLE =
            "CREATE TABLE IF NOT EXISTS categories (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER, " +
                    "name TEXT NOT NULL, " +
                    "icon TEXT, " +
                    "color TEXT, " +
                    "type TEXT NOT NULL CHECK(type IN ('income','expense','both')), " +
                    "is_default INTEGER NOT NULL DEFAULT 0, " +
                    "FOREIGN KEY(user_id) REFERENCES users(id))"

        const val CREATE_BUDGETS_TABLE =
            "CREATE TABLE IF NOT EXISTS budgets (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER NOT NULL, " +
                    "category_id INTEGER NOT NULL, " +
                    "amount REAL NOT NULL, " +
                    "period TEXT NOT NULL CHECK(period IN ('daily','weekly','monthly','yearly')), " +
                    "FOREIGN KEY(user_id) REFERENCES users(id), " +
                    "FOREIGN KEY(category_id) REFERENCES categories(id))"

        const val CREATE_REGULAR_PAYMENTS_TABLE =
            "CREATE TABLE IF NOT EXISTS regular_payments (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER NOT NULL, " +
                    "category_id INTEGER, " +
                    "amount REAL NOT NULL, " +
                    "type TEXT NOT NULL CHECK(type IN ('income','expense')), " +
                    "note TEXT, " +
                    "interval_days INTEGER NOT NULL, " +
                    "next_date INTEGER NOT NULL, " +
                    "FOREIGN KEY(user_id) REFERENCES users(id), " +
                    "FOREIGN KEY(category_id) REFERENCES categories(id))"
    }
}

// ── Repository ──────────────────────────────────────────────────────────────

class AuthRepository(private val dbHelper: MoneyFlowDatabaseHelper? = null) {

    private fun hashPassword(password: String): String {
        val md = java.security.MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /** Возвращает всех зарегистрированных пользователей (для списка аккаунтов на экране входа) */
    fun getAllUsers(): List<User> {
        val db = dbHelper?.readableDatabase ?: return emptyList()
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COL_USER_ID, COL_USER_LOGIN, COL_USER_PASSWORD_HASH, COL_USER_BIOMETRIC_ENABLED),
            null, null, null, null,
            "$COL_USER_CREATED_AT ASC"
        )
        val users = mutableListOf<User>()
        while (cursor.moveToNext()) {
            users.add(
                User(
                    id = cursor.getLong(0),
                    login = cursor.getString(1),
                    passwordHash = cursor.getString(2),
                    biometricEnabled = cursor.getInt(3) == 1
                )
            )
        }
        cursor.close()
        return users
    }

    /** Вход по логину + пароль */
    suspend fun login(login: String, password: String): AuthResult {
        val db = dbHelper?.readableDatabase
            ?: return AuthResult.Error("База данных недоступна")

        val passwordHash = hashPassword(password)
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COL_USER_ID),
            "$COL_USER_LOGIN = ? AND $COL_USER_PASSWORD_HASH = ?",
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

    /**
     * Вход по биометрии.
     * Пользователь сам выбрал свой аккаунт, биометрия подтверждена через BiometricPrompt.
     * Просто проверяем, что биометрия включена для этого userId, и авторизуем.
     */
    fun loginWithBiometric(userId: Long): AuthResult {
        val db = dbHelper?.readableDatabase
            ?: return AuthResult.Error("База данных недоступна")

        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COL_USER_ID, COL_USER_BIOMETRIC_ENABLED),
            "$COL_USER_ID = ?",
            arrayOf(userId.toString()),
            null, null, null
        )
        return if (cursor.moveToFirst()) {
            val biometricEnabled = cursor.getInt(1) == 1
            cursor.close()
            if (biometricEnabled) AuthResult.Success(userId)
            else AuthResult.Error("Биометрия не подключена для этого аккаунта")
        } else {
            cursor.close()
            AuthResult.Error("Пользователь не найден")
        }
    }

    /** Регистрация нового пользователя */
    suspend fun register(
        login: String,
        password: String,
        biometricEnabled: Boolean = false
    ): AuthResult {
        val db = dbHelper?.writableDatabase
            ?: return AuthResult.Error("База данных недоступна")

        val checkCursor = db.query(
            TABLE_USERS,
            arrayOf(COL_USER_ID),
            "$COL_USER_LOGIN = ?",
            arrayOf(login),
            null, null, null
        )
        if (checkCursor.moveToFirst()) {
            checkCursor.close()
            return AuthResult.Error("Пользователь с таким логином уже существует")
        }
        checkCursor.close()

        val values = ContentValues().apply {
            put(COL_USER_LOGIN, login)
            put(COL_USER_PASSWORD_HASH, hashPassword(password))
            put(COL_USER_BIOMETRIC_ENABLED, if (biometricEnabled) 1 else 0)
        }
        val userId = db.insert(TABLE_USERS, null, values)
        return if (userId != -1L) AuthResult.Success(userId)
        else AuthResult.Error("Ошибка при регистрации. Попробуйте ещё раз.")
    }

    /** Включить/отключить биометрию для существующего пользователя */
    fun setBiometricEnabled(userId: Long, enabled: Boolean) {
        val db = dbHelper?.writableDatabase ?: return
        val values = ContentValues().apply {
            put(COL_USER_BIOMETRIC_ENABLED, if (enabled) 1 else 0)
        }
        db.update(TABLE_USERS, values, "$COL_USER_ID = ?", arrayOf(userId.toString()))
    }

    companion object {
        private const val TABLE_USERS = MoneyFlowDatabaseHelper.TABLE_USERS
        private const val COL_USER_ID = MoneyFlowDatabaseHelper.COL_USER_ID
        private const val COL_USER_LOGIN = MoneyFlowDatabaseHelper.COL_USER_LOGIN
        private const val COL_USER_PASSWORD_HASH = MoneyFlowDatabaseHelper.COL_USER_PASSWORD_HASH
        private const val COL_USER_BIOMETRIC_ENABLED = MoneyFlowDatabaseHelper.COL_USER_BIOMETRIC_ENABLED
        private const val COL_USER_CREATED_AT = MoneyFlowDatabaseHelper.COL_USER_CREATED_AT
    }
}