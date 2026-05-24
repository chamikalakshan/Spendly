package com.spendly.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendly.app.data.model.SavingsGoal
import com.spendly.app.repository.AuthRepository
import com.spendly.app.repository.GoalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditGoalUiState(
    val goalName: String = "",
    val targetAmount: String = "",
    val targetDate: Long = System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 30 * 6), // Default 6 months from now
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false
)

@HiltViewModel
class EditGoalViewModel @Inject constructor(
    private val goalRepository: GoalRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditGoalUiState())
    val uiState: StateFlow<EditGoalUiState> = _uiState.asStateFlow()

    fun onNameChanged(name: String) {
        _uiState.update { it.copy(goalName = name, error = null) }
    }

    fun onAmountChanged(amount: String) {
        if (amount.isEmpty() || (amount.all { it.isDigit() || it == '.' } && amount.count { it == '.' } <= 1)) {
            _uiState.update { it.copy(targetAmount = amount, error = null) }
        }
    }

    fun onDateSelected(dateMs: Long) {
        _uiState.update { it.copy(targetDate = dateMs) }
    }

    fun saveGoal() {
        val state = _uiState.value
        val userId = authRepository.getCurrentUserId() ?: return

        if (state.goalName.isBlank()) {
            _uiState.update { it.copy(error = "Please enter a goal name") }
            return
        }

        val amount = state.targetAmount.toDoubleOrNull() ?: 0.0
        if (amount <= 0) {
            _uiState.update { it.copy(error = "Please enter a valid target amount") }
            return
        }

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val goal = SavingsGoal(
                userId = userId,
                goalName = state.goalName,
                targetAmount = amount,
                targetDate = state.targetDate,
                createdAt = System.currentTimeMillis()
            )

            goalRepository.saveGoal(goal)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, isSaved = true) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }
    
    fun clearError() = _uiState.update { it.copy(error = null) }
}
