package com.example.moneyflow.view

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.moneyflow.viewmodel.MainScreenViewModel


@Composable
fun MainScreen(
    viewModel: MainScreenViewModel,
    modifier: Modifier) {
    Text(
        text = "Hello android",
        modifier = modifier
    )
}