package com.example.moneyflow.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moneyflow.view.elements.*
import com.example.moneyflow.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Show snackbar on success
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.updateSuccess) {
        if (uiState.updateSuccess) {
            snackbarHostState.showSnackbar("Данные обновлены")
            viewModel.clearSuccessFlag()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1C1C1E)) // dark outer background
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // ── Top label ──────────────────────────────────────────────────
            Text(
                text = "Профиль",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
            )

            // ── Green card ─────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(GreenPrimary)
                    .weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(32.dp))

                    // ── Avatar ─────────────────────────────────────────────
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFBDBDBD)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Avatar",
                            tint = Color(0xFF757575),
                            modifier = Modifier.size(52.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // ── Login field ────────────────────────────────────────
                    MoneyFlowTextField(
                        value = uiState.loginInput,
                        onValueChange = viewModel::onLoginChange,
                        placeholder = "Логин",
                        isError = uiState.errorMessage != null && uiState.loginInput.isBlank()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // ── Password field ─────────────────────────────────────
                    MoneyFlowTextField(
                        value = uiState.passwordInput,
                        onValueChange = viewModel::onPasswordChange,
                        placeholder = "Пароль",
                        isPassword = true,
                        isError = uiState.errorMessage != null && uiState.passwordInput.isBlank()
                    )

                    // ── Error message ──────────────────────────────────────
                    if (uiState.errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.errorMessage!!,
                            color = Color(0xFFFFCDD2),
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // ── Save button ────────────────────────────────────────
                    PrimaryButton(
                        text = if (uiState.isLoading) "Сохранение..." else "Изменить",
                        onClick = viewModel::saveChanges,
                        enabled = !uiState.isLoading,
                        modifier = Modifier.width(160.dp)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // ── Bottom bar ─────────────────────────────────────────
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Back button
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(GreenDark)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Назад",
                                tint = Color.White
                            )
                        }

                        // Logout button
                        SecondaryButton(
                            text = "Выйти из профиля",
                            onClick = onLogout,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
