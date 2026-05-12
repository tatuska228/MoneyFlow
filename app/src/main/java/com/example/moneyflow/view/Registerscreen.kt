package com.example.moneyflow.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.activity.compose.LocalActivity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moneyflow.ui.theme.*
import com.example.moneyflow.view.elements.AppSnackbarHost
import com.example.moneyflow.view.elements.AppTextField
import com.example.moneyflow.view.elements.PrimaryButton
import com.example.moneyflow.view.elements.TextLinkButton
import com.example.moneyflow.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
    onContinueAsGuest: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val activity = LocalActivity.current as FragmentActivity

    LaunchedEffect(Unit) { viewModel.init(activity) }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) onRegisterSuccess()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Surface(modifier = Modifier.fillMaxSize(), color = Primary) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("MoneyFlow", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = OnPrimary)

                Spacer(Modifier.height(48.dp))

                Text("Регистрация", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = OnPrimary)

                Spacer(Modifier.height(24.dp))

                AppTextField(
                    value = uiState.login,
                    onValueChange = viewModel::onLoginChange,
                    label = "Логин",
                    isError = uiState.loginError != null,
                    errorMessage = uiState.loginError
                )
                Spacer(Modifier.height(16.dp))
                AppTextField(
                    value = uiState.password,
                    onValueChange = viewModel::onPasswordChange,
                    label = "Пароль",
                    isPassword = true,
                    isError = uiState.passwordError != null,
                    errorMessage = uiState.passwordError
                )
                Spacer(Modifier.height(16.dp))
                AppTextField(
                    value = uiState.confirmPassword,
                    onValueChange = viewModel::onConfirmPasswordChange,
                    label = "Повторите пароль",
                    isPassword = true,
                    isError = uiState.confirmPasswordError != null,
                    errorMessage = uiState.confirmPasswordError
                )

                Spacer(Modifier.height(20.dp))

                // ── Опция биометрии ──────────────────────────────────────────
                if (uiState.isBiometricAvailable) {
                    BiometricCheckbox(
                        checked = uiState.enableBiometricOnRegister,
                        onCheckedChange = viewModel::onEnableBiometricChanged
                    )
                    Spacer(Modifier.height(16.dp))
                } else {
                    Spacer(Modifier.height(4.dp))
                }

                if (uiState.isLoading) {
                    CircularProgressIndicator(color = OnPrimary)
                } else {
                    PrimaryButton(
                        text = "Зарегистрироваться",
                        onClick = { viewModel.register(activity) }
                    )
                }

                Spacer(Modifier.height(24.dp))
                Text("Уже есть аккаунт?", color = TextSecondary, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                PrimaryButton(text = "Войти", onClick = onNavigateToLogin)

                Spacer(Modifier.height(16.dp))
                TextLinkButton(text = "Продолжить как гость", onClick = onContinueAsGuest)
            }
        }

        AppSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// ── Чекбокс подключения биометрии ───────────────────────────────────────────

@Composable
private fun BiometricCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = ButtonSecondary,
                uncheckedColor = TextSecondary,
                checkmarkColor = OnPrimary
            )
        )
        Spacer(Modifier.width(8.dp))
        Icon(
            imageVector = Icons.Default.Fingerprint,
            contentDescription = null,
            tint = if (checked) OnPrimary else TextSecondary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = "Войти по отпечатку пальца",
            color = if (checked) OnPrimary else TextSecondary,
            fontSize = 14.sp,
            fontWeight = if (checked) FontWeight.Medium else FontWeight.Normal
        )
    }
}