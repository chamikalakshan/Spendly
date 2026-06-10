package com.spendly.financetracker.data.service

import com.spendly.financetracker.data.local.dao.SyncMetadataDao
import com.spendly.financetracker.data.local.entity.SyncMetadataEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncMetadataStore @Inject constructor(
    private val dao: SyncMetadataDao
) {
    suspend fun lastPullMillis(userId: String, collectionName: String): Long =
        dao.get(userId, collectionName)?.lastPullMillis ?: 0L

    suspend fun markSyncing(userId: String, collectionName: String) {
        val current = dao.get(userId, collectionName)
        dao.upsert(
            current?.copy(isSyncing = true, lastError = null)
                ?: SyncMetadataEntity(userId, collectionName, isSyncing = true)
        )
    }

    suspend fun markSuccess(userId: String, collectionName: String, lastPullMillis: Long) {
        val now = System.currentTimeMillis()
        val current = dao.get(userId, collectionName)
        dao.upsert(
            (current ?: SyncMetadataEntity(userId, collectionName)).copy(
                lastPullMillis = maxOf(current?.lastPullMillis ?: 0L, lastPullMillis),
                lastSuccessfulSyncMillis = now,
                lastError = null,
                isSyncing = false
            )
        )
    }

    suspend fun markFailure(userId: String, collectionName: String, error: Throwable) {
        val current = dao.get(userId, collectionName)
        dao.upsert(
            (current ?: SyncMetadataEntity(userId, collectionName)).copy(
                lastError = error.message ?: error::class.java.simpleName,
                isSyncing = false
            )
        )
    }
}
