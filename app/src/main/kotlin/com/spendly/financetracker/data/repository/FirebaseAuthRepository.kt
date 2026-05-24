package com.spendly.financetracker.data.repository

import android.content.Context
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.spendly.financetracker.data.firebase.FirebaseBootstrap
import com.spendly.financetracker.data.local.dao.UserProfileDao
import com.spendly.financetracker.data.local.entity.UserProfileEntity
import com.spendly.financetracker.data.model.UserSession
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userProfileDao: UserProfileDao
) : AuthRepository {
    override val isFirebaseConfigured: Boolean
        get() = FirebaseBootstrap.isConfigured(context)

    override fun observeSession(): Flow<UserSession?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser?.toSession())
        }
        auth.addAuthStateListener(listener)
        trySend(auth.currentUser?.toSession())
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun signIn(email: String, password: String): Result<Unit> = runCatching {
        auth.signInWithEmailAndPassword(email.trim(), password).await()
    }

    override suspend fun createAccount(
        name: String,
        email: String,
        password: String,
        defaultCurrency: String
    ): Result<Unit> = runCatching {
        val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
        val uid = result.user?.uid ?: error("Registration failed")
        val now = System.currentTimeMillis()
        val profile = UserProfileEntity(
            uid = uid,
            name = name.trim(),
            email = email.trim(),
            defaultCurrency = defaultCurrency.trim().ifBlank { "LKR" }.uppercase(),
            createdAtMillis = now,
            updatedAtMillis = now,
            isSynced = false,
            profileImageUri = null,
            exchangeRateSettings = "",
            notificationFrequency = null,
            reminderTime = null,
            categorySettingsJson = ""
        )
        userProfileDao.upsert(profile)
        firestore.collection("users").document(uid).collection("profile").document("main")
            .set(
                mapOf(
                    "uid" to profile.uid,
                    "name" to profile.name,
                    "email" to profile.email,
                    "defaultCurrency" to profile.defaultCurrency,
                    "createdAtMillis" to profile.createdAtMillis,
                    "updatedAtMillis" to profile.updatedAtMillis,
                    "profileImageUri" to profile.profileImageUri,
                    "exchangeRateSettings" to profile.exchangeRateSettings,
                    "notificationFrequency" to profile.notificationFrequency,
                    "reminderTime" to profile.reminderTime,
                    "categorySettingsJson" to profile.categorySettingsJson
                )
            )
            .await()
        userProfileDao.markAsSynced(uid)
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> = runCatching {
        auth.sendPasswordResetEmail(email.trim()).await()
    }

    override suspend fun updatePassword(currentPassword: String, newPassword: String): Result<Unit> = runCatching {
        val user = auth.currentUser ?: error("Please sign in again")
        val email = user.email ?: error("Please sign in again")
        user.reauthenticate(EmailAuthProvider.getCredential(email, currentPassword)).await()
        user.updatePassword(newPassword).await()
    }

    override suspend fun deleteAccount(currentPassword: String): Result<Unit> = runCatching {
        val user = auth.currentUser ?: error("Please sign in again")
        val email = user.email ?: error("Please sign in again")
        user.reauthenticate(EmailAuthProvider.getCredential(email, currentPassword)).await()
        val uid = user.uid
        firestore.collection("users").document(uid).delete().await()
        user.delete().await()
    }

    override fun signOut() {
        auth.signOut()
    }

    override fun getCurrentUserId(): String? = auth.currentUser?.uid

    private fun FirebaseUser.toSession(): UserSession =
        UserSession(uid = uid, email = email)
}
