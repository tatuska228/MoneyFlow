package com.example.moneyflow.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneyflow.data.model.User
import com.example.moneyflow.data.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ProfileUiState(
    val user: User? = null,
    val loginInput: String = "",
    val passwordInput: String = "",
    val isLoading: Boolean = false,
    val updateSuccess: Boolean = false,
    val errorMessage: String? = null
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UserRepository(application)

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            val user = withContext(Dispatchers.IO) { repository.getUser() }
            _uiState.update {
                it.copy(
                    user = user,
                    loginInput = user?.login ?: ""
                )
            }
        }
    }

    fun onLoginChange(value: String) {
        _uiState.update { it.copy(loginInput = value, errorMessage = null, updateSuccess = false) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(passwordInput = value, errorMessage = null, updateSuccess = false) }
    }

    fun saveChanges() {
        val state = _uiState.value
        val userId = state.user?.id ?: return

        if (state.loginInput.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Логин не может быть пустым") }
            return
        }
        if (state.passwordInput.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Пароль не может быть пустым") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val loginTaken = withContext(Dispatchers.IO) {
                repository.isLoginTaken(state.loginInput, userId)
            }

            if (loginTaken) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Логин уже занят") }
                return@launch
            }

            val success = withContext(Dispatchers.IO) {
                repository.updateLoginAndPassword(userId, state.loginInput, state.passwordInput)
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    updateSuccess = success,
                    passwordInput = "",
                    errorMessage = if (!success) "Ошибка при сохранении" else null,
                    user = if (success) it.user?.copy(login = state.loginInput) else it.user
                )
            }
        }
    }

    fun clearSuccessFlag() {
        _uiState.update { it.copy(updateSuccess = false) }
    }
}
