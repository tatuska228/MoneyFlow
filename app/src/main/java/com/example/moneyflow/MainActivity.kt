package com.example.moneyflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.moneyflow.model.AuthRepository
import com.example.moneyflow.model.MoneyFlowDatabaseHelper
import com.example.moneyflow.ui.theme.MoneyFlowTheme
import com.example.moneyflow.view.LoginScreen
import com.example.moneyflow.view.RegisterScreen
import com.example.moneyflow.viewmodel.AuthViewModel

// Простая навигация
enum class Screen {
    LOGIN,
    REGISTER,
    HOME
}

class MainActivity : FragmentActivity() {

    // Инициализируем хелпер один раз на всё время жизни Activity
    private lateinit var dbHelper: MoneyFlowDatabaseHelper
    private lateinit var repository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Инициализируем базу и репозиторий
        dbHelper = MoneyFlowDatabaseHelper(this)
        repository = AuthRepository(dbHelper)

        setContent {
            MoneyFlowTheme {
                // Передаем репозиторий в основное приложение
                MoneyFlowApp(repository)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dbHelper.close()
    }
}

@Composable
fun MoneyFlowApp(repository: AuthRepository) {
    var currentScreen by remember { mutableStateOf(Screen.LOGIN) }

    // Создаем ViewModel с помощью фабрики, чтобы передать туда репозиторий
    val authViewModel: AuthViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AuthViewModel(repository) as T
            }
        }
    )

    when (currentScreen) {
        Screen.LOGIN -> LoginScreen(
            viewModel = authViewModel, // Передаем созданную вьюмодель в экран
            onNavigateToRegister = { currentScreen = Screen.REGISTER },
            onLoginSuccess = { currentScreen = Screen.HOME }
        )

        Screen.REGISTER -> RegisterScreen(
            viewModel = authViewModel, // Используем ту же вьюмодель
            onNavigateToLogin = { currentScreen = Screen.LOGIN },
            onRegisterSuccess = { currentScreen = Screen.HOME },
            onContinueAsGuest = { currentScreen = Screen.HOME }
        )

        Screen.HOME -> {
            androidx.compose.material3.Text("Главный экран — база данных теперь подключена!")
        }
    }
}