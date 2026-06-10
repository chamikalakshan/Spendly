package com.spendly.financetracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "sync_metadata",
    primaryKeys = ["userId", "collectionName"],
    indices = [Index("userId")]
)
data class SyncMetadataEntity(
    val userId: String,
    val collectionName: String,
    @ColumnInfo(defaultValue = "0") val lastPullMillis: Long = 0L,
    @ColumnInfo(defaultValue = "0") val lastSuccessfulSyncMillis: Long = 0L,
    val lastError: String? = null,
    @ColumnInfo(defaultValue = "0") val isSyncing: Boolean = false
)
