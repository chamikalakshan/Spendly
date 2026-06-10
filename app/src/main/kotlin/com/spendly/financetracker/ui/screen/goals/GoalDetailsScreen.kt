package com.spendly.financetracker.ui.screen.goals

import androidx.compose.runtime.Composable
import com.spendly.financetracker.ui.viewmodel.FinanceUiState

@Composable
fun GoalDetailScreen(
    state: FinanceUiState,
    goalId: String?,
    onAddSavings: OnAddSavings,
    onEdit: (String) -> Unit,
    onBack: () -> Unit
) {
    GoalDetailScreenContent(
        state = state,
        goalId = goalId,
        onAddSavings = onAddSavings,
        onEdit = onEdit,
        onBack = onBack
    )
}
