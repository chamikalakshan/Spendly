package com.spendly.financetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.spendly.financetracker.data.local.entity.RecurringRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringRuleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: RecurringRuleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<RecurringRuleEntity>)

    @Query("SELECT * FROM recurring_rules WHERE userId = :userId AND deletedAtMillis IS NULL ORDER BY isActive DESC, nextRunDateMillis ASC")
    fun observeByUser(userId: String): Flow<List<RecurringRuleEntity>>

    @Query("SELECT * FROM recurring_rules WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): RecurringRuleEntity?

    @Query("SELECT * FROM recurring_rules WHERE userId = :userId AND isActive = 1 AND deletedAtMillis IS NULL AND nextRunDateMillis <= :nowMillis ORDER BY nextRunDateMillis ASC")
    suspend fun getDueRules(userId: String, nowMillis: Long): List<RecurringRuleEntity>

    @Query("SELECT * FROM recurring_rules WHERE userId = :userId AND isSynced = 0")
    suspend fun getUnsynced(userId: String): List<RecurringRuleEntity>

    @Query("UPDATE recurring_rules SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)
}
