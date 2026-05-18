package com.spendly.app.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.spendly.app.data.local.dao.GoalDao
import com.spendly.app.data.model.SavingsGoal
import com.spendly.app.repository.GoalRepository
import com.spendly.app.utils.toEntity
import com.spendly.app.utils.toModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoalRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val goalDao: GoalDao
) : GoalRepository {

    override suspend fun saveGoal(goal: SavingsGoal): Result<Unit> {
        return try {
            val id = if (goal.id.isEmpty()) UUID.randomUUID().toString() else goal.id
            val finalGoal = goal.copy(id = id, isSynced = false)
            
            goalDao.insert(finalGoal.toEntity())
            
            try {
                firestore.collection("users")
                    .document(finalGoal.userId)
                    .collection("goals")
                    .document(id)
                    .set(finalGoal)
                    .await()
                
                goalDao.markAsSynced(id)
            } catch (e: Exception) { }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateGoal(goal: SavingsGoal): Result<Unit> {
        return saveGoal(goal) // Same logic for update in this case
    }

    override suspend fun deleteGoal(id: String): Result<Unit> {
        return try {
            val existing = goalDao.getById(id)
            goalDao.deleteById(id)

            existing?.let { entity ->
                try {
                    firestore.collection("users")
                        .document(entity.userId)
                        .collection("goals")
                        .document(id)
                        .delete()
                        .await()
                } catch (e: Exception) { }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getGoals(userId: String): Flow<List<SavingsGoal>> {
        return goalDao.getAllByUser(userId).map { list ->
            list.map { it.toModel() }
        }
    }

    override suspend fun syncUnsyncedToFirestore(userId: String) {
        try {
            val unsynced = goalDao.getUnsynced(userId)
            unsynced.forEach { entity ->
                val model = entity.toModel()
                firestore.collection("users")
                    .document(userId)
                    .collection("goals")
                    .document(model.id)
                    .set(model)
                    .await()
                goalDao.markAsSynced(model.id)
            }
        } catch (e: Exception) { }
    }
}
