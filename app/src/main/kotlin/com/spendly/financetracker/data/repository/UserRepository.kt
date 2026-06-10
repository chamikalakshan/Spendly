package com.spendly.financetracker.data.repository

import com.spendly.financetracker.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun observeProfile(uid: String): Flow<UserProfile?>
    suspend fun upsertProfile(profile: UserProfile): Result<Unit>
    suspend fun syncWithFirestore(uid: String)
}
