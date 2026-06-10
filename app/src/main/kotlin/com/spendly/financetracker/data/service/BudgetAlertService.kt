package com.spendly.financetracker.data.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.spendly.financetracker.R
import com.spendly.financetracker.data.local.dao.BudgetAlertDao
import com.spendly.financetracker.data.local.dao.BudgetDao
import com.spendly.financetracker.data.local.dao.ExpenseDao
import com.spendly.financetracker.data.local.dao.UserProfileDao
import com.spendly.financetracker.data.local.entity.BudgetAlertEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetAlertService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val budgetDao: BudgetDao,
    private val expenseDao: ExpenseDao,
    private val userProfileDao: UserProfileDao,
    private val budgetAlertDao: BudgetAlertDao
) {
    suspend fun checkAndNotify(userId: String): Int {
        val profile = userProfileDao.getById(userId) ?: return 0
        if (!profile.budgetAlertsEnabled) return 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return 0

        createChannel()
        val monthStart = monthStart(System.currentTimeMillis())
        val nextMonth = nextMonthStart(monthStart)
        val expenses = expenseDao.getByDateRange(userId, monthStart, nextMonth)
        var posted = 0
        budgetDao.getByMonth(userId, monthStart).forEach { budget ->
            val spent = expenses
                .filter { it.category.equals(budget.category, ignoreCase = true) }
                .sumOf { it.amountCents }
            val progress = if (budget.limitCents > 0L) ((spent * 100L) / budget.limitCents).toInt() else 0
            val thresholdType = when {
                progress > 100 -> "EXCEEDED"
                progress >= profile.budgetAlertThresholdPercent -> "WARNING"
                else -> null
            } ?: return@forEach
            if (budgetAlertDao.getExisting(userId, budget.id, monthStart, thresholdType) == null) {
                val now = System.currentTimeMillis()
                budgetAlertDao.upsert(
                    BudgetAlertEntity(
                        id = "${budget.id}-$monthStart-$thresholdType",
                        userId = userId,
                        budgetId = budget.id,
                        category = budget.category,
                        monthStartMillis = monthStart,
                        thresholdType = thresholdType,
                        progressPercent = progress,
                        notifiedAtMillis = now
                    )
                )
                postBudgetNotification(budget.category, thresholdType, progress, budget.id.hashCode())
                posted += 1
            }
        }
        return posted
    }

    private fun postBudgetNotification(category: String, thresholdType: String, progress: Int, id: Int) {
        val title = if (thresholdType == "EXCEEDED") "Budget exceeded" else "Budget warning"
        val body = "$category budget is now at $progress%."
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(id, notification)
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(CHANNEL_ID, "Budget alerts", NotificationManager.IMPORTANCE_DEFAULT)
            manager.createNotificationChannel(channel)
        }
    }

    private fun monthStart(timeMillis: Long): Long {
        val calendar = Calendar.getInstance().apply { timeInMillis = timeMillis }
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun nextMonthStart(monthStart: Long): Long {
        val calendar = Calendar.getInstance().apply { timeInMillis = monthStart }
        calendar.add(Calendar.MONTH, 1)
        return calendar.timeInMillis
    }

    companion object {
        const val CHANNEL_ID = "spendly_budget_alerts"
    }
}
