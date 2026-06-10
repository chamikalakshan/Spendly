package com.spendly.financetracker.ui.navigation

import androidx.compose.runtime.Composable
import com.spendly.financetracker.ui.FinanceTrackerApp
import com.spendly.financetracker.ui.viewmodel.FinanceViewModel

@Composable
fun SpendlyNavGraph(viewModel: FinanceViewModel) {
    FinanceTrackerApp(viewModel = viewModel)
}
