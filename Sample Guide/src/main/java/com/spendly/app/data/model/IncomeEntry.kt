package com.spendly.app.data.model

import com.spendly.app.data.model.enums.Currency
import com.spendly.app.data.model.enums.IncomeSource
import com.spendly.app.data.model.enums.InvoiceStatus

data class IncomeEntry(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val sourceType: IncomeSource = IncomeSource.SALARY,
    val amount: Double = 0.0,
    val currency: Currency = Currency.LKR,
    val amountLKR: Double = 0.0,
    val exchangeRate: Double = 1.0,
    val coin: String? = null,            // CRYPTO only
    val projectName: String? = null,     // FREELANCE only
    val invoiceStatus: InvoiceStatus? = null, // FREELANCE only
    val date: Long = 0L,
    val isRecurring: Boolean = false,
    val note: String? = null,
    val isSynced: Boolean = false,       // for offline tracking
    val createdAt: Long = 0L
)
