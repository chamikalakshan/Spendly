package com.spendly.financetracker.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.spendly.financetracker.data.local.dao.BudgetDao
import com.spendly.financetracker.data.local.entity.BudgetEntity
import com.spendly.financetracker.data.model.Budget
import com.spendly.financetracker.data.model.BudgetDraft
import com.spendly.financetracker.data.repository.BudgetRepository
import com.spendly.financetracker.data.service.SyncMetadataStore
import com.spendly.financetracker.util.toModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val budgetDao: BudgetDao,
    private val syncMetadataStore: SyncMetadataStore
) : BudgetRepository {
    override fun observeBudgets(userId: String): Flow<List<Budget>> =
        budgetDao.observeByUser(userId).map { rows -> rows.map { it.toModel() } }

    override fun observeBudgetsForMonth(userId: String, monthStartMillis: Long): Flow<List<Budget>> =
        budgetDao.observeByMonth(userId, monthStartMillis).map { rows -> rows.map { it.toModel() } }

    override suspend fun saveBudget(userId: String, draft: BudgetDraft): Result<Unit> = runCatching {
        require(draft.category.isNotBlank()) { "Select a category" }
        require(draft.monthStartMillis > 0L) { "Select a month" }
        require(draft.limitCents > 0L) { "Enter a valid budget limit" }
        val now = System.currentTimeMillis()
        val existing = draft.id?.let { budgetDao.getById(it) }
            ?: budgetDao.getActiveForCategoryMonth(userId, draft.category, draft.monthStartMillis)
        val entity = BudgetEntity(
            id = existing?.id ?: UUID.randomUUID().toString(),
            userId = userId,
            category = draft.category.trim(),
            monthStartMillis = draft.monthStartMillis,
            limitCents = draft.limitCents,
            defaultCurrency = draft.defaultCurrency.ifBlank { "LKR" },
            alertThresholdPercent = draft.alertThresholdPercent.coerceIn(1, 100),
            note = draft.note.trim(),
            isSynced = false,
            createdAtMillis = existing?.createdAtMillis ?: now,
            updatedAtMillis = now,
            deletedAtMillis = null
        )
        budgetDao.upsert(entity)
        syncOne(entity)
    }

    override suspend fun deleteBudget(id: String): Result<Unit> = runCatching {
        val existing = budgetDao.getById(id) ?: return@runCatching
        val deleted = existing.copy(
            deletedAtMillis = System.currentTimeMillis(),
            updatedAtMillis = System.currentTimeMillis(),
            isSynced = false
        )
        budgetDao.upsert(deleted)
        syncOne(deleted)
    }

    override suspend fun syncWithFirestore(userId: String) {
        val collection = "budgets"
        syncMetadataStore.markSyncing(userId, collection)
        try {
            budgetDao.getUnsynced(userId).forEach { syncOne(it) }
            val lastPull = syncMetadataStore.lastPullMillis(userId, collection)
            val remoteRef = firestore.collection("users").document(userId).collection(collection)
            val snapshot = if (lastPull > 0L) {
                remoteRef.whereGreaterThan("updatedAtMillis", lastPull).get().await()
            } else {
                remoteRef.get().await()
            }
            var maxRemoteUpdatedAt = lastPull
            val remoteRows = snapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                BudgetEntity(
                    id = doc.id,
                    userId = userId,
                    category = data["category"] as? String ?: return@mapNotNull null,
                    monthStartMillis = (data["monthStartMillis"] as? Number)?.toLong() ?: return@mapNotNull null,
                    limitCents = (data["limitCents"] as? Number)?.toLong() ?: return@mapNotNull null,
                    defaultCurrency = data["defaultCurrency"] as? String ?: "LKR",
                    alertThresholdPercent = (data["alertThresholdPercent"] as? Number)?.toInt() ?: 80,
                    note = data["note"] as? String,
                    isSynced = true,
                    createdAtMillis = (data["createdAtMillis"] as? Number)?.toLong() ?: 0L,
                    updatedAtMillis = (data["updatedAtMillis"] as? Number)?.toLong() ?: 0L,
                    deletedAtMillis = (data["deletedAtMillis"] as? Number)?.toLong()
                ).also { maxRemoteUpdatedAt = maxOf(maxRemoteUpdatedAt, it.updatedAtMillis) }
            }.filter { remote ->
                val local = budgetDao.getById(remote.id)
                local == null || remote.updatedAtMillis >= local.updatedAtMillis
            }
            budgetDao.upsertAll(remoteRows)
            syncMetadataStore.markSuccess(userId, collection, maxRemoteUpdatedAt)
        } catch (error: Exception) {
            syncMetadataStore.markFailure(userId, collection, error)
            throw error
        }
    }

    private suspend fun syncOne(entity: BudgetEntity) {
        firestore.collection("users").document(entity.userId)
            .collection("budgets").document(entity.id)
            .set(entity.toFirestoreMap())
            .await()
        budgetDao.markAsSynced(entity.id)
    }

    private fun BudgetEntity.toFirestoreMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "userId" to userId,
        "category" to category,
        "monthStartMillis" to monthStartMillis,
        "limitCents" to limitCents,
        "defaultCurrency" to defaultCurrency,
        "alertThresholdPercent" to alertThresholdPercent,
        "note" to note,
        "createdAtMillis" to createdAtMillis,
        "updatedAtMillis" to updatedAtMillis,
        "deletedAtMillis" to deletedAtMillis
    )
}
