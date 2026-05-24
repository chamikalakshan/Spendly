package com.spendly.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "savings_goals",
    indices = [Index("userId")]
)
data class SavingsGoalEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val goalName: String,
    val targetAmount: Double,
    val targetDate: Long,
    val isSynced: Boolean = false,
    val createdAt: Long
)
