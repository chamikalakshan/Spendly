package com.spendly.financetracker.ui.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.ui.graphics.vector.ImageVector

data class GoalIconOption(
    val key: String,
    val label: String,
    val icon: ImageVector
)

val goalIconOptions = listOf(
    GoalIconOption("transport", "Transport", Icons.Default.DirectionsCar),
    GoalIconOption("home", "Home", Icons.Default.AccountBalance),
    GoalIconOption("travel", "Travel", Icons.Default.Flight),
    GoalIconOption("education", "Education", Icons.Default.School),
    GoalIconOption("laptop", "Laptop", Icons.Default.Laptop),
    GoalIconOption("phone", "Phone", Icons.Default.PhoneIphone),
    GoalIconOption("heart", "Wedding/Gift", Icons.Default.Favorite),
    GoalIconOption("health", "Health", Icons.Default.HealthAndSafety),
    GoalIconOption("money", "Savings", Icons.Default.AccountBalanceWallet),
    GoalIconOption("shopping", "Shopping", Icons.Default.ShoppingBag),
    GoalIconOption("business", "Business", Icons.Default.BusinessCenter),
    GoalIconOption("goal", "Goal", Icons.Default.Flag)
)

fun suggestedGoalIconKey(goalName: String): String {
    val name = goalName.lowercase()
    return when {
        listOf("car", "vehicle", "bike").any { it in name } -> "transport"
        listOf("house", "home", "rent").any { it in name } -> "home"
        listOf("travel", "trip", "vacation").any { it in name } -> "travel"
        listOf("education", "course", "university").any { it in name } -> "education"
        listOf("laptop", "computer", "pc").any { it in name } -> "laptop"
        listOf("phone", "mobile").any { it in name } -> "phone"
        listOf("wedding", "gift").any { it in name } -> "heart"
        listOf("emergency", "medical", "health").any { it in name } -> "health"
        listOf("savings", "money", "fund").any { it in name } -> "money"
        "shopping" in name -> "shopping"
        listOf("business", "startup").any { it in name } -> "business"
        else -> "goal"
    }
}

fun goalIconForKey(key: String): ImageVector =
    goalIconOptions.firstOrNull { it.key == key }?.icon ?: Icons.Default.Flag
