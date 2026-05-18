package com.spendly.app.utils

import android.content.Context
import androidx.work.*
import com.spendly.app.worker.SpendlySyncWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val workManager: WorkManager
) {
    fun schedulePeriodicSync() {
        workManager.enqueueUniquePeriodicWork(
            SpendlySyncWorker.UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            SpendlySyncWorker.buildRequest()
        )
    }

    fun startImmediateSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<SpendlySyncWorker>()
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniqueWork(
            "SpendlyImmediateSync",
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )
    }
}
