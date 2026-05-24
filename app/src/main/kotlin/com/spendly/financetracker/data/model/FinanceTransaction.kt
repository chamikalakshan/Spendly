package com.spendly.financetracker.data.model

enum class TransactionType {
    INCOME,
    EXPENSE
}

enum class ExpenseType {
    COMMITTED,
    DISCRETIONARY
}

data class FinanceTransaction(
    val id: String,
    val userId: String,
    val title: String,
    val amountCents: Long,
    val type: TransactionType,
    val category: String,
    val source: String,
    val note: String,
    val dateMillis: Long,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
    val isSynced: Boolean,
    val originalAmount: Double = amountCents / 100.0,
    val originalCurrency: String = "LKR",
    val defaultCurrency: String = "LKR",
    val exchangeRate: Double? = null,
    val paymentMethod: String? = null,
    val expenseType: ExpenseType? = null,
    val goalId: String? = null,
    val isRecurring: Boolean = false,
    val cryptoCoin: String? = null,
    val cryptoAmount: Double? = null,
    val cryptoRate: Double? = null,
    val cryptoRateSource: String? = null,
    val cryptoRateFetchedAt: Long? = null
) {
    val signedAmountCents: Long
        get() = if (type == TransactionType.INCOME) amountCents else -amountCents
}

data class TransactionDraft(
    val title: String,
    val amountCents: Long,
    val type: TransactionType,
    val category: String = "",
    val source: String = "",
    val note: String,
    val dateMillis: Long,
    val originalAmount: Double = amountCents / 100.0,
    val originalCurrency: String = "LKR",
    val defaultCurrency: String = "LKR",
    val exchangeRate: Double? = null,
    val paymentMethod: String? = null,
    val expenseType: ExpenseType? = null,
    val goalId: String? = null,
    val isRecurring: Boolean = false,
    val cryptoCoin: String? = null,
    val cryptoAmount: Double? = null,
    val cryptoRate: Double? = null,
    val cryptoRateSource: String? = null,
    val cryptoRateFetchedAt: Long? = null
)
