package com.spendly.financetracker.data.repository

import com.spendly.financetracker.data.model.Budget
import com.spendly.financetracker.data.model.BudgetDraft
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    fun observeBudgets(userId: String): Flow<List<Budget>>
    fun observeBudgetsForMonth(userId: String, monthStartMillis: Long): Flow<List<Budget>>
    suspend fun saveBudget(userId: String, draft: BudgetDraft): Result<Unit>
    suspend fun deleteBudget(id: String): Result<Unit>
    suspend fun syncWithFirestore(userId: String)
}
