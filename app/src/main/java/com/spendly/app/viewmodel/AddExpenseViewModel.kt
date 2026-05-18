package com.spendly.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendly.app.data.model.ExpenseEntry
import com.spendly.app.data.model.enums.ExpenseCategory
import com.spendly.app.data.model.enums.ExpenseType
import com.spendly.app.data.model.enums.PaymentMethod
import com.spendly.app.repository.AuthRepository
import com.spendly.app.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddExpenseUiState(
    val amount: String = "",
    val name: String = "",
    val selectedCategory: ExpenseCategory? = null,
    val selectedCustomCategory: String? = null,
    val customCategories: List<String> = emptyList(),
    val expenseType: ExpenseType = ExpenseType.DISCRETIONARY,
    val paymentMethod: PaymentMethod = PaymentMethod.CARD,
    val customPaymentMethods: List<String> = emptyList(),
    val selectedDate: Long = System.currentTimeMillis(),
    val note: String = "",
    val showAddPaymentDialog: Boolean = false,
    val showAddCategoryDialog: Boolean = false,
    val newPaymentInput: String = "",
    val newCategoryInput: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val amountError: String? = null,
    val categoryError: String? = null,
    val isSaved: Boolean = false,
    val editingExpenseId: String? = null,
    val editingCreatedAt: Long = 0L
)

@HiltViewModel
class AddExpenseViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddExpenseUiState())
    val uiState: StateFlow<AddExpenseUiState> = _uiState.asStateFlow()

    fun onAmountChanged(value: String) {
        if (value.isEmpty() || value.all { it.isDigit() || it == '.' }) {
            if (value.count { it == '.' } <= 1) {
                _uiState.update { it.copy(amount = value, amountError = null) }
            }
        }
    }

    fun onNameChanged(value: String) {
        _uiState.update { it.copy(name = value, error = null) }
    }

    fun onCategorySelected(category: ExpenseCategory) {
        val autoType = when (category) {
            ExpenseCategory.RENT, ExpenseCategory.SUBSCRIPTIONS, ExpenseCategory.GYM -> ExpenseType.COMMITTED
            else -> ExpenseType.DISCRETIONARY
        }
        _uiState.update {
            it.copy(
                selectedCategory = category,
                selectedCustomCategory = null,
                expenseType = autoType,
                categoryError = null
            )
        }
    }

    fun onCustomCategorySelected(category: String) {
        _uiState.update {
            it.copy(
                selectedCategory = ExpenseCategory.OTHER,
                selectedCustomCategory = category,
                expenseType = ExpenseType.DISCRETIONARY,
                name = it.name.ifBlank { category },
                categoryError = null
            )
        }
    }

    fun onExpenseTypeSelected(type: ExpenseType) {
        _uiState.update { it.copy(expenseType = type) }
    }

    fun onPaymentMethodSelected(method: PaymentMethod) {
        _uiState.update { it.copy(paymentMethod = method) }
    }

    fun onDateSelected(ms: Long) {
        _uiState.update { it.copy(selectedDate = ms) }
    }

    fun onNoteChanged(value: String) {
        _uiState.update { it.copy(note = value) }
    }

    // Custom payment method management
    fun showAddPaymentDialog() {
        _uiState.update { it.copy(showAddPaymentDialog = true, newPaymentInput = "") }
    }

    fun onNewPaymentInputChanged(value: String) {
        _uiState.update { it.copy(newPaymentInput = value) }
    }

    fun confirmAddPayment() {
        val input = _uiState.value.newPaymentInput
        if (input.isNotBlank()) {
            _uiState.update {
                it.copy(
                    customPaymentMethods = it.customPaymentMethods + input,
                    showAddPaymentDialog = false
                )
            }
        }
    }

    fun dismissAddPaymentDialog() {
        _uiState.update { it.copy(showAddPaymentDialog = false) }
    }

    // Custom category management
    fun showAddCategoryDialog() {
        _uiState.update { it.copy(showAddCategoryDialog = true, newCategoryInput = "") }
    }

    fun onNewCategoryInputChanged(value: String) {
        _uiState.update { it.copy(newCategoryInput = value) }
    }

    fun confirmAddCategory() {
        val input = _uiState.value.newCategoryInput
        if (input.isNotBlank()) {
            _uiState.update {
                it.copy(
                    customCategories = it.customCategories + input,
                    selectedCategory = ExpenseCategory.OTHER,
                    selectedCustomCategory = input,
                    name = it.name.ifBlank { input },
                    categoryError = null,
                    showAddCategoryDialog = false
                )
            }
        }
    }

    fun dismissAddCategoryDialog() {
        _uiState.update { it.copy(showAddCategoryDialog = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun loadExpenseForEdit(id: String?) {
        if (id.isNullOrBlank() || _uiState.value.editingExpenseId == id) return

        val userId = authRepository.getCurrentUserId()
        if (userId.isNullOrBlank()) {
            _uiState.update { it.copy(error = "Please log in again") }
            return
        }

        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val entry = expenseRepository.getAllExpenses(userId)
                    .first()
                    .firstOrNull { it.id == id }

                if (entry == null) {
                    _uiState.update { it.copy(isLoading = false, error = "Expense entry not found") }
                    return@launch
                }

                _uiState.update {
                    it.copy(
                        amount = entry.amount.toString(),
                        name = entry.name,
                        selectedCategory = entry.category,
                        selectedCustomCategory = null,
                        expenseType = entry.expenseType,
                        paymentMethod = entry.paymentMethod,
                        selectedDate = entry.date,
                        note = entry.note.orEmpty(),
                        isLoading = false,
                        editingExpenseId = entry.id,
                        editingCreatedAt = entry.createdAt
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load expense") }
            }
        }
    }

    fun saveExpense() {
        val state = _uiState.value
        val userId = authRepository.getCurrentUserId() ?: return

        val amountVal = state.amount.toDoubleOrNull() ?: 0.0
        if (amountVal <= 0) {
            _uiState.update { it.copy(amountError = "Please enter a valid amount") }
            return
        }

        if (state.selectedCategory == null) {
            _uiState.update { it.copy(categoryError = "Please select a category") }
            return
        }

        if (state.name.isBlank()) {
            _uiState.update { it.copy(error = "Please enter a name") }
            return
        }

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val entry = ExpenseEntry(
                id = state.editingExpenseId.orEmpty(),
                userId = userId,
                name = state.name,
                amount = amountVal,
                category = state.selectedCategory,
                expenseType = state.expenseType,
                paymentMethod = state.paymentMethod,
                date = state.selectedDate,
                note = state.note.ifBlank { null },
                createdAt = state.editingCreatedAt.takeIf { it != 0L } ?: System.currentTimeMillis()
            )

            val result = if (state.editingExpenseId.isNullOrBlank()) {
                expenseRepository.addExpense(entry)
            } else {
                expenseRepository.updateExpense(entry)
            }

            result
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, isSaved = true) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to save expense") }
                }
        }
    }
}
