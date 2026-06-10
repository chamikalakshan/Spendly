package com.spendly.financetracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendly.financetracker.data.model.Budget
import com.spendly.financetracker.data.model.BudgetProgress
import com.spendly.financetracker.data.model.ExpenseType
import com.spendly.financetracker.data.model.FinanceTransaction
import com.spendly.financetracker.data.model.SavingsGoal
import com.spendly.financetracker.data.model.TransactionType
import com.spendly.financetracker.data.model.UserProfile
import com.spendly.financetracker.data.repository.AuthRepository
import com.spendly.financetracker.data.repository.BudgetRepository
import com.spendly.financetracker.data.repository.GoalRepository
import com.spendly.financetracker.data.repository.TransactionRepository
import com.spendly.financetracker.data.repository.UserRepository
import com.spendly.financetracker.data.service.ExportedReport
import com.spendly.financetracker.data.service.RecommendationService
import com.spendly.financetracker.data.service.ReportExportService
import com.spendly.financetracker.data.service.SmartInsight
import com.spendly.financetracker.ui.util.MonthOption
import com.spendly.financetracker.ui.util.monthLabel
import com.spendly.financetracker.ui.util.monthOptions
import com.spendly.financetracker.ui.util.monthStart
import com.spendly.financetracker.ui.util.nextMonthStart
import com.spendly.financetracker.ui.util.shiftMonth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AnalyticsUiState(
    val transactions: List<FinanceTransaction> = emptyList(),
    val budgets: List<Budget> = emptyList(),
    val goals: List<SavingsGoal> = emptyList(),
    val profile: UserProfile? = null,
    val selectedMonthStartMillis: Long = monthStart(System.currentTimeMillis()),
    val monthOptions: List<MonthOption> = monthOptions(),
    val smartInsights: List<SmartInsight> = emptyList(),
    val exportedReport: ExportedReport? = null,
    val message: String? = null,
    val isLoading: Boolean = true,
    val error: String? = null
) {
    val selectedMonthLabel: String get() = monthLabel(selectedMonthStartMillis)
    val selectedMonthTransactions: List<FinanceTransaction> by lazy {
        transactions.filter { it.dateMillis in selectedMonthStartMillis until nextMonthStart(selectedMonthStartMillis) }
    }
    val totalIncome: Long by lazy { selectedMonthTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amountCents } }
    val totalExpense: Long by lazy { selectedMonthTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountCents } }
    val budgetProgress: List<BudgetProgress> by lazy {
            val monthBudgets = budgets.filter { it.monthStartMillis == selectedMonthStartMillis && it.deletedAtMillis == null }
            val expenses = selectedMonthTransactions.filter { it.type == TransactionType.EXPENSE }
            monthBudgets.map { budget ->
                val spent = expenses.filter { it.category.equals(budget.category, ignoreCase = true) }.sumOf { it.amountCents }
                val percent = if (budget.limitCents > 0L) ((spent * 100L) / budget.limitCents).toInt() else 0
                BudgetProgress(
                    budget = budget,
                    spentCents = spent,
                    remainingCents = (budget.limitCents - spent).coerceAtLeast(0L),
                    progressPercent = percent,
                    isWarning = percent >= budget.alertThresholdPercent && spent <= budget.limitCents,
                    isExceeded = spent > budget.limitCents
                )
            }
        }
    val spendingByCategory: List<AnalyticsSlice> by lazy {
        selectedMonthTransactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category.ifBlank { "Other" }.take(18) }
            .mapValues { entry -> entry.value.sumOf { it.amountCents } }
            .toList()
            .sortedByDescending { it.second }
            .map { (label, amount) -> AnalyticsSlice(label, amount, percentOf(amount, totalExpense)) }
    }

    val spendingSplit: SpendingSplitUi by lazy {
            val committed = selectedMonthTransactions
                .filter { it.type == TransactionType.EXPENSE && (it.expenseType == ExpenseType.COMMITTED || it.category in committedCategories) }
                .sumOf { it.amountCents }
            val discretionary = (totalExpense - committed).coerceAtLeast(0L)
            SpendingSplitUi(
                committedCents = committed,
                discretionaryCents = discretionary,
                committedPercent = percentOf(committed, totalExpense),
                discretionaryPercent = percentOf(discretionary, totalExpense)
            )
        }

    val monthlyOverview: List<AnalyticsMonth> by lazy {
        (4 downTo 0).map { offset ->
            val start = shiftMonth(selectedMonthStartMillis, -offset)
            val end = nextMonthStart(start)
            val monthTransactions = transactions.filter { it.dateMillis in start until end }
            AnalyticsMonth(
                label = monthLabel(start).take(3),
                income = monthTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amountCents },
                expense = monthTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountCents }
            )
        }
    }

    val incomeSources: List<AnalyticsSlice> by lazy {
        selectedMonthTransactions
            .filter { it.type == TransactionType.INCOME }
            .groupBy { it.source.ifBlank { "Other" }.take(18) }
            .mapValues { entry -> entry.value.sumOf { it.amountCents } }
            .toList()
            .sortedByDescending { it.second }
            .map { (label, amount) -> AnalyticsSlice(label, amount, percentOf(amount, totalIncome)) }
    }

    private fun percentOf(amount: Long, total: Long): Double =
        if (total > 0L) (amount.toDouble() * 100.0) / total.toDouble() else 0.0

    private companion object {
        val committedCategories = setOf("Rent", "Subscriptions", "Gym")
    }
}

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository,
    private val goalRepository: GoalRepository,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val reportExportService: ReportExportService,
    private val recommendationService: RecommendationService
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        val userId = authRepository.getCurrentUserId()
        if (userId == null) {
            _uiState.update { it.copy(isLoading = false, error = "Please log in again") }
        } else {
            viewModelScope.launch {
                combine(
                    transactionRepository.observeTransactions(userId),
                    budgetRepository.observeBudgets(userId),
                    goalRepository.observeGoals(userId),
                    userRepository.observeProfile(userId)
                ) { transactions, budgets, goals, profile ->
                    AnalyticsUiState(
                        transactions = transactions,
                        budgets = budgets,
                        goals = goals,
                        profile = profile,
                        selectedMonthStartMillis = _uiState.value.selectedMonthStartMillis,
                        monthOptions = _uiState.value.monthOptions,
                        smartInsights = recommendationService.buildInsights(
                            transactions,
                            _uiState.value.selectedMonthStartMillis,
                            _uiState.value.copy(transactions = transactions, budgets = budgets).budgetProgress
                        ),
                        isLoading = false
                    )
                }
                    .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                    .collect { next -> _uiState.value = next }
            }
        }
    }

    fun selectMonth(startMillis: Long) = _uiState.update {
        it.copy(
            selectedMonthStartMillis = startMillis,
            smartInsights = recommendationService.buildInsights(it.transactions, startMillis, it.copy(selectedMonthStartMillis = startMillis).budgetProgress)
        )
    }

    fun exportCsv() {
        val state = _uiState.value
        viewModelScope.launch {
            reportExportService.exportCsv(state.profile, state.transactions, state.selectedMonthStartMillis)
                .onSuccess { report -> _uiState.update { it.copy(exportedReport = report, message = "CSV report created.") } }
                .onFailure { error -> _uiState.update { it.copy(message = error.message ?: "Failed to export CSV.") } }
        }
    }

    fun exportPdf() {
        val state = _uiState.value
        viewModelScope.launch {
            reportExportService.exportPdf(state.profile, state.transactions, state.budgetProgress, state.goals, state.selectedMonthStartMillis)
                .onSuccess { report -> _uiState.update { it.copy(exportedReport = report, message = "PDF report created.") } }
                .onFailure { error -> _uiState.update { it.copy(message = error.message ?: "Failed to export PDF.") } }
        }
    }

    fun clearExport() = _uiState.update { it.copy(exportedReport = null, message = null) }
}
