package com.spendly.financetracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendly.financetracker.data.model.FinanceTransaction
import com.spendly.financetracker.data.model.TransactionType
import com.spendly.financetracker.data.repository.AuthRepository
import com.spendly.financetracker.data.repository.TransactionRepository
import com.spendly.financetracker.ui.util.MonthOption
import com.spendly.financetracker.ui.util.monthLabel
import com.spendly.financetracker.ui.util.monthOptions
import com.spendly.financetracker.ui.util.monthStart
import com.spendly.financetracker.ui.util.nextMonthStart
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

data class TransactionDateGroup(
    val label: String,
    val transactions: List<FinanceTransaction>
)

private fun fullDateLabel(timeMillis: Long): String =
    SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(timeMillis)

data class TransactionsUiState(
    val transactions: List<FinanceTransaction> = emptyList(),
    val filter: TransactionTab = TransactionTab.ALL,
    val selectedMonthStartMillis: Long = monthStart(System.currentTimeMillis()),
    val transactionPendingDelete: FinanceTransaction? = null,
    val monthOptions: List<MonthOption> = monthOptions(),
    val isLoading: Boolean = true,
    val error: String? = null
) {
    val selectedMonthLabel: String
        get() = monthLabel(selectedMonthStartMillis)

    val filtered: List<FinanceTransaction>
        get() = when (filter) {
            TransactionTab.ALL -> transactions
            TransactionTab.EXPENSES -> transactions.filter { it.type == TransactionType.EXPENSE }
            TransactionTab.INCOMES -> transactions.filter { it.type == TransactionType.INCOME }
        }.filter { it.dateMillis in selectedMonthStartMillis until nextMonthStart(selectedMonthStartMillis) }
            .sortedByDescending { it.dateMillis }

    val groupedTransactions: List<TransactionDateGroup>
        get() = filtered
            .groupBy { fullDateLabel(it.dateMillis) }
            .map { TransactionDateGroup(it.key, it.value) }
}

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(TransactionsUiState())
    val uiState: StateFlow<TransactionsUiState> = _uiState.asStateFlow()

    init {
        observeTransactions()
    }

    fun setFilter(tab: TransactionTab) = _uiState.update { it.copy(filter = tab) }
    fun selectMonth(startMillis: Long) = _uiState.update { it.copy(selectedMonthStartMillis = startMillis) }
    fun requestDelete(transaction: FinanceTransaction) = _uiState.update { it.copy(transactionPendingDelete = transaction) }
    fun cancelDelete() = _uiState.update { it.copy(transactionPendingDelete = null) }

    fun delete(transaction: FinanceTransaction) {
        viewModelScope.launch {
            transactionRepository.deleteTransaction(transaction)
                .onSuccess { _uiState.update { it.copy(transactionPendingDelete = null, error = null) } }
                .onFailure { e -> _uiState.update { it.copy(transactionPendingDelete = null, error = e.message) } }
        }
    }

    private fun observeTransactions() {
        val uid = authRepository.getCurrentUserId()
        if (uid == null) {
            _uiState.update { it.copy(isLoading = false, error = "Please log in again") }
            return
        }
        viewModelScope.launch {
            transactionRepository.observeTransactions(uid)
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { list -> _uiState.update { it.copy(transactions = list, isLoading = false) } }
        }
    }

}
