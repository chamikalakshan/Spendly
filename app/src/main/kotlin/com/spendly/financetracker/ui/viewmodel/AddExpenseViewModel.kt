package com.spendly.financetracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.spendly.financetracker.data.model.ExpenseType
import com.spendly.financetracker.data.model.TransactionDraft
import com.spendly.financetracker.data.model.TransactionType
import com.spendly.financetracker.data.model.UserProfile
import com.spendly.financetracker.data.repository.AuthRepository
import com.spendly.financetracker.data.repository.ExpenseRepository
import com.spendly.financetracker.data.repository.TransactionRepository
import com.spendly.financetracker.data.repository.UserRepository
import com.spendly.financetracker.ui.util.CategorySettings
import com.spendly.financetracker.ui.util.parseCategorySettings
import com.spendly.financetracker.ui.util.toJson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddExpenseUiState(
    val name: String = "",
    val amount: String = "",
    val note: String = "",
    val selectedCategory: String = "Food",
    val categories: List<String> = expenseCategories,
    val paymentMethods: List<String> = defaultPaymentMethods,
    val selectedPaymentMethod: String = "Card",
    val expenseType: ExpenseType = ExpenseType.DISCRETIONARY,
    val defaultCurrency: String = "LKR",
    val selectedCurrency: String = "LKR",
    val exchangeRate: String = "",
    val convertedAmountCents: Long = 0L,
    val selectedDate: Long = System.currentTimeMillis(),
    val availableBalanceCents: Long = 0L,
    val categorySettings: CategorySettings = CategorySettings(),
    val editId: String? = null,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)
{
    val needsExchangeRate: Boolean get() = selectedCurrency != defaultCurrency
    val visibleCurrencies: List<String>
        get() = if (defaultCurrency == "USD") listOf("USD", "LKR") else listOf(defaultCurrency, "USD").distinct()
}

val expenseCategories = listOf("Food", "Transport", "Rent", "Subscriptions", "Entertainment", "Gym", "Goal", "Other")
val defaultPaymentMethods = listOf("Card", "Cash", "Auto-debit")

@HiltViewModel
class AddExpenseViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val expenseRepository: ExpenseRepository,
    private val transactionRepository: TransactionRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AddExpenseUiState())
    val uiState: StateFlow<AddExpenseUiState> = _uiState.asStateFlow()
    private var profile: UserProfile? = null

    private val editId: String? = savedStateHandle["expenseId"]

    init {
        _uiState.update { it.copy(editId = editId) }
        val uid = authRepository.getCurrentUserId()
        if (uid != null) {
            viewModelScope.launch {
                transactionRepository.observeTransactions(uid).collect { transactions ->
                    _uiState.update { state ->
                        state.copy(availableBalanceCents = transactions.sumOf { it.signedAmountCents })
                    }
                }
            }
            viewModelScope.launch {
                userRepository.observeProfile(uid).collect { profile ->
                    this@AddExpenseViewModel.profile = profile
                    val currency = profile?.defaultCurrency?.ifBlank { "LKR" } ?: "LKR"
                    val settings = parseCategorySettings(profile?.categorySettingsJson)
                    _uiState.update { state ->
                        val categories = settings.visibleExpenses(expenseCategories)
                        state.copy(
                            defaultCurrency = currency,
                            selectedCurrency = if (state.selectedCurrency == "LKR") currency else state.selectedCurrency,
                            categorySettings = settings,
                            categories = categories,
                            selectedCategory = state.selectedCategory.takeIf { it in categories } ?: categories.firstOrNull().orEmpty()
                        ).recalculate()
                    }
                }
            }
        }
        if (!editId.isNullOrBlank()) loadExisting(editId)
    }

    fun onNameChanged(v: String) = _uiState.update { it.copy(name = v, error = null) }
    fun onAmountChanged(v: String) {
        if (v.isEmpty() || (v.all { it.isDigit() || it == '.' } && v.count { it == '.' } <= 1)) {
            _uiState.update { it.copy(amount = v, error = null).recalculate() }
        }
    }
    fun onNoteChanged(v: String) = _uiState.update { it.copy(note = v) }
    fun onCategorySelected(c: String) = _uiState.update { it.copy(selectedCategory = c, expenseType = defaultTypeFor(c)) }
    fun onDateSelected(ms: Long) = _uiState.update { it.copy(selectedDate = ms) }
    fun onPaymentMethodSelected(v: String) = _uiState.update { it.copy(selectedPaymentMethod = v) }
    fun onExpenseTypeSelected(v: ExpenseType) = _uiState.update { it.copy(expenseType = v) }
    fun onCurrencySelected(v: String) = _uiState.update { it.copy(selectedCurrency = v, error = null).recalculate() }
    fun onExchangeRateChanged(v: String) {
        if (v.isEmpty() || (v.all { it.isDigit() || it == '.' } && v.count { it == '.' } <= 1)) {
            _uiState.update { it.copy(exchangeRate = v, error = null).recalculate() }
        }
    }
    fun addCustomCategory(v: String) {
        val value = v.trim()
        if (value.isBlank()) return
        val settings = _uiState.value.categorySettings.copy(
            customExpenseCategories = (_uiState.value.categorySettings.customExpenseCategories + value).distinct(),
            hiddenExpenseCategories = _uiState.value.categorySettings.hiddenExpenseCategories - value
        )
        saveCategorySettings(settings)
        _uiState.update { state ->
            val categories = settings.visibleExpenses(expenseCategories)
            state.copy(categories = categories, selectedCategory = value, categorySettings = settings, expenseType = defaultTypeFor(value))
        }
    }
    fun deleteCategory(v: String) {
        val value = v.trim()
        if (value.isBlank()) return
        val current = _uiState.value.categorySettings
        val settings = if (value in expenseCategories) {
            current.copy(hiddenExpenseCategories = (current.hiddenExpenseCategories + value).distinct())
        } else {
            current.copy(customExpenseCategories = current.customExpenseCategories - value)
        }
        saveCategorySettings(settings)
        _uiState.update { state ->
            val categories = settings.visibleExpenses(expenseCategories)
            state.copy(
                categories = categories,
                categorySettings = settings,
                selectedCategory = state.selectedCategory.takeIf { it in categories } ?: categories.firstOrNull().orEmpty()
            )
        }
    }
    fun addCustomPaymentMethod(v: String) {
        val value = v.trim()
        if (value.isBlank()) return
        _uiState.update { state ->
            state.copy(paymentMethods = (state.paymentMethods + value).distinct(), selectedPaymentMethod = value)
        }
    }

    fun save() {
        val uid = authRepository.getCurrentUserId()
        val s = _uiState.value
        val amountCents = s.convertedAmountCents.takeIf { it > 0L } ?: parseAmountCents(s.amount)
        if (uid == null) { _uiState.update { it.copy(error = "Please log in again") }; return }
        if (s.name.isBlank()) { _uiState.update { it.copy(error = "Enter a name") }; return }
        if (amountCents == null || amountCents <= 0L) { _uiState.update { it.copy(error = "Enter a valid amount") }; return }
        if (s.needsExchangeRate && (s.exchangeRate.toDoubleOrNull() ?: 0.0) <= 0.0) {
            _uiState.update { it.copy(error = "Enter a valid exchange rate") }
            return
        }
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val currentExpense = s.editId?.let { expenseRepository.getExpense(it)?.amountCents } ?: 0L
            if (amountCents > s.availableBalanceCents + currentExpense) {
                _uiState.update { it.copy(isLoading = false, error = "balance exceed") }
                return@launch
            }
            val draft = TransactionDraft(
                title = s.name.trim(),
                amountCents = amountCents,
                type = TransactionType.EXPENSE,
                category = s.selectedCategory,
                note = s.note.trim(),
                dateMillis = s.selectedDate,
                originalAmount = s.amount.toDoubleOrNull() ?: 0.0,
                originalCurrency = s.selectedCurrency,
                defaultCurrency = s.defaultCurrency,
                exchangeRate = if (s.needsExchangeRate) s.exchangeRate.toDoubleOrNull() else null,
                paymentMethod = s.selectedPaymentMethod,
                expenseType = s.expenseType
            )
            val result = if (s.editId.isNullOrBlank()) expenseRepository.addExpense(uid, draft)
                else expenseRepository.updateExpense(s.editId, draft)
            _uiState.update {
                if (result.isSuccess) it.copy(isLoading = false, isSaved = true)
                else it.copy(isLoading = false, error = result.exceptionOrNull()?.message ?: "Failed to save")
            }
        }
    }

    private fun loadExisting(id: String) {
        viewModelScope.launch {
            val transaction = expenseRepository.getExpense(id) ?: return@launch
            _uiState.update { state ->
                val category = transaction.category.ifBlank { "Other" }
                val payment = transaction.paymentMethod ?: "Card"
                state.copy(
                    name = transaction.title,
                    amount = transaction.originalAmount.takeIf { it > 0.0 }?.toPlainInput()
                        ?: (transaction.amountCents / 100.0).toPlainInput(),
                    note = transaction.note,
                    selectedCategory = category,
                    categories = (state.categories + category).distinct(),
                    selectedPaymentMethod = payment,
                    paymentMethods = (state.paymentMethods + payment).distinct(),
                    expenseType = transaction.expenseType ?: defaultTypeFor(category),
                    selectedDate = transaction.dateMillis,
                    defaultCurrency = transaction.defaultCurrency,
                    selectedCurrency = transaction.originalCurrency.ifBlank { transaction.defaultCurrency },
                    exchangeRate = transaction.exchangeRate?.toPlainInput().orEmpty(),
                    editId = id
                ).recalculate()
            }
        }
    }

    private fun parseAmountCents(input: String): Long? {
        val n = input.trim()
        if (!Regex("^\\d+([.]\\d{1,2})?$").matches(n)) return null
        val parts = n.split(".")
        val whole = parts[0].toLongOrNull() ?: return null
        val cents = parts.getOrNull(1)?.padEnd(2, '0') ?: "00"
        return whole * 100L + (cents.toLongOrNull() ?: return null)
    }

    private fun defaultTypeFor(category: String): ExpenseType =
        if (category in setOf("Rent", "Subscriptions", "Gym", "Goal")) ExpenseType.COMMITTED
        else ExpenseType.DISCRETIONARY

    private fun saveCategorySettings(settings: CategorySettings) {
        val current = profile ?: return
        viewModelScope.launch {
            userRepository.upsertProfile(current.copy(categorySettingsJson = settings.toJson(), isSynced = false))
        }
    }

    private fun AddExpenseUiState.recalculate(): AddExpenseUiState {
        val amountValue = amount.toDoubleOrNull() ?: 0.0
        val rate = if (selectedCurrency == defaultCurrency) 1.0 else exchangeRate.toDoubleOrNull() ?: 0.0
        return copy(convertedAmountCents = (amountValue * rate * 100.0).toLong().coerceAtLeast(0L))
    }

    private fun Double.toPlainInput(): String =
        if (this % 1.0 == 0.0) toLong().toString() else toString()
}
