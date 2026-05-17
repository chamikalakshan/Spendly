package com.spendly.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendly.app.data.model.IncomeEntry
import com.spendly.app.data.model.enums.Currency
import com.spendly.app.data.model.enums.IncomeSource
import com.spendly.app.data.model.enums.InvoiceStatus
import com.spendly.app.repository.AuthRepository
import com.spendly.app.repository.IncomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject

data class AddIncomeUiState(
    val selectedSource: IncomeSource = IncomeSource.SALARY,
    val customSources: List<String> = emptyList(),
    val name: String = "",
    val amount: String = "",
    val currency: Currency = Currency.LKR,
    val exchangeRate: String = "320.5",
    val amountLKR: String = "",
    val coin: String = "",
    val projectName: String = "",
    val invoiceStatus: InvoiceStatus = InvoiceStatus.RECEIVED,
    val selectedDate: Long = System.currentTimeMillis(),
    val isRecurring: Boolean = false,
    val note: String = "",
    val showAddSourceDialog: Boolean = false,
    val newSourceInput: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false
)

@HiltViewModel
class AddIncomeViewModel @Inject constructor(
    private val incomeRepository: IncomeRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddIncomeUiState())
    val uiState: StateFlow<AddIncomeUiState> = _uiState.asStateFlow()

    private fun recalculateLKR() {
        val state = _uiState.value
        val amt = state.amount.toDoubleOrNull() ?: 0.0
        val rate = state.exchangeRate.toDoubleOrNull() ?: 320.5
        val result = if (state.currency == Currency.USD) amt * rate else amt
        
        _uiState.update { it.copy(
            amountLKR = if (result > 0) formatLKRValue(result) else ""
        ) }
    }

    private fun formatLKRValue(amount: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("en", "LK"))
        formatter.currency = java.util.Currency.getInstance("LKR")
        return formatter.format(amount).replace("LKR", "Rs. ")
    }

    fun onSourceSelected(source: IncomeSource) {
        _uiState.update { it.copy(selectedSource = source) }
    }

    fun onNameChanged(value: String) {
        _uiState.update { it.copy(name = value) }
    }

    fun onAmountChanged(value: String) {
        // Allow only numbers and a single decimal point
        if (value.isEmpty() || value.all { it.isDigit() || it == '.' }) {
            if (value.count { it == '.' } <= 1) {
                _uiState.update { it.copy(amount = value) }
                recalculateLKR()
            }
        }
    }

    fun onCurrencyChanged(currency: Currency) {
        _uiState.update { it.copy(currency = currency) }
        recalculateLKR()
    }

    fun onExchangeRateChanged(rate: String) {
        if (rate.isEmpty() || rate.all { it.isDigit() || it == '.' }) {
            if (rate.count { it == '.' } <= 1) {
                _uiState.update { it.copy(exchangeRate = rate) }
                recalculateLKR()
            }
        }
    }

    fun onCoinSelected(coin: String) {
        _uiState.update { it.copy(coin = coin) }
    }

    fun onProjectNameChanged(value: String) {
        _uiState.update { it.copy(projectName = value) }
    }

    fun onInvoiceStatusSelected(status: InvoiceStatus) {
        _uiState.update { it.copy(invoiceStatus = status) }
    }

    fun onDateSelected(ms: Long) {
        _uiState.update { it.copy(selectedDate = ms) }
    }

    fun onRecurringToggled(value: Boolean) {
        _uiState.update { it.copy(isRecurring = value) }
    }

    fun onNoteChanged(value: String) {
        _uiState.update { it.copy(note = value) }
    }

    fun showAddSourceDialog() {
        _uiState.update { it.copy(showAddSourceDialog = true, newSourceInput = "") }
    }

    fun onNewSourceInputChanged(value: String) {
        _uiState.update { it.copy(newSourceInput = value) }
    }

    fun confirmAddSource() {
        val input = _uiState.value.newSourceInput
        if (input.isNotBlank()) {
            _uiState.update { it.copy(
                customSources = it.customSources + input,
                showAddSourceDialog = false
            ) }
        }
    }

    fun dismissAddSourceDialog() {
        _uiState.update { it.copy(showAddSourceDialog = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun saveIncome() {
        val state = _uiState.value
        val userId = authRepository.getCurrentUserId() ?: return

        if (state.name.isBlank()) {
            _uiState.update { it.copy(error = "Please enter a name") }
            return
        }

        val amountVal = state.amount.toDoubleOrNull() ?: 0.0
        if (amountVal <= 0) {
            _uiState.update { it.copy(error = "Please enter a valid amount") }
            return
        }

        if (state.selectedSource == IncomeSource.FREELANCE && state.projectName.isBlank()) {
            _uiState.update { it.copy(error = "Please enter a project name") }
            return
        }

        if (state.selectedSource == IncomeSource.CRYPTO && state.coin.isBlank()) {
            _uiState.update { it.copy(error = "Please enter a coin name") }
            return
        }

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val rateVal = state.exchangeRate.toDoubleOrNull() ?: 320.5
            val amountLKRVal = if (state.currency == Currency.USD) amountVal * rateVal else amountVal
            
            val entry = IncomeEntry(
                userId = userId,
                sourceType = state.selectedSource,
                amount = amountVal,
                currency = state.currency,
                amountLKR = amountLKRVal,
                exchangeRate = if (state.currency == Currency.USD) rateVal else 1.0,
                coin = if (state.selectedSource == IncomeSource.CRYPTO) state.coin else null,
                projectName = if (state.selectedSource == IncomeSource.FREELANCE) state.projectName else null,
                invoiceStatus = if (state.selectedSource == IncomeSource.FREELANCE) state.invoiceStatus else null,
                date = state.selectedDate,
                isRecurring = state.isRecurring,
                note = state.note.ifBlank { null },
                createdAt = System.currentTimeMillis()
            )

            incomeRepository.addIncome(entry)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, isSaved = true) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to save income") }
                }
        }
    }
}
