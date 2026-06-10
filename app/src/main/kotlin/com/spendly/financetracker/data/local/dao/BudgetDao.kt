package com.spendly.financetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.spendly.financetracker.data.local.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: BudgetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<BudgetEntity>)

    @Query("SELECT * FROM budget_entries WHERE userId = :userId AND deletedAtMillis IS NULL ORDER BY monthStartMillis DESC, category ASC")
    fun observeByUser(userId: String): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budget_entries WHERE userId = :userId AND monthStartMillis = :monthStartMillis AND deletedAtMillis IS NULL ORDER BY category ASC")
    fun observeByMonth(userId: String, monthStartMillis: Long): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budget_entries WHERE userId = :userId AND monthStartMillis = :monthStartMillis AND deletedAtMillis IS NULL ORDER BY category ASC")
    suspend fun getByMonth(userId: String, monthStartMillis: Long): List<BudgetEntity>

    @Query("SELECT * FROM budget_entries WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): BudgetEntity?

    @Query("SELECT * FROM budget_entries WHERE userId = :userId AND category = :category AND monthStartMillis = :monthStartMillis AND deletedAtMillis IS NULL LIMIT 1")
    suspend fun getActiveForCategoryMonth(userId: String, category: String, monthStartMillis: Long): BudgetEntity?

    @Query("SELECT * FROM budget_entries WHERE userId = :userId AND isSynced = 0")
    suspend fun getUnsynced(userId: String): List<BudgetEntity>

    @Query("UPDATE budget_entries SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)
}
