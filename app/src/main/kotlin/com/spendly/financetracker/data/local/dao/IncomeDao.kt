package com.spendly.financetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.spendly.financetracker.data.local.entity.IncomeEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IncomeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: IncomeEntryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<IncomeEntryEntity>)

    @Update
    suspend fun update(entity: IncomeEntryEntity)

    @Query("DELETE FROM income_entries WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM income_entries WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): IncomeEntryEntity?

    @Query("SELECT * FROM income_entries WHERE userId = :userId ORDER BY dateMillis DESC")
    fun observeByUser(userId: String): Flow<List<IncomeEntryEntity>>

    @Query("SELECT * FROM income_entries WHERE userId = :userId AND dateMillis BETWEEN :startMillis AND :endMillis ORDER BY dateMillis DESC")
    fun observeByMonth(userId: String, startMillis: Long, endMillis: Long): Flow<List<IncomeEntryEntity>>

    @Query("SELECT COUNT(*) FROM income_entries WHERE userId = :userId AND dateMillis >= :startMillis AND dateMillis < :endMillis")
    suspend fun countByDateRange(userId: String, startMillis: Long, endMillis: Long): Int

    @Query("SELECT * FROM income_entries WHERE isSynced = 0 AND userId = :userId")
    suspend fun getUnsynced(userId: String): List<IncomeEntryEntity>

    @Query("UPDATE income_entries SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)

    @Query("SELECT COUNT(*) FROM income_entries WHERE userId = :userId AND recurringRuleId = :ruleId AND recurringPeriodKey = :periodKey")
    suspend fun countGenerated(userId: String, ruleId: String, periodKey: String): Int
}
