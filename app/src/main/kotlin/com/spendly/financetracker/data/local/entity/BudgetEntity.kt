package com.spendly.financetracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "budget_entries",
    indices = [
        Index("userId"),
        Index("monthStartMillis"),
        Index(value = ["userId", "monthStartMillis"]),
        Index(value = ["userId", "category", "monthStartMillis"])
    ]
)
data class BudgetEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val category: String,
    val monthStartMillis: Long,
    val limitCents: Long,
    @ColumnInfo(defaultValue = "'LKR'") val defaultCurrency: String = "LKR",
    @ColumnInfo(defaultValue = "80") val alertThresholdPercent: Int = 80,
    val note: String? = null,
    val isSynced: Boolean,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
    val deletedAtMillis: Long? = null
)
