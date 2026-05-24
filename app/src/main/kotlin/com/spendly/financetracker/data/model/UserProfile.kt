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
    val categorySettingsJson: String = ""
)
