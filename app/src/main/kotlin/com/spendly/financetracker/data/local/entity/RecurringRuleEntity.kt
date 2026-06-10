package com.spendly.financetracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "recurring_rules",
    indices = [
        Index("userId"),
        Index("nextRunDateMillis"),
        Index(value = ["userId", "isActive", "nextRunDateMillis"])
    ]
)
data class RecurringRuleEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val type: String,
    val name: String,
    val amountCents: Long,
    @ColumnInfo(defaultValue = "0.0") val originalAmount: Double = amountCents / 100.0,
    @ColumnInfo(defaultValue = "'LKR'") val originalCurrency: String = "LKR",
    @ColumnInfo(defaultValue = "'LKR'") val defaultCurrency: String = "LKR",
    val exchangeRate: Double? = null,
    val source: String? = null,
    val category: String? = null,
    val paymentMethod: String? = null,
    val expenseType: String? = null,
    val note: String? = null,
    @ColumnInfo(defaultValue = "'MONTHLY'") val frequency: String = "MONTHLY",
    @ColumnInfo(defaultValue = "1") val interval: Int = 1,
    val startDateMillis: Long,
    val nextRunDateMillis: Long,
    val endDateMillis: Long? = null,
    @ColumnInfo(defaultValue = "1") val isActive: Boolean = true,
    val lastGeneratedAtMillis: Long? = null,
    val isSynced: Boolean,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
    val deletedAtMillis: Long? = null
)
