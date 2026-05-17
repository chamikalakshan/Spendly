package com.spendly.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spendly.app.data.model.*
import com.spendly.app.data.model.enums.*
import com.spendly.app.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.util.*
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val incomeRepository: IncomeRepository,
    private val expenseRepository: ExpenseRepository,
    private val goalRepository: GoalRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Sample Data as requested in spec
    val userName = MutableStateFlow("Kavindu").asStateFlow()
    val userInitials = MutableStateFlow("KS").asStateFlow()

    val currentMonthIncome = MutableStateFlow(215413.0).asStateFlow()
    val currentMonthExpenses = MutableStateFlow(88200.0).asStateFlow()
    
    val netSavings = combine(currentMonthIncome, currentMonthExpenses) { income, expenses ->
        income - expenses
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 127213.0)

    val savingsRate = combine(currentMonthIncome, netSavings) { income, savings ->
        if (income > 0) ((savings / income) * 100).toInt() else 0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 59)

    val activeGoal = MutableStateFlow<SavingsGoal?>(
        SavingsGoal(
            id = "macbook_goal",
            goalName = "MacBook Pro M4",
            targetAmount = 490000.0,
            targetDate = GregorianCalendar(2027, Calendar.MAY, 1).timeInMillis,
            createdAt = System.currentTimeMillis()
        )
    ).asStateFlow()

    val goalProgressPercent = MutableStateFlow(0.22f).asStateFlow()
    val requiredMonthlySavings = MutableStateFlow(38280.0).asStateFlow()
    val isOnTrack = MutableStateFlow(true).asStateFlow()

    val recentTransactions = MutableStateFlow(
        listOf(
            TransactionItem.Expense(ExpenseEntry(id="1", note="UberEats", category=ExpenseCategory.FOOD, expenseType=ExpenseType.DISCRETIONARY, paymentMethod=PaymentMethod.UBEREATS, amount=1450.0, date=System.currentTimeMillis())),
            TransactionItem.Expense(ExpenseEntry(id="2", note="PickMe", category=ExpenseCategory.TRANSPORT, expenseType=ExpenseType.DISCRETIONARY, paymentMethod=PaymentMethod.PICKME, amount=650.0, date=System.currentTimeMillis())),
            TransactionItem.Income(IncomeEntry(id="3", sourceType=IncomeSource.SALARY, amountLKR=128000.0, date=GregorianCalendar(2026, Calendar.MAY, 25).timeInMillis)),
            TransactionItem.Income(IncomeEntry(id="4", sourceType=IncomeSource.FREELANCE, projectName="React App", amountLKR=65000.0, date=GregorianCalendar(2026, Calendar.MAY, 22).timeInMillis)),
            TransactionItem.Income(IncomeEntry(id="5", sourceType=IncomeSource.CRYPTO, coin="ETH", amountLKR=14400.0, date=GregorianCalendar(2026, Calendar.MAY, 18).timeInMillis)),
            TransactionItem.Income(IncomeEntry(id="6", sourceType=IncomeSource.ADSENSE, amountLKR=8013.0, date=GregorianCalendar(2026, Calendar.MAY, 15).timeInMillis)),
            TransactionItem.Expense(ExpenseEntry(id="7", note="Rent", category=ExpenseCategory.RENT, expenseType=ExpenseType.COMMITTED, paymentMethod=PaymentMethod.AUTODEBIT, amount=34000.0, date=GregorianCalendar(2026, Calendar.MAY, 1).timeInMillis)),
            TransactionItem.Expense(ExpenseEntry(id="8", note="Gym membership", category=ExpenseCategory.GYM, expenseType=ExpenseType.COMMITTED, paymentMethod=PaymentMethod.AUTODEBIT, amount=3500.0, date=GregorianCalendar(2026, Calendar.MAY, 1).timeInMillis))
        )
    ).asStateFlow()

    init {
        // Month boundaries for Step 17 implementation
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startMs = calendar.timeInMillis
        
        calendar.add(Calendar.MONTH, 1)
        calendar.add(Calendar.MILLISECOND, -1)
        val endMs = calendar.timeInMillis
    }
}
