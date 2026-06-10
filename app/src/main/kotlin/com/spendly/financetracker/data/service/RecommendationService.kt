package com.spendly.financetracker.data.service

import com.spendly.financetracker.data.model.BudgetProgress
import com.spendly.financetracker.data.model.FinanceTransaction
import com.spendly.financetracker.data.model.TransactionType
import com.spendly.financetracker.ui.util.nextMonthStart
import com.spendly.financetracker.ui.util.shiftMonth
import javax.inject.Inject
import javax.inject.Singleton

data class SmartInsight(
    val title: String,
    val message: String,
    val severity: String = "INFO"
)

@Singleton
class RecommendationService @Inject constructor() {
    fun buildInsights(
        transactions: List<FinanceTransaction>,
        selectedMonthStart: Long,
        budgets: List<BudgetProgress> = emptyList()
    ): List<SmartInsight> {
        val monthEnd = nextMonthStart(selectedMonthStart)
        val previousStart = shiftMonth(selectedMonthStart, -1)
        val previousEnd = selectedMonthStart
        val current = transactions.filter { it.dateMillis in selectedMonthStart until monthEnd }
        val previous = transactions.filter { it.dateMillis in previousStart until previousEnd }
        val currentExpense = current.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountCents }
        val previousExpense = previous.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountCents }
        val currentIncome = current.filter { it.type == TransactionType.INCOME }.sumOf { it.amountCents }

        val insights = mutableListOf<SmartInsight>()
        if (previousExpense > 0 && currentExpense > previousExpense) {
            val increase = (((currentExpense - previousExpense).toDouble() / previousExpense.toDouble()) * 100).toInt()
            insights += SmartInsight("Spending increased", "Expenses are $increase% higher than last month.", "WARNING")
        }
        budgets.firstOrNull { it.isExceeded }?.let {
            insights += SmartInsight("Budget exceeded", "${it.budget.category} is over the monthly budget.", "DANGER")
        }
        budgets.firstOrNull { it.isWarning && !it.isExceeded }?.let {
            insights += SmartInsight("Budget warning", "${it.budget.category} is near the budget limit.", "WARNING")
        }
        if (currentIncome > 0) {
            val savingsRate = (((currentIncome - currentExpense).coerceAtLeast(0L).toDouble() / currentIncome.toDouble()) * 100).toInt()
            insights += SmartInsight("Savings rate", "You saved about $savingsRate% of this month's income.", "INFO")
        }
        return insights.take(4)
    }
}
