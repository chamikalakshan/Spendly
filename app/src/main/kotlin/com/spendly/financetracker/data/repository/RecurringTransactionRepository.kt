package com.spendly.financetracker.data.repository

import com.spendly.financetracker.data.model.RecurringRule
import com.spendly.financetracker.data.model.RecurringRuleDraft
import kotlinx.coroutines.flow.Flow

interface RecurringTransactionRepository {
    fun observeRules(userId: String): Flow<List<RecurringRule>>
    suspend fun saveRule(userId: String, draft: RecurringRuleDraft): Result<Unit>
    suspend fun pauseRule(ruleId: String): Result<Unit>
    suspend fun resumeRule(ruleId: String): Result<Unit>
    suspend fun deleteRule(ruleId: String): Result<Unit>
    suspend fun generateDueTransactions(userId: String, nowMillis: Long = System.currentTimeMillis()): Result<Int>
    suspend fun syncWithFirestore(userId: String)
}
