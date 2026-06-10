package com.spendly.financetracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.spendly.financetracker.data.model.UserSession
import com.spendly.financetracker.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ProfileUiState(
    val session: UserSession? = null,
    val isSignedOut: Boolean = false
)

class ProfileViewModel(
    private val authRepository: AuthRepository,
    session: UserSession?
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState(session = session))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun signOut() {
        authRepository.signOut()
        _uiState.value = _uiState.value.copy(isSignedOut = true)
    }

    class Factory(
        private val authRepository: AuthRepository,
        private val session: UserSession?
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ProfileViewModel(authRepository, session) as T
    }
}
