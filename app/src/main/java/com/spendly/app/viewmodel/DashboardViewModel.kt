package com.spendly.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendly.app.data.model.SavingsGoal
import com.spendly.app.data.model.TransactionItem
import com.spendly.app.data.model.dateMs
import com.spendly.app.repository.AuthRepository
import com.spendly.app.repository.ExpenseRepository
import com.spendly.app.repository.GoalRepository
import com.spendly.app.repository.IncomeRepository
import com.spendly.app.utils.FormatUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.max

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val incomeRepository: IncomeRepository,
    private val expenseRepository: ExpenseRepository,
    private val goalRepository: GoalRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val userId = authRepository.getCurrentUserId().orEmpty()
    private val currentUser = authRepository.getCurrentUser()
    private val monthBounds = currentMonthBounds()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val userName: StateFlow<String> = MutableStateFlow(
        currentUser?.name?.takeIf { it.isNotBlank() } ?: "User"
    ).asStateFlow()

    val userInitials: StateFlow<String> = userName
        .map { name -> initialsFor(name) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initialsFor(userName.value))

    val currentMonthIncome: StateFlow<Double> =
        if (userId.isBlank()) {
            flowOf(0.0)
        } else {
            incomeRepository.getMonthlyIncome(userId, monthBounds.first, monthBounds.second)
                .map { entries -> entries.sumOf { it.amountLKR } }
        }.catch { e ->
            _error.value = e.message
            emit(0.0)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val currentMonthExpenses: StateFlow<Double> =
        if (userId.isBlank()) {
            flowOf(0.0)
        } else {
            expenseRepository.getMonthlyExpenses(userId, monthBounds.first, monthBounds.second)
                .map { entries -> entries.sumOf { it.amount } }
        }.catch { e ->
            _error.value = e.message
            emit(0.0)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val netSavings: StateFlow<Double> = combine(currentMonthIncome, currentMonthExpenses) { income, expenses ->
        income - expenses
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val savingsRate: StateFlow<Int> = combine(currentMonthIncome, netSavings) { income, savings ->
        if (income > 0.0) ((savings / income) * 100).toInt() else 0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val recentTransactions: StateFlow<List<TransactionItem>> =
        if (userId.isBlank()) {
            flowOf(emptyList())
        } else {
            combine(
                incomeRepository.getAllIncome(userId),
                expenseRepository.getAllExpenses(userId)
            ) { income, expenses ->
                (income.map { TransactionItem.Income(it) } + expenses.map { TransactionItem.Expense(it) })
                    .sortedByDescending { it.dateMs }
                    .take(6)
            }
        }.catch { e ->
            _error.value = e.message
            emit(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeGoal: StateFlow<SavingsGoal?> =
        if (userId.isBlank()) {
            flowOf(null)
        } else {
            goalRepository.getGoals(userId).map { goals -> goals.firstOrNull() }
        }.catch { e ->
            _error.value = e.message
            emit(null)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val goalProgressPercent: StateFlow<Float> = combine(activeGoal, netSavings) { goal, savings ->
        progressFor(goal, savings)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    val requiredMonthlySavings: StateFlow<Double> = combine(activeGoal, netSavings) { goal, savings ->
        requiredMonthlyFor(goal, savings)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val isOnTrack: StateFlow<Boolean> = combine(netSavings, requiredMonthlySavings) { savings, required ->
        required <= 0.0 || savings >= required
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private fun currentMonthBounds(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        return FormatUtils.getMonthBoundaries(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH)
        )
    }

    private fun initialsFor(name: String): String {
        return name
            .trim()
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
            .ifBlank { "U" }
    }

    private fun progressFor(goal: SavingsGoal?, savings: Double): Float {
        if (goal == null || goal.targetAmount <= 0.0) return 0f
        return (savings.coerceAtLeast(0.0) / goal.targetAmount).coerceIn(0.0, 1.0).toFloat()
    }

    private fun requiredMonthlyFor(goal: SavingsGoal?, savings: Double): Double {
        if (goal == null) return 0.0
        val remaining = max(goal.targetAmount - savings.coerceAtLeast(0.0), 0.0)
        if (remaining <= 0.0) return 0.0

        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply { timeInMillis = goal.targetDate }
        val monthDiff = ((target.get(Calendar.YEAR) - now.get(Calendar.YEAR)) * 12) +
            (target.get(Calendar.MONTH) - now.get(Calendar.MONTH))
        val monthsLeft = max(ceil(monthDiff.toDouble()).toInt(), 1)
        return remaining / monthsLeft
    }
}
