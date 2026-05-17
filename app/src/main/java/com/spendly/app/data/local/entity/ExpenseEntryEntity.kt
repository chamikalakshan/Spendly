package com.spendly.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.spendly.app.data.model.enums.ExpenseCategory
import com.spendly.app.data.model.enums.ExpenseType
import com.spendly.app.data.model.enums.PaymentMethod

@Entity(
    tableName = "expense_entries",
    indices = [
        Index("userId"),
        Index("date"),
        Index(value = ["userId", "date"]),
        Index("category")
    ]
)
data class ExpenseEntryEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val name: String,
    val amount: Double,
    val category: ExpenseCategory,
    val expenseType: ExpenseType,
    val paymentMethod: PaymentMethod,
    val date: Long,
    val note: String?,
    val isSynced: Boolean = false,
    val createdAt: Long
)
