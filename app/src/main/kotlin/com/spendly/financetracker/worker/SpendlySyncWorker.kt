package com.spendly.financetracker.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import android.util.Log
import com.spendly.financetracker.data.repository.AuthRepository
import com.spendly.financetracker.data.repository.BudgetRepository
import com.spendly.financetracker.data.repository.ExpenseRepository
import com.spendly.financetracker.data.repository.GoalRepository
import com.spendly.financetracker.data.repository.IncomeRepository
import com.spendly.financetracker.data.repository.RecurringTransactionRepository
import com.spendly.financetracker.data.repository.UserRepository
import com.spendly.financetracker.data.service.SyncMetadataStore
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.util.concurrent.TimeUnit

@HiltWorker
class SpendlySyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val authRepository: AuthRepository,
    private val incomeRepository: IncomeRepository,
    private val expenseRepository: ExpenseRepository,
    private val goalRepository: GoalRepository,
    private val userRepository: UserRepository,
    private val budgetRepository: BudgetRepository,
    private val recurringTransactionRepository: RecurringTransactionRepository,
    private val syncMetadataStore: SyncMetadataStore
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result = coroutineScope {
        val uid = authRepository.getCurrentUserId() ?: return@coroutineScope Result.success()
        try {
            syncMetadataStore.markSyncing(uid, OVERALL_COLLECTION)
            recurringTransactionRepository.generateDueTransactions(uid)
            awaitAll(
                async { userRepository.syncWithFirestore(uid) },
                async { incomeRepository.syncWithFirestore(uid) },
                async { expenseRepository.syncWithFirestore(uid) },
                async { goalRepository.syncWithFirestore(uid) },
                async { budgetRepository.syncWithFirestore(uid) },
                async { recurringTransactionRepository.syncWithFirestore(uid) }
            )
            syncMetadataStore.markSuccess(uid, OVERALL_COLLECTION, System.currentTimeMillis())
            Result.success()
        } catch (error: Exception) {
            Log.w(TAG, "Spendly sync failed", error)
            syncMetadataStore.markFailure(uid, OVERALL_COLLECTION, error)
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    companion object {
        const val PERIODIC_WORK_NAME = "SpendlyPeriodicSync"
        const val IMMEDIATE_WORK_NAME = "SpendlyImmediateSync"
        private const val TAG = "SpendlySyncWorker"
        private const val OVERALL_COLLECTION = "_overall"

        fun periodicRequest(): PeriodicWorkRequest {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            return PeriodicWorkRequestBuilder<SpendlySyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.MINUTES)
                .build()
        }
    }
}
