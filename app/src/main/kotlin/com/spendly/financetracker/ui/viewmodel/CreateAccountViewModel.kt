package com.spendly.financetracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendly.financetracker.data.repository.AuthRepository
import com.spendly.financetracker.util.SyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateAccountUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val currency: String = "",
    val isBusy: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CreateAccountViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val syncManager: SyncManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateAccountUiState())
    val uiState: StateFlow<CreateAccountUiState> = _uiState.asStateFlow()

    fun onNameChange(v: String) = _uiState.update { it.copy(name = v, error = null) }
    fun onEmailChange(v: String) = _uiState.update { it.copy(email = v, error = null) }
    fun onPasswordChange(v: String) = _uiState.update { it.copy(password = v, error = null) }
    fun onConfirmPasswordChange(v: String) = _uiState.update { it.copy(confirmPassword = v, error = null) }
    fun onCurrencyChange(v: String) = _uiState.update { it.copy(currency = v) }

    fun submit() {
        val s = _uiState.value
        if (s.name.isBlank()) { _uiState.update { it.copy(error = "Enter your name") }; return }
        if (s.email.isBlank() || "@" !in s.email) { _uiState.update { it.copy(error = "Enter a valid email") }; return }
        if (s.password.length < 6) { _uiState.update { it.copy(error = "Password must be at least 6 characters") }; return }
        if (s.password != s.confirmPassword) { _uiState.update { it.copy(error = "Passwords do not match") }; return }

        _uiState.update { it.copy(isBusy = true, error = null) }
        viewModelScope.launch {
            val result = authRepository.createAccount(s.name.trim(), s.email.trim(), s.password, s.currency)
            if (result.isSuccess) syncManager.startImmediateSync()
            _uiState.update {
                if (result.isSuccess) it.copy(isBusy = false, isSuccess = true)
                else it.copy(isBusy = false, error = result.exceptionOrNull()?.message ?: "Registration failed")
            }
        }
    }
}
