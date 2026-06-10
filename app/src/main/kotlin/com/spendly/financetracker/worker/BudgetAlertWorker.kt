package com.spendly.financetracker.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import com.spendly.financetracker.data.repository.AuthRepository
import com.spendly.financetracker.data.service.BudgetAlertService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class BudgetAlertWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val authRepository: AuthRepository,
    private val budgetAlertService: BudgetAlertService
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val uid = authRepository.getCurrentUserId() ?: return Result.success()
        return runCatching {
            budgetAlertService.checkAndNotify(uid)
            Result.success()
        }.getOrElse { Result.retry() }
    }

    companion object {
        const val WORK_NAME = "SpendlyBudgetAlerts"

        fun periodicRequest(): PeriodicWorkRequest =
            PeriodicWorkRequestBuilder<BudgetAlertWorker>(15, TimeUnit.MINUTES).build()
    }
}
