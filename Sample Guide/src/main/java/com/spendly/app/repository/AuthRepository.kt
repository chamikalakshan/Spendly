package com.spendly.app.repository

import com.spendly.app.data.model.User
import com.spendly.app.data.model.enums.Currency

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(
        name: String,
        email: String,
        password: String,
        defaultCurrency: Currency = Currency.LKR
    ): Result<User>
    fun logout()
    fun getCurrentUserId(): String?
    fun getCurrentUser(): User?
    fun isLoggedIn(): Boolean
}
