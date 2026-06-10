package com.spendly.financetracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notifications",
    indices = [Index("userId"), Index(value = ["userId", "createdAtMillis"])]
)
data class NotificationEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val body: String,
    val type: String,
    @ColumnInfo(defaultValue = "0") val isRead: Boolean = false,
    val createdAtMillis: Long
)
