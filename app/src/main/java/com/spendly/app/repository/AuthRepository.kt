package com.spendly.app.repository

import com.spendly.app.data.model.User

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(name: String, email: String, password: String): Result<User>
    fun logout()
    fun getCurrentUserId(): String?
    fun getCurrentUser(): User?
    fun isLoggedIn(): Boolean
}
