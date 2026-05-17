package com.spendly.app.data.model

import com.spendly.app.data.model.enums.Currency

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val defaultCurrency: Currency = Currency.LKR,
    val usdToLkrRate: Double = 320.5,
    val createdAt: Long = 0L
)
