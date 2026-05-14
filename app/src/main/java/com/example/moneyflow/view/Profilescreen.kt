package com.example.moneyflow.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.moneyflow.ui.theme.*
import com.example.moneyflow.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val uiState   by viewModel.uiState.collectAsStateWithLifecycle()
    val context    = LocalContext.current
    val snackState = remember { SnackbarHostState() }

    // Show success / error messages in snackbar
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { snackState.showSnackbar(it); viewModel.clearMessages() }
    }
    LaunchedEffect(uiState.pdfError) {
        uiState.pdfError?.let { snackState.showSnackbar(it); viewModel.clearMessages() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary)
            .padding(top = 20.dp, bottom = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ── Top bar ────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text       = "Профиль",
                    color      = OnPrimary,
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier   = Modifier.align(Alignment.Center)
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Avatar ─────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFB0D4C4))
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.Default.Person,
                    contentDescription = "Аватар",
                    tint               = Color(0xFF78978B),
                    modifier           = Modifier.size(64.dp)
                )
            }

            Spacer(Modifier.height(32.dp))

            // ── Form card ──────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Login field
                ProfileTextField(
                    value         = uiState.newLogin,
                    onValueChange = viewModel::onLoginChange,
                    label         = "Логин",
                    isError       = uiState.loginError != null,
                    errorText     = uiState.loginError
                )

                // Password field
                ProfileTextField(
                    value         = uiState.newPassword,
                    onValueChange = viewModel::onPasswordChange,
                    label         = "Пароль",
                    isPassword    = true,
                    placeholder   = "Оставьте пустым, чтобы не менять",
                    isError       = uiState.passwordError != null,
                    errorText     = uiState.passwordError
                )

                // Save button
                if (uiState.isLoading) {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = OnPrimary)
                    }
                } else {
                    Button(
                        onClick  = viewModel::saveChanges,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape    = RoundedCornerShape(26.dp),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor = ButtonSecondary,
                            contentColor   = OnPrimary
                        )
                    ) {
                        Text("Изменить", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                }

                Spacer(Modifier.height(16.dp))

                // ── PDF Export ────────────────────────────────────
                HorizontalDivider(color = Divider)

                Spacer(Modifier.height(4.dp))

                Text(
                    text     = "Экспорт данных",
                    color    = TextSecondary,
                    fontSize = 13.sp
                )

                if (uiState.isPdfExporting) {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(
                                color    = OnPrimary,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                            Text("Создание PDF...", color = TextSecondary, fontSize = 14.sp)
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick  = { viewModel.exportToPdf(context) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape    = RoundedCornerShape(26.dp),
                        colors   = ButtonDefaults.outlinedButtonColors(contentColor = OnPrimary),
                        border   = androidx.compose.foundation.BorderStroke(1.5.dp, OnPrimary.copy(alpha = 0.7f))
                    ) {
                        Text("Экспортировать в PDF", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // ── Bottom bar: Back + Logout ──────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                // Back arrow button
                IconButton(
                    onClick  = onBack,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(ButtonSecondary)
                ) {
                    Icon(
                        imageVector        = Icons.Default.ArrowBack,
                        contentDescription = "Назад",
                        tint               = OnPrimary
                    )
                }

                // Logout button
                Button(
                    onClick  = onLogout,
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape    = RoundedCornerShape(26.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = ButtonSecondary,
                        contentColor   = OnPrimary
                    )
                ) {
                    Text("Выйти из профиля", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        // ── Snackbar ───────────────────────────────────────────────
        SnackbarHost(
            hostState = snackState,
            modifier  = Modifier.align(Alignment.BottomCenter).padding(16.dp)
        ) { data ->
            Snackbar(
                snackbarData   = data,
                containerColor = Color.Black,
                contentColor   = Color.White,
                shape          = RoundedCornerShape(12.dp)
            )
        }
    }
}

// ── Reusable text field for profile ───────────────────────────────

@Composable
private fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isPassword: Boolean = false,
    placeholder: String = "",
    isError: Boolean = false,
    errorText: String? = null
) {
    Column {
        OutlinedTextField(
            value         = value,
            onValueChange = onValueChange,
            label         = { Text(label, color = InputText.copy(alpha = 0.6f)) },
            placeholder   = if (placeholder.isNotEmpty()) {
                { Text(placeholder, color = InputText.copy(alpha = 0.4f), fontSize = 13.sp) }
            } else null,
            modifier      = Modifier.fillMaxWidth(),
            singleLine    = true,
            isError       = isError,
            visualTransformation = if (isPassword) PasswordVisualTransformation()
            else androidx.compose.ui.text.input.VisualTransformation.None,
            keyboardOptions = if (isPassword) KeyboardOptions(keyboardType = KeyboardType.Password)
            else KeyboardOptions.Default,
            shape  = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor   = InputBackground,
                unfocusedContainerColor = InputBackground,
                focusedTextColor        = InputText,
                unfocusedTextColor      = InputText,
                focusedBorderColor      = PrimaryDark,
                unfocusedBorderColor    = InputBackground,
                errorContainerColor     = InputBackground,
                errorTextColor          = InputText,
                errorBorderColor        = Expense
            )
        )
        if (isError && errorText != null) {
            Text(
                text     = errorText,
                color    = Expense,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 12.dp, top = 4.dp)
            )
        }
    }
}