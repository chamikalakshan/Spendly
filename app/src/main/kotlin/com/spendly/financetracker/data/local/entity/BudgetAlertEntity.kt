package com.spendly.financetracker.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "budget_alerts",
    indices = [
        Index("userId"),
        Index(value = ["userId", "budgetId", "monthStartMillis", "thresholdType"], unique = true)
    ]
)
data class BudgetAlertEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val budgetId: String,
    val category: String,
    val monthStartMillis: Long,
    val thresholdType: String,
    val progressPercent: Int,
    val notifiedAtMillis: Long
)
