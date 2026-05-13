package com.example.moneyflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.moneyflow.ui.theme.BackgroundMain
import com.example.moneyflow.ui.theme.MoneyFlowTheme
import com.example.moneyflow.view.MainScreen
import com.example.moneyflow.viewmodel.MainScreenViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainScreenViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // userId передаётся через Intent после успешного входа/регистрации.
        // Если запущено напрямую (отладка) — используем -1, экран покажет пустое состояние.
        val userId = intent.getLongExtra(EXTRA_USER_ID, -1L)
        viewModel.init(userId)

        setContent {
            MoneyFlowTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color    = BackgroundMain
                ) {
                    MainScreen(
                        viewModel      = viewModel,
                        onProfileClick = {
                            // TODO: запустить ProfileActivity / навигацию на экран профиля
                        }
                    )
                }
            }
        }
    }

    companion object {
        const val EXTRA_USER_ID = "extra_user_id"
    }
}