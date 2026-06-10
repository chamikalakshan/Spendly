package com.spendly.financetracker.data.model

data class RecurringRule(
    val id: String = "",
    val userId: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val name: String = "",
    val amountCents: Long = 0L,
    val originalAmount: Double = amountCents / 100.0,
    val originalCurrency: String = "LKR",
    val defaultCurrency: String = "LKR",
    val exchangeRate: Double? = null,
    val source: String? = null,
    val category: String? = null,
    val paymentMethod: String? = null,
    val expenseType: ExpenseType? = null,
    val note: String = "",
    val frequency: RecurringFrequency = RecurringFrequency.MONTHLY,
    val interval: Int = 1,
    val startDateMillis: Long = 0L,
    val nextRunDateMillis: Long = 0L,
    val endDateMillis: Long? = null,
    val isActive: Boolean = true,
    val lastGeneratedAtMillis: Long? = null,
    val isSynced: Boolean = false,
    val createdAtMillis: Long = 0L,
    val updatedAtMillis: Long = 0L,
    val deletedAtMillis: Long? = null
)

data class RecurringRuleDraft(
    val id: String? = null,
    val type: TransactionType,
    val name: String,
    val amountCents: Long,
    val originalAmount: Double = amountCents / 100.0,
    val originalCurrency: String = "LKR",
    val defaultCurrency: String = "LKR",
    val exchangeRate: Double? = null,
    val source: String? = null,
    val category: String? = null,
    val paymentMethod: String? = null,
    val expenseType: ExpenseType? = null,
    val note: String = "",
    val frequency: RecurringFrequency = RecurringFrequency.MONTHLY,
    val interval: Int = 1,
    val startDateMillis: Long,
    val nextRunDateMillis: Long = startDateMillis,
    val endDateMillis: Long? = null,
    val isActive: Boolean = true
)

enum class RecurringFrequency {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
}
