package com.spendly.financetracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendly.financetracker.data.firebase.FirebaseBootstrap
import com.spendly.financetracker.data.model.TransactionDraft
import com.spendly.financetracker.data.model.TransactionType
import com.spendly.financetracker.data.model.UserProfile
import com.spendly.financetracker.data.repository.AuthRepository
import com.spendly.financetracker.data.repository.GoalRepository
import com.spendly.financetracker.data.repository.TransactionRepository
import com.spendly.financetracker.data.repository.UserRepository
import com.spendly.financetracker.util.SyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FinanceViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val transactionRepository: TransactionRepository,
    private val goalRepository: GoalRepository,
    private val userRepository: UserRepository,
    private val syncManager: SyncManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(FinanceUiState())
    val uiState: StateFlow<FinanceUiState> = _uiState.asStateFlow()

    private var dataJob: Job? = null

    init {
        if (!authRepository.isFirebaseConfigured) {
            _uiState.update {
                it.copy(
                    isFirebaseConfigured = false,
                    isLoading = false,
                    message = FirebaseBootstrap.MISSING_CONFIG_MESSAGE
                )
            }
        } else {
            observeSession()
        }
    }

    fun updateEmail(email: String) = _uiState.update { it.copy(email = email, message = null) }
    fun updatePassword(password: String) = _uiState.update { it.copy(password = password, message = null) }
    fun clearMessage() = _uiState.update { it.copy(message = null) }

    fun toggleAuthMode() {
        _uiState.update {
            it.copy(
                authMode = if (it.authMode == AuthMode.SIGN_IN) AuthMode.CREATE_ACCOUNT else AuthMode.SIGN_IN,
                password = "",
                message = null
            )
        }
    }

    fun submitAuth() {
        val state = _uiState.value
        val email = state.email.trim()
        val password = state.password
        if (email.isBlank() || "@" !in email) {
            _uiState.update { it.copy(message = "Enter a valid email address.") }
            return
        }
        if (password.length < 6) {
            _uiState.update { it.copy(message = "Password must be at least 6 characters.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, message = null) }
            val result = authRepository.signIn(email, password)
            if (result.isSuccess) {
                authRepository.getCurrentUserId()?.let { syncNow(it) }
                syncManager.startImmediateSync()
            }
            _uiState.update {
                it.copy(
                    isBusy = false,
                    password = if (result.isSuccess) "" else it.password,
                    message = result.exceptionOrNull()?.let { "invalid user credentials" }
                )
            }
        }
    }

    fun signOut() {
        dataJob?.cancel()
        dataJob = null
        authRepository.signOut()
        _uiState.update {
            FinanceUiState(isFirebaseConfigured = it.isFirebaseConfigured, isLoading = false)
        }
    }

    fun updateProfile(profile: UserProfile) {
        viewModelScope.launch {
            userRepository.upsertProfile(profile)
                .onSuccess { _uiState.update { it.copy(message = "Profile updated.") } }
                .onFailure { error -> _uiState.update { it.copy(message = error.userMessage()) } }
        }
    }

    fun addInitialIncome(amount: String) {
        val state = _uiState.value
        val uid = state.session?.uid ?: return
        val amountCents = parseAmountCents(amount)
        if (amountCents == null || amountCents <= 0L) {
            _uiState.update { it.copy(message = "Enter a valid initial income.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, message = null) }
            val currency = state.profile?.defaultCurrency ?: "LKR"
            val result = transactionRepository.addTransaction(
                uid,
                TransactionDraft(
                    title = "Initial Income",
                    amountCents = amountCents,
                    type = TransactionType.INCOME,
                    source = "Initial Income",
                    note = "Initial balance setup",
                    dateMillis = System.currentTimeMillis(),
                    originalAmount = amountCents / 100.0,
                    originalCurrency = currency,
                    defaultCurrency = currency
                )
            )
            _uiState.update {
                if (result.isSuccess) it.copy(isBusy = false, message = "Initial income saved.")
                else it.copy(isBusy = false, message = result.exceptionOrNull()?.userMessage())
            }
        }
    }

    fun sendPasswordReset() {
        val email = _uiState.value.email.trim()
        if (email.isBlank() || "@" !in email) {
            _uiState.update { it.copy(message = "Enter your email address first.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, message = null) }
            val result = authRepository.sendPasswordResetEmail(email)
            _uiState.update {
                it.copy(
                    isBusy = false,
                    message = if (result.isSuccess) "Password reset email sent." else result.exceptionOrNull()?.userMessage()
                )
            }
        }
    }

    fun changePassword(currentPassword: String, newPassword: String, confirmPassword: String) {
        if (currentPassword.isBlank()) {
            _uiState.update { it.copy(message = "Enter your current password.") }
            return
        }
        if (newPassword.length < 6) {
            _uiState.update { it.copy(message = "Password must be at least 6 characters.") }
            return
        }
        if (newPassword != confirmPassword) {
            _uiState.update { it.copy(message = "Passwords do not match.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, message = null) }
            val result = authRepository.updatePassword(currentPassword, newPassword)
            _uiState.update {
                it.copy(
                    isBusy = false,
                    message = if (result.isSuccess) "Password updated." else result.exceptionOrNull()?.userMessage()
                )
            }
        }
    }

    fun deleteAccount(currentPassword: String) {
        if (currentPassword.isBlank()) {
            _uiState.update { it.copy(message = "Enter your current password.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, message = null) }
            val result = authRepository.deleteAccount(currentPassword)
            if (result.isSuccess) {
                dataJob?.cancel()
                dataJob = null
            }
            _uiState.update {
                if (result.isSuccess) {
                    FinanceUiState(isFirebaseConfigured = it.isFirebaseConfigured, isLoading = false, message = "Account deleted.")
                } else {
                    it.copy(isBusy = false, message = result.exceptionOrNull()?.userMessage())
                }
            }
        }
    }

    fun addGoal() {
        _uiState.update { it.copy(message = null) }
    }

    fun updateTransactionTitle(title: String) = _uiState.update { it.copy(transactionTitle = title, message = null) }
    fun updateTransactionAmount(amount: String) = _uiState.update { it.copy(transactionAmount = amount, message = null) }
    fun updateTransactionNote(note: String) = _uiState.update { it.copy(transactionNote = note, message = null) }
    fun selectTransactionType(type: TransactionType) = _uiState.update { it.copy(transactionType = type, message = null) }

    fun selectTab(tab: AppTab) = _uiState.update { it.copy(currentTab = tab) }
    fun selectTransactionTab(tab: TransactionTab) = _uiState.update { it.copy(transactionTab = tab) }

    fun addTransaction() {
        val state = _uiState.value
        val uid = state.session?.uid ?: return
        val amountCents = parseAmountCents(state.transactionAmount)
        if (state.transactionTitle.isBlank()) {
            _uiState.update { it.copy(message = "Enter a transaction title.") }
            return
        }
        if (amountCents == null || amountCents <= 0L) {
            _uiState.update { it.copy(message = "Enter a positive amount with up to 2 decimals.") }
            return
        }
        val draft = TransactionDraft(
            title = state.transactionTitle.trim(),
            amountCents = amountCents,
            type = state.transactionType,
            category = if (state.transactionType == TransactionType.EXPENSE) "Other" else "",
            source = if (state.transactionType == TransactionType.INCOME) "Other" else "",
            note = state.transactionNote.trim(),
            dateMillis = System.currentTimeMillis()
        )
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, message = null) }
            val result = transactionRepository.addTransaction(uid, draft)
            _uiState.update {
                if (result.isSuccess) {
                    it.copy(isBusy = false, transactionTitle = "", transactionAmount = "", transactionNote = "", message = "Transaction saved.")
                } else {
                    it.copy(isBusy = false, message = result.exceptionOrNull()?.userMessage())
                }
            }
        }
    }

    fun deleteTransaction(id: String) {
        val transaction = _uiState.value.transactions.firstOrNull { it.id == id } ?: return
        viewModelScope.launch {
            transactionRepository.deleteTransaction(transaction)
                .onFailure { error -> _uiState.update { it.copy(message = error.userMessage()) } }
        }
    }

    private fun observeSession() {
        viewModelScope.launch {
            authRepository.observeSession()
                .catch { error -> _uiState.update { it.copy(isLoading = false, message = error.userMessage()) } }
                .collect { session ->
                    dataJob?.cancel()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            session = session,
                            profile = null,
                            transactions = emptyList(),
                            goals = emptyList()
                        )
                    }
                    if (session != null) {
                        syncNow(session.uid)
                        syncManager.startImmediateSync()
                        observeUserData(session.uid)
                    }
                }
        }
    }

    private fun observeUserData(uid: String) {
        dataJob = viewModelScope.launch {
            combine(
                userRepository.observeProfile(uid),
                transactionRepository.observeTransactions(uid),
                goalRepository.observeGoals(uid)
            ) { profile, transactions, goals ->
                Triple(profile, transactions, goals)
            }.catch { error ->
                _uiState.update { it.copy(message = error.userMessage()) }
            }.collect { (profile, transactions, goals) ->
                _uiState.update {
                    it.copy(profile = profile, transactions = transactions, goals = goals)
                }
            }
        }
    }

    private suspend fun syncNow(uid: String) {
        runCatching {
            userRepository.syncWithFirestore(uid)
            transactionRepository.syncWithFirestore(uid)
            goalRepository.syncWithFirestore(uid)
        }.onFailure { error ->
            _uiState.update { it.copy(message = error.userMessage()) }
        }
    }

    private fun parseAmountCents(input: String): Long? {
        val normalized = input.trim()
        if (!Regex("^\\d+([.]\\d{1,2})?$").matches(normalized)) return null
        val parts = normalized.split(".")
        val whole = parts.getOrNull(0)?.toLongOrNull() ?: return null
        val cents = parts.getOrNull(1)?.padEnd(2, '0') ?: "00"
        return whole * 100L + (cents.toLongOrNull() ?: return null)
    }

    private fun Throwable.userMessage(): String =
        message?.takeIf { it.isNotBlank() } ?: "Something went wrong. Please try again."
}
