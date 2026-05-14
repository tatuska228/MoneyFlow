package com.example.moneyflow.model

import java.security.MessageDigest

class AuthRepository(private val dbHelper: MoneyFlowDatabaseHelper) {

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun getAllUsers(): List<User> = dbHelper.getAllUsers()

    suspend fun login(login: String, password: String): AuthResult {
        val user = dbHelper.getUserByLoginAndHash(login, hashPassword(password))
        return if (user != null) AuthResult.Success(user.id)
        else AuthResult.Error("Неверный логин или пароль")
    }

    fun loginWithBiometric(userId: Long): AuthResult {
        val user = dbHelper.getUserById(userId)
            ?: return AuthResult.Error("Пользователь не найден")
        return if (user.biometricEnabled) AuthResult.Success(user.id)
        else AuthResult.Error("Биометрия не подключена для этого аккаунта")
    }

    suspend fun register(
        login: String,
        password: String,
        biometricEnabled: Boolean = false
    ): AuthResult {
        if (dbHelper.getAllUsers().any { it.login == login })
            return AuthResult.Error("Пользователь с таким логином уже существует")

        val userId = dbHelper.insertUser(login, hashPassword(password), biometricEnabled)
        return if (userId != -1L) AuthResult.Success(userId)
        else AuthResult.Error("Ошибка при регистрации. Попробуйте ещё раз.")
    }

    fun setBiometricEnabled(userId: Long, enabled: Boolean) =
        dbHelper.setBiometricEnabled(userId, enabled)
}