package com.spendly.financetracker.ui.screen.goals

import androidx.compose.runtime.Composable
import com.spendly.financetracker.ui.viewmodel.FinanceUiState

@Composable
fun GoalsScreen(
    state: FinanceUiState,
    onAddGoal: OnAddGoal,
    onGoalSelected: OnGoalSelected
) {
    GoalsScreenContent(
        state = state,
        onAddGoal = onAddGoal,
        onGoalSelected = onGoalSelected
    )
}
