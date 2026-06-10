package com.spendly.financetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.spendly.financetracker.data.local.entity.SyncMetadataEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncMetadataDao {
    @Query("SELECT * FROM sync_metadata WHERE userId = :userId AND collectionName = :collectionName LIMIT 1")
    suspend fun get(userId: String, collectionName: String): SyncMetadataEntity?

    @Query("SELECT * FROM sync_metadata WHERE userId = :userId ORDER BY collectionName")
    fun observeByUser(userId: String): Flow<List<SyncMetadataEntity>>

    @Upsert
    suspend fun upsert(entity: SyncMetadataEntity)
}
