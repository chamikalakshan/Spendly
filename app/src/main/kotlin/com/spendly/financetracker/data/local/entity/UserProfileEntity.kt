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
    @ColumnInfo(defaultValue = "''") val categorySettingsJson: String = ""
)
