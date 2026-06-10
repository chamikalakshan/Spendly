package com.spendly.financetracker.data.repository

import com.spendly.financetracker.data.model.FinanceTransaction
import com.spendly.financetracker.data.model.TransactionDraft
import kotlinx.coroutines.flow.Flow

interface IncomeRepository {
    fun observeIncome(userId: String): Flow<List<FinanceTransaction>>
    fun observeMonthlyIncome(userId: String, startMillis: Long, endMillis: Long): Flow<List<FinanceTransaction>>
    suspend fun getIncome(id: String): FinanceTransaction?
    suspend fun addIncome(userId: String, draft: TransactionDraft): Result<Unit>
    suspend fun updateIncome(id: String, draft: TransactionDraft): Result<Unit>
    suspend fun deleteIncome(id: String): Result<Unit>
    suspend fun syncWithFirestore(userId: String)
}
