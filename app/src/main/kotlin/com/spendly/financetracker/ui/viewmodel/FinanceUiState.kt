package com.spendly.financetracker.ui.viewmodel

import com.spendly.financetracker.data.model.FinanceTransaction
import com.spendly.financetracker.data.model.SavingsGoal
import com.spendly.financetracker.data.model.TransactionType
import com.spendly.financetracker.data.model.UserProfile
import com.spendly.financetracker.data.model.UserSession
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

typealias Goal = SavingsGoal
typealias GoalDraft = com.spendly.financetracker.data.model.GoalDraft

enum class AuthMode {
    SIGN_IN,
    CREATE_ACCOUNT
}

enum class AppTab {
    HOME,
    TRANSACTIONS,
    GOALS,
    ANALYTICS,
    PROFILE
}

enum class TransactionTab(val title: String) {
    ALL("All"),
    EXPENSES("Expenses"),
    INCOMES("Incomes")
}

data class AnalyticsSlice(
    val label: String,
    val amountCents: Long,
    val percent: Double
)

data class AnalyticsMonth(
    val label: String,
    val income: Long,
    val expense: Long
)

data class SpendingSplitUi(
    val committedCents: Long = 0L,
    val discretionaryCents: Long = 0L,
    val committedPercent: Double = 0.0,
    val discretionaryPercent: Double = 0.0
)

data class FinanceUiState(
    val isFirebaseConfigured: Boolean = true,
    val isLoading: Boolean = true,
    val isSyncing: Boolean = false,
    val isBusy: Boolean = false,
    val session: UserSession? = null,
    val profile: UserProfile? = null,
    val authMode: AuthMode = AuthMode.SIGN_IN,
    val email: String = "",
    val password: String = "",
    val transactionTitle: String = "",
    val transactionAmount: String = "",
    val transactionNote: String = "",
    val transactionType: TransactionType = TransactionType.EXPENSE,
    val currentTab: AppTab = AppTab.HOME,
    val transactionTab: TransactionTab = TransactionTab.ALL,
    val goals: List<SavingsGoal> = emptyList(),
    val transactions: List<FinanceTransaction> = emptyList(),
    val openSyncConflictCount: Int = 0,
    val message: String? = null
) {
    val incomeCents: Long by lazy {
        transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amountCents }
    }

    val expenseCents: Long by lazy {
        transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountCents }
    }

    val balanceCents: Long by lazy { transactions.sumOf { it.signedAmountCents } }

    private val currentMonthTransactions: List<FinanceTransaction> by lazy { currentMonthTransactions() }

    val currentMonthIncomeCents: Long by lazy {
        currentMonthTransactions
            .filter { it.type == TransactionType.INCOME && !it.isInitialIncome() }
            .sumOf { it.amountCents }
    }

    val currentMonthExpenseCents: Long by lazy {
        currentMonthTransactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amountCents }
    }

    val currentMonthNetSavingsCents: Long by lazy { currentMonthIncomeCents - currentMonthExpenseCents }

    val primaryGoal: SavingsGoal? by lazy { goals.firstOrNull { it.isPrimary } ?: goals.firstOrNull() }

    val primaryGoals: List<SavingsGoal> by lazy {
        goals.filter { it.isPrimary }.ifEmpty { primaryGoal?.let(::listOf) ?: emptyList() }
    }

    val otherGoals: List<SavingsGoal> by lazy {
        val primaryIds = primaryGoals.mapTo(hashSetOf()) { it.id }
        goals.filterNot { it.id in primaryIds }
    }

    val savingsRate: Int by lazy {
        if (currentMonthIncomeCents <= 0L) 0
        else (((currentMonthIncomeCents - currentMonthExpenseCents) * 100) / currentMonthIncomeCents).toInt().coerceIn(0, 100)
    }

    val recentTransactions: List<FinanceTransaction> by lazy {
        transactions.sortedWith(compareByDescending<FinanceTransaction> { it.dateMillis }.thenByDescending { it.createdAtMillis }).take(6)
    }

    val primaryGoalMonthlyNeedCents: Long by lazy {
        primaryGoal?.let { goal ->
            if (goal.remainingCents <= 0L) 0L else (goal.remainingCents + 11L) / 12L
        } ?: 0L
    }

    val currentYearSavingsCents: Long by lazy {
            val now = Calendar.getInstance()
            transactions
                .filter {
                    val calendar = Calendar.getInstance().apply { timeInMillis = it.dateMillis }
                    calendar.get(Calendar.YEAR) == now.get(Calendar.YEAR)
                }
                .sumOf { it.signedAmountCents }
        }

    val spendingByCategory: List<AnalyticsSlice> by lazy {
        transactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category.ifBlank { "Other" }.take(18) }
            .mapValues { entry -> entry.value.sumOf { it.amountCents } }
            .toList()
            .sortedByDescending { it.second }
            .map { (category, amount) -> AnalyticsSlice(category, amount, percentOf(amount, expenseCents)) }
    }

    val incomeSources: List<AnalyticsSlice> by lazy {
        transactions
            .filter { it.type == TransactionType.INCOME }
            .groupBy { it.source.ifBlank { "Other" }.take(18) }
            .mapValues { entry -> entry.value.sumOf { it.amountCents } }
            .toList()
            .sortedByDescending { it.second }
            .map { (source, amount) -> AnalyticsSlice(source, amount, percentOf(amount, incomeCents)) }
    }

    val spendingSplit: SpendingSplitUi by lazy {
            val committedCategories = setOf("Rent", "Subscriptions", "Gym", "Goal")
            val committed = transactions
                .filter { it.type == TransactionType.EXPENSE && it.category in committedCategories }
                .sumOf { it.amountCents }
            val discretionary = (expenseCents - committed).coerceAtLeast(0L)
            SpendingSplitUi(
                committedCents = committed,
                discretionaryCents = discretionary,
                committedPercent = percentOf(committed, expenseCents),
                discretionaryPercent = percentOf(discretionary, expenseCents)
            )
        }

    val monthlyOverview: List<AnalyticsMonth> by lazy {
        lastFiveMonths().map { (label, month, year) ->
            val monthTransactions = transactions.filter {
                val calendar = Calendar.getInstance().apply { timeInMillis = it.dateMillis }
                calendar.get(Calendar.MONTH) == month && calendar.get(Calendar.YEAR) == year
            }
            AnalyticsMonth(
                label = label,
                income = monthTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amountCents },
                expense = monthTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountCents }
            )
        }
    }

    private fun percentOf(amount: Long, total: Long): Double =
        if (total > 0L) (amount.toDouble() * 100.0) / total.toDouble() else 0.0

    private fun currentMonthTransactions(): List<FinanceTransaction> {
        val now = Calendar.getInstance()
        return transactions.filter {
            val calendar = Calendar.getInstance().apply { timeInMillis = it.dateMillis }
            calendar.get(Calendar.MONTH) == now.get(Calendar.MONTH) &&
                calendar.get(Calendar.YEAR) == now.get(Calendar.YEAR)
        }
    }

private fun lastFiveMonths(): List<Triple<String, Int, Int>> {
        val formatter = SimpleDateFormat("MMM", Locale.getDefault())
        val calendar = Calendar.getInstance()
        return (4 downTo 0).map { offset ->
            val monthCalendar = calendar.clone() as Calendar
            monthCalendar.add(Calendar.MONTH, -offset)
            Triple(
                formatter.format(Date(monthCalendar.timeInMillis)),
                monthCalendar.get(Calendar.MONTH),
                monthCalendar.get(Calendar.YEAR)
            )
        }
    }
}

private fun FinanceTransaction.isInitialIncome(): Boolean =
    type == TransactionType.INCOME && source.equals("Initial Income", ignoreCase = true)
