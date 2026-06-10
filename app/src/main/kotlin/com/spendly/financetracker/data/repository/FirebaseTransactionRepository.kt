package com.spendly.financetracker.data.repository

import com.spendly.financetracker.data.model.FinanceTransaction
import com.spendly.financetracker.data.model.TransactionDraft
import com.spendly.financetracker.data.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseTransactionRepository @Inject constructor(
    private val incomeRepository: IncomeRepository,
    private val expenseRepository: ExpenseRepository
) : TransactionRepository {
    override fun observeTransactions(userId: String): Flow<List<FinanceTransaction>> =
        combine(
            incomeRepository.observeIncome(userId),
            expenseRepository.observeExpenses(userId)
        ) { income, expenses ->
            (income + expenses).sortedByDescending { it.dateMillis }
        }

    override suspend fun getTransaction(id: String, type: TransactionType?): FinanceTransaction? =
        when (type) {
            TransactionType.INCOME -> incomeRepository.getIncome(id)
            TransactionType.EXPENSE -> expenseRepository.getExpense(id)
            null -> incomeRepository.getIncome(id) ?: expenseRepository.getExpense(id)
        }

    override suspend fun addTransaction(userId: String, draft: TransactionDraft): Result<Unit> =
        when (draft.type) {
            TransactionType.INCOME -> incomeRepository.addIncome(userId, draft)
            TransactionType.EXPENSE -> expenseRepository.addExpense(userId, draft)
        }

    override suspend fun updateTransaction(id: String, draft: TransactionDraft): Result<Unit> =
        when (draft.type) {
            TransactionType.INCOME -> incomeRepository.updateIncome(id, draft)
            TransactionType.EXPENSE -> expenseRepository.updateExpense(id, draft)
        }

    override suspend fun deleteTransaction(transaction: FinanceTransaction): Result<Unit> =
        when (transaction.type) {
            TransactionType.INCOME -> incomeRepository.deleteIncome(transaction.id)
            TransactionType.EXPENSE -> expenseRepository.deleteExpense(transaction.id)
        }

    override suspend fun syncWithFirestore(userId: String) {
        incomeRepository.syncWithFirestore(userId)
        expenseRepository.syncWithFirestore(userId)
    }
}
