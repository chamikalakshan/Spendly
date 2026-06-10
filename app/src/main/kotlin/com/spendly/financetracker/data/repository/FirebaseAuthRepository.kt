package com.spendly.financetracker.data.repository

import android.content.Context
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.spendly.financetracker.data.firebase.FirebaseBootstrap
import com.spendly.financetracker.data.local.dao.UserProfileDao
import com.spendly.financetracker.data.local.db.SpendlyDatabase
import com.spendly.financetracker.data.local.entity.UserProfileEntity
import com.spendly.financetracker.data.model.UserSession
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val database: SpendlyDatabase,
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

    override suspend fun signInWithGoogle(idToken: String): Result<Unit> = runCatching {
        require(idToken.isNotBlank()) { "Google sign-in did not return a valid account token." }
        val result = auth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null)).await()
        val user = result.user ?: error("Google sign-in failed.")
        if (result.additionalUserInfo?.isNewUser == true) {
            createAndSyncProfile(
                user = user,
                name = user.displayName.orEmpty(),
                email = user.email.orEmpty(),
                defaultCurrency = "LKR",
                profileImageUri = user.photoUrl?.toString()
            )
        }
    }

    override suspend fun createAccount(
        name: String,
        email: String,
        password: String,
        defaultCurrency: String
    ): Result<Unit> = runCatching {
        val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
        val user = result.user ?: error("Registration failed")
        createAndSyncProfile(
            user = user,
            name = name,
            email = email,
            defaultCurrency = defaultCurrency,
            profileImageUri = null
        )
    }

    private suspend fun createAndSyncProfile(
        user: FirebaseUser,
        name: String,
        email: String,
        defaultCurrency: String,
        profileImageUri: String?
    ) {
        val uid = user.uid
        val now = System.currentTimeMillis()
        val profile = UserProfileEntity(
            uid = uid,
            name = name.trim().ifBlank {
                email.substringBefore("@").replaceFirstChar { character -> character.uppercase() }
            },
            email = email.trim(),
            defaultCurrency = defaultCurrency.trim().ifBlank { "LKR" }.uppercase(),
            createdAtMillis = now,
            updatedAtMillis = now,
            isSynced = false,
            profileImageUri = profileImageUri,
            exchangeRateSettings = "",
            notificationFrequency = null,
            reminderTime = null,
            categorySettingsJson = "",
            themeMode = "SYSTEM",
            budgetAlertsEnabled = true,
            budgetAlertThresholdPercent = 80,
            profileImageStoragePath = null,
            accentColorKey = "GREEN",
            dailyRemindersEnabled = false,
            remindExpenses = true,
            remindIncome = true,
            smartReminderMode = true
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
                    "categorySettingsJson" to profile.categorySettingsJson,
                    "themeMode" to profile.themeMode,
                    "budgetAlertsEnabled" to profile.budgetAlertsEnabled,
                    "budgetAlertThresholdPercent" to profile.budgetAlertThresholdPercent,
                    "profileImageStoragePath" to profile.profileImageStoragePath,
                    "accentColorKey" to profile.accentColorKey,
                    "dailyRemindersEnabled" to profile.dailyRemindersEnabled,
                    "remindExpenses" to profile.remindExpenses,
                    "remindIncome" to profile.remindIncome,
                    "smartReminderMode" to profile.smartReminderMode
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
        USER_COLLECTIONS.forEach { collection -> deleteCollection(uid, collection) }
        runCatching {
            storage.reference.child("users/$uid/profile/profile.jpg").delete().await()
        }
        user.delete().await()
        withContext(Dispatchers.IO) { database.clearAllTables() }
    }

    override fun signOut() {
        auth.signOut()
    }

    override fun getCurrentUserId(): String? = auth.currentUser?.uid

    private fun FirebaseUser.toSession(): UserSession =
        UserSession(uid = uid, email = email)

    private suspend fun deleteCollection(uid: String, collectionName: String) {
        val collection = firestore.collection("users").document(uid).collection(collectionName)
        while (true) {
            val documents = collection.limit(DELETE_BATCH_SIZE.toLong()).get().await().documents
            if (documents.isEmpty()) return
            val batch = firestore.batch()
            documents.forEach { batch.delete(it.reference) }
            batch.commit().await()
        }
    }

    private companion object {
        const val DELETE_BATCH_SIZE = 450
        val USER_COLLECTIONS = listOf(
            "profile",
            "income",
            "expenses",
            "goals",
            "budgets",
            "recurringRules",
            "recurringInstances",
            "notifications"
        )
    }
}
