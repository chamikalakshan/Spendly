package com.spendly.financetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.spendly.financetracker.data.local.entity.NotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY createdAtMillis DESC")
    fun observeByUser(userId: String): Flow<List<NotificationEntity>>

    @Upsert
    suspend fun upsert(entity: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllRead(userId: String)

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteById(id: String)
}
