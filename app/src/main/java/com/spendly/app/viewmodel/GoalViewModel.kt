package com.spendly.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendly.app.data.model.ExpenseEntry
import com.spendly.app.data.model.IncomeEntry
import com.spendly.app.data.model.SavingsGoal
import com.spendly.app.repository.AuthRepository
import com.spendly.app.repository.ExpenseRepository
import com.spendly.app.repository.GoalRepository
import com.spendly.app.repository.IncomeRepository
import com.spendly.app.utils.FormatUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject
import kotlin.math.max

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

    private val _uiState = MutableStateFlow(GoalUiState(isLoading = true))
    val uiState: StateFlow<GoalUiState> = _uiState.asStateFlow()

    private val userId = authRepository.getCurrentUserId().orEmpty()
    private var manualSavingsAdjustment = 0.0
    private var selectedGoalId: String? = null

    init {
        loadGoalsData()
    }

    private fun loadGoalsData() {
        if (userId.isBlank()) {
            _uiState.update { it.copy(isLoading = false, error = "Please log in again") }
            return
        }

        viewModelScope.launch {
            combine(
                goalRepository.getGoals(userId),
                incomeRepository.getAllIncome(userId),
                expenseRepository.getAllExpenses(userId)
            ) { goals, income, expenses ->
                buildState(goals, income, expenses, manualSavingsAdjustment)
            }.catch { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }.collect { calculated ->
                _uiState.update { current ->
                    calculated.copy(
                        showDeleteDialog = current.showDeleteDialog,
                        showAddSavingsDialog = current.showAddSavingsDialog,
                        addSavingsAmount = current.addSavingsAmount,
                        isSaved = current.isSaved,
                        isDeleted = current.isDeleted,
                        error = current.error
                    )
                }
            }
        }
    }

    private fun buildState(
        goals: List<SavingsGoal>,
        income: List<IncomeEntry>,
        expenses: List<ExpenseEntry>,
        adjustment: Double
    ): GoalUiState {
        val totalIncome = income.sumOf { it.amountLKR }
        val totalExpenses = expenses.sumOf { it.amount }
        val saved = (totalIncome - totalExpenses + adjustment).coerceAtLeast(0.0)
        val primary = selectedGoalId
            ?.let { id -> goals.firstOrNull { it.id == id } }
            ?: goals.firstOrNull()
        val remaining = (primary?.targetAmount ?: 0.0).minus(saved).coerceAtLeast(0.0)
        val monthsLeft = monthsUntil(primary?.targetDate ?: System.currentTimeMillis())
        val requiredMonthly = if (remaining > 0.0) remaining / monthsLeft.coerceAtLeast(1) else 0.0
        val currentMonthSavings = currentMonthSavings(income, expenses)
        val progress = getGoalProgress(primary, saved)
        val monthlyPairs = monthlySavings(income, expenses)

        return GoalUiState(
            allGoals = goals,
            primaryGoal = primary,
            otherGoals = if (selectedGoalId == null) {
                goals.drop(1)
            } else {
                goals.filterNot { it.id == primary?.id }
            },
            savedAmount = saved,
            progressPercent = progress,
            progressDisplay = (progress * 100).toInt(),
            remainingAmount = remaining,
            monthsLeft = monthsLeft,
            requiredMonthly = requiredMonthly,
            currentMonthlySavings = currentMonthSavings,
            isOnTrack = currentMonthSavings >= requiredMonthly || remaining == 0.0,
            projectionText = buildProjectionText(saved, primary, currentMonthSavings),
            projectedDate = projectedDate(saved, primary, currentMonthSavings),
            monthlySavings = monthlyPairs.map { it.second },
            monthLabels = monthlyPairs.map { it.first },
            isLoading = false
        )
    }

    fun selectGoal(goalId: String?) {
        selectedGoalId = goalId?.takeIf { it.isNotBlank() }
        val current = _uiState.value
        if (current.allGoals.isEmpty()) return

        val selected = selectedGoalId
            ?.let { id -> current.allGoals.firstOrNull { it.id == id } }
            ?: current.allGoals.firstOrNull()

        val saved = current.savedAmount
        val remaining = (selected?.targetAmount ?: 0.0).minus(saved).coerceAtLeast(0.0)
        val monthsLeft = monthsUntil(selected?.targetDate ?: System.currentTimeMillis())
        val requiredMonthly = if (remaining > 0.0) remaining / monthsLeft.coerceAtLeast(1) else 0.0
        val progress = getGoalProgress(selected, saved)

        _uiState.update {
            it.copy(
                primaryGoal = selected,
                otherGoals = if (selectedGoalId == null) {
                    it.allGoals.drop(1)
                } else {
                    it.allGoals.filterNot { goal -> goal.id == selected?.id }
                },
                progressPercent = progress,
                progressDisplay = (progress * 100).toInt(),
                remainingAmount = remaining,
                monthsLeft = monthsLeft,
                requiredMonthly = requiredMonthly,
                isOnTrack = it.currentMonthlySavings >= requiredMonthly || remaining == 0.0,
                projectionText = buildProjectionText(saved, selected, it.currentMonthlySavings),
                projectedDate = projectedDate(saved, selected, it.currentMonthlySavings)
            )
        }
    }

    fun getGoalProgress(goal: SavingsGoal?): Float {
        return getGoalProgress(goal, _uiState.value.savedAmount)
    }

    private fun getGoalProgress(goal: SavingsGoal?, savedAmount: Double): Float {
        if (goal == null || goal.targetAmount <= 0.0) return 0f
        return (savedAmount / goal.targetAmount).coerceIn(0.0, 1.0).toFloat()
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
        if (extra <= 0.0) {
            _uiState.update { it.copy(error = "Enter a valid savings amount") }
            return
        }

        manualSavingsAdjustment += extra
        _uiState.update {
            val saved = it.savedAmount + extra
            val progress = getGoalProgress(it.primaryGoal, saved)
            it.copy(
                showAddSavingsDialog = false,
                addSavingsAmount = "",
                savedAmount = saved,
                remainingAmount = ((it.primaryGoal?.targetAmount ?: 0.0) - saved).coerceAtLeast(0.0),
                progressPercent = progress,
                progressDisplay = (progress * 100).toInt()
            )
        }
    }

    fun dismissAddSavingsDialog() {
        _uiState.update { it.copy(showAddSavingsDialog = false) }
    }

    fun saveGoal(goal: SavingsGoal) {
        if (userId.isBlank()) {
            _uiState.update { it.copy(error = "Please log in again") }
            return
        }

        _uiState.update { it.copy(isLoading = true, isSaved = false) }
        viewModelScope.launch {
            val finalGoal = goal.copy(
                id = goal.id.ifBlank { UUID.randomUUID().toString() },
                userId = goal.userId.ifBlank { userId },
                isSynced = false,
                createdAt = if (goal.createdAt == 0L) System.currentTimeMillis() else goal.createdAt
            )

            goalRepository.saveGoal(finalGoal)
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
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            goalRepository.deleteGoal(id)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isDeleted = true,
                            showDeleteDialog = false
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message, showDeleteDialog = false) }
                }
        }
    }

    fun buildProjectionText(
        savedAmount: Double = _uiState.value.savedAmount,
        goal: SavingsGoal? = _uiState.value.primaryGoal,
        monthlySavings: Double = _uiState.value.currentMonthlySavings
    ): String {
        if (goal == null) return "Create a goal to see your live projection."
        if (goal.targetAmount <= savedAmount) return "Goal reached. Keep the momentum going."
        if (monthlySavings <= 0.0) return "Add positive monthly savings to calculate a completion date."

        val monthsNeeded = max(((goal.targetAmount - savedAmount) / monthlySavings).toInt(), 1)
        val projected = Calendar.getInstance().apply { add(Calendar.MONTH, monthsNeeded) }
        val target = Calendar.getInstance().apply { timeInMillis = goal.targetDate }
        val projectedText = FormatUtils.formatMonthYear(projected.timeInMillis)

        return if (projected.timeInMillis <= target.timeInMillis) {
            "At your current rate, you'll reach your goal by $projectedText, ahead of schedule."
        } else {
            "At your current rate, you'll reach your goal by $projectedText, after your target date."
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun projectedDate(savedAmount: Double, goal: SavingsGoal?, monthlySavings: Double): String {
        if (goal == null || monthlySavings <= 0.0) return "Not available"
        if (goal.targetAmount <= savedAmount) return FormatUtils.formatMonthYear(System.currentTimeMillis())

        val monthsNeeded = max(((goal.targetAmount - savedAmount) / monthlySavings).toInt(), 1)
        return FormatUtils.formatMonthYear(
            Calendar.getInstance().apply { add(Calendar.MONTH, monthsNeeded) }.timeInMillis
        )
    }

    private fun monthsUntil(targetDate: Long): Int {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply { timeInMillis = targetDate }
        val months = ((target.get(Calendar.YEAR) - now.get(Calendar.YEAR)) * 12) +
            (target.get(Calendar.MONTH) - now.get(Calendar.MONTH))
        return max(months, 1)
    }

    private fun currentMonthSavings(income: List<IncomeEntry>, expenses: List<ExpenseEntry>): Double {
        val now = Calendar.getInstance()
        val (start, end) = FormatUtils.getMonthBoundaries(
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH)
        )
        return income.filter { it.date in start..end }.sumOf { it.amountLKR } -
            expenses.filter { it.date in start..end }.sumOf { it.amount }
    }

    private fun monthlySavings(
        income: List<IncomeEntry>,
        expenses: List<ExpenseEntry>
    ): List<Pair<String, Double>> {
        return FormatUtils.getLast6Months().map { (label, start, end) ->
            val savings = income.filter { it.date in start..end }.sumOf { it.amountLKR } -
                expenses.filter { it.date in start..end }.sumOf { it.amount }
            label.take(3) to savings
        }
    }
}
