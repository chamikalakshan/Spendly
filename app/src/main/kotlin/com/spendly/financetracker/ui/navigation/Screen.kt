package com.spendly.financetracker.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Auth : Screen("auth")
    object FirebaseSetup : Screen("firebase_setup")
    object Home : Screen("home")
    object Events : Screen("events")
    object Analytics : Screen("analytics")
    object Goals : Screen("goals")
    object AddGoal : Screen("add_goal")
    object GoalDetails : Screen("goal_detail") {
        const val ARG_ID = "goalId"
        const val routeWithArgs = "goal_detail?$ARG_ID={$ARG_ID}"
        fun detailRoute(id: String) = "goal_detail?$ARG_ID=$id"
    }
    object EditGoal : Screen("edit_goal") {
        const val ARG_ID = "goalId"
        const val routeWithArgs = "edit_goal?$ARG_ID={$ARG_ID}"
        fun editRoute(id: String) = "edit_goal?$ARG_ID=$id"
    }
    object Profile : Screen("profile")

    object AddIncome : Screen("add_income") {
        const val ARG_ID = "incomeId"
        const val routeWithArgs = "add_income?$ARG_ID={$ARG_ID}"
        fun editRoute(id: String) = "add_income?$ARG_ID=$id"
    }

    object AddExpense : Screen("add_expense") {
        const val ARG_ID = "expenseId"
        const val routeWithArgs = "add_expense?$ARG_ID={$ARG_ID}"
        fun editRoute(id: String) = "add_expense?$ARG_ID=$id"
    }

    object CreateAccount : Screen("create_account")
}

val bottomNavRoutes = setOf(
    Screen.Home.route,
    Screen.Events.route,
    Screen.Analytics.route,
    Screen.Goals.route,
    Screen.Profile.route
)
