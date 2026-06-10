package com.spendly.financetracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.spendly.financetracker.data.model.FinanceTransaction
import com.spendly.financetracker.data.model.TransactionType
import com.spendly.financetracker.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val transactions: List<FinanceTransaction> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
) {
    val incomeCents: Long get() = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amountCents }
    val expenseCents: Long get() = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountCents }
    val balanceCents: Long get() = transactions.sumOf { it.signedAmountCents }
    val recentTransactions: List<FinanceTransaction> get() = transactions.sortedByDescending { it.dateMillis }.take(5)
}

class HomeViewModel(
    private val transactionRepository: TransactionRepository,
    private val userId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            transactionRepository.observeTransactions(userId)
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { list -> _uiState.update { it.copy(transactions = list, isLoading = false) } }
        }
    }

    class Factory(
        private val transactionRepository: TransactionRepository,
        private val userId: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            HomeViewModel(transactionRepository, userId) as T
    }
}
