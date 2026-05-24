package com.spendly.app.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.spendly.app.data.local.dao.IncomeDao
import com.spendly.app.data.model.IncomeEntry
import com.spendly.app.repository.IncomeRepository
import com.spendly.app.utils.toEntity
import com.spendly.app.utils.toModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IncomeRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val incomeDao: IncomeDao
) : IncomeRepository {

    override suspend fun addIncome(entry: IncomeEntry): Result<Unit> {
        return try {
            val id = UUID.randomUUID().toString()
            val finalEntry = entry.copy(id = id, isSynced = false)
            
            // 1. Insert to Room
            incomeDao.insert(finalEntry.toEntity())
            
            // 2. Try Firestore
            try {
                firestore.collection("users")
                    .document(finalEntry.userId)
                    .collection("income")
                    .document(id)
                    .set(finalEntry)
                    .await()
                
                incomeDao.markAsSynced(id)
            } catch (e: Exception) {
                // Fail silently, isSynced remains false for background sync
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateIncome(entry: IncomeEntry): Result<Unit> {
        return try {
            val updatedEntry = entry.copy(isSynced = false)
            incomeDao.update(updatedEntry.toEntity())
            
            try {
                firestore.collection("users")
                    .document(updatedEntry.userId)
                    .collection("income")
                    .document(updatedEntry.id)
                    .set(updatedEntry)
                    .await()
                
                incomeDao.markAsSynced(updatedEntry.id)
            } catch (e: Exception) { }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteIncome(id: String): Result<Unit> {
        return try {
            val existing = incomeDao.getById(id)
            incomeDao.deleteById(id)

            existing?.let { entity ->
                try {
                    firestore.collection("users")
                        .document(entity.userId)
                        .collection("income")
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

    override fun getAllIncome(userId: String): Flow<List<IncomeEntry>> {
        return incomeDao.getAllByUser(userId).map { list ->
            list.map { it.toModel() }
        }
    }

    override fun getMonthlyIncome(userId: String, startMs: Long, endMs: Long): Flow<List<IncomeEntry>> {
        return incomeDao.getByMonth(userId, startMs, endMs).map { list ->
            list.map { it.toModel() }
        }
    }

    override suspend fun syncUnsyncedToFirestore(userId: String) {
        try {
            val unsynced = incomeDao.getUnsynced(userId)
            unsynced.forEach { entity ->
                val model = entity.toModel()
                firestore.collection("users")
                    .document(userId)
                    .collection("income")
                    .document(model.id)
                    .set(model)
                    .await()
                incomeDao.markAsSynced(model.id)
            }
        } catch (e: Exception) { }
    }
}
