package com.spendly.financetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spendly.financetracker.ui.FinanceTrackerApp
import com.spendly.financetracker.ui.theme.FinanceTrackerTheme
import com.spendly.financetracker.ui.theme.ThemeMode
import com.spendly.financetracker.ui.viewmodel.FinanceViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val viewModel: FinanceViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            val systemDarkTheme = isSystemInDarkTheme()
            val darkTheme = when (ThemeMode.fromStorage(state.profile?.themeMode)) {
                ThemeMode.SYSTEM -> systemDarkTheme
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            FinanceTrackerTheme(darkTheme = darkTheme, accentColorKey = state.profile?.accentColorKey) {
                FinanceTrackerApp(viewModel = viewModel)
            }
        }
    }
}
