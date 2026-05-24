package com.spendly.financetracker.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.spendly.financetracker.data.local.dao.ExpenseDao
import com.spendly.financetracker.data.local.dao.GoalDao
import com.spendly.financetracker.data.local.entity.ExpenseEntryEntity
import com.spendly.financetracker.data.local.entity.SavingsGoalEntity
import com.spendly.financetracker.data.model.SavingsGoal
import com.spendly.financetracker.data.repository.GoalRepository
import com.spendly.financetracker.util.toEntity
import com.spendly.financetracker.util.toModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoalRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val goalDao: GoalDao,
    private val expenseDao: ExpenseDao
) : GoalRepository {
    override fun observeGoals(userId: String): Flow<List<SavingsGoal>> =
        goalDao.observeByUser(userId).map { rows -> rows.map { it.toModel() } }

    override suspend fun getGoal(id: String): SavingsGoal? = goalDao.getById(id)?.toModel()

    override suspend fun saveGoal(goal: SavingsGoal): Result<Unit> = runCatching {
        val now = System.currentTimeMillis()
        val entity = goal.copy(
            id = goal.id.ifBlank { UUID.randomUUID().toString() },
            isSynced = false,
            createdAtMillis = if (goal.createdAtMillis == 0L) now else goal.createdAtMillis,
            updatedAtMillis = now
        ).toEntity()
        goalDao.insert(entity)
        syncOne(entity)
        if (goal.id.isBlank() && entity.initialSavedCents > 0L) {
            saveGoalExpense(entity, entity.initialSavedCents, now)
        }
    }

    override suspend fun deleteGoal(id: String): Result<Unit> = runCatching {
        val existing = goalDao.getById(id)
        goalDao.deleteById(id)
        existing?.let {
            firestore.collection("users").document(it.userId)
                .collection("goals").document(id).delete().await()
        }
    }

    override suspend fun addSavings(goalId: String, amountCents: Long): Result<Unit> = runCatching {
        val existing = goalDao.getById(goalId) ?: error("Goal not found")
        val remaining = (existing.targetCents - existing.savedCents).coerceAtLeast(0L)
        require(amountCents > 0L) { "Enter a valid savings amount" }
        require(amountCents <= remaining) { "Amount exceed target value" }
        val now = System.currentTimeMillis()
        val newSavedCents = existing.savedCents + amountCents
        val isDone = newSavedCents >= existing.targetCents && existing.targetCents > 0L
        val updated = existing.copy(
            savedCents = newSavedCents,
            status = if (isDone) "Done" else existing.status,
            isPrimary = if (isDone) false else existing.isPrimary,
            isSynced = false,
            updatedAtMillis = now
        )
        goalDao.insert(updated)
        syncOne(updated)
        saveGoalExpense(updated, amountCents, now)
    }

    override suspend fun syncWithFirestore(userId: String) {
        goalDao.getUnsynced(userId).forEach { syncOne(it) }
        val snapshot = firestore.collection("users").document(userId).collection("goals").get().await()
        snapshot.documents.mapNotNull { doc ->
            val data = doc.data ?: return@mapNotNull null
            SavingsGoalEntity(
                id = doc.id,
                userId = userId,
                title = data["title"] as? String ?: return@mapNotNull null,
                status = data["status"] as? String ?: "On track",
                targetCents = (data["targetCents"] as? Number)?.toLong() ?: 0L,
                savedCents = (data["savedCents"] as? Number)?.toLong() ?: 0L,
                dueDateMillis = (data["dueDateMillis"] as? Number)?.toLong() ?: 0L,
                category = data["category"] as? String ?: "Custom",
                isPrimary = data["isPrimary"] as? Boolean ?: false,
                isSynced = true,
                createdAtMillis = (data["createdAtMillis"] as? Number)?.toLong() ?: 0L,
                updatedAtMillis = (data["updatedAtMillis"] as? Number)?.toLong() ?: 0L,
                initialSavedCents = (data["initialSavedCents"] as? Number)?.toLong() ?: 0L,
                defaultCurrency = data["defaultCurrency"] as? String ?: "LKR",
                iconKey = data["iconKey"] as? String ?: "goal"
            )
        }.forEach { remote ->
            val local = goalDao.getById(remote.id)
            if (local == null || remote.updatedAtMillis >= local.updatedAtMillis) {
                goalDao.insert(remote)
            }
        }
    }

    private suspend fun syncOne(entity: SavingsGoalEntity) {
        firestore.collection("users").document(entity.userId)
            .collection("goals").document(entity.id)
            .set(entity.toFirestoreMap())
            .await()
        goalDao.markAsSynced(entity.id)
    }

    private suspend fun saveGoalExpense(goal: SavingsGoalEntity, amountCents: Long, now: Long) {
        val expense = ExpenseEntryEntity(
            id = UUID.randomUUID().toString(),
            userId = goal.userId,
            name = goal.title,
            amountCents = amountCents,
            category = "Goal",
            dateMillis = now,
            note = goal.title,
            isSynced = false,
            createdAtMillis = now,
            updatedAtMillis = now,
            originalAmount = amountCents / 100.0,
            originalCurrency = goal.defaultCurrency,
            defaultCurrency = goal.defaultCurrency,
            paymentMethod = "Goal transfer",
            expenseType = "DISCRETIONARY",
            goalId = goal.id
        )
        expenseDao.insert(expense)
        firestore.collection("users").document(expense.userId)
            .collection("expenses").document(expense.id)
            .set(
                mapOf(
                    "id" to expense.id,
                    "userId" to expense.userId,
                    "name" to expense.name,
                    "amountCents" to expense.amountCents,
                    "category" to expense.category,
                    "dateMillis" to expense.dateMillis,
                    "note" to expense.note,
                    "createdAtMillis" to expense.createdAtMillis,
                    "updatedAtMillis" to expense.updatedAtMillis,
                    "originalAmount" to expense.originalAmount,
                    "originalCurrency" to expense.originalCurrency,
                    "defaultCurrency" to expense.defaultCurrency,
                    "paymentMethod" to expense.paymentMethod,
                    "expenseType" to expense.expenseType,
                    "goalId" to expense.goalId
                )
            )
            .await()
        expenseDao.markAsSynced(expense.id)
    }

    private fun SavingsGoalEntity.toFirestoreMap(): Map<String, Any> = mapOf(
        "id" to id,
        "userId" to userId,
        "title" to title,
        "status" to status,
        "targetCents" to targetCents,
        "savedCents" to savedCents,
        "dueDateMillis" to dueDateMillis,
        "category" to category,
        "isPrimary" to isPrimary,
        "createdAtMillis" to createdAtMillis,
        "updatedAtMillis" to updatedAtMillis,
        "initialSavedCents" to initialSavedCents,
        "defaultCurrency" to defaultCurrency,
        "iconKey" to iconKey
    )
}
