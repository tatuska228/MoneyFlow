package com.example.moneyflow.data.repository

import android.content.ContentValues
import android.content.Context
import com.example.moneyflow.data.database.DatabaseHelper
import com.example.moneyflow.data.model.User

class UserRepository(context: Context) {

    private val dbHelper = DatabaseHelper(context)

    fun getUser(userId: Int = 1): User? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_USERS,
            null,
            "${DatabaseHelper.COL_USER_ID} = ?",
            arrayOf(userId.toString()),
            null, null, null
        )
        return cursor.use {
            if (it.moveToFirst()) {
                User(
                    id = it.getInt(it.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ID)),
                    login = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COL_USER_LOGIN)),
                    passwordHash = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COL_USER_PASSWORD_HASH))
                )
            } else null
        }
    }

    fun updateLoginAndPassword(userId: Int, newLogin: String, newPassword: String): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_USER_LOGIN, newLogin)
            put(DatabaseHelper.COL_USER_PASSWORD_HASH, DatabaseHelper.hashPassword(newPassword))
        }
        val rows = db.update(
            DatabaseHelper.TABLE_USERS,
            values,
            "${DatabaseHelper.COL_USER_ID} = ?",
            arrayOf(userId.toString())
        )
        return rows > 0
    }

    fun validateCredentials(login: String, password: String): Boolean {
        val db = dbHelper.readableDatabase
        val hash = DatabaseHelper.hashPassword(password)
        val cursor = db.query(
            DatabaseHelper.TABLE_USERS,
            arrayOf(DatabaseHelper.COL_USER_ID),
            "${DatabaseHelper.COL_USER_LOGIN} = ? AND ${DatabaseHelper.COL_USER_PASSWORD_HASH} = ?",
            arrayOf(login, hash),
            null, null, null
        )
        return cursor.use { it.moveToFirst() }
    }

    fun isLoginTaken(login: String, excludeUserId: Int): Boolean {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_USERS,
            arrayOf(DatabaseHelper.COL_USER_ID),
            "${DatabaseHelper.COL_USER_LOGIN} = ? AND ${DatabaseHelper.COL_USER_ID} != ?",
            arrayOf(login, excludeUserId.toString()),
            null, null, null
        )
        return cursor.use { it.moveToFirst() }
    }
}
