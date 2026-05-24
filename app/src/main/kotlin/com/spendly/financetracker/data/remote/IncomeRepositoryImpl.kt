package com.spendly.financetracker.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.spendly.financetracker.data.local.dao.IncomeDao
import com.spendly.financetracker.data.local.entity.IncomeEntryEntity
import com.spendly.financetracker.data.model.FinanceTransaction
import com.spendly.financetracker.data.model.TransactionDraft
import com.spendly.financetracker.data.repository.IncomeRepository
import com.spendly.financetracker.util.toTransaction
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
    override fun observeIncome(userId: String): Flow<List<FinanceTransaction>> =
        incomeDao.observeByUser(userId).map { rows -> rows.map { it.toTransaction() } }

    override fun observeMonthlyIncome(
        userId: String,
        startMillis: Long,
        endMillis: Long
    ): Flow<List<FinanceTransaction>> =
        incomeDao.observeByMonth(userId, startMillis, endMillis).map { rows -> rows.map { it.toTransaction() } }

    override suspend fun getIncome(id: String): FinanceTransaction? = incomeDao.getById(id)?.toTransaction()

    override suspend fun addIncome(userId: String, draft: TransactionDraft): Result<Unit> = runCatching {
        val now = System.currentTimeMillis()
        val entity = draft.toEntity(
            id = UUID.randomUUID().toString(),
            userId = userId,
            createdAtMillis = now,
            updatedAtMillis = now
        )
        incomeDao.insert(entity)
        syncOne(entity)
    }

    override suspend fun updateIncome(id: String, draft: TransactionDraft): Result<Unit> = runCatching {
        val existing = incomeDao.getById(id) ?: error("Income not found")
        val entity = draft.toEntity(
            id = id,
            userId = existing.userId,
            createdAtMillis = existing.createdAtMillis,
            updatedAtMillis = System.currentTimeMillis()
        )
        incomeDao.insert(entity)
        syncOne(entity)
    }

    override suspend fun deleteIncome(id: String): Result<Unit> = runCatching {
        val existing = incomeDao.getById(id)
        incomeDao.deleteById(id)
        existing?.let {
            firestore.collection("users").document(it.userId)
                .collection("income").document(id).delete().await()
        }
    }

    override suspend fun syncWithFirestore(userId: String) {
        incomeDao.getUnsynced(userId).forEach { syncOne(it) }
        val snapshot = firestore.collection("users").document(userId).collection("income").get().await()
        val remoteRows = snapshot.documents.mapNotNull { doc ->
            val data = doc.data ?: return@mapNotNull null
            IncomeEntryEntity(
                id = doc.id,
                userId = userId,
                name = data["name"] as? String ?: return@mapNotNull null,
                amountCents = (data["amountCents"] as? Number)?.toLong() ?: return@mapNotNull null,
                source = data["source"] as? String ?: "Salary",
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
                isRecurring = data["isRecurring"] as? Boolean ?: false,
                cryptoCoin = data["cryptoCoin"] as? String,
                cryptoAmount = (data["cryptoAmount"] as? Number)?.toDouble(),
                cryptoRate = (data["cryptoRate"] as? Number)?.toDouble(),
                cryptoRateSource = data["cryptoRateSource"] as? String,
                cryptoRateFetchedAt = (data["cryptoRateFetchedAt"] as? Number)?.toLong()
            )
        }.filter { remote ->
            val local = incomeDao.getById(remote.id)
            local == null || remote.updatedAtMillis >= local.updatedAtMillis
        }
        incomeDao.insertAll(remoteRows)
    }

    private suspend fun syncOne(entity: IncomeEntryEntity) {
        firestore.collection("users").document(entity.userId)
            .collection("income").document(entity.id)
            .set(entity.toFirestoreMap())
            .await()
        incomeDao.markAsSynced(entity.id)
    }

    private fun TransactionDraft.toEntity(
        id: String,
        userId: String,
        createdAtMillis: Long,
        updatedAtMillis: Long
    ): IncomeEntryEntity = IncomeEntryEntity(
        id = id,
        userId = userId,
        name = title,
        amountCents = amountCents,
        source = source.ifBlank { "Salary" },
        dateMillis = dateMillis,
        note = note,
        isSynced = false,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis,
        originalAmount = originalAmount,
        originalCurrency = originalCurrency,
        defaultCurrency = defaultCurrency,
        exchangeRate = exchangeRate,
        isRecurring = isRecurring,
        cryptoCoin = cryptoCoin,
        cryptoAmount = cryptoAmount,
        cryptoRate = cryptoRate,
        cryptoRateSource = cryptoRateSource,
        cryptoRateFetchedAt = cryptoRateFetchedAt
    )

    private fun IncomeEntryEntity.toFirestoreMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "userId" to userId,
        "name" to name,
        "amountCents" to amountCents,
        "source" to source,
        "dateMillis" to dateMillis,
        "note" to note,
        "createdAtMillis" to createdAtMillis,
        "updatedAtMillis" to updatedAtMillis,
        "originalAmount" to originalAmount,
        "originalCurrency" to originalCurrency,
        "defaultCurrency" to defaultCurrency,
        "exchangeRate" to exchangeRate,
        "isRecurring" to isRecurring,
        "cryptoCoin" to cryptoCoin,
        "cryptoAmount" to cryptoAmount,
        "cryptoRate" to cryptoRate,
        "cryptoRateSource" to cryptoRateSource,
        "cryptoRateFetchedAt" to cryptoRateFetchedAt
    )
}
