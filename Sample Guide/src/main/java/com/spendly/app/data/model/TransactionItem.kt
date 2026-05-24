package com.spendly.app.data.model

import com.spendly.app.data.model.enums.ExpenseCategory
import com.spendly.app.data.model.enums.ExpenseType
import com.spendly.app.data.model.enums.IncomeSource
import com.spendly.app.data.model.enums.PaymentMethod

sealed class TransactionItem {
    data class Income(val entry: IncomeEntry) : TransactionItem()
    data class Expense(val entry: ExpenseEntry) : TransactionItem()
}

val TransactionItem.id: String
    get() = when (this) {
        is TransactionItem.Income -> entry.id
        is TransactionItem.Expense -> entry.id
    }

val TransactionItem.dateMs: Long
    get() = when (this) {
        is TransactionItem.Income -> entry.date
        is TransactionItem.Expense -> entry.date
    }

val TransactionItem.amountLKR: Double
    get() = when (this) {
        is TransactionItem.Income -> entry.amountLKR
        is TransactionItem.Expense -> entry.amount // Assuming Expense is always LKR based on model, or add LKR field if needed. 
                                                  // Requirement says amountLKR property should exist.
    }

val TransactionItem.isIncome: Boolean
    get() = this is TransactionItem.Income

val TransactionItem.displayName: String
    get() = when (this) {
        is TransactionItem.Income -> {
            when (entry.sourceType) {
                IncomeSource.SALARY -> "Salary"
                IncomeSource.FREELANCE -> entry.projectName ?: "Freelance"
                IncomeSource.ADSENSE -> "AdSense"
                IncomeSource.CRYPTO -> if (entry.coin != null) "Crypto (${entry.coin})" else "Crypto"
            }
        }
        is TransactionItem.Expense -> {
            when (entry.category) {
                ExpenseCategory.FOOD -> "Food & Dining"
                ExpenseCategory.TRANSPORT -> {
                    if (entry.paymentMethod == PaymentMethod.PICKME || entry.paymentMethod == PaymentMethod.UBEREATS) {
                        entry.paymentMethod.displayName
                    } else {
                        entry.category.displayName
                    }
                }
                else -> entry.category.displayName
            }
        }
    }

val TransactionItem.displayCategory: String
    get() = when (this) {
        is TransactionItem.Income -> entry.sourceType.displayName
        is TransactionItem.Expense -> entry.category.displayName
    }

val TransactionItem.isCommitted: Boolean
    get() = when (this) {
        is TransactionItem.Income -> false
        is TransactionItem.Expense -> entry.expenseType == ExpenseType.COMMITTED
    }
