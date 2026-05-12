package com.example.moneyflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.moneyflow.ui.theme.BackgroundDark
import com.example.moneyflow.ui.theme.MoneyFlowTheme
import com.example.moneyflow.view.MainScreen
import com.example.moneyflow.viewmodel.MainScreenViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainScreenViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MoneyFlowTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BackgroundDark
                ) {
                    MainScreen(viewModel = viewModel)
                }
            }
        }
    }
}