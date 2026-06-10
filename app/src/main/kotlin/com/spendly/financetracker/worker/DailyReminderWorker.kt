package com.spendly.financetracker.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import com.spendly.financetracker.data.repository.AuthRepository
import com.spendly.financetracker.data.service.DailyReminderService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class DailyReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val authRepository: AuthRepository,
    private val dailyReminderService: DailyReminderService
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val uid = authRepository.getCurrentUserId() ?: return Result.success()
        return runCatching {
            dailyReminderService.checkAndNotify(uid)
            Result.success()
        }.getOrElse { Result.retry() }
    }

    companion object {
        const val WORK_NAME = "SpendlyDailyReminders"

        fun periodicRequest(initialDelayMillis: Long): PeriodicWorkRequest =
            PeriodicWorkRequestBuilder<DailyReminderWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(initialDelayMillis, TimeUnit.MILLISECONDS)
                .build()
    }
}
