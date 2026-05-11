package com.example.moneyflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneyflow.model.AuthRepository
import com.example.moneyflow.model.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val login: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoggedIn: Boolean = false,
    val loginError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null
)

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onLoginChange(value: String) {
        _uiState.update { it.copy(login = value, loginError = null, errorMessage = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, passwordError = null, errorMessage = null) }
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.update { it.copy(confirmPassword = value, confirmPasswordError = null, errorMessage = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun login() {
        val state = _uiState.value
        if (!validateLoginInputs(state.login, state.password)) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.login(state.login.trim(), state.password)
            when (result) {
                is AuthResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
                }
                is AuthResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun register() {
        val state = _uiState.value
        if (!validateRegisterInputs(state.login, state.password, state.confirmPassword)) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.register(state.login.trim(), state.password)
            when (result) {
                is AuthResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
                }
                is AuthResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun continueAsGuest() {
        _uiState.update { it.copy(isLoggedIn = true) }
    }

    private fun validateLoginInputs(login: String, password: String): Boolean {
        var valid = true
        val trimmedLogin = login.trim()

        if (trimmedLogin.isBlank()) {
            _uiState.update { it.copy(loginError = "Введите логин") }
            valid = false
        }
        if (password.isBlank()) {
            _uiState.update { it.copy(passwordError = "Введите пароль") }
            valid = false
        }
        return valid
    }

    private fun validateRegisterInputs(login: String, password: String, confirmPassword: String): Boolean {
        var valid = true
        val trimmedLogin = login.trim()

        if (trimmedLogin.isBlank()) {
            _uiState.update { it.copy(loginError = "Введите логин") }
            valid = false
        } else if (trimmedLogin.length < 3) {
            _uiState.update { it.copy(loginError = "Логин должен содержать минимум 3 символа") }
            valid = false
        }

        if (password.isBlank()) {
            _uiState.update { it.copy(passwordError = "Введите пароль") }
            valid = false
        } else if (password.length < 6) {
            _uiState.update { it.copy(passwordError = "Пароль должен содержать минимум 6 символов") }
            valid = false
        }

        if (confirmPassword != password) {
            _uiState.update { it.copy(confirmPasswordError = "Пароли не совпадают") }
            valid = false
        }

        return valid
    }
}