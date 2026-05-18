package com.spendly.app.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.spendly.app.data.model.User
import com.spendly.app.data.model.enums.Currency
import com.spendly.app.repository.AuthRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    private var cachedUser: User? = null

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("Login failed: No UID")
            
            val userDoc = firestore.collection("users").document(uid).get().await()
            val user = userDoc.toObject(User::class.java) ?: throw Exception("User data not found")
            cachedUser = user
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(
        name: String,
        email: String,
        password: String,
        defaultCurrency: Currency
    ): Result<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("Registration failed: No UID")
            
            val user = User(
                uid = uid,
                name = name,
                email = email,
                defaultCurrency = defaultCurrency,
                usdToLkrRate = 320.5,
                createdAt = System.currentTimeMillis()
            )
            
            firestore.collection("users").document(uid).set(user).await()
            cachedUser = user
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun logout() {
        cachedUser = null
        auth.signOut()
    }

    override fun getCurrentUserId(): String? = auth.currentUser?.uid

    override fun getCurrentUser(): User? {
        cachedUser?.let { return it }

        val firebaseUser = auth.currentUser ?: return null
        return User(
            uid = firebaseUser.uid,
            name = firebaseUser.displayName
                ?: firebaseUser.email?.substringBefore("@")
                ?: "Spendly User",
            email = firebaseUser.email.orEmpty(),
            defaultCurrency = Currency.LKR,
            usdToLkrRate = 320.5,
            createdAt = 0L
        ).also { cachedUser = it }
    }

    override fun isLoggedIn(): Boolean = auth.currentUser != null
}
