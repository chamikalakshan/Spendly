package com.spendly.financetracker.data.repository

import com.spendly.financetracker.data.model.SavingsGoal
import kotlinx.coroutines.flow.Flow

interface GoalRepository {
    fun observeGoals(userId: String): Flow<List<SavingsGoal>>
    suspend fun getGoal(id: String): SavingsGoal?
    suspend fun saveGoal(goal: SavingsGoal): Result<Unit>
    suspend fun deleteGoal(id: String): Result<Unit>
    suspend fun addSavings(goalId: String, amountCents: Long): Result<Unit>
    suspend fun syncWithFirestore(userId: String)
}
