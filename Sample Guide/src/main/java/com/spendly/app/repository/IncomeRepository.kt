package com.spendly.app.repository

import com.spendly.app.data.model.IncomeEntry
import kotlinx.coroutines.flow.Flow

interface IncomeRepository {
    suspend fun addIncome(entry: IncomeEntry): Result<Unit>
    suspend fun updateIncome(entry: IncomeEntry): Result<Unit>
    suspend fun deleteIncome(id: String): Result<Unit>
    fun getAllIncome(userId: String): Flow<List<IncomeEntry>>
    fun getMonthlyIncome(
        userId: String, startMs: Long, endMs: Long
    ): Flow<List<IncomeEntry>>
    suspend fun syncUnsyncedToFirestore(userId: String)
}
