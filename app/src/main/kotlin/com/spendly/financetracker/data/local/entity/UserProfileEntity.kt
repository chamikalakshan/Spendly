package com.spendly.financetracker.data.local.entity

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey val uid: String,
    val name: String,
    val email: String,
    val defaultCurrency: String,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
    val isSynced: Boolean,
    val profileImageUri: String? = null,
    @ColumnInfo(defaultValue = "''") val exchangeRateSettings: String = "",
    val notificationFrequency: String? = null,
    val reminderTime: String? = null,
    @ColumnInfo(defaultValue = "''") val categorySettingsJson: String = "",
    @ColumnInfo(defaultValue = "'SYSTEM'") val themeMode: String = "SYSTEM",
    @ColumnInfo(defaultValue = "1") val budgetAlertsEnabled: Boolean = true,
    @ColumnInfo(defaultValue = "80") val budgetAlertThresholdPercent: Int = 80,
    val profileImageStoragePath: String? = null,
    @ColumnInfo(defaultValue = "'GREEN'") val accentColorKey: String = "GREEN",
    @ColumnInfo(defaultValue = "0") val dailyRemindersEnabled: Boolean = false,
    @ColumnInfo(defaultValue = "1") val remindExpenses: Boolean = true,
    @ColumnInfo(defaultValue = "1") val remindIncome: Boolean = true,
    @ColumnInfo(defaultValue = "1") val smartReminderMode: Boolean = true
)
