package com.spendly.financetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.spendly.financetracker.data.local.entity.SyncConflictEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncConflictDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: SyncConflictEntity)

    @Query("SELECT * FROM sync_conflicts WHERE userId = :userId AND status = 'OPEN' ORDER BY createdAtMillis DESC")
    fun observeOpen(userId: String): Flow<List<SyncConflictEntity>>

    @Query("SELECT COUNT(*) FROM sync_conflicts WHERE userId = :userId AND status = 'OPEN'")
    suspend fun countOpen(userId: String): Int

    @Query("UPDATE sync_conflicts SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)
}
