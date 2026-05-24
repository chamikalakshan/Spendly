package com.spendly.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.spendly.app.data.model.enums.Currency
import com.spendly.app.data.model.enums.IncomeSource
import com.spendly.app.data.model.enums.InvoiceStatus

@Entity(
    tableName = "income_entries",
    indices = [
        Index("userId"),
        Index("date"),
        Index(value = ["userId", "date"])
    ]
)
data class IncomeEntryEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val name: String,
    val sourceType: IncomeSource,
    val amount: Double,
    val currency: Currency,
    val amountLKR: Double,
    val exchangeRate: Double,
    val coin: String?,
    val projectName: String?,
    val invoiceStatus: InvoiceStatus?,
    val date: Long,
    val isRecurring: Boolean,
    val note: String?,
    val isSynced: Boolean = false,
    val createdAt: Long
)
