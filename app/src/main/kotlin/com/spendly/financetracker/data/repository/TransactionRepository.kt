package com.spendly.financetracker.data.repository

import com.spendly.financetracker.data.model.FinanceTransaction
import com.spendly.financetracker.data.model.TransactionDraft
import com.spendly.financetracker.data.model.TransactionType
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun observeTransactions(userId: String): Flow<List<FinanceTransaction>>

    suspend fun getTransaction(id: String, type: TransactionType? = null): FinanceTransaction?

    suspend fun addTransaction(userId: String, draft: TransactionDraft): Result<Unit>

    suspend fun updateTransaction(id: String, draft: TransactionDraft): Result<Unit>

    suspend fun deleteTransaction(transaction: FinanceTransaction): Result<Unit>

    suspend fun syncWithFirestore(userId: String)
}
