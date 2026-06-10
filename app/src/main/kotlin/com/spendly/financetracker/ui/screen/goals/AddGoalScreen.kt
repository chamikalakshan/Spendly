package com.spendly.financetracker.ui.screen.goals

import androidx.compose.runtime.Composable

@Composable
fun AddGoalScreen(
    onBack: () -> Unit,
    onSave: OnSaveGoal
) {
    AddGoalScreenContent(
        onBack = onBack,
        onSave = onSave
    )
}
