package com.spendly.app.utils

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

object FormatUtils {

  fun formatLKR(amount: Double): String {
    val format = NumberFormat.getNumberInstance(Locale.US)
    format.maximumFractionDigits = 0
    format.minimumFractionDigits = 0
    return "LKR ${format.format(amount)}"
  }

  fun formatPercent(value: Int): String = "$value%"

  fun formatDateShort(ms: Long): String {
    val sdf = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
    return sdf.format(Date(ms))
  }

  fun formatDateGroupHeader(ms: Long): String {
    val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
    return sdf.format(Date(ms)).uppercase()
  }

  fun formatMonthYear(ms: Long): String {
    val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    return sdf.format(Date(ms))
  }

  fun getGreeting(): String {
    return when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
      in 5..11 -> "Good morning"
      in 12..16 -> "Good afternoon"
      in 17..20 -> "Good evening"
      else -> "Hello"
    }
  }

  fun getMonthBoundaries(
    year: Int, month: Int
  ): Pair<Long, Long> {
    val cal = Calendar.getInstance()
    cal.set(year, month, 1, 0, 0, 0)
    cal.set(Calendar.MILLISECOND, 0)
    val start = cal.timeInMillis
    cal.set(Calendar.DAY_OF_MONTH, 
      cal.getActualMaximum(Calendar.DAY_OF_MONTH))
    cal.set(Calendar.HOUR_OF_DAY, 23)
    cal.set(Calendar.MINUTE, 59)
    cal.set(Calendar.SECOND, 59)
    val end = cal.timeInMillis
    return start to end
  }

  fun getLast6Months(): List<Triple<String, Long, Long>> {
    val result = mutableListOf<Triple<String, Long, Long>>()
    val cal = Calendar.getInstance()
    repeat(6) {
      val (start, end) = getMonthBoundaries(
        cal.get(Calendar.YEAR), 
        cal.get(Calendar.MONTH))
      val label = formatMonthYear(start)
      result.add(0, Triple(label, start, end))
      cal.add(Calendar.MONTH, -1)
    }
    return result
  }
}
