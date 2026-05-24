package com.spendly.financetracker.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey

@Entity(
    tableName = "income_entries",
    indices = [
        Index("userId"),
        Index("dateMillis"),
        Index(value = ["userId", "dateMillis"]),
        Index("source")
    ]
)
data class IncomeEntryEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val amountCents: Long,
    val source: String,
    val dateMillis: Long,
    val note: String,
    val isSynced: Boolean,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
    @ColumnInfo(defaultValue = "0.0") val originalAmount: Double = amountCents / 100.0,
    @ColumnInfo(defaultValue = "'LKR'") val originalCurrency: String = "LKR",
    @ColumnInfo(defaultValue = "'LKR'") val defaultCurrency: String = "LKR",
    val exchangeRate: Double? = null,
    @ColumnInfo(defaultValue = "0") val isRecurring: Boolean = false,
    val cryptoCoin: String? = null,
    val cryptoAmount: Double? = null,
    val cryptoRate: Double? = null,
    val cryptoRateSource: String? = null,
    val cryptoRateFetchedAt: Long? = null
)
