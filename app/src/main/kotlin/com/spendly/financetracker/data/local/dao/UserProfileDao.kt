package com.spendly.financetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.spendly.financetracker.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: UserProfileEntity)

    @Query("SELECT * FROM user_profiles WHERE uid = :uid LIMIT 1")
    suspend fun getById(uid: String): UserProfileEntity?

    @Query("SELECT * FROM user_profiles WHERE uid = :uid LIMIT 1")
    fun observeById(uid: String): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profiles WHERE isSynced = 0 AND uid = :uid")
    suspend fun getUnsynced(uid: String): List<UserProfileEntity>

    @Query("UPDATE user_profiles SET isSynced = 1 WHERE uid = :uid")
    suspend fun markAsSynced(uid: String)
}
