package com.spendly.financetracker.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sync_conflicts",
    indices = [
        Index("userId"),
        Index(value = ["userId", "collectionName", "documentId", "status"])
    ]
)
data class SyncConflictEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val collectionName: String,
    val documentId: String,
    val localUpdatedAtMillis: Long,
    val remoteUpdatedAtMillis: Long,
    val status: String,
    val createdAtMillis: Long
)
