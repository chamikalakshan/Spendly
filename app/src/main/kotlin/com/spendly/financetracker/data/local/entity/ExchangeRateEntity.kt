package com.spendly.financetracker.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "exchange_rates",
    indices = [Index(value = ["fromCurrency", "toCurrency"], unique = true)]
)
data class ExchangeRateEntity(
    @PrimaryKey val id: String,
    val fromCurrency: String,
    val toCurrency: String,
    val rate: Double,
    val source: String,
    val fetchedAtMillis: Long,
    val expiresAtMillis: Long
)
