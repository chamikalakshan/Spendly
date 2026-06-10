package com.spendly.financetracker.data.model

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val defaultCurrency: String = "LKR",
    val createdAtMillis: Long = 0L,
    val updatedAtMillis: Long = 0L,
    val isSynced: Boolean = false,
    val profileImageUri: String? = null,
    val exchangeRateSettings: String = "",
    val notificationFrequency: String? = null,
    val reminderTime: String? = null,
    val categorySettingsJson: String = "",
    val themeMode: String = "SYSTEM",
    val budgetAlertsEnabled: Boolean = true,
    val budgetAlertThresholdPercent: Int = 80,
    val profileImageStoragePath: String? = null,
    val accentColorKey: String = "GREEN",
    val dailyRemindersEnabled: Boolean = false,
    val remindExpenses: Boolean = true,
    val remindIncome: Boolean = true,
    val smartReminderMode: Boolean = true
)
