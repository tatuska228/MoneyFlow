package com.example.moneyflow.viewmodel

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneyflow.model.AuthRepository
import com.example.moneyflow.model.AuthResult
import com.example.moneyflow.model.BiometricHelper
import com.example.moneyflow.model.BiometricResult
import com.example.moneyflow.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val login: String = "",
    val password: String = "",
    val confirmPassword: String = "",

    val loginError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,

    val isLoading: Boolean = false,
    val errorMessage: String? = null,

    // After success contains the userId to pass to MainScreen
    val loggedInUserId: Long? = null,

    val isBiometricAvailable: Boolean = false,
    val enableBiometricOnRegister: Boolean = false,

    val registeredUsers: List<User> = emptyList(),
    val selectedUserId: Long? = null
)

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // ── Init ──────────────────────────────────────────────────────

    fun init(activity: FragmentActivity) {
        _uiState.update {
            it.copy(
                isBiometricAvailable = BiometricHelper.isAvailable(activity),
                registeredUsers      = repository.getAllUsers()
            )
        }
    }

    // ── Field updates ─────────────────────────────────────────────

    fun onLoginChange(value: String) =
        _uiState.update { it.copy(login = value, loginError = null, errorMessage = null) }

    fun onPasswordChange(value: String) =
        _uiState.update { it.copy(password = value, passwordError = null, errorMessage = null) }

    fun onConfirmPasswordChange(value: String) =
        _uiState.update { it.copy(confirmPassword = value, confirmPasswordError = null, errorMessage = null) }

    fun onEnableBiometricChanged(enabled: Boolean) =
        _uiState.update { it.copy(enableBiometricOnRegister = enabled) }

    fun onUserSelected(userId: Long?) =
        _uiState.update { it.copy(selectedUserId = userId) }

    fun clearError() =
        _uiState.update { it.copy(errorMessage = null) }

    // ── Login ─────────────────────────────────────────────────────

    fun login() {
        val state = _uiState.value
        if (!validateLoginInputs(state.login, state.password)) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = repository.login(state.login.trim(), state.password)) {
                is AuthResult.Success -> _uiState.update { it.copy(isLoading = false, loggedInUserId = result.userId) }
                is AuthResult.Error   -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
            }
        }
    }

    // ── Biometric login ───────────────────────────────────────────

    fun loginWithBiometric(activity: FragmentActivity) {
        val userId = _uiState.value.selectedUserId
            ?: run { _uiState.update { it.copy(errorMessage = "Выберите аккаунт для входа") }; return }

        val userName = _uiState.value.registeredUsers.firstOrNull { it.id == userId }?.login ?: "аккаунт"

        BiometricHelper.authenticate(
            activity = activity,
            title    = "Вход в $userName",
            subtitle = "Приложите палец для подтверждения"
        ) { result ->
            when (result) {
                is BiometricResult.Success   -> {
                    when (val r = repository.loginWithBiometric(userId)) {
                        is AuthResult.Success -> _uiState.update { it.copy(loggedInUserId = r.userId) }
                        is AuthResult.Error   -> _uiState.update { it.copy(errorMessage = r.message) }
                    }
                }
                is BiometricResult.Cancelled -> { /* user dismissed */ }
                is BiometricResult.Error     -> _uiState.update { it.copy(errorMessage = result.message) }
            }
        }
    }

    // ── Register ──────────────────────────────────────────────────

    fun register(activity: FragmentActivity) {
        val state = _uiState.value
        if (!validateRegisterInputs(state.login, state.password, state.confirmPassword)) return

        if (state.enableBiometricOnRegister && state.isBiometricAvailable) {
            BiometricHelper.authenticate(
                activity           = activity,
                title              = "Подключение отпечатка",
                subtitle           = "Приложите палец, чтобы привязать его к аккаунту",
                negativeButtonText = "Пропустить"
            ) { biometricResult ->
                when (biometricResult) {
                    is BiometricResult.Success   -> doRegister(state.login, state.password, true)
                    is BiometricResult.Cancelled -> doRegister(state.login, state.password, false)
                    is BiometricResult.Error     -> _uiState.update { it.copy(errorMessage = biometricResult.message) }
                }
            }
        } else {
            doRegister(state.login, state.password, false)
        }
    }

    private fun doRegister(login: String, password: String, biometricEnabled: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = repository.register(login.trim(), password, biometricEnabled)) {
                is AuthResult.Success -> _uiState.update { it.copy(isLoading = false, loggedInUserId = result.userId) }
                is AuthResult.Error   -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
            }
        }
    }

    fun continueAsGuest() {
        // Guest: userId = 0 (special value, transactions won't be saved across sessions)
        _uiState.update { it.copy(loggedInUserId = 0L) }
    }

    // ── Validation ────────────────────────────────────────────────

    private fun validateLoginInputs(login: String, password: String): Boolean {
        var valid = true
        if (login.isBlank()) { _uiState.update { it.copy(loginError = "Введите логин") };    valid = false }
        if (password.isBlank()) { _uiState.update { it.copy(passwordError = "Введите пароль") }; valid = false }
        return valid
    }

    private fun validateRegisterInputs(login: String, password: String, confirm: String): Boolean {
        var valid = true
        val t = login.trim()
        if (t.isBlank())        { _uiState.update { it.copy(loginError = "Введите логин") };          valid = false }
        else if (t.length < 3)  { _uiState.update { it.copy(loginError = "Минимум 3 символа") };      valid = false }
        if (password.isBlank()) { _uiState.update { it.copy(passwordError = "Введите пароль") };       valid = false }
        else if (password.length < 6) { _uiState.update { it.copy(passwordError = "Минимум 6 символов") }; valid = false }
        if (confirm != password){ _uiState.update { it.copy(confirmPasswordError = "Пароли не совпадают") }; valid = false }
        return valid
    }
}