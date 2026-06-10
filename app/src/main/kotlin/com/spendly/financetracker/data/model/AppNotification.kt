package com.spendly.financetracker.data.model

data class AppNotification(
    val id: String,
    val userId: String,
    val title: String,
    val body: String,
    val type: String,
    val isRead: Boolean,
    val createdAtMillis: Long
)
