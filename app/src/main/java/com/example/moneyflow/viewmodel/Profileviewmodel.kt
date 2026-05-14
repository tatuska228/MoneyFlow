package com.example.moneyflow.viewmodel

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.moneyflow.model.MoneyFlowDatabaseHelper
import com.example.moneyflow.model.PdfExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.MessageDigest

data class ProfileUiState(
    val userId: Long    = -1L,
    val currentLogin: String = "",
    val newLogin: String     = "",
    val newPassword: String  = "",
    val loginError: String?  = null,
    val passwordError: String? = null,
    val successMessage: String? = null,
    val isLoading: Boolean   = false,
    val isPdfExporting: Boolean = false,
    val pdfError: String?    = null
)

class ProfileViewModel(private val db: MoneyFlowDatabaseHelper) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    // ── Init ──────────────────────────────────────────────────────

    fun init(userId: Long) {
        val user = db.getUserById(userId) ?: return
        _uiState.value = ProfileUiState(
            userId       = userId,
            currentLogin = user.login,
            newLogin     = user.login
        )
    }

    // ── Field updates ─────────────────────────────────────────────

    fun onLoginChange(v: String) =
        _uiState.value.let { _uiState.value = it.copy(newLogin = v, loginError = null, successMessage = null) }

    fun onPasswordChange(v: String) =
        _uiState.value.let { _uiState.value = it.copy(newPassword = v, passwordError = null, successMessage = null) }

    fun clearMessages() =
        _uiState.value.let { _uiState.value = it.copy(successMessage = null, loginError = null, passwordError = null, pdfError = null) }

    // ── Save changes ──────────────────────────────────────────────

    fun saveChanges() {
        val state = _uiState.value
        var hasError = false

        // Validate login
        val newLogin = state.newLogin.trim()
        if (newLogin.length < 3) {
            _uiState.value = _uiState.value.copy(loginError = "Минимум 3 символа")
            hasError = true
        }

        // Validate password (optional — only update if not blank)
        val newPassword = state.newPassword
        if (newPassword.isNotBlank() && newPassword.length < 6) {
            _uiState.value = _uiState.value.copy(passwordError = "Минимум 6 символов")
            hasError = true
        }

        if (hasError) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val loginChanged = newLogin != state.currentLogin
            val passwordChanged = newPassword.isNotBlank()

            withContext(Dispatchers.IO) {
                if (loginChanged) {
                    val ok = db.updateUserLogin(state.userId, newLogin)
                    if (!ok) {
                        _uiState.value = _uiState.value.copy(
                            isLoading  = false,
                            loginError = "Этот логин уже занят"
                        )
                        return@withContext
                    }
                }
                if (passwordChanged) {
                    val hash = hashPassword(newPassword)
                    db.updateUserPassword(state.userId, hash)
                }
            }

            if (_uiState.value.loginError != null) return@launch  // abort if login taken

            _uiState.value = _uiState.value.copy(
                isLoading      = false,
                currentLogin   = newLogin,
                newPassword    = "",
                successMessage = "Данные успешно обновлены"
            )
        }
    }

    // ── PDF export ────────────────────────────────────────────────

    fun exportToPdf(context: Context) {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isPdfExporting = true, pdfError = null)

            val file = withContext(Dispatchers.IO) {
                PdfExporter.export(context, state.userId, state.currentLogin, db)
            }

            if (file == null) {
                _uiState.value = _uiState.value.copy(
                    isPdfExporting = false,
                    pdfError       = "Не удалось создать PDF"
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(isPdfExporting = false)

            // Open the PDF with system viewer
            try {
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    pdfError = "PDF сохранён, но не удалось открыть просмотрщик"
                )
            }
        }
    }

    // ── Hash ──────────────────────────────────────────────────────

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    // ── Factory ───────────────────────────────────────────────────

    class Factory(private val db: MoneyFlowDatabaseHelper) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(db) as T
        }
    }
}