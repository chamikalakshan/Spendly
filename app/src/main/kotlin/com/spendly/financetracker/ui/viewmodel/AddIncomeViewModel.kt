package com.spendly.financetracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.spendly.financetracker.data.model.TransactionDraft
import com.spendly.financetracker.data.model.TransactionType
import com.spendly.financetracker.data.model.UserProfile
import com.spendly.financetracker.data.repository.AuthRepository
import com.spendly.financetracker.data.repository.IncomeRepository
import com.spendly.financetracker.data.repository.UserRepository
import com.spendly.financetracker.data.service.CryptoRateService
import com.spendly.financetracker.data.service.CurrencyRateService
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

enum class RateStatus {
    IDLE,
    UPDATING,
    UPDATED,
    MANUAL_REQUIRED,
    UNAVAILABLE
}

data class AddIncomeUiState(
    val name: String = "",
    val amount: String = "",
    val note: String = "",
    val selectedSource: String = "Salary",
    val incomeSources: List<String> = defaultIncomeSources,
    val selectedDate: Long = System.currentTimeMillis(),
    val defaultCurrency: String = "LKR",
    val selectedCurrency: String = "LKR",
    val exchangeRate: String = "",
    val convertedAmountCents: Long = 0L,
    val isRecurring: Boolean = false,
    val selectedCoin: String = "BTC",
    val customCryptoCoin: String = "",
    val cryptoAmount: String = "",
    val cryptoRate: String = "",
    val cryptoRateSource: String = "MANUAL",
    val cryptoRateFetchedAt: Long? = null,
    val isFetchingRate: Boolean = false,
    val fiatRateStatus: RateStatus = RateStatus.IDLE,
    val cryptoRateStatus: RateStatus = RateStatus.IDLE,
    val categorySettings: CategorySettings = CategorySettings(),
    val editId: String? = null,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
) {
    val isCrypto: Boolean get() = selectedSource == "Crypto"
    val needsExchangeRate: Boolean get() = selectedCurrency != defaultCurrency
    val visibleCurrencies: List<String>
        get() = if (defaultCurrency == "USD") listOf("USD", "LKR") else listOf(defaultCurrency, "USD").distinct()
}

val spendlyCurrencies = listOf("LKR", "USD", "EUR", "GBP", "INR", "AUD", "CAD", "JPY", "SGD", "AED")
val cryptoCoins = listOf("BTC", "ETH", "USDT", "BNB", "SOL", "XRP", "DOGE", "Other")
val defaultIncomeSources = listOf("Salary", "Freelance", "Crypto", "AdSense", "Other")
val incomeSources = defaultIncomeSources

@HiltViewModel
class AddIncomeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val incomeRepository: IncomeRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val currencyRateService: CurrencyRateService,
    private val cryptoRateService: CryptoRateService
) : ViewModel() {
    private val _uiState = MutableStateFlow(AddIncomeUiState())
    val uiState: StateFlow<AddIncomeUiState> = _uiState.asStateFlow()
    private var profile: UserProfile? = null

    private val editId: String? = savedStateHandle["incomeId"]

    init {
        _uiState.update { it.copy(editId = editId) }
        val uid = authRepository.getCurrentUserId()
        if (uid != null) {
            viewModelScope.launch {
                userRepository.observeProfile(uid).collect { profile ->
                    this@AddIncomeViewModel.profile = profile
                    val currency = profile?.defaultCurrency?.ifBlank { "LKR" } ?: "LKR"
                    val settings = parseCategorySettings(profile?.categorySettingsJson)
                    _uiState.update { state ->
                        val sources = settings.visibleIncomeSources(defaultIncomeSources)
                        state.copy(
                            defaultCurrency = currency,
                            selectedCurrency = if (state.selectedCurrency == "LKR") currency else state.selectedCurrency,
                            incomeSources = sources,
                            selectedSource = state.selectedSource.takeIf { it in sources } ?: sources.firstOrNull().orEmpty(),
                            categorySettings = settings
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
    fun onSourceSelected(s: String) = _uiState.update { it.copy(selectedSource = s, error = null).recalculate() }
    fun onDateSelected(ms: Long) = _uiState.update { it.copy(selectedDate = ms) }
    fun onCurrencySelected(currency: String) = _uiState.update { it.copy(selectedCurrency = currency, error = null).recalculate() }
    fun onExchangeRateChanged(v: String) {
        if (v.isEmpty() || (v.all { it.isDigit() || it == '.' } && v.count { it == '.' } <= 1)) {
            _uiState.update {
                it.copy(
                    exchangeRate = v,
                    fiatRateStatus = if (v.isBlank()) RateStatus.IDLE else RateStatus.MANUAL_REQUIRED,
                    error = null
                ).recalculate()
            }
        }
    }
    fun onRecurringChanged(v: Boolean) = _uiState.update { it.copy(isRecurring = v) }
    fun onCoinSelected(v: String) = _uiState.update { it.copy(selectedCoin = v, error = null) }
    fun onCustomCryptoCoinChanged(v: String) = _uiState.update { it.copy(customCryptoCoin = v, error = null) }
    fun onCryptoAmountChanged(v: String) {
        if (v.isEmpty() || (v.all { it.isDigit() || it == '.' } && v.count { it == '.' } <= 1)) {
            _uiState.update { it.copy(cryptoAmount = v, amount = v, error = null).recalculate() }
        }
    }
    fun onCryptoRateChanged(v: String) {
        if (v.isEmpty() || (v.all { it.isDigit() || it == '.' } && v.count { it == '.' } <= 1)) {
            _uiState.update {
                it.copy(
                    cryptoRate = v,
                    cryptoRateSource = "MANUAL",
                    cryptoRateStatus = if (v.isBlank()) RateStatus.IDLE else RateStatus.MANUAL_REQUIRED,
                    error = null
                ).recalculate()
            }
        }
    }

    fun fetchExchangeRate() {
        val s = _uiState.value
        viewModelScope.launch {
            if (!s.needsExchangeRate) return@launch
            _uiState.update { it.copy(isFetchingRate = true, fiatRateStatus = RateStatus.UPDATING, error = null) }
            currencyRateService.getRate(s.selectedCurrency, s.defaultCurrency)
                .onSuccess { rate ->
                    _uiState.update {
                        it.copy(
                            exchangeRate = rate.rate.toString(),
                            isFetchingRate = false,
                            fiatRateStatus = RateStatus.UPDATED
                        ).recalculate()
                    }
                }
                .onFailure {
                    _uiState.update { state ->
                        state.copy(
                            isFetchingRate = false,
                            fiatRateStatus = RateStatus.MANUAL_REQUIRED,
                            error = "Live rate unavailable. Enter it manually."
                        )
                    }
                }
        }
    }

    fun addCustomSource(v: String) {
        val value = v.trim()
        if (value.isBlank()) return
        val settings = _uiState.value.categorySettings.copy(
            customIncomeSources = (_uiState.value.categorySettings.customIncomeSources + value).distinct(),
            hiddenIncomeSources = _uiState.value.categorySettings.hiddenIncomeSources - value
        )
        saveCategorySettings(settings)
        _uiState.update { state ->
            val sources = settings.visibleIncomeSources(defaultIncomeSources)
            state.copy(incomeSources = sources, selectedSource = value, categorySettings = settings).recalculate()
        }
    }

    fun deleteSource(v: String) {
        val value = v.trim()
        if (value.isBlank()) return
        val current = _uiState.value.categorySettings
        val settings = if (value in defaultIncomeSources) {
            current.copy(hiddenIncomeSources = (current.hiddenIncomeSources + value).distinct())
        } else {
            current.copy(customIncomeSources = current.customIncomeSources - value)
        }
        saveCategorySettings(settings)
        _uiState.update { state ->
            val sources = settings.visibleIncomeSources(defaultIncomeSources)
            state.copy(
                incomeSources = sources,
                selectedSource = state.selectedSource.takeIf { it in sources } ?: sources.firstOrNull().orEmpty(),
                categorySettings = settings
            ).recalculate()
        }
    }

    fun fetchCryptoRate() {
        val s = _uiState.value
        viewModelScope.launch {
            if (s.selectedCoin == "Other") {
                _uiState.update {
                    it.copy(
                        cryptoRateStatus = RateStatus.MANUAL_REQUIRED,
                        cryptoRateSource = "MANUAL",
                        error = "Enter the coin rate manually for Other."
                    )
                }
                return@launch
            }
            _uiState.update { it.copy(isFetchingRate = true, cryptoRateStatus = RateStatus.UPDATING, error = null) }
            cryptoRateService.getRate(s.selectedCoin, s.defaultCurrency)
                .onSuccess { rate ->
                    _uiState.update {
                        it.copy(
                            cryptoRate = rate.rate.toString(),
                            cryptoRateSource = rate.source,
                            cryptoRateFetchedAt = rate.fetchedAtMillis,
                            isFetchingRate = false,
                            cryptoRateStatus = RateStatus.UPDATED
                        ).recalculate()
                    }
                }
                .onFailure {
                    _uiState.update { state ->
                        state.copy(
                            isFetchingRate = false,
                            cryptoRateStatus = RateStatus.MANUAL_REQUIRED,
                            error = "Live crypto rate unavailable. Enter it manually."
                        )
                    }
                }
        }
    }

    fun save() {
        val uid = authRepository.getCurrentUserId()
        val s = _uiState.value
        val amountCents = s.convertedAmountCents.takeIf { it > 0L } ?: parseAmountCents(s.amount)
        if (uid == null) { _uiState.update { it.copy(error = "Please log in again") }; return }
        if (s.name.isBlank()) { _uiState.update { it.copy(error = "Enter a name") }; return }
        if (amountCents == null || amountCents <= 0L) { _uiState.update { it.copy(error = "Enter a valid amount") }; return }
        if (!s.isCrypto && s.needsExchangeRate && (s.exchangeRate.toDoubleOrNull() ?: 0.0) <= 0.0) {
            _uiState.update { it.copy(error = "Enter a valid exchange rate") }
            return
        }
        if (s.isCrypto && (s.cryptoRate.toDoubleOrNull() ?: 0.0) <= 0.0) {
            _uiState.update { it.copy(error = "Enter a valid crypto rate") }
            return
        }
        val cryptoCoin = if (s.selectedCoin == "Other") s.customCryptoCoin.trim() else s.selectedCoin
        if (s.isCrypto && cryptoCoin.isBlank()) {
            _uiState.update { it.copy(error = "Enter a crypto coin name") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val draft = TransactionDraft(
                title = s.name.trim(),
                amountCents = amountCents,
                type = TransactionType.INCOME,
                source = s.selectedSource,
                note = s.note.trim(),
                dateMillis = s.selectedDate,
                originalAmount = if (s.isCrypto) (s.cryptoAmount.toDoubleOrNull() ?: s.amount.toDoubleOrNull() ?: 0.0) else (s.amount.toDoubleOrNull() ?: 0.0),
                originalCurrency = if (s.isCrypto) cryptoCoin else s.selectedCurrency,
                defaultCurrency = s.defaultCurrency,
                exchangeRate = if (s.isCrypto) s.cryptoRate.toDoubleOrNull() else s.exchangeRate.toDoubleOrNull(),
                isRecurring = s.isRecurring,
                cryptoCoin = if (s.isCrypto) cryptoCoin else null,
                cryptoAmount = if (s.isCrypto) (s.cryptoAmount.toDoubleOrNull() ?: s.amount.toDoubleOrNull()) else null,
                cryptoRate = if (s.isCrypto) s.cryptoRate.toDoubleOrNull() else null,
                cryptoRateSource = if (s.isCrypto) s.cryptoRateSource else null,
                cryptoRateFetchedAt = if (s.isCrypto) s.cryptoRateFetchedAt else null
            )
            val result = if (s.editId.isNullOrBlank()) incomeRepository.addIncome(uid, draft)
                else incomeRepository.updateIncome(s.editId, draft)
            _uiState.update {
                if (result.isSuccess) it.copy(isLoading = false, isSaved = true)
                else it.copy(isLoading = false, error = result.exceptionOrNull()?.message ?: "Failed to save")
            }
        }
    }

    private fun loadExisting(id: String) {
        viewModelScope.launch {
            val transaction = incomeRepository.getIncome(id) ?: return@launch
            _uiState.update {
                it.copy(
                    name = transaction.title,
                    amount = transaction.originalAmount.takeIf { amount -> amount > 0.0 }?.toPlainInput()
                        ?: (transaction.amountCents / 100.0).toPlainInput(),
                    note = transaction.note,
                    selectedSource = transaction.source.ifBlank { "Salary" },
                    selectedDate = transaction.dateMillis,
                    selectedCurrency = transaction.originalCurrency,
                    defaultCurrency = transaction.defaultCurrency,
                    exchangeRate = transaction.exchangeRate?.toPlainInput().orEmpty(),
                    isRecurring = transaction.isRecurring,
                    selectedCoin = transaction.cryptoCoin?.takeIf { it in cryptoCoins } ?: if (transaction.cryptoCoin.isNullOrBlank()) "BTC" else "Other",
                    customCryptoCoin = transaction.cryptoCoin?.takeIf { it !in cryptoCoins }.orEmpty(),
                    cryptoAmount = transaction.cryptoAmount?.toPlainInput().orEmpty(),
                    cryptoRate = transaction.cryptoRate?.toPlainInput().orEmpty(),
                    cryptoRateSource = transaction.cryptoRateSource ?: "MANUAL",
                    cryptoRateFetchedAt = transaction.cryptoRateFetchedAt,
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

    private fun saveCategorySettings(settings: CategorySettings) {
        val current = profile ?: return
        viewModelScope.launch {
            userRepository.upsertProfile(current.copy(categorySettingsJson = settings.toJson(), isSynced = false))
        }
    }

    private fun AddIncomeUiState.recalculate(): AddIncomeUiState {
        val converted = if (isCrypto) {
            val amountValue = cryptoAmount.toDoubleOrNull() ?: amount.toDoubleOrNull() ?: 0.0
            val rate = cryptoRate.toDoubleOrNull() ?: 0.0
            amountValue * rate
        } else {
            val amountValue = amount.toDoubleOrNull() ?: 0.0
            val rate = if (selectedCurrency == defaultCurrency) 1.0 else exchangeRate.toDoubleOrNull() ?: 0.0
            amountValue * rate
        }
        return copy(convertedAmountCents = (converted * 100.0).toLong().coerceAtLeast(0L))
    }

    private fun Double.toPlainInput(): String =
        if (this % 1.0 == 0.0) toLong().toString() else toString()
}
