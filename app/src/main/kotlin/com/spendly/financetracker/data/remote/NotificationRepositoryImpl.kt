package com.spendly.financetracker.data.remote

import com.spendly.financetracker.data.local.dao.NotificationDao
import com.spendly.financetracker.data.local.entity.NotificationEntity
import com.spendly.financetracker.data.model.AppNotification
import com.spendly.financetracker.data.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val dao: NotificationDao
) : NotificationRepository {
    override fun observeNotifications(userId: String): Flow<List<AppNotification>> =
        dao.observeByUser(userId).map { rows -> rows.map { it.toModel() } }

    override suspend fun getNotification(id: String): AppNotification? = dao.getById(id)?.toModel()

    override suspend fun upsert(notification: AppNotification) {
        dao.upsert(notification.toEntity())
    }

    override suspend fun markAllRead(userId: String) = dao.markAllRead(userId)

    override suspend fun deleteNotification(id: String) = dao.deleteById(id)

    private fun NotificationEntity.toModel(): AppNotification = AppNotification(
        id = id,
        userId = userId,
        title = title,
        body = body,
        type = type,
        isRead = isRead,
        createdAtMillis = createdAtMillis
    )

    private fun AppNotification.toEntity(): NotificationEntity = NotificationEntity(
        id = id,
        userId = userId,
        title = title,
        body = body,
        type = type,
        isRead = isRead,
        createdAtMillis = createdAtMillis
    )
}
