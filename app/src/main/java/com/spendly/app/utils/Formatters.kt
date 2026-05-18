package com.spendly.app.utils

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

object Formatters {
    fun formatLKR(amount: Double): String {
        return FormatUtils.formatLKR(amount)
    }

    fun formatMonthYear(timeMs: Long): String {
        return SimpleDateFormat("MMMM yyyy", Locale.US).format(Date(timeMs))
    }

    fun formatDateShort(timeMs: Long): String {
        val now = Calendar.getInstance()
        val date = Calendar.getInstance().apply { timeInMillis = timeMs }
        
        return if (now.get(Calendar.DATE) == date.get(Calendar.DATE) &&
            now.get(Calendar.MONTH) == date.get(Calendar.MONTH) &&
            now.get(Calendar.YEAR) == date.get(Calendar.YEAR)) {
            "Today"
        } else {
            SimpleDateFormat("dd MMM", Locale.US).format(Date(timeMs))
        }
    }
}
