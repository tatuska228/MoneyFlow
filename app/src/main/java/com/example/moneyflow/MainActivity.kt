package com.example.moneyflow

import android.os.Bundle
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moneyflow.model.AuthRepository
import com.example.moneyflow.model.MoneyFlowDatabaseHelper
import com.example.moneyflow.model.TransactionRepository
import com.example.moneyflow.ui.theme.MoneyFlowTheme
import com.example.moneyflow.view.LoginScreen
import com.example.moneyflow.view.MainScreen
import com.example.moneyflow.view.ProfileScreen
import com.example.moneyflow.view.RegisterScreen
import com.example.moneyflow.viewmodel.AuthViewModel
import com.example.moneyflow.viewmodel.MainScreenViewModel
import com.example.moneyflow.viewmodel.ProfileViewModel

enum class Screen { LOGIN, REGISTER, HOME, PROFILE }

class MainActivity : FragmentActivity() {

    private lateinit var dbHelper: MoneyFlowDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        dbHelper = MoneyFlowDatabaseHelper(this)

        setContent {
            MoneyFlowTheme {
                MoneyFlowApp(dbHelper = dbHelper)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dbHelper.close()
    }
}

@Composable
fun MoneyFlowApp(dbHelper: MoneyFlowDatabaseHelper) {
    var currentScreen  by remember { mutableStateOf(Screen.LOGIN) }
    var loggedInUserId by remember { mutableStateOf(-1L) }

    // Needed to call viewModel.logout(activity) which reinitialises biometric state
    val activity = LocalActivity.current as FragmentActivity

    // ── AuthViewModel ─────────────────────────────────────────────
    val authViewModel: AuthViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return AuthViewModel(AuthRepository(dbHelper)) as T
            }
        }
    )

    // ── MainScreenViewModel ───────────────────────────────────────
    val mainViewModel: MainScreenViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return MainScreenViewModel(TransactionRepository(dbHelper)) as T
            }
        }
    )

    // ── ProfileViewModel ──────────────────────────────────────────
    val profileViewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModel.Factory(dbHelper)
    )

    // Init MainScreen when userId is known
    LaunchedEffect(loggedInUserId) {
        if (loggedInUserId >= 0L) {
            mainViewModel.init(loggedInUserId)
        }
    }

    // Init ProfileScreen when navigating to it
    LaunchedEffect(currentScreen) {
        if (currentScreen == Screen.PROFILE && loggedInUserId >= 0L) {
            profileViewModel.init(loggedInUserId)
        }
    }

    when (currentScreen) {

        Screen.LOGIN -> LoginScreen(
            viewModel            = authViewModel,
            onNavigateToRegister = { currentScreen = Screen.REGISTER },
            onLoginSuccess       = { userId ->
                loggedInUserId = userId
                currentScreen  = Screen.HOME
            }
        )

        Screen.REGISTER -> RegisterScreen(
            viewModel         = authViewModel,
            onNavigateToLogin = { currentScreen = Screen.LOGIN },
            onRegisterSuccess = { userId ->
                loggedInUserId = userId
                currentScreen  = Screen.HOME
            },
            onContinueAsGuest = {
                loggedInUserId = 0L
                currentScreen  = Screen.HOME
            }
        )

        Screen.HOME -> MainScreen(
            viewModel      = mainViewModel,
            onProfileClick = { currentScreen = Screen.PROFILE }
        )

        Screen.PROFILE -> ProfileScreen(
            viewModel = profileViewModel,
            onBack    = { currentScreen = Screen.HOME },
            onLogout  = {
                // 1. Fully reset AuthViewModel so LoginScreen starts clean
                authViewModel.logout(activity)
                // 2. Clear the stored userId
                loggedInUserId = -1L
                // 3. Navigate to login
                currentScreen = Screen.LOGIN
            }
        )
    }
}