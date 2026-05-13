package com.example.moneyflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.moneyflow.ui.theme.MoneyFlowTheme
import com.example.moneyflow.view.ProfileScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MoneyFlowTheme {
                ProfileScreen(
                    onNavigateBack = {},
                    onLogout = {}
                )
            }
        }
    }
}