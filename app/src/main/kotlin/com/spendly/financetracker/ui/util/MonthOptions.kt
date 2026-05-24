package com.spendly.financetracker.ui.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class MonthOption(
    val startMillis: Long,
    val label: String
)

fun monthStart(timeMillis: Long): Long {
    return Calendar.getInstance().apply {
        timeInMillis = timeMillis
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

fun nextMonthStart(timeMillis: Long): Long = shiftMonth(timeMillis, 1)

fun shiftMonth(timeMillis: Long, amount: Int): Long {
    return Calendar.getInstance().apply {
        timeInMillis = timeMillis
        add(Calendar.MONTH, amount)
    }.timeInMillis
}

fun monthLabel(timeMillis: Long): String =
    SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(timeMillis)

fun monthOptions(centerMillis: Long = System.currentTimeMillis(), monthsBack: Int = 24, monthsForward: Int = 3): List<MonthOption> {
    val centerStart = monthStart(centerMillis)
    return (monthsBack downTo -monthsForward).map { offset ->
        val start = shiftMonth(centerStart, -offset)
        MonthOption(startMillis = start, label = monthLabel(start))
    }
}
