package com.spendly.financetracker.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey

@Entity(
    tableName = "savings_goals",
    indices = [
        Index("userId"),
        Index(value = ["userId", "isPrimary"])
    ]
)
data class SavingsGoalEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val status: String,
    val targetCents: Long,
    val savedCents: Long,
    val dueDateMillis: Long,
    val category: String,
    val isPrimary: Boolean,
    val isSynced: Boolean,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
    @ColumnInfo(defaultValue = "0") val initialSavedCents: Long = 0L,
    @ColumnInfo(defaultValue = "'LKR'") val defaultCurrency: String = "LKR",
    @ColumnInfo(defaultValue = "'goal'") val iconKey: String = "goal"
)
