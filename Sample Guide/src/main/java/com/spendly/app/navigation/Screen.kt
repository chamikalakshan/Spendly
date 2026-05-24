package com.spendly.app.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Dashboard : Screen("dashboard")
    object AddIncome : Screen("add_income") {
        const val ARG_INCOME_ID = "incomeId"
        const val routeWithArgs = "add_income?$ARG_INCOME_ID={$ARG_INCOME_ID}"
        fun editRoute(id: String) = "add_income?$ARG_INCOME_ID=$id"
    }
    object AddExpense : Screen("add_expense") {
        const val ARG_EXPENSE_ID = "expenseId"
        const val routeWithArgs = "add_expense?$ARG_EXPENSE_ID={$ARG_EXPENSE_ID}"
        fun editRoute(id: String) = "add_expense?$ARG_EXPENSE_ID=$id"
    }
    object Transactions : Screen("transactions")
    object Analytics : Screen("analytics")
    object GoalTracker : Screen("goal_tracker")
    object EditGoal : Screen("edit_goal") {
        const val ARG_GOAL_ID = "goalId"
        const val routeWithArgs = "edit_goal?$ARG_GOAL_ID={$ARG_GOAL_ID}"
        fun editRoute(id: String) = "edit_goal?$ARG_GOAL_ID=$id"
    }
    object PrimaryGoal : Screen("primary_goal") {
        const val ARG_GOAL_ID = "goalId"
        const val routeWithArgs = "primary_goal?$ARG_GOAL_ID={$ARG_GOAL_ID}"
        fun detailRoute(id: String) = "primary_goal?$ARG_GOAL_ID=$id"
    }
    object Profile : Screen("profile")
}

val bottomNavScreens = listOf(
    Screen.Dashboard.route,
    Screen.Transactions.route,
    Screen.Analytics.route,
    Screen.GoalTracker.route,
    Screen.Profile.route
)
