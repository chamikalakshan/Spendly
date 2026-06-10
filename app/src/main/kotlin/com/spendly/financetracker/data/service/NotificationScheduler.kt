package com.spendly.financetracker.data.service

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import com.spendly.financetracker.data.model.UserProfile
import com.spendly.financetracker.worker.DailyReminderWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val workManager: WorkManager
) {
    fun scheduleDailyReminder(profile: UserProfile? = null) {
        DailyReminderService.createChannel(context)
        val reminderTime = profile?.reminderTime?.takeIf { it.isNotBlank() } ?: DEFAULT_REMINDER_TIME
        workManager.enqueueUniquePeriodicWork(
            DailyReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            DailyReminderWorker.periodicRequest(initialDelayMillis(reminderTime))
        )
    }

    fun cancelDailyReminder() {
        workManager.cancelUniqueWork(DailyReminderWorker.WORK_NAME)
    }

    private fun initialDelayMillis(reminderTime: String): Long {
        val parts = reminderTime.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull()?.coerceIn(0, 23) ?: 20
        val minute = parts.getOrNull(1)?.toIntOrNull()?.coerceIn(0, 59) ?: 0
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (!after(now)) add(Calendar.DAY_OF_YEAR, 1)
        }
        return (target.timeInMillis - now.timeInMillis).coerceAtLeast(TimeUnit.MINUTES.toMillis(15))
    }

    companion object {
        const val DEFAULT_REMINDER_TIME = "20:00"
    }
}
