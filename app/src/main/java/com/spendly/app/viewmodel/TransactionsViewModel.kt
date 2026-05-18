package com.spendly.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendly.app.data.model.TransactionItem
import com.spendly.app.data.model.amountLKR
import com.spendly.app.data.model.dateMs
import com.spendly.app.data.model.enums.TransactionFilter
import com.spendly.app.data.model.id
import com.spendly.app.repository.AuthRepository
import com.spendly.app.repository.ExpenseRepository
import com.spendly.app.repository.IncomeRepository
import com.spendly.app.utils.FormatUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TransactionsUiState(
    val transactions: List<TransactionItem> = emptyList(),
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val selectedMonthLabel: String = "",
    val filter: TransactionFilter = TransactionFilter.ALL,
    val expandedItemId: String? = null,
    val deleteConfirmItem: TransactionItem? = null,
    val isLoading: Boolean = false
)

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val incomeRepository: IncomeRepository,
    private val expenseRepository: ExpenseRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionsUiState())
    val uiState: StateFlow<TransactionsUiState> = _uiState.asStateFlow()

    private val _selectedMonthRange = MutableStateFlow<Pair<Long, Long>?>(null)

    val availableMonths: List<Triple<String, Long, Long>> = generateAvailableMonths()

    val filteredTransactions = combine(_uiState, _selectedMonthRange) { state, _ ->
        state.transactions.filter { tx ->
            when (state.filter) {
                TransactionFilter.ALL -> true
                TransactionFilter.INCOME -> tx is TransactionItem.Income
                TransactionFilter.EXPENSE -> tx is TransactionItem.Expense
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val groupedByDate = filteredTransactions.map { list ->
        list.groupBy { formatDateLabel(it.dateMs) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    init {
        val currentMonth = availableMonths.first()
        setMonth(currentMonth.second, currentMonth.third, currentMonth.first)
    }

    fun setFilter(filter: TransactionFilter) {
        _uiState.update { it.copy(filter = filter) }
    }

    fun toggleExpand(id: String) {
        _uiState.update { 
            it.copy(expandedItemId = if (it.expandedItemId == id) null else id)
        }
    }

    fun setMonth(startMs: Long, endMs: Long, label: String) {
        _uiState.update { it.copy(selectedMonthLabel = label, isLoading = true) }
        _selectedMonthRange.value = startMs to endMs
        loadTransactions(startMs, endMs)
    }

    fun confirmDelete(item: TransactionItem) {
        _uiState.update { it.copy(deleteConfirmItem = item) }
    }

    fun dismissDelete() {
        _uiState.update { it.copy(deleteConfirmItem = null) }
    }

    fun deleteTransaction(item: TransactionItem) {
        viewModelScope.launch {
            when (item) {
                is TransactionItem.Income -> incomeRepository.deleteIncome(item.id)
                is TransactionItem.Expense -> expenseRepository.deleteExpense(item.id)
            }
            dismissDelete()
        }
    }

    private fun loadTransactions(startMs: Long, endMs: Long) {
        val userId = authRepository.getCurrentUserId() ?: return
        
        viewModelScope.launch {
            combine(
                incomeRepository.getMonthlyIncome(userId, startMs, endMs),
                expenseRepository.getMonthlyExpenses(userId, startMs, endMs)
            ) { incomeList, expenseList ->
                val all = (incomeList.map { TransactionItem.Income(it) } + 
                           expenseList.map { TransactionItem.Expense(it) })
                           .sortedByDescending { it.dateMs }

                _uiState.value.copy(
                    transactions = all,
                    totalIncome = incomeList.sumOf { it.amountLKR },
                    totalExpense = expenseList.sumOf { it.amount }, // amount is LKR for Expense
                    isLoading = false
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    private fun generateAvailableMonths(): List<Triple<String, Long, Long>> {
        return FormatUtils.getLast6Months().asReversed()
    }

    private fun formatDateLabel(timeMs: Long): String {
        return FormatUtils.formatDateGroupHeader(timeMs)
    }
}
