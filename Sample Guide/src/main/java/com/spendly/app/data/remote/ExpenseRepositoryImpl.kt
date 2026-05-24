package com.spendly.app.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.spendly.app.data.local.dao.ExpenseDao
import com.spendly.app.data.model.ExpenseEntry
import com.spendly.app.repository.ExpenseRepository
import com.spendly.app.utils.toEntity
import com.spendly.app.utils.toModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val expenseDao: ExpenseDao
) : ExpenseRepository {

    override suspend fun addExpense(entry: ExpenseEntry): Result<Unit> {
        return try {
            val id = UUID.randomUUID().toString()
            val finalEntry = entry.copy(id = id, isSynced = false)
            
            expenseDao.insert(finalEntry.toEntity())
            
            try {
                firestore.collection("users")
                    .document(finalEntry.userId)
                    .collection("expenses")
                    .document(id)
                    .set(finalEntry)
                    .await()
                
                expenseDao.markAsSynced(id)
            } catch (e: Exception) { }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateExpense(entry: ExpenseEntry): Result<Unit> {
        return try {
            val updatedEntry = entry.copy(isSynced = false)
            expenseDao.update(updatedEntry.toEntity())
            
            try {
                firestore.collection("users")
                    .document(updatedEntry.userId)
                    .collection("expenses")
                    .document(updatedEntry.id)
                    .set(updatedEntry)
                    .await()
                
                expenseDao.markAsSynced(updatedEntry.id)
            } catch (e: Exception) { }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteExpense(id: String): Result<Unit> {
        return try {
            val existing = expenseDao.getById(id)
            expenseDao.deleteById(id)

            existing?.let { entity ->
                try {
                    firestore.collection("users")
                        .document(entity.userId)
                        .collection("expenses")
                        .document(id)
                        .delete()
                        .await()
                } catch (e: Exception) { }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getAllExpenses(userId: String): Flow<List<ExpenseEntry>> {
        return expenseDao.getAllByUser(userId).map { list ->
            list.map { it.toModel() }
        }
    }

    override fun getMonthlyExpenses(userId: String, startMs: Long, endMs: Long): Flow<List<ExpenseEntry>> {
        return expenseDao.getByMonth(userId, startMs, endMs).map { list ->
            list.map { it.toModel() }
        }
    }

    override suspend fun syncUnsyncedToFirestore(userId: String) {
        try {
            val unsynced = expenseDao.getUnsynced(userId)
            unsynced.forEach { entity ->
                val model = entity.toModel()
                firestore.collection("users")
                    .document(userId)
                    .collection("expenses")
                    .document(model.id)
                    .set(model)
                    .await()
                expenseDao.markAsSynced(model.id)
            }
        } catch (e: Exception) { }
    }
}
