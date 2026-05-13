package com.example.moneyflow.model

import java.security.MessageDigest

/**
 * AuthRepository — регистрация и вход пользователей.
 * Использует MoneyFlowDatabaseHelper (единая БД приложения).
 */
class AuthRepository(private val dbHelper: MoneyFlowDatabaseHelper) {

    private fun hashPassword(password: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun getAllUsers(): List<User> = dbHelper.getAllUsers()

    /** Вход по логину + паролю */
    suspend fun login(login: String, password: String): AuthResult {
        val hash = hashPassword(password)
        val user = dbHelper.getUserByLoginAndHash(login, hash)
        return if (user != null) AuthResult.Success(user.id)
        else AuthResult.Error("Неверный логин или пароль")
    }

    /**
     * Биометрический вход: пользователь уже выбрал аккаунт,
     * биометрия подтверждена через BiometricPrompt.
     */
    fun loginWithBiometric(userId: Long): AuthResult {
        val user = dbHelper.getUserById(userId)
            ?: return AuthResult.Error("Пользователь не найден")
        return if (user.biometricEnabled) AuthResult.Success(user.id)
        else AuthResult.Error("Биометрия не подключена для этого аккаунта")
    }

    /** Регистрация нового пользователя */
    suspend fun register(
        login: String,
        password: String,
        biometricEnabled: Boolean = false
    ): AuthResult {
        val existing = dbHelper.getAllUsers().any { it.login == login }
        if (existing) return AuthResult.Error("Пользователь с таким логином уже существует")

        val hash = hashPassword(password)
        val userId = dbHelper.insertUser(login, hash, biometricEnabled)
        return if (userId != -1L) AuthResult.Success(userId)
        else AuthResult.Error("Ошибка при регистрации. Попробуйте ещё раз.")
    }

    fun setBiometricEnabled(userId: Long, enabled: Boolean) {
        dbHelper.setBiometricEnabled(userId, enabled)
    }
}