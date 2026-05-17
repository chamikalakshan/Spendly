package com.spendly.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendly.app.data.local.pref.UserPreferencesRepository
import com.spendly.app.data.model.User
import com.spendly.app.data.model.enums.Currency
import com.spendly.app.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val user: User? = null,
    val defaultCurrency: Currency = Currency.LKR,
    val usdToLkrRate: Double = 320.5,
    val syncOverWifiOnly: Boolean = false,
    val isLoggedOut: Boolean = false,
    val isLoading: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userPrefs: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfileData()
    }

    private fun loadProfileData() {
        val user = authRepository.getCurrentUser()
        _uiState.update { it.copy(user = user) }

        viewModelScope.launch {
            combine(
                userPrefs.defaultCurrency,
                userPrefs.usdToLkrRate,
                userPrefs.syncOverWifiOnly
            ) { currency, rate, wifiSync ->
                _uiState.value.copy(
                    defaultCurrency = currency,
                    usdToLkrRate = rate,
                    syncOverWifiOnly = wifiSync
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun updateCurrency(currency: Currency) {
        viewModelScope.launch {
            userPrefs.setDefaultCurrency(currency)
        }
    }

    fun updateExchangeRate(rate: Double) {
        viewModelScope.launch {
            userPrefs.setUsdToLkrRate(rate)
        }
    }

    fun toggleWifiSync(enabled: Boolean) {
        viewModelScope.launch {
            userPrefs.setSyncOverWifiOnly(enabled)
        }
    }

    fun logout() {
        authRepository.logout()
        _uiState.update { it.copy(isLoggedOut = true) }
    }
}
