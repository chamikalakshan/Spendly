package com.spendly.financetracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendly.financetracker.data.model.ExpenseType
import com.spendly.financetracker.data.model.FinanceTransaction
import com.spendly.financetracker.data.model.TransactionType
import com.spendly.financetracker.data.repository.AuthRepository
import com.spendly.financetracker.data.repository.TransactionRepository
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AnalyticsUiState(
    val transactions: List<FinanceTransaction> = emptyList(),
    val selectedMonthStartMillis: Long = monthStart(System.currentTimeMillis()),
    val monthOptions: List<MonthOption> = monthOptions(),
    val isLoading: Boolean = true,
    val error: String? = null
) {
    val selectedMonthLabel: String get() = monthLabel(selectedMonthStartMillis)
    val selectedMonthTransactions: List<FinanceTransaction>
        get() = transactions.filter { it.dateMillis in selectedMonthStartMillis until nextMonthStart(selectedMonthStartMillis) }
    val totalIncome: Long get() = selectedMonthTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amountCents }
    val totalExpense: Long get() = selectedMonthTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountCents }
    val spendingByCategory: List<AnalyticsSlice>
        get() = selectedMonthTransactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category.ifBlank { "Other" }.take(18) }
            .mapValues { entry -> entry.value.sumOf { it.amountCents } }
            .toList()
            .sortedByDescending { it.second }
            .map { (label, amount) -> AnalyticsSlice(label, amount, percentOf(amount, totalExpense)) }

    val spendingSplit: SpendingSplitUi
        get() {
            val committed = selectedMonthTransactions
                .filter { it.type == TransactionType.EXPENSE && (it.expenseType == ExpenseType.COMMITTED || it.category in committedCategories) }
                .sumOf { it.amountCents }
            val discretionary = (totalExpense - committed).coerceAtLeast(0L)
            return SpendingSplitUi(
                committedCents = committed,
                discretionaryCents = discretionary,
                committedPercent = percentOf(committed, totalExpense),
                discretionaryPercent = percentOf(discretionary, totalExpense)
            )
        }

    val monthlyOverview: List<AnalyticsMonth>
        get() = (4 downTo 0).map { offset ->
            val start = shiftMonth(selectedMonthStartMillis, -offset)
            val end = nextMonthStart(start)
            val monthTransactions = transactions.filter { it.dateMillis in start until end }
            AnalyticsMonth(
                label = monthLabel(start).take(3),
                income = monthTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amountCents },
                expense = monthTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountCents }
            )
        }

    val incomeSources: List<AnalyticsSlice>
        get() = selectedMonthTransactions
            .filter { it.type == TransactionType.INCOME }
            .groupBy { it.source.ifBlank { "Other" }.take(18) }
            .mapValues { entry -> entry.value.sumOf { it.amountCents } }
            .toList()
            .sortedByDescending { it.second }
            .map { (label, amount) -> AnalyticsSlice(label, amount, percentOf(amount, totalIncome)) }

    private fun percentOf(amount: Long, total: Long): Double =
        if (total > 0L) (amount.toDouble() * 100.0) / total.toDouble() else 0.0

    private companion object {
        val committedCategories = setOf("Rent", "Subscriptions", "Gym")
    }
}

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        val userId = authRepository.getCurrentUserId()
        if (userId == null) {
            _uiState.update { it.copy(isLoading = false, error = "Please log in again") }
        } else {
            viewModelScope.launch {
                transactionRepository.observeTransactions(userId)
                    .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                    .collect { list -> _uiState.update { it.copy(transactions = list, isLoading = false) } }
            }
        }
    }

    fun selectMonth(startMillis: Long) = _uiState.update { it.copy(selectedMonthStartMillis = startMillis) }
}
