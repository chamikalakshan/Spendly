package com.spendly.financetracker.data.model

data class Budget(
    val id: String = "",
    val userId: String = "",
    val category: String = "",
    val monthStartMillis: Long = 0L,
    val limitCents: Long = 0L,
    val defaultCurrency: String = "LKR",
    val alertThresholdPercent: Int = 80,
    val note: String = "",
    val isSynced: Boolean = false,
    val createdAtMillis: Long = 0L,
    val updatedAtMillis: Long = 0L,
    val deletedAtMillis: Long? = null
)

data class BudgetDraft(
    val id: String? = null,
    val category: String,
    val monthStartMillis: Long,
    val limitCents: Long,
    val defaultCurrency: String = "LKR",
    val alertThresholdPercent: Int = 80,
    val note: String = ""
)

data class BudgetProgress(
    val budget: Budget,
    val spentCents: Long,
    val remainingCents: Long,
    val progressPercent: Int,
    val isWarning: Boolean,
    val isExceeded: Boolean
)
