package com.spendly.financetracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendly.financetracker.data.model.ExpenseType
import com.spendly.financetracker.data.model.RecurringFrequency
import com.spendly.financetracker.data.model.RecurringRule
import com.spendly.financetracker.data.model.RecurringRuleDraft
import com.spendly.financetracker.data.model.TransactionType
import com.spendly.financetracker.data.repository.AuthRepository
import com.spendly.financetracker.data.repository.RecurringTransactionRepository
import com.spendly.financetracker.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecurringUiState(
    val rules: List<RecurringRule> = emptyList(),
    val defaultCurrency: String = "LKR",
    val isLoading: Boolean = true,
    val error: String? = null,
    val message: String? = null
) {
    val activeRules: List<RecurringRule> get() = rules.filter { it.isActive }
    val pausedRules: List<RecurringRule> get() = rules.filterNot { it.isActive }
}

@HiltViewModel
class RecurringViewModel @Inject constructor(
    private val recurringRepository: RecurringTransactionRepository,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(RecurringUiState())
    val uiState: StateFlow<RecurringUiState> = _uiState.asStateFlow()

    init {
        val uid = authRepository.getCurrentUserId()
        if (uid == null) {
            _uiState.update { it.copy(isLoading = false, error = "Please log in again") }
        } else {
            viewModelScope.launch {
                combine(
                    recurringRepository.observeRules(uid),
                    userRepository.observeProfile(uid)
                ) { rules, profile ->
                    RecurringUiState(
                        rules = rules,
                        defaultCurrency = profile?.defaultCurrency ?: "LKR",
                        isLoading = false
                    )
                }.collect { _uiState.value = it }
            }
        }
    }

    fun saveRule(
        type: TransactionType,
        name: String,
        amountInput: String,
        categoryOrSource: String,
        frequency: RecurringFrequency,
        startDateMillis: Long,
        paymentMethod: String = "Card",
        expenseType: ExpenseType = ExpenseType.DISCRETIONARY,
        note: String = "",
        editingId: String? = null
    ) {
        val uid = authRepository.getCurrentUserId() ?: return setError("Please log in again")
        val amountCents = parseAmountCents(amountInput)
        val currency = _uiState.value.defaultCurrency
        if (name.isBlank()) return setError("Enter a name")
        if (amountCents == null || amountCents <= 0L) return setError("Enter a valid amount")
        if (categoryOrSource.isBlank()) return setError("Select a category or source")
        viewModelScope.launch {
            recurringRepository.saveRule(
                uid,
                RecurringRuleDraft(
                    id = editingId,
                    type = type,
                    name = name.trim(),
                    amountCents = amountCents,
                    originalAmount = amountCents / 100.0,
                    originalCurrency = currency,
                    defaultCurrency = currency,
                    source = if (type == TransactionType.INCOME) categoryOrSource else null,
                    category = if (type == TransactionType.EXPENSE) categoryOrSource else null,
                    paymentMethod = if (type == TransactionType.EXPENSE) paymentMethod else null,
                    expenseType = if (type == TransactionType.EXPENSE) expenseType else null,
                    note = note.trim(),
                    frequency = frequency,
                    startDateMillis = startDateMillis,
                    nextRunDateMillis = startDateMillis
                )
            ).onSuccess {
                _uiState.update { it.copy(message = "Recurring rule saved", error = null) }
            }.onFailure {
                setError(it.message ?: "Failed to save recurring rule")
            }
        }
    }

    fun pause(ruleId: String) {
        viewModelScope.launch {
            recurringRepository.pauseRule(ruleId)
                .onSuccess { _uiState.update { it.copy(message = "Recurring rule paused") } }
                .onFailure { setError(it.message ?: "Failed to pause") }
        }
    }

    fun resume(ruleId: String) {
        viewModelScope.launch {
            recurringRepository.resumeRule(ruleId)
                .onSuccess { _uiState.update { it.copy(message = "Recurring rule resumed") } }
                .onFailure { setError(it.message ?: "Failed to resume") }
        }
    }

    fun delete(ruleId: String) {
        viewModelScope.launch {
            recurringRepository.deleteRule(ruleId)
                .onSuccess { _uiState.update { it.copy(message = "Recurring rule deleted") } }
                .onFailure { setError(it.message ?: "Failed to delete") }
        }
    }

    fun generateDueNow() {
        val uid = authRepository.getCurrentUserId() ?: return setError("Please log in again")
        viewModelScope.launch {
            recurringRepository.generateDueTransactions(uid)
                .onSuccess { count -> _uiState.update { it.copy(message = "Generated $count due transaction(s)") } }
                .onFailure { setError(it.message ?: "Failed to generate due transactions") }
        }
    }

    fun clearMessage() = _uiState.update { it.copy(message = null, error = null) }

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
