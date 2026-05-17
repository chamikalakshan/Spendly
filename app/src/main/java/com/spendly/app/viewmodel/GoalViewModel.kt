package com.spendly.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendly.app.data.model.SavingsGoal
import com.spendly.app.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class GoalUiState(
    val allGoals: List<SavingsGoal> = emptyList(),
    val primaryGoal: SavingsGoal? = null,
    val otherGoals: List<SavingsGoal> = emptyList(),
    val savedAmount: Double = 0.0,
    val progressPercent: Float = 0f,
    val progressDisplay: Int = 0,
    val remainingAmount: Double = 0.0,
    val monthsLeft: Int = 0,
    val requiredMonthly: Double = 0.0,
    val currentMonthlySavings: Double = 0.0,
    val isOnTrack: Boolean = false,
    val projectionText: String = "",
    val projectedDate: String = "",
    val monthlySavings: List<Double> = emptyList(),
    val monthLabels: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val isDeleted: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val showAddSavingsDialog: Boolean = false,
    val addSavingsAmount: String = "",
    val error: String? = null
)

@HiltViewModel
class GoalViewModel @Inject constructor(
    private val goalRepository: GoalRepository,
    private val incomeRepository: IncomeRepository,
    private val expenseRepository: ExpenseRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GoalUiState())
    val uiState: StateFlow<GoalUiState> = _uiState.asStateFlow()

    init {
        loadGoalsData()
    }

    private fun loadGoalsData() {
        // Hardcoded sample data as requested
        val sampleGoals = listOf(
            SavingsGoal(
                id = "1",
                goalName = "MacBook Pro M4",
                targetAmount = 490000.0,
                savedAmount = 107200.0,
                targetDate = GregorianCalendar(2027, Calendar.MAY, 1).timeInMillis
            ),
            SavingsGoal(
                id = "2",
                goalName = "Emergency Fund",
                targetAmount = 200000.0,
                savedAmount = 150000.0,
                targetDate = GregorianCalendar(2026, Calendar.DECEMBER, 1).timeInMillis
            ),
            SavingsGoal(
                id = "3",
                goalName = "Trip to Ella",
                targetAmount = 50000.0,
                savedAmount = 10000.0,
                targetDate = GregorianCalendar(2026, Calendar.AUGUST, 1).timeInMillis
            )
        )

        _uiState.update {
            it.copy(
                allGoals = sampleGoals,
                primaryGoal = sampleGoals[0],
                otherGoals = sampleGoals.drop(1),
                savedAmount = 107200.0,
                progressPercent = 0.22f,
                progressDisplay = 22,
                remainingAmount = 382800.0,
                monthsLeft = 10,
                requiredMonthly = 38280.0,
                currentMonthlySavings = 127213.0,
                isOnTrack = true,
                projectedDate = "September 2026",
                projectionText = "At your current rate, you'll reach your goal by September 2026 — ahead of schedule ✓",
                monthlySavings = listOf(12000.0, 44000.0, 14000.0, 76000.0, 127213.0),
                monthLabels = listOf("Jan", "Feb", "Mar", "Apr", "May")
            )
        }
    }

    fun getGoalProgress(goal: SavingsGoal): Float {
        return if (goal.targetAmount > 0) (goal.savedAmount / goal.targetAmount).toFloat() else 0f
    }

    fun showAddSavingsDialog() {
        _uiState.update { it.copy(showAddSavingsDialog = true, addSavingsAmount = "") }
    }

    fun onAddSavingsAmountChanged(value: String) {
        if (value.isEmpty() || (value.all { it.isDigit() || it == '.' } && value.count { it == '.' } <= 1)) {
            _uiState.update { it.copy(addSavingsAmount = value) }
        }
    }

    fun confirmAddSavings() {
        val extra = _uiState.value.addSavingsAmount.toDoubleOrNull() ?: 0.0
        if (extra > 0) {
            val primary = _uiState.value.primaryGoal ?: return
            val updated = primary.copy(savedAmount = primary.savedAmount + extra)
            viewModelScope.launch {
                goalRepository.saveGoal(updated)
                    .onSuccess {
                        _uiState.update {
                            it.copy(
                                showAddSavingsDialog = false,
                                savedAmount = it.savedAmount + extra,
                                progressPercent = ((it.savedAmount + extra) / primary.targetAmount).toFloat(),
                                progressDisplay = (((it.savedAmount + extra) / primary.targetAmount) * 100).toInt()
                            )
                        }
                    }
            }
        }
    }

    fun dismissAddSavingsDialog() {
        _uiState.update { it.copy(showAddSavingsDialog = false) }
    }

    fun saveGoal(goal: SavingsGoal) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            goalRepository.saveGoal(goal)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, isSaved = true) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun confirmDeleteGoal() {
        _uiState.update { it.copy(showDeleteDialog = true) }
    }

    fun cancelDeleteGoal() {
        _uiState.update { it.copy(showDeleteDialog = false) }
    }

    fun executeDeleteGoal() {
        val id = _uiState.value.primaryGoal?.id ?: return
        deleteGoal(id)
    }

    fun deleteGoal(id: String) {
        viewModelScope.launch {
            goalRepository.deleteGoal(id)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            allGoals = state.allGoals.filterNot { it.id == id },
                            otherGoals = state.otherGoals.filterNot { it.id == id },
                            primaryGoal = state.primaryGoal?.takeUnless { it.id == id },
                            isDeleted = true,
                            showDeleteDialog = false
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message, showDeleteDialog = false) }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
