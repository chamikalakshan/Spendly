package com.spendly.financetracker.data.repository

import com.spendly.financetracker.data.model.UserSession
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val isFirebaseConfigured: Boolean

    fun observeSession(): Flow<UserSession?>

    suspend fun signIn(email: String, password: String): Result<Unit>

    suspend fun signInWithGoogle(idToken: String): Result<Unit>

    suspend fun createAccount(
        name: String,
        email: String,
        password: String,
        defaultCurrency: String = "LKR"
    ): Result<Unit>

    suspend fun sendPasswordResetEmail(email: String): Result<Unit>

    suspend fun updatePassword(currentPassword: String, newPassword: String): Result<Unit>

    suspend fun deleteAccount(currentPassword: String): Result<Unit>

    fun signOut()

    fun getCurrentUserId(): String?
}
