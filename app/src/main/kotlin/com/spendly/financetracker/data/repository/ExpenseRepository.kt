package com.spendly.financetracker.data.repository

import com.spendly.financetracker.data.model.FinanceTransaction
import com.spendly.financetracker.data.model.TransactionDraft
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    fun observeExpenses(userId: String): Flow<List<FinanceTransaction>>
    fun observeMonthlyExpenses(userId: String, startMillis: Long, endMillis: Long): Flow<List<FinanceTransaction>>
    suspend fun getExpense(id: String): FinanceTransaction?
    suspend fun addExpense(userId: String, draft: TransactionDraft): Result<Unit>
    suspend fun updateExpense(id: String, draft: TransactionDraft): Result<Unit>
    suspend fun deleteExpense(id: String): Result<Unit>
    suspend fun syncWithFirestore(userId: String)
}
