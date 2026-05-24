package com.spendly.app.data.model

data class SavingsGoal(
    val id: String = "",
    val userId: String = "",
    val goalName: String = "",
    val targetAmount: Double = 0.0,
    val targetDate: Long = 0L,
    val isSynced: Boolean = false,
    val createdAt: Long = 0L
)
