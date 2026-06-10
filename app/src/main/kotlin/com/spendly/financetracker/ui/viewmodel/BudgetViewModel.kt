package com.spendly.financetracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendly.financetracker.data.model.Budget
import com.spendly.financetracker.data.model.BudgetDraft
import com.spendly.financetracker.data.model.BudgetProgress
import com.spendly.financetracker.data.model.TransactionType
import com.spendly.financetracker.data.repository.AuthRepository
import com.spendly.financetracker.data.repository.BudgetRepository
import com.spendly.financetracker.data.repository.TransactionRepository
import com.spendly.financetracker.data.repository.UserRepository
import com.spendly.financetracker.ui.util.MonthOption
import com.spendly.financetracker.ui.util.monthLabel
import com.spendly.financetracker.ui.util.monthOptions
import com.spendly.financetracker.ui.util.monthStart
import com.spendly.financetracker.ui.util.nextMonthStart
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BudgetUiState(
    val selectedMonthStart: Long = monthStart(System.currentTimeMillis()),
    val selectedMonthLabel: String = monthLabel(monthStart(System.currentTimeMillis())),
    val monthOptions: List<MonthOption> = monthOptions(),
    val defaultCurrency: String = "LKR",
    val budgets: List<BudgetProgress> = emptyList(),
    val totalLimitCents: Long = 0L,
    val totalSpentCents: Long = 0L,
    val isLoading: Boolean = true,
    val error: String? = null,
    val message: String? = null
)

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()
    private var observeJob: Job? = null

    init {
        observeData()
    }

    fun selectMonth(monthStartMillis: Long) {
        _uiState.update {
            it.copy(
                selectedMonthStart = monthStartMillis,
                selectedMonthLabel = monthLabel(monthStartMillis),
                isLoading = true
            )
        }
        observeData()
    }

    fun saveBudget(category: String, limitInput: String, note: String = "", editingId: String? = null) {
        val uid = authRepository.getCurrentUserId() ?: return setError("Please log in again")
        val amountCents = parseAmountCents(limitInput)
        val state = _uiState.value
        if (category.isBlank()) return setError("Select a category")
        if (amountCents == null || amountCents <= 0L) return setError("Enter a valid budget limit")
        viewModelScope.launch {
            budgetRepository.saveBudget(
                uid,
                BudgetDraft(
                    id = editingId,
                    category = category.trim(),
                    monthStartMillis = state.selectedMonthStart,
                    limitCents = amountCents,
                    defaultCurrency = state.defaultCurrency,
                    note = note.trim()
                )
            ).onSuccess {
                _uiState.update { it.copy(message = "Budget saved", error = null) }
            }.onFailure { error ->
                setError(error.message ?: "Failed to save budget")
            }
        }
    }

    fun deleteBudget(id: String) {
        viewModelScope.launch {
            budgetRepository.deleteBudget(id)
                .onSuccess { _uiState.update { it.copy(message = "Budget deleted") } }
                .onFailure { setError(it.message ?: "Failed to delete budget") }
        }
    }

    fun clearMessage() = _uiState.update { it.copy(message = null, error = null) }

    private fun observeData() {
        val uid = authRepository.getCurrentUserId()
        if (uid == null) {
            _uiState.update { it.copy(isLoading = false, error = "Please log in again") }
            return
        }
        val selectedMonth = _uiState.value.selectedMonthStart
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            combine(
                budgetRepository.observeBudgetsForMonth(uid, selectedMonth),
                transactionRepository.observeTransactions(uid),
                userRepository.observeProfile(uid)
            ) { budgets, transactions, profile ->
                val monthEnd = nextMonthStart(selectedMonth)
                val expenses = transactions.filter {
                    it.type == TransactionType.EXPENSE &&
                        it.dateMillis >= selectedMonth &&
                        it.dateMillis < monthEnd
                }
                val progress = budgets.map { budget ->
                    val spent = expenses
                        .filter { it.category.equals(budget.category, ignoreCase = true) }
                        .sumOf { it.amountCents }
                    val percent = if (budget.limitCents <= 0L) 0 else ((spent * 100) / budget.limitCents).toInt()
                    BudgetProgress(
                        budget = budget,
                        spentCents = spent,
                        remainingCents = (budget.limitCents - spent).coerceAtLeast(0L),
                        progressPercent = percent.coerceAtLeast(0),
                        isWarning = percent >= budget.alertThresholdPercent && spent <= budget.limitCents,
                        isExceeded = spent > budget.limitCents
                    )
                }
                _uiState.value.copy(
                    defaultCurrency = profile?.defaultCurrency ?: "LKR",
                    budgets = progress,
                    totalLimitCents = budgets.sumOf { it.limitCents },
                    totalSpentCents = progress.sumOf { it.spentCents },
                    isLoading = false
                )
            }.collect { next -> _uiState.value = next }
        }
    }

    private fun setError(message: String) {
        _uiState.update { it.copy(error = message, isLoading = false) }
    }

    private fun parseAmountCents(input: String): Long? {
        val n = input.trim()
        if (!Regex("^\\d+([.]\\d{1,2})?$").matches(n)) return null
        val parts = n.split(".")
        val whole = parts[0].toLongOrNull() ?: return null
        val cents = parts.getOrNull(1)?.padEnd(2, '0') ?: "00"
        return whole * 100L + (cents.toLongOrNull() ?: return null)
    }
}
