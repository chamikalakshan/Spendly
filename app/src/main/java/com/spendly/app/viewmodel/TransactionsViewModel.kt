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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
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
            val userId = authRepository.getCurrentUserId() ?: return@launch
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
        val months = mutableListOf<Triple<String, Long, Long>>()
        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.US)

        for (i in 0 until 12) {
            val label = sdf.format(calendar.time)
            val start = (calendar.clone() as Calendar).apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val end = (calendar.clone() as Calendar).apply {
                set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis

            months.add(Triple(label, start, end))
            calendar.add(Calendar.MONTH, -1)
        }
        return months
    }

    private fun formatDateLabel(timeMs: Long): String {
        val now = Calendar.getInstance()
        val date = Calendar.getInstance().apply { timeInMillis = timeMs }
        return when {
            isSameDay(now, date) -> "Today"
            isYesterday(now, date) -> "Yesterday"
            else -> SimpleDateFormat("EEE, dd MMM yyyy", Locale.US).format(Date(timeMs))
        }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean =
        cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
        cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)

    private fun isYesterday(now: Calendar, date: Calendar): Boolean {
        val yesterday = (now.clone() as Calendar).apply { add(Calendar.DATE, -1) }
        return isSameDay(yesterday, date)
    }
}
