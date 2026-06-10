package com.spendly.financetracker.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.spendly.financetracker.data.local.dao.ExpenseDao
import com.spendly.financetracker.data.local.dao.IncomeDao
import com.spendly.financetracker.data.local.dao.RecurringRuleDao
import com.spendly.financetracker.data.local.entity.RecurringRuleEntity
import com.spendly.financetracker.data.model.RecurringFrequency
import com.spendly.financetracker.data.model.RecurringRule
import com.spendly.financetracker.data.model.RecurringRuleDraft
import com.spendly.financetracker.data.model.TransactionDraft
import com.spendly.financetracker.data.model.TransactionType
import com.spendly.financetracker.data.repository.ExpenseRepository
import com.spendly.financetracker.data.repository.IncomeRepository
import com.spendly.financetracker.data.repository.RecurringTransactionRepository
import com.spendly.financetracker.data.service.SyncMetadataStore
import com.spendly.financetracker.util.toModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecurringTransactionRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val recurringRuleDao: RecurringRuleDao,
    private val incomeDao: IncomeDao,
    private val expenseDao: ExpenseDao,
    private val incomeRepository: IncomeRepository,
    private val expenseRepository: ExpenseRepository,
    private val syncMetadataStore: SyncMetadataStore
) : RecurringTransactionRepository {
    override fun observeRules(userId: String): Flow<List<RecurringRule>> =
        recurringRuleDao.observeByUser(userId).map { rows -> rows.map { it.toModel() } }

    override suspend fun saveRule(userId: String, draft: RecurringRuleDraft): Result<Unit> = runCatching {
        require(draft.name.isNotBlank()) { "Enter a name" }
        require(draft.amountCents > 0L) { "Enter a valid amount" }
        val now = System.currentTimeMillis()
        val existing = draft.id?.let { recurringRuleDao.getById(it) }
        val entity = RecurringRuleEntity(
            id = existing?.id ?: UUID.randomUUID().toString(),
            userId = userId,
            type = draft.type.name,
            name = draft.name.trim(),
            amountCents = draft.amountCents,
            originalAmount = draft.originalAmount,
            originalCurrency = draft.originalCurrency,
            defaultCurrency = draft.defaultCurrency,
            exchangeRate = draft.exchangeRate,
            source = draft.source,
            category = draft.category,
            paymentMethod = draft.paymentMethod,
            expenseType = draft.expenseType?.name,
            note = draft.note.trim(),
            frequency = draft.frequency.name,
            interval = draft.interval.coerceAtLeast(1),
            startDateMillis = draft.startDateMillis,
            nextRunDateMillis = draft.nextRunDateMillis,
            endDateMillis = draft.endDateMillis,
            isActive = draft.isActive,
            lastGeneratedAtMillis = existing?.lastGeneratedAtMillis,
            isSynced = false,
            createdAtMillis = existing?.createdAtMillis ?: now,
            updatedAtMillis = now,
            deletedAtMillis = null
        )
        recurringRuleDao.upsert(entity)
        syncOne(entity)
    }

    override suspend fun pauseRule(ruleId: String): Result<Unit> = updateRuleState(ruleId, false)

    override suspend fun resumeRule(ruleId: String): Result<Unit> = updateRuleState(ruleId, true)

    override suspend fun deleteRule(ruleId: String): Result<Unit> = runCatching {
        val existing = recurringRuleDao.getById(ruleId) ?: return@runCatching
        val now = System.currentTimeMillis()
        val deleted = existing.copy(isActive = false, deletedAtMillis = now, updatedAtMillis = now, isSynced = false)
        recurringRuleDao.upsert(deleted)
        syncOne(deleted)
    }

    override suspend fun generateDueTransactions(userId: String, nowMillis: Long): Result<Int> = runCatching {
        var generated = 0
        recurringRuleDao.getDueRules(userId, nowMillis).forEach { rule ->
            var current = rule
            var guard = 0
            while (
                current.isActive &&
                current.deletedAtMillis == null &&
                current.nextRunDateMillis <= nowMillis &&
                (current.endDateMillis?.let { current.nextRunDateMillis <= it } ?: true) &&
                guard < 12
            ) {
                val periodKey = periodKey(current.frequency, current.nextRunDateMillis)
                val exists = when (current.type) {
                    TransactionType.INCOME.name -> incomeDao.countGenerated(userId, current.id, periodKey)
                    else -> expenseDao.countGenerated(userId, current.id, periodKey)
                } > 0
                if (!exists) {
                    val draft = current.toTransactionDraft(periodKey)
                    val result = when (draft.type) {
                        TransactionType.INCOME -> incomeRepository.addIncome(userId, draft)
                        TransactionType.EXPENSE -> expenseRepository.addExpense(userId, draft)
                    }
                    if (result.isSuccess) generated++ else break
                }
                val advanced = advance(current.nextRunDateMillis, current.frequency, current.interval)
                current = current.copy(
                    nextRunDateMillis = advanced,
                    lastGeneratedAtMillis = current.nextRunDateMillis,
                    updatedAtMillis = System.currentTimeMillis(),
                    isSynced = false
                )
                recurringRuleDao.upsert(current)
                guard++
            }
            if (current.id == rule.id && current.updatedAtMillis != rule.updatedAtMillis) {
                syncOne(current)
            }
        }
        generated
    }

    override suspend fun syncWithFirestore(userId: String) {
        val collection = "recurringRules"
        syncMetadataStore.markSyncing(userId, collection)
        try {
            recurringRuleDao.getUnsynced(userId).forEach { syncOne(it) }
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
                RecurringRuleEntity(
                    id = doc.id,
                    userId = userId,
                    type = data["type"] as? String ?: "EXPENSE",
                    name = data["name"] as? String ?: return@mapNotNull null,
                    amountCents = (data["amountCents"] as? Number)?.toLong() ?: return@mapNotNull null,
                    originalAmount = (data["originalAmount"] as? Number)?.toDouble() ?: 0.0,
                    originalCurrency = data["originalCurrency"] as? String ?: "LKR",
                    defaultCurrency = data["defaultCurrency"] as? String ?: "LKR",
                    exchangeRate = (data["exchangeRate"] as? Number)?.toDouble(),
                    source = data["source"] as? String,
                    category = data["category"] as? String,
                    paymentMethod = data["paymentMethod"] as? String,
                    expenseType = data["expenseType"] as? String,
                    note = data["note"] as? String,
                    frequency = data["frequency"] as? String ?: "MONTHLY",
                    interval = (data["interval"] as? Number)?.toInt() ?: 1,
                    startDateMillis = (data["startDateMillis"] as? Number)?.toLong() ?: 0L,
                    nextRunDateMillis = (data["nextRunDateMillis"] as? Number)?.toLong() ?: 0L,
                    endDateMillis = (data["endDateMillis"] as? Number)?.toLong(),
                    isActive = data["active"] as? Boolean ?: data["isActive"] as? Boolean ?: true,
                    lastGeneratedAtMillis = (data["lastGeneratedAtMillis"] as? Number)?.toLong(),
                    isSynced = true,
                    createdAtMillis = (data["createdAtMillis"] as? Number)?.toLong() ?: 0L,
                    updatedAtMillis = (data["updatedAtMillis"] as? Number)?.toLong() ?: 0L,
                    deletedAtMillis = (data["deletedAtMillis"] as? Number)?.toLong()
                ).also { maxRemoteUpdatedAt = maxOf(maxRemoteUpdatedAt, it.updatedAtMillis) }
            }.filter { remote ->
                val local = recurringRuleDao.getById(remote.id)
                local == null || remote.updatedAtMillis >= local.updatedAtMillis
            }
            recurringRuleDao.upsertAll(remoteRows)
            syncMetadataStore.markSuccess(userId, collection, maxRemoteUpdatedAt)
        } catch (error: Exception) {
            syncMetadataStore.markFailure(userId, collection, error)
            throw error
        }
    }

    private suspend fun updateRuleState(ruleId: String, active: Boolean): Result<Unit> = runCatching {
        val existing = recurringRuleDao.getById(ruleId) ?: return@runCatching
        val updated = existing.copy(isActive = active, updatedAtMillis = System.currentTimeMillis(), isSynced = false)
        recurringRuleDao.upsert(updated)
        syncOne(updated)
    }

    private suspend fun syncOne(entity: RecurringRuleEntity) {
        firestore.collection("users").document(entity.userId)
            .collection("recurringRules").document(entity.id)
            .set(entity.toFirestoreMap())
            .await()
        recurringRuleDao.markAsSynced(entity.id)
    }

    private fun RecurringRuleEntity.toTransactionDraft(periodKey: String): TransactionDraft {
        val transactionType = runCatching { TransactionType.valueOf(type) }.getOrDefault(TransactionType.EXPENSE)
        return TransactionDraft(
            title = name,
            amountCents = amountCents,
            type = transactionType,
            category = category.orEmpty(),
            source = source.orEmpty(),
            note = note.orEmpty(),
            dateMillis = nextRunDateMillis,
            originalAmount = originalAmount.takeIf { it > 0.0 } ?: amountCents / 100.0,
            originalCurrency = originalCurrency,
            defaultCurrency = defaultCurrency,
            exchangeRate = exchangeRate,
            paymentMethod = paymentMethod,
            expenseType = expenseType?.let { runCatching { com.spendly.financetracker.data.model.ExpenseType.valueOf(it) }.getOrNull() },
            isRecurring = true,
            recurringRuleId = id,
            recurringPeriodKey = periodKey
        )
    }

    private fun RecurringRuleEntity.toFirestoreMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "userId" to userId,
        "type" to type,
        "name" to name,
        "amountCents" to amountCents,
        "originalAmount" to originalAmount,
        "originalCurrency" to originalCurrency,
        "defaultCurrency" to defaultCurrency,
        "exchangeRate" to exchangeRate,
        "source" to source,
        "category" to category,
        "paymentMethod" to paymentMethod,
        "expenseType" to expenseType,
        "note" to note,
        "frequency" to frequency,
        "interval" to interval,
        "startDateMillis" to startDateMillis,
        "nextRunDateMillis" to nextRunDateMillis,
        "endDateMillis" to endDateMillis,
        "isActive" to isActive,
        "lastGeneratedAtMillis" to lastGeneratedAtMillis,
        "createdAtMillis" to createdAtMillis,
        "updatedAtMillis" to updatedAtMillis,
        "deletedAtMillis" to deletedAtMillis
    )

    private fun advance(timeMillis: Long, frequency: String, interval: Int): Long {
        val field = when (runCatching { RecurringFrequency.valueOf(frequency) }.getOrDefault(RecurringFrequency.MONTHLY)) {
            RecurringFrequency.DAILY -> Calendar.DAY_OF_YEAR
            RecurringFrequency.WEEKLY -> Calendar.WEEK_OF_YEAR
            RecurringFrequency.MONTHLY -> Calendar.MONTH
            RecurringFrequency.YEARLY -> Calendar.YEAR
        }
        return Calendar.getInstance().apply {
            timeInMillis = timeMillis
            add(field, interval.coerceAtLeast(1))
        }.timeInMillis
    }

    private fun periodKey(frequency: String, timeMillis: Long): String {
        val pattern = when (runCatching { RecurringFrequency.valueOf(frequency) }.getOrDefault(RecurringFrequency.MONTHLY)) {
            RecurringFrequency.DAILY -> "yyyyMMdd"
            RecurringFrequency.WEEKLY -> "yyyy-'W'ww"
            RecurringFrequency.MONTHLY -> "yyyyMM"
            RecurringFrequency.YEARLY -> "yyyy"
        }
        return SimpleDateFormat(pattern, Locale.US).format(timeMillis)
    }
}
