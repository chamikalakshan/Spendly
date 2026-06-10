package com.spendly.financetracker.data.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.spendly.financetracker.MainActivity
import com.spendly.financetracker.R
import com.spendly.financetracker.data.local.dao.ExpenseDao
import com.spendly.financetracker.data.local.dao.IncomeDao
import com.spendly.financetracker.data.local.dao.UserProfileDao
import com.spendly.financetracker.data.model.AppNotification
import com.spendly.financetracker.data.repository.NotificationRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DailyReminderService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userProfileDao: UserProfileDao,
    private val incomeDao: IncomeDao,
    private val expenseDao: ExpenseDao,
    private val notificationRepository: NotificationRepository
) {
    suspend fun checkAndNotify(userId: String): Boolean {
        val profile = userProfileDao.getById(userId) ?: return false
        if (!profile.dailyRemindersEnabled) return false
        if (!profile.remindExpenses && !profile.remindIncome) return false

        val (todayStart, tomorrowStart) = todayRange()
        val incomeCount = if (profile.remindIncome) incomeDao.countByDateRange(userId, todayStart, tomorrowStart) else 0
        val expenseCount = if (profile.remindExpenses) expenseDao.countByDateRange(userId, todayStart, tomorrowStart) else 0
        val missingIncome = profile.remindIncome && incomeCount == 0
        val missingExpense = profile.remindExpenses && expenseCount == 0

        if (profile.smartReminderMode && !missingIncome && !missingExpense) return false

        val body = when {
            missingIncome && missingExpense -> "Don't forget to add today's expenses and income."
            missingExpense -> "Don't forget to add today's expenses."
            missingIncome -> "Don't forget to add today's income."
            else -> "Don't forget to add today's expenses and income."
        }
        val id = "daily-reminder-$userId-$todayStart"
        val now = System.currentTimeMillis()
        notificationRepository.upsert(
            AppNotification(
                id = id,
                userId = userId,
                title = "Daily Spendly reminder",
                body = body,
                type = "DAILY_REMINDER",
                isRead = false,
                createdAtMillis = now
            )
        )

        if (hasNotificationPermission()) {
            createChannel(context)
            postNotification(id.hashCode(), body)
        }
        return true
    }

    private fun postNotification(id: Int, body: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("spendly_destination", "home")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Daily Spendly reminder")
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(id, notification)
    }

    private fun hasNotificationPermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    private fun todayRange(): Pair<Long, Long> {
        val start = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val end = start.clone() as Calendar
        end.add(Calendar.DAY_OF_YEAR, 1)
        return start.timeInMillis to end.timeInMillis
    }

    companion object {
        const val CHANNEL_ID = "spendly_daily_reminders"
        const val CHANNEL_NAME = "Spendly Reminders"

        fun createChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val manager = context.getSystemService(NotificationManager::class.java)
                val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
                manager.createNotificationChannel(channel)
            }
        }
    }
}
