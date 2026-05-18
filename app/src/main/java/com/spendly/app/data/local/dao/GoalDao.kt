package com.spendly.app.data.local.dao

import androidx.room.*
import com.spendly.app.data.local.entity.SavingsGoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SavingsGoalEntity)

    @Update
    suspend fun update(entity: SavingsGoalEntity)

    @Query("DELETE FROM savings_goals WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM savings_goals WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): SavingsGoalEntity?

    @Query("SELECT * FROM savings_goals WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllByUser(userId: String): Flow<List<SavingsGoalEntity>>

    @Query("SELECT * FROM savings_goals WHERE isSynced = 0 AND userId = :userId")
    suspend fun getUnsynced(userId: String): List<SavingsGoalEntity>

    @Query("UPDATE savings_goals SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)
}
