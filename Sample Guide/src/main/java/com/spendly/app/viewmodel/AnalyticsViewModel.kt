package com.spendly.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendly.app.data.model.enums.ExpenseCategory
import com.spendly.app.data.model.enums.ExpenseType
import com.spendly.app.data.model.enums.IncomeSource
import com.spendly.app.repository.AuthRepository
import com.spendly.app.repository.ExpenseRepository
import com.spendly.app.repository.IncomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class MonthlyOverviewItem(
    val monthLabel: String,
    val income: Double,
    val expense: Double
)

data class AnalyticsUiState(
    val selectedMonthLabel: String = "",
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val expensesByCategory: Map<ExpenseCategory, Double> = emptyMap(),
    val incomeBySource: Map<IncomeSource, Double> = emptyMap(),
    val committedTotal: Double = 0.0,
    val committedPercent: Int = 0,
    val discretionaryTotal: Double = 0.0,
    val discretionaryPercent: Int = 0,
    val committedSubcategories: String = "",
    val discretionarySubcategories: String = "",
    val monthlyOverviewData: List<MonthlyOverviewItem> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val incomeRepository: IncomeRepository,
    private val expenseRepository: ExpenseRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    val totalIncome: StateFlow<Double> = uiState
        .map { it.totalIncome }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalExpenses: StateFlow<Double> = uiState
        .map { it.totalExpense }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val expensesByCategory: StateFlow<Map<ExpenseCategory, Double>> = uiState
        .map { it.expensesByCategory }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val committedTotal: StateFlow<Double> = uiState
        .map { it.committedTotal }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val committedPercent: StateFlow<Int> = uiState
        .map { it.committedPercent }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val discretionaryTotal: StateFlow<Double> = uiState
        .map { it.discretionaryTotal }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val discretionaryPercent: StateFlow<Int> = uiState
        .map { it.discretionaryPercent }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val monthlyIncome: StateFlow<List<Double>> = uiState
        .map { state -> state.monthlyOverviewData.map { it.income } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthlyExpenses: StateFlow<List<Double>> = uiState
        .map { state -> state.monthlyOverviewData.map { it.expense } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthLabels: StateFlow<List<String>> = uiState
        .map { state -> state.monthlyOverviewData.map { it.monthLabel } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val incomeBySource: StateFlow<Map<IncomeSource, Double>> = uiState
        .map { it.incomeBySource }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    private val calendar = Calendar.getInstance()

    init {
        updateMonthLabel()
        loadAnalyticsData()
    }

    fun setMonth(startMs: Long, endMs: Long, label: String) {
        calendar.timeInMillis = startMs
        _uiState.update { it.copy(selectedMonthLabel = label, isLoading = true) }
        loadAnalyticsData()
    }

    fun nextMonth() {
        calendar.add(Calendar.MONTH, 1)
        updateMonthLabel()
        loadAnalyticsData()
    }

    fun previousMonth() {
        calendar.add(Calendar.MONTH, -1)
        updateMonthLabel()
        loadAnalyticsData()
    }

    private fun updateMonthLabel() {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.US)
        _uiState.update { it.copy(selectedMonthLabel = sdf.format(calendar.time)) }
    }

    private fun loadAnalyticsData() {
        val userId = authRepository.getCurrentUserId() ?: return
        
        val currentMonthCalendar = calendar.clone() as Calendar
        val startMs = (currentMonthCalendar.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val endMs = (currentMonthCalendar.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val incomeFlow = incomeRepository.getMonthlyIncome(userId, startMs, endMs)
            val expenseFlow = expenseRepository.getMonthlyExpenses(userId, startMs, endMs)

            combine(incomeFlow, expenseFlow) { incomeList, expenseList ->
                val totalIn = incomeList.sumOf { it.amountLKR }
                val totalEx = expenseList.sumOf { it.amount }
                
                val expByCategory = expenseList.groupBy { it.category }
                    .mapValues { it.value.sumOf { e -> e.amount } }

                val incBySource = incomeList.groupBy { it.sourceType }
                    .mapValues { it.value.sumOf { i -> i.amountLKR } }

                val committed = expenseList.filter { it.expenseType == ExpenseType.COMMITTED }
                val discretionary = expenseList.filter { it.expenseType == ExpenseType.DISCRETIONARY }

                val committedTotal = committed.sumOf { it.amount }
                val discretionaryTotal = discretionary.sumOf { it.amount }
                val committedPercent = if (totalEx > 0.0) ((committedTotal / totalEx) * 100).toInt() else 0
                val discretionaryPercent = if (totalEx > 0.0) ((discretionaryTotal / totalEx) * 100).toInt() else 0

                val committedSub = committed.map { it.category.displayName }.distinct().joinToString(" · ")
                val discretionarySub = discretionary.map { it.category.displayName }.distinct().joinToString(" · ")

                _uiState.value.copy(
                    totalIncome = totalIn,
                    totalExpense = totalEx,
                    expensesByCategory = expByCategory,
                    incomeBySource = incBySource,
                    committedTotal = committedTotal,
                    committedPercent = committedPercent,
                    discretionaryTotal = discretionaryTotal,
                    discretionaryPercent = discretionaryPercent,
                    committedSubcategories = committedSub,
                    discretionarySubcategories = discretionarySub,
                    isLoading = false
                )
            }.collect { newState ->
                _uiState.value = newState
                loadMonthlyOverview(userId)
            }
        }
    }

    private suspend fun loadMonthlyOverview(userId: String) {
        val overview = mutableListOf<MonthlyOverviewItem>()
        val tempCal = calendar.clone() as Calendar
        tempCal.add(Calendar.MONTH, -4) 

        for (i in 0 until 5) {
            val start = (tempCal.clone() as Calendar).apply { 
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0) 
            }.timeInMillis
            
            val end = (tempCal.clone() as Calendar).apply { 
                set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59) 
            }.timeInMillis
            
            val monthLabel = SimpleDateFormat("MMM", Locale.US).format(tempCal.time)
            
            val incomeList = incomeRepository.getMonthlyIncome(userId, start, end).first()
            val expenseList = expenseRepository.getMonthlyExpenses(userId, start, end).first()
            
            overview.add(MonthlyOverviewItem(monthLabel, incomeList.sumOf { it.amountLKR }, expenseList.sumOf { it.amount }))
            tempCal.add(Calendar.MONTH, 1)
        }
        
        _uiState.update { it.copy(monthlyOverviewData = overview) }
    }
}
