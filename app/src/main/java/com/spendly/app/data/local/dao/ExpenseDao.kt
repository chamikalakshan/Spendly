package com.spendly.app.data.local.dao

import androidx.room.*
import com.spendly.app.data.local.entity.ExpenseEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ExpenseEntryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<ExpenseEntryEntity>)

    @Update
    suspend fun update(entity: ExpenseEntryEntity)

    @Delete
    suspend fun delete(entity: ExpenseEntryEntity)

    @Query("DELETE FROM expense_entries WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM expense_entries WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ExpenseEntryEntity?

    @Query("SELECT * FROM expense_entries WHERE userId = :userId ORDER BY date DESC")
    fun getAllByUser(userId: String): Flow<List<ExpenseEntryEntity>>

    @Query("SELECT * FROM expense_entries WHERE userId = :userId AND category = :category ORDER BY date DESC")
    fun getByCategory(userId: String, category: String): Flow<List<ExpenseEntryEntity>>

    @Query("SELECT * FROM expense_entries WHERE userId = :userId AND date BETWEEN :startMs AND :endMs ORDER BY date DESC")
    fun getByMonth(userId: String, startMs: Long, endMs: Long): Flow<List<ExpenseEntryEntity>>

    @Query("SELECT * FROM expense_entries WHERE isSynced = 0 AND userId = :userId")
    suspend fun getUnsynced(userId: String): List<ExpenseEntryEntity>

    @Query("UPDATE expense_entries SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)
}
