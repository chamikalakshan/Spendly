package com.spendly.app.data.local.dao

import androidx.room.*
import com.spendly.app.data.local.entity.IncomeEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IncomeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: IncomeEntryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<IncomeEntryEntity>)

    @Update
    suspend fun update(entity: IncomeEntryEntity)

    @Delete
    suspend fun delete(entity: IncomeEntryEntity)

    @Query("DELETE FROM income_entries WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM income_entries WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): IncomeEntryEntity?

    @Query("SELECT * FROM income_entries WHERE userId = :userId ORDER BY date DESC")
    fun getAllByUser(userId: String): Flow<List<IncomeEntryEntity>>

    @Query("SELECT * FROM income_entries WHERE userId = :userId AND date BETWEEN :startMs AND :endMs ORDER BY date DESC")
    fun getByMonth(userId: String, startMs: Long, endMs: Long): Flow<List<IncomeEntryEntity>>

    @Query("SELECT * FROM income_entries WHERE isSynced = 0 AND userId = :userId")
    suspend fun getUnsynced(userId: String): List<IncomeEntryEntity>

    @Query("UPDATE income_entries SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)
}
