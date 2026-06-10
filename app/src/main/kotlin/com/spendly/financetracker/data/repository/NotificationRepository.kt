package com.spendly.financetracker.data.repository

import com.spendly.financetracker.data.model.AppNotification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun observeNotifications(userId: String): Flow<List<AppNotification>>
    suspend fun getNotification(id: String): AppNotification?
    suspend fun upsert(notification: AppNotification)
    suspend fun markAllRead(userId: String)
    suspend fun deleteNotification(id: String)
}
