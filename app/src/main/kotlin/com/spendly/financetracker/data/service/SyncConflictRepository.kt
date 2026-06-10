package com.spendly.financetracker.data.service

import com.spendly.financetracker.data.local.dao.SyncConflictDao
import com.spendly.financetracker.data.local.entity.SyncConflictEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncConflictRepository @Inject constructor(
    private val syncConflictDao: SyncConflictDao
) {
    fun observeOpen(userId: String): Flow<List<SyncConflictEntity>> = syncConflictDao.observeOpen(userId)

    suspend fun countOpen(userId: String): Int = syncConflictDao.countOpen(userId)

    suspend fun record(
        userId: String,
        collectionName: String,
        documentId: String,
        localUpdatedAtMillis: Long,
        remoteUpdatedAtMillis: Long
    ) {
        syncConflictDao.upsert(
            SyncConflictEntity(
                id = "$collectionName-$documentId-${UUID.randomUUID()}",
                userId = userId,
                collectionName = collectionName,
                documentId = documentId,
                localUpdatedAtMillis = localUpdatedAtMillis,
                remoteUpdatedAtMillis = remoteUpdatedAtMillis,
                status = "OPEN",
                createdAtMillis = System.currentTimeMillis()
            )
        )
    }
}
