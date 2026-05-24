package com.spendly.financetracker.ui.util

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun formatMoney(cents: Long, currency: String = "LKR"): String {
    val sign = if (cents < 0L) "-" else ""
    val absolute = kotlin.math.abs(cents)
    val amount = absolute / 100.0
    val pattern = if (absolute % 100L == 0L) "#,##0" else "#,##0.00"
    val formatted = DecimalFormat(pattern).format(amount)
    return "${sign}${currency.ifBlank { "LKR" }} $formatted"
}

fun formatPercent(value: Int): String = "$value%"

fun formatDateShort(timeMillis: Long): String {
    if (timeMillis <= 0L) return "Today"
    return SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timeMillis))
}

fun formatDateFull(timeMillis: Long): String {
    if (timeMillis <= 0L) return "Today"
    return SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(timeMillis))
}

fun currentMonthLabel(): String =
    SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())

fun greetingForNow(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        else -> "Good evening"
    }
}

fun initialsFromEmail(email: String?): String {
    val trimmed = email.orEmpty().trim()
    if (trimmed.isBlank()) return "U"
    val localPart = trimmed.substringBefore("@")
    return localPart
        .split('.', '_', '-', ' ')
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.take(1).uppercase(Locale.getDefault()) }
        .ifBlank { trimmed.take(1).uppercase(Locale.getDefault()) }
}

fun displayNameFromEmail(email: String?): String {
    val localPart = email.orEmpty().substringBefore("@").ifBlank { "Spendly User" }
    return localPart
        .split('.', '_', '-')
        .filter { it.isNotBlank() }
        .joinToString(" ") { part ->
            part.replaceFirstChar { char ->
                if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString()
            }
        }
        .ifBlank { "Spendly User" }
}
