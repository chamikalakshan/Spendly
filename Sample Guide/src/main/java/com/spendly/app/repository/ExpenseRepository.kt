package com.spendly.app.repository

import com.spendly.app.data.model.ExpenseEntry
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    suspend fun addExpense(entry: ExpenseEntry): Result<Unit>
    suspend fun updateExpense(entry: ExpenseEntry): Result<Unit>
    suspend fun deleteExpense(id: String): Result<Unit>
    fun getAllExpenses(userId: String): Flow<List<ExpenseEntry>>
    fun getMonthlyExpenses(
        userId: String, startMs: Long, endMs: Long
    ): Flow<List<ExpenseEntry>>
    suspend fun syncUnsyncedToFirestore(userId: String)
}
