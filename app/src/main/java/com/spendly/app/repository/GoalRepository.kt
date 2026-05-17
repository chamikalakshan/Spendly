package com.spendly.app.repository

import com.spendly.app.data.model.SavingsGoal
import kotlinx.coroutines.flow.Flow

interface GoalRepository {
    suspend fun saveGoal(goal: SavingsGoal): Result<Unit>
    suspend fun updateGoal(goal: SavingsGoal): Result<Unit>
    suspend fun deleteGoal(id: String): Result<Unit>
    fun getGoals(userId: String): Flow<List<SavingsGoal>>
    suspend fun syncUnsyncedToFirestore(userId: String)
}
