package com.spendly.financetracker.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey

@Entity(
    tableName = "expense_entries",
    indices = [
        Index("userId"),
        Index("dateMillis"),
        Index(value = ["userId", "dateMillis"]),
        Index("category")
    ]
)
data class ExpenseEntryEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val amountCents: Long,
    val category: String,
    val dateMillis: Long,
    val note: String,
    val isSynced: Boolean,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
    @ColumnInfo(defaultValue = "0.0") val originalAmount: Double = amountCents / 100.0,
    @ColumnInfo(defaultValue = "'LKR'") val originalCurrency: String = "LKR",
    @ColumnInfo(defaultValue = "'LKR'") val defaultCurrency: String = "LKR",
    val exchangeRate: Double? = null,
    val paymentMethod: String? = null,
    val expenseType: String? = null,
    val goalId: String? = null
)
