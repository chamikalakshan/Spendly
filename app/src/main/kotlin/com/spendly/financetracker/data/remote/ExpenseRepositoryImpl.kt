package com.spendly.financetracker.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.spendly.financetracker.data.local.dao.ExpenseDao
import com.spendly.financetracker.data.local.dao.GoalDao
import com.spendly.financetracker.data.local.entity.ExpenseEntryEntity
import com.spendly.financetracker.data.model.FinanceTransaction
import com.spendly.financetracker.data.model.TransactionDraft
import com.spendly.financetracker.data.repository.ExpenseRepository
import com.spendly.financetracker.util.toTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val expenseDao: ExpenseDao,
    private val goalDao: GoalDao
) : ExpenseRepository {
    override fun observeExpenses(userId: String): Flow<List<FinanceTransaction>> =
        expenseDao.observeByUser(userId).map { rows -> rows.map { it.toTransaction() } }

    override fun observeMonthlyExpenses(
        userId: String,
        startMillis: Long,
        endMillis: Long
    ): Flow<List<FinanceTransaction>> =
        expenseDao.observeByMonth(userId, startMillis, endMillis).map { rows -> rows.map { it.toTransaction() } }

    override suspend fun getExpense(id: String): FinanceTransaction? = expenseDao.getById(id)?.toTransaction()

    override suspend fun addExpense(userId: String, draft: TransactionDraft): Result<Unit> = runCatching {
        val now = System.currentTimeMillis()
        val entity = draft.toEntity(
            id = UUID.randomUUID().toString(),
            userId = userId,
            createdAtMillis = now,
            updatedAtMillis = now
        )
        expenseDao.insert(entity)
        syncOne(entity)
    }

    override suspend fun updateExpense(id: String, draft: TransactionDraft): Result<Unit> = runCatching {
        val existing = expenseDao.getById(id) ?: error("Expense not found")
        val entity = draft.toEntity(
            id = id,
            userId = existing.userId,
            createdAtMillis = existing.createdAtMillis,
            updatedAtMillis = System.currentTimeMillis()
        )
        expenseDao.insert(entity)
        syncOne(entity)
    }

    override suspend fun deleteExpense(id: String): Result<Unit> = runCatching {
        val existing = expenseDao.getById(id)
        expenseDao.deleteById(id)
        if (existing?.goalId != null) {
            subtractGoalSaving(existing.goalId, existing.amountCents)
        }
        existing?.let {
            firestore.collection("users").document(it.userId)
                .collection("expenses").document(id).delete().await()
        }
    }

    override suspend fun syncWithFirestore(userId: String) {
        expenseDao.getUnsynced(userId).forEach { syncOne(it) }
        val snapshot = firestore.collection("users").document(userId).collection("expenses").get().await()
        val remoteRows = snapshot.documents.mapNotNull { doc ->
            val data = doc.data ?: return@mapNotNull null
            ExpenseEntryEntity(
                id = doc.id,
                userId = userId,
                name = data["name"] as? String ?: return@mapNotNull null,
                amountCents = (data["amountCents"] as? Number)?.toLong() ?: return@mapNotNull null,
                category = data["category"] as? String ?: "Other",
                dateMillis = (data["dateMillis"] as? Number)?.toLong() ?: 0L,
                note = data["note"] as? String ?: "",
                isSynced = true,
                createdAtMillis = (data["createdAtMillis"] as? Number)?.toLong() ?: 0L,
                updatedAtMillis = (data["updatedAtMillis"] as? Number)?.toLong() ?: 0L,
                originalAmount = (data["originalAmount"] as? Number)?.toDouble()
                    ?: ((data["amountCents"] as? Number)?.toLong() ?: 0L) / 100.0,
                originalCurrency = data["originalCurrency"] as? String ?: data["defaultCurrency"] as? String ?: "LKR",
                defaultCurrency = data["defaultCurrency"] as? String ?: "LKR",
                exchangeRate = (data["exchangeRate"] as? Number)?.toDouble(),
                paymentMethod = data["paymentMethod"] as? String,
                expenseType = data["expenseType"] as? String,
                goalId = data["goalId"] as? String
            )
        }.filter { remote ->
            val local = expenseDao.getById(remote.id)
            local == null || remote.updatedAtMillis >= local.updatedAtMillis
        }
        expenseDao.insertAll(remoteRows)
    }

    private suspend fun syncOne(entity: ExpenseEntryEntity) {
        firestore.collection("users").document(entity.userId)
            .collection("expenses").document(entity.id)
            .set(entity.toFirestoreMap())
            .await()
        expenseDao.markAsSynced(entity.id)
    }

    private suspend fun subtractGoalSaving(goalId: String, amountCents: Long) {
        val goal = goalDao.getById(goalId) ?: return
        val updated = goal.copy(
            savedCents = (goal.savedCents - amountCents).coerceAtLeast(0L),
            isSynced = false,
            updatedAtMillis = System.currentTimeMillis()
        )
        goalDao.insert(updated)
        firestore.collection("users").document(updated.userId)
            .collection("goals").document(updated.id)
            .set(
                mapOf(
                    "id" to updated.id,
                    "userId" to updated.userId,
                    "title" to updated.title,
                    "status" to updated.status,
                    "targetCents" to updated.targetCents,
                    "savedCents" to updated.savedCents,
                    "dueDateMillis" to updated.dueDateMillis,
                    "category" to updated.category,
                    "isPrimary" to updated.isPrimary,
                    "createdAtMillis" to updated.createdAtMillis,
                    "updatedAtMillis" to updated.updatedAtMillis,
                    "initialSavedCents" to updated.initialSavedCents,
                    "defaultCurrency" to updated.defaultCurrency
                )
            )
            .await()
        goalDao.markAsSynced(updated.id)
    }

    private fun TransactionDraft.toEntity(
        id: String,
        userId: String,
        createdAtMillis: Long,
        updatedAtMillis: Long
    ): ExpenseEntryEntity = ExpenseEntryEntity(
        id = id,
        userId = userId,
        name = title,
        amountCents = amountCents,
        category = category.ifBlank { "Other" },
        dateMillis = dateMillis,
        note = note,
        isSynced = false,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis,
        originalAmount = originalAmount,
        originalCurrency = originalCurrency,
        defaultCurrency = defaultCurrency,
        exchangeRate = exchangeRate,
        paymentMethod = paymentMethod,
        expenseType = expenseType?.name,
        goalId = goalId
    )

    private fun ExpenseEntryEntity.toFirestoreMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "userId" to userId,
        "name" to name,
        "amountCents" to amountCents,
        "category" to category,
        "dateMillis" to dateMillis,
        "note" to note,
        "createdAtMillis" to createdAtMillis,
        "updatedAtMillis" to updatedAtMillis,
        "originalAmount" to originalAmount,
        "originalCurrency" to originalCurrency,
        "defaultCurrency" to defaultCurrency,
        "exchangeRate" to exchangeRate,
        "paymentMethod" to paymentMethod,
        "expenseType" to expenseType,
        "goalId" to goalId
    )
}
