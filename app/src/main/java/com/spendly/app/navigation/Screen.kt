package com.spendly.app.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Dashboard : Screen("dashboard")
    object AddIncome : Screen("add_income")
    object AddExpense : Screen("add_expense")
    object Transactions : Screen("transactions")
    object Analytics : Screen("analytics")
    object GoalTracker : Screen("goal_tracker")
    object EditGoal : Screen("edit_goal")
    object PrimaryGoal : Screen("primary_goal")
    object Profile : Screen("profile")
}

val bottomNavScreens = listOf(
    Screen.Dashboard.route,
    Screen.Transactions.route,
    Screen.Analytics.route,
    Screen.GoalTracker.route,
    Screen.Profile.route
)
