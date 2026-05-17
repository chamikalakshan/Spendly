package com.spendly.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val label: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem("Home", Screen.Dashboard.route,
        Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem("History", Screen.Transactions.route,
        Icons.Filled.Receipt, Icons.Outlined.Receipt),
    BottomNavItem("Analytics", Screen.Analytics.route,
        Icons.Filled.BarChart, Icons.Outlined.BarChart),
    BottomNavItem("Goal", Screen.GoalTracker.route,
        Icons.Filled.TrackChanges, Icons.Outlined.TrackChanges),
    BottomNavItem("Profile", Screen.Profile.route,
        Icons.Filled.Person, Icons.Outlined.Person)
)
