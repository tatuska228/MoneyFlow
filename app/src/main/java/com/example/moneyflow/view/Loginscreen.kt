package com.example.moneyflow.view

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.moneyflow.model.User
import com.example.moneyflow.ui.theme.*
import com.example.moneyflow.view.elements.AppSnackbarHost
import com.example.moneyflow.view.elements.AppTextField
import com.example.moneyflow.view.elements.PrimaryButton
import com.example.moneyflow.view.elements.SecondaryButton
import com.example.moneyflow.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: (userId: Long) -> Unit   // passes userId to caller
) {
    val uiState          by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val activity          = LocalActivity.current as FragmentActivity

    LaunchedEffect(Unit) { viewModel.init(activity) }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearError() }
    }

    // Only react when loggedInUserId is freshly set (non-null) after the user actually logs in.
    // Using the value as key ensures we don't re-trigger on recomposition.
    val loggedInUserId = uiState.loggedInUserId
    LaunchedEffect(loggedInUserId) {
        if (loggedInUserId != null) {
            onLoginSuccess(loggedInUserId)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Surface(modifier = Modifier.fillMaxSize(), color = Primary) {
            Column(
                modifier            = Modifier.fillMaxSize().padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("MoneyFlow", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = OnPrimary)
                Spacer(Modifier.height(48.dp))
                Text("Вход", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = OnPrimary)
                Spacer(Modifier.height(24.dp))

                // Registered accounts carousel
                if (uiState.registeredUsers.isNotEmpty()) {
                    Text("Выберите аккаунт", color = TextSecondary, fontSize = 13.sp,
                        modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(uiState.registeredUsers) { user ->
                            UserAvatarCard(
                                user       = user,
                                isSelected = uiState.selectedUserId == user.id,
                                onClick    = {
                                    viewModel.onUserSelected(
                                        if (uiState.selectedUserId == user.id) null else user.id
                                    )
                                }
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))

                    val selectedUser = uiState.registeredUsers
                        .firstOrNull { it.id == uiState.selectedUserId }
                    if (selectedUser != null && selectedUser.biometricEnabled && uiState.isBiometricAvailable) {
                        BiometricLoginButton(
                            userName = selectedUser.login,
                            onClick  = { viewModel.loginWithBiometric(activity) }
                        )
                        Spacer(Modifier.height(16.dp))
                        Text("— или введите пароль —", color = TextSecondary, fontSize = 12.sp)
                        Spacer(Modifier.height(12.dp))
                    }
                }

                AppTextField(
                    value         = uiState.login,
                    onValueChange = viewModel::onLoginChange,
                    label         = "Логин",
                    isError       = uiState.loginError != null,
                    errorMessage  = uiState.loginError
                )
                Spacer(Modifier.height(16.dp))
                AppTextField(
                    value         = uiState.password,
                    onValueChange = viewModel::onPasswordChange,
                    label         = "Пароль",
                    isPassword    = true,
                    isError       = uiState.passwordError != null,
                    errorMessage  = uiState.passwordError
                )
                Spacer(Modifier.height(24.dp))

                if (uiState.isLoading) CircularProgressIndicator(color = OnPrimary)
                else PrimaryButton(text = "Войти", onClick = viewModel::login)

                Spacer(Modifier.height(24.dp))
                Text("Ещё нет аккаунта?", color = TextSecondary, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                SecondaryButton(text = "Зарегистрироваться", onClick = onNavigateToRegister)
            }
        }
        AppSnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun UserAvatarCard(user: User, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick).padding(4.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(56.dp).clip(CircleShape)
                .background(if (isSelected) ButtonSecondary else SurfaceVariant)
                .border(
                    width = if (isSelected) 2.dp else 0.dp,
                    color = if (isSelected) OnPrimary else Color.Transparent,
                    shape = CircleShape
                )
        ) {
            Text(
                text       = user.login.take(1).uppercase(),
                fontSize   = 22.sp,
                fontWeight = FontWeight.Bold,
                color      = OnPrimary
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text       = user.login,
            fontSize   = 11.sp,
            color      = if (isSelected) OnPrimary else TextSecondary,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            textAlign  = TextAlign.Center,
            maxLines   = 1,
            modifier   = Modifier.widthIn(max = 64.dp)
        )
        if (user.biometricEnabled) {
            Icon(Icons.Default.Fingerprint, contentDescription = null,
                tint = TextSecondary, modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
private fun BiometricLoginButton(userName: String, onClick: () -> Unit) {
    Button(
        onClick  = onClick,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape    = RoundedCornerShape(26.dp),
        colors   = ButtonDefaults.buttonColors(containerColor = PrimaryDark, contentColor = OnPrimary)
    ) {
        Icon(Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(8.dp))
        Text("Войти по отпечатку ($userName)", fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}