package com.spendly.app.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.spendly.app.repository.AuthRepository
import com.spendly.app.repository.ExpenseRepository
import com.spendly.app.repository.GoalRepository
import com.spendly.app.repository.IncomeRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val authRepository: AuthRepository,
    private val incomeRepository: IncomeRepository,
    private val expenseRepository: ExpenseRepository,
    private val goalRepository: GoalRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = coroutineScope {
        val userId = authRepository.getCurrentUserId() ?: return@coroutineScope Result.success()

        try {
            val incomeSync = async { incomeRepository.syncUnsyncedToFirestore(userId) }
            val expenseSync = async { expenseRepository.syncUnsyncedToFirestore(userId) }
            val goalSync = async { goalRepository.syncUnsyncedToFirestore(userId) }

            awaitAll(incomeSync, expenseSync, goalSync)

            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}
