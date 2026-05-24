package com.spendly.app.data.model

import com.spendly.app.data.model.enums.ExpenseCategory
import com.spendly.app.data.model.enums.ExpenseType
import com.spendly.app.data.model.enums.PaymentMethod

data class ExpenseEntry(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val amount: Double = 0.0,
    val category: ExpenseCategory = ExpenseCategory.OTHER,
    val expenseType: ExpenseType = ExpenseType.DISCRETIONARY,
    val paymentMethod: PaymentMethod = PaymentMethod.CARD,
    val date: Long = 0L,
    val note: String? = null,
    val isSynced: Boolean = false,       // for offline tracking
    val createdAt: Long = 0L
)
