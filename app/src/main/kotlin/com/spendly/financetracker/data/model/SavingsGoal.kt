package com.spendly.financetracker.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SavingsGoal(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val status: String = "Tracking",
    val targetCents: Long = 0L,
    val savedCents: Long = 0L,
    val dueDateMillis: Long = 0L,
    val category: String = "Custom",
    val isPrimary: Boolean = false,
    val isSynced: Boolean = false,
    val createdAtMillis: Long = 0L,
    val updatedAtMillis: Long = 0L,
    val initialSavedCents: Long = 0L,
    val defaultCurrency: String = "LKR",
    val iconKey: String = "goal"
) {
    val progressPercent: Int
        get() = if (targetCents <= 0L) 0 else ((savedCents * 100) / targetCents).toInt().coerceIn(0, 100)

    val remainingCents: Long
        get() = (targetCents - savedCents).coerceAtLeast(0L)

    val dueDate: String
        get() = if (dueDateMillis > 0L) {
            SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(dueDateMillis))
        } else {
            "No target date"
        }
}

data class GoalDraft(
    val title: String,
    val status: String,
    val targetAmount: String,
    val targetDate: String,
    val initialSaved: String = "",
    val category: String = "Custom",
    val dueDateMillis: Long? = null,
    val defaultCurrency: String = "LKR",
    val isPrimary: Boolean = false,
    val iconKey: String = "goal"
)
