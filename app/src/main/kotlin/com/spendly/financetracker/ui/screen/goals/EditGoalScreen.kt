package com.spendly.financetracker.ui.screen.goals

import androidx.compose.runtime.Composable
import com.spendly.financetracker.ui.viewmodel.Goal

@Composable
fun EditGoalScreen(
    onBack: () -> Unit,
    goal: Goal,
    onSave: OnUpdateGoal,
    onDelete: OnDeleteGoal
) {
    EditGoalScreenContent(
        onBack = onBack,
        goal = goal,
        onSave = onSave,
        onDelete = onDelete
    )
}
