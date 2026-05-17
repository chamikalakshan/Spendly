package com.spendly.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.spendly.app.ui.screens.*

@Composable
fun SpendlyNavGraph(navController: NavHostController) {
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomNavScreens) {
                SpendlyBottomNavBar(
                    navController = navController,
                    currentRoute = currentRoute
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(navController)
            }
            composable(Screen.Login.route) {
                LoginScreen(navController)
            }
            composable(Screen.Register.route) {
                RegisterScreen(navController)
            }
            composable(Screen.Dashboard.route) {
                DashboardScreen(navController)
            }
            composable(Screen.AddIncome.route) {
                AddIncomeScreen(navController)
            }
            composable(Screen.AddExpense.route) {
                AddExpenseScreen(navController)
            }
            composable(Screen.Transactions.route) {
                TransactionsScreen(navController)
            }
            composable(Screen.Analytics.route) {
                AnalyticsScreen(navController)
            }
            composable(Screen.GoalTracker.route) {
                GoalTrackerScreen(navController)
            }
            composable(Screen.EditGoal.route) {
                EditGoalScreen(navController)
            }
            composable(Screen.PrimaryGoal.route) {
                PrimaryGoalScreen(navController)
            }
            composable(Screen.Profile.route) {
                ProfileScreen(navController)
            }
        }
    }
}

@Composable
fun ScreenPlaceholder(name: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = name, style = MaterialTheme.typography.headlineMedium)
    }
}
