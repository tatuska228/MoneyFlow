package com.example.moneyflow.view

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moneyflow.ui.theme.*
import com.example.moneyflow.view.elements.AppTextField
import com.example.moneyflow.view.elements.PrimaryButton
import com.example.moneyflow.view.elements.SecondaryButton
import com.example.moneyflow.view.elements.TextLinkButton
import com.example.moneyflow.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle error messages via Snackbar
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    // Navigate on successful login
    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(0.dp)
    ) {
        androidx.compose.material3.Surface(
            modifier = Modifier.fillMaxSize(),
            color = Primary
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // App name
                Text(
                    text = "MoneyFlow",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnPrimary
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Screen title
                Text(
                    text = "Вход",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnPrimary
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Login field
                AppTextField(
                    value = uiState.login,
                    onValueChange = viewModel::onLoginChange,
                    label = "Логин",
                    isError = uiState.loginError != null,
                    errorMessage = uiState.loginError
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Password field
                AppTextField(
                    value = uiState.password,
                    onValueChange = viewModel::onPasswordChange,
                    label = "Пароль",
                    isPassword = true,
                    isError = uiState.passwordError != null,
                    errorMessage = uiState.passwordError
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Login button
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = OnPrimary)
                } else {
                    PrimaryButton(
                        text = "Войти",
                        onClick = viewModel::login
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Navigate to register
                Text(
                    text = "Ещё нет аккаунта?",
                    color = TextSecondary,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                SecondaryButton(
                    text = "Зарегистрироваться",
                    onClick = onNavigateToRegister
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}