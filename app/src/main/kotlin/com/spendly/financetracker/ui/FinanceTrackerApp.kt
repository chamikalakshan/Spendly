package com.spendly.financetracker.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.spendly.financetracker.ui.components.AppBottomNavigation
import com.spendly.financetracker.ui.components.SpendlySpacing
import com.spendly.financetracker.ui.navigation.Screen
import com.spendly.financetracker.ui.navigation.bottomNavRoutes
import com.spendly.financetracker.ui.screen.AuthScreen
import com.spendly.financetracker.ui.screen.CreateAccountScreen
import com.spendly.financetracker.ui.screen.FirebaseSetupScreen
import com.spendly.financetracker.ui.screen.SplashScreen
import com.spendly.financetracker.ui.screen.analytics.AnalyticsScreen
import com.spendly.financetracker.ui.screen.budget.BudgetScreen
import com.spendly.financetracker.ui.screen.goals.GoalsScreen
import com.spendly.financetracker.ui.screen.goals.AddGoalScreen
import com.spendly.financetracker.ui.screen.goals.EditGoalScreen
import com.spendly.financetracker.ui.screen.goals.GoalDetailScreen
import com.spendly.financetracker.ui.screen.home.HomeScreen
import com.spendly.financetracker.ui.screen.notifications.NotificationsScreen
import com.spendly.financetracker.ui.screen.profile.ProfileScreen
import com.spendly.financetracker.ui.screen.recurring.RecurringScreen
import com.spendly.financetracker.ui.screen.transactions.AddExpenseScreen
import com.spendly.financetracker.ui.screen.transactions.AddIncomeScreen
import com.spendly.financetracker.ui.screen.transactions.TransactionsScreen
import com.spendly.financetracker.ui.viewmodel.AppTab
import com.spendly.financetracker.ui.viewmodel.FinanceViewModel
import com.spendly.financetracker.ui.viewmodel.GoalsViewModel
import com.spendly.financetracker.ui.theme.SpendlyGreen
import com.spendly.financetracker.ui.theme.SpendlyRed

@Composable
fun FinanceTrackerApp(viewModel: FinanceViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var splashFinished by remember { mutableStateOf(false) }

    LaunchedEffect(state.message) {
        val message = state.message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.clearMessage()
    }

    when {
        !splashFinished -> SplashScreen(onFinished = { splashFinished = true })
        !state.isFirebaseConfigured -> Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
            FirebaseSetupScreen(contentPadding = padding)
        }
        state.isLoading -> Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
            LoadingScreen(padding)
        }
        state.session == null -> {
            val authNavController = rememberNavController()
            Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
                NavHost(
                    navController = authNavController,
                    startDestination = Screen.Auth.route,
                    enterTransition = { spendlyForwardEnterTransition() },
                    exitTransition = { spendlyForwardExitTransition() },
                    popEnterTransition = { spendlyBackEnterTransition() },
                    popExitTransition = { spendlyBackExitTransition() }
                ) {
                    composable(Screen.Auth.route) {
                        AuthScreen(
                            state = state,
                            contentPadding = padding,
                            onEmailChange = viewModel::updateEmail,
                            onPasswordChange = viewModel::updatePassword,
                            onSubmit = viewModel::submitAuth,
                            onForgotPassword = viewModel::sendPasswordReset,
                            onToggleMode = viewModel::toggleAuthMode,
                            onCreateAccount = { authNavController.navigate(Screen.CreateAccount.route) }
                        )
                    }

                    composable(Screen.CreateAccount.route) {
                        CreateAccountScreen(
                            contentPadding = padding,
                            onBack = { authNavController.popBackStack() }
                        )
                    }
                }
            }
        }
        else -> {
            val navController = rememberNavController()
            val currentBackStack by navController.currentBackStackEntryAsState()
            val currentRoute = currentBackStack?.destination?.route
            var showInitialIncomeDialog by remember(state.session?.uid) { mutableStateOf(false) }
            var initialIncomeAmount by remember(state.session?.uid) { mutableStateOf("") }
            var addMenuExpanded by remember { mutableStateOf(false) }

            LaunchedEffect(state.profile?.uid, state.transactions.size) {
                val profile = state.profile ?: return@LaunchedEffect
                val isNewAccount = System.currentTimeMillis() - profile.createdAtMillis < 2 * 60 * 1000L
                val hasInitialIncome = state.transactions.any {
                    it.type == com.spendly.financetracker.data.model.TransactionType.INCOME &&
                        it.source.equals("Initial Income", ignoreCase = true)
                }
                if (isNewAccount && !hasInitialIncome) {
                    showInitialIncomeDialog = true
                }
            }

            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                bottomBar = {
                    if (currentRoute in bottomNavRoutes) {
                        val currentTab = when {
                            currentRoute == Screen.Home.route || currentRoute?.startsWith("${Screen.Home.route}/") == true -> AppTab.HOME
                            currentRoute == Screen.Events.route -> AppTab.TRANSACTIONS
                            currentRoute == Screen.Analytics.route -> AppTab.ANALYTICS
                            currentRoute == Screen.Profile.route -> AppTab.PROFILE
                            else -> null
                        }
                        AppBottomNavigation(
                            currentTab = currentTab,
                            addMenuExpanded = addMenuExpanded,
                            onAddClick = { addMenuExpanded = !addMenuExpanded },
                            onTabSelected = { tab ->
                                addMenuExpanded = false
                                val route = when (tab) {
                                    AppTab.HOME -> Screen.Home.route
                                    AppTab.TRANSACTIONS -> Screen.Events.route
                                    AppTab.ANALYTICS -> Screen.Analytics.route
                                    AppTab.GOALS -> Screen.Goals.route
                                    AppTab.PROFILE -> Screen.Profile.route
                                }
                                if (currentRoute != route) {
                                    navController.navigate(route) {
                                        popUpTo(Screen.Home.route) {
                                            saveState = tab != AppTab.HOME
                                            inclusive = false
                                        }
                                        launchSingleTop = true
                                        restoreState = tab != AppTab.HOME
                                    }
                                }
                            }
                        )
                    }
                }
            ) { _ ->
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Home.route,
                        modifier = Modifier.fillMaxSize(),
                        enterTransition = { spendlyForwardEnterTransition() },
                        exitTransition = { spendlyForwardExitTransition() },
                        popEnterTransition = { spendlyBackEnterTransition() },
                        popExitTransition = { spendlyBackExitTransition() }
                    ) {
                        composable(Screen.Home.route) {
                            HomeScreen(
                                state = state,
                                onOpenProfile = { navController.navigate(Screen.Profile.route) },
                                onOpenTransactions = { navController.navigate(Screen.Events.route) },
                                onOpenGoal = {
                                    state.primaryGoal?.let { navController.navigate(Screen.GoalDetails.detailRoute(it.id)) }
                                        ?: navController.navigate(Screen.Goals.route)
                                },
                                onOpenGoals = { navController.navigate(Screen.Goals.route) },
                                onOpenBudget = { navController.navigate(Screen.Budget.route) },
                                onOpenRecurring = { navController.navigate(Screen.Recurring.route) },
                                onOpenAnalytics = { navController.navigate(Screen.Analytics.route) },
                                onOpenNotifications = { navController.navigate(Screen.Notifications.route) },
                                onAddIncome = { navController.navigate(Screen.AddIncome.route) },
                                onAddExpense = { navController.navigate(Screen.AddExpense.route) }
                            )
                        }

                        composable(Screen.Events.route) {
                            TransactionsScreen(navController = navController)
                        }

                        composable(Screen.Analytics.route) {
                            AnalyticsScreen()
                        }

                        composable(Screen.Budget.route) {
                            BudgetScreen(onBack = { navController.popBackStack() })
                        }

                        composable(Screen.Recurring.route) {
                            RecurringScreen(onBack = { navController.popBackStack() })
                        }

                        composable(Screen.Notifications.route) {
                            NotificationsScreen(onBack = { navController.popBackStack() })
                        }

                        composable(Screen.Goals.route) {
                            GoalsScreen(
                                state = state,
                                onAddGoal = { navController.navigate(Screen.AddGoal.route) },
                                onGoalSelected = { goalId -> navController.navigate(Screen.GoalDetails.detailRoute(goalId)) }
                            )
                        }

                        composable(Screen.AddGoal.route) {
                            val goalsViewModel: GoalsViewModel = hiltViewModel()
                            val goalsState by goalsViewModel.uiState.collectAsStateWithLifecycle()
                            LaunchedEffect(goalsState.error) {
                                goalsState.error?.let { snackbarHostState.showSnackbar(it) }
                            }
                            AddGoalScreen(
                                onBack = { navController.popBackStack() },
                                onSave = { draft ->
                                    val accepted = goalsViewModel.saveDraft(draft)
                                    if (accepted) navController.popBackStack()
                                    accepted
                                }
                            )
                        }

                        composable(
                            route = Screen.GoalDetails.routeWithArgs,
                            arguments = listOf(navArgument(Screen.GoalDetails.ARG_ID) {
                                type = NavType.StringType; nullable = true; defaultValue = null
                            })
                        ) { entry ->
                            val goalsViewModel: GoalsViewModel = hiltViewModel()
                            val goalsState by goalsViewModel.uiState.collectAsStateWithLifecycle()
                            LaunchedEffect(goalsState.error) {
                                goalsState.error?.let { snackbarHostState.showSnackbar(it) }
                            }
                            GoalDetailScreen(
                                state = state,
                                goalId = entry.arguments?.getString(Screen.GoalDetails.ARG_ID),
                                onAddSavings = goalsViewModel::addSavings,
                                onEdit = { goalId -> navController.navigate(Screen.EditGoal.editRoute(goalId)) },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(
                            route = Screen.EditGoal.routeWithArgs,
                            arguments = listOf(navArgument(Screen.EditGoal.ARG_ID) {
                                type = NavType.StringType; nullable = true; defaultValue = null
                            })
                        ) { entry ->
                            val goalsViewModel: GoalsViewModel = hiltViewModel()
                            val goalsState by goalsViewModel.uiState.collectAsStateWithLifecycle()
                            LaunchedEffect(goalsState.error) {
                                goalsState.error?.let { snackbarHostState.showSnackbar(it) }
                            }
                            val goalId = entry.arguments?.getString(Screen.EditGoal.ARG_ID)
                            val goal = state.goals.firstOrNull { it.id == goalId }
                            if (goal != null) {
                                EditGoalScreen(
                                    onBack = { navController.popBackStack() },
                                    goal = goal,
                                    onSave = { _, draft ->
                                        val accepted = goalsViewModel.saveDraft(draft, goal)
                                        if (accepted) navController.popBackStack()
                                        accepted
                                    },
                                    onDelete = { id ->
                                        val accepted = goalsViewModel.deleteGoal(id)
                                        if (accepted) navController.popBackStack(Screen.Goals.route, false)
                                        accepted
                                    }
                                )
                            } else {
                                LoadingScreen(PaddingValues())
                            }
                        }

                        composable(Screen.Profile.route) {
                            ProfileScreen(
                                state = state,
                                onUpdateProfile = viewModel::updateProfile,
                                onChangePassword = viewModel::changePassword,
                                onDeleteAccount = viewModel::deleteAccount,
                                onOpenBudget = { navController.navigate(Screen.Budget.route) },
                                onOpenRecurring = { navController.navigate(Screen.Recurring.route) },
                                onSignOut = {
                                    viewModel.signOut()
                                }
                            )
                        }

                        composable(
                            route = Screen.AddIncome.routeWithArgs,
                            arguments = listOf(navArgument(Screen.AddIncome.ARG_ID) {
                                type = NavType.StringType; nullable = true; defaultValue = null
                            })
                        ) {
                            AddIncomeScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(
                            route = Screen.AddExpense.routeWithArgs,
                            arguments = listOf(navArgument(Screen.AddExpense.ARG_ID) {
                                type = NavType.StringType; nullable = true; defaultValue = null
                            })
                        ) {
                            AddExpenseScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                    if (state.isSyncing) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .fillMaxWidth()
                        )
                    }

                }
            }
            AnimatedVisibility(
                visible = addMenuExpanded && currentRoute in bottomNavRoutes,
                enter = fadeIn(tween(140)) + scaleIn(initialScale = 0.96f, animationSpec = tween(170, easing = FastOutSlowInEasing)),
                exit = fadeOut(tween(110)) + scaleOut(targetScale = 0.96f, animationSpec = tween(130, easing = FastOutSlowInEasing)),
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colorStops = arrayOf(
                                        0f to Color.Black.copy(alpha = 0.50f),
                                        0.72f to Color.Black.copy(alpha = 0.46f),
                                        0.90f to Color.Black.copy(alpha = 0.30f),
                                        1f to Color.Transparent
                                    )
                                )
                            )
                    )
                    SpendlyFloatingAddMenu(
                        modifier = Modifier
                            .navigationBarsPadding()
                            .padding(bottom = SpendlySpacing.floatingNavContentPadding)
                            .fillMaxWidth()
                            .padding(horizontal = 28.dp),
                        onAddIncome = {
                            addMenuExpanded = false
                            navController.navigate(Screen.AddIncome.route)
                        },
                        onAddExpense = {
                            addMenuExpanded = false
                            navController.navigate(Screen.AddExpense.route)
                        },
                        onAddGoal = {
                            addMenuExpanded = false
                            navController.navigate(Screen.AddGoal.route)
                        },
                        onCreateBudget = {
                            addMenuExpanded = false
                            navController.navigate(Screen.Budget.route)
                        }
                    )
                }
            }

            if (showInitialIncomeDialog) {
                AlertDialog(
                    onDismissRequest = { },
                    title = { Text("Set initial income") },
                    text = {
                        OutlinedTextField(
                            value = initialIncomeAmount,
                            onValueChange = { value ->
                                if (value.isEmpty() || (value.all { it.isDigit() || it == '.' } && value.count { it == '.' } <= 1)) {
                                    initialIncomeAmount = value
                                }
                            },
                            label = { Text("Initial income") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    },
                    confirmButton = {
                        Button(onClick = {
                            viewModel.addInitialIncome(initialIncomeAmount)
                            showInitialIncomeDialog = false
                        }) { Text("Save") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showInitialIncomeDialog = false }) {
                            Text("Skip")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun SpendlyFloatingAddMenu(
    modifier: Modifier = Modifier,
    onAddIncome: () -> Unit,
    onAddExpense: () -> Unit,
    onAddGoal: () -> Unit,
    onCreateBudget: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            AddActionCard("Add Income", Icons.Default.TrendingUp, SpendlyGreen, onAddIncome, Modifier.weight(1f))
            AddActionCard("Add Expense", Icons.Default.TrendingDown, SpendlyRed, onAddExpense, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            AddActionCard("Add Goal", Icons.Default.Savings, MaterialTheme.colorScheme.primary, onAddGoal, Modifier.weight(1f))
            AddActionCard("Create Budget", Icons.Default.AccountBalanceWallet, MaterialTheme.colorScheme.primary, onCreateBudget, Modifier.weight(1f))
        }
    }
}

@Composable
private fun AddActionCard(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.32f)),
        shadowElevation = 18.dp,
        tonalElevation = 6.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier.size(30.dp).background(color.copy(alpha = 0.14f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        }
    }
}

@Composable
private fun LoadingScreen(contentPadding: PaddingValues) {
    Box(
        modifier = Modifier.fillMaxSize().padding(contentPadding),
        contentAlignment = Alignment.Center
    ) { CircularProgressIndicator() }
}

private const val SPENDLY_NAV_DURATION = 220
private const val SPENDLY_NAV_FADE_DURATION = 120

private fun AnimatedContentTransitionScope<NavBackStackEntry>.spendlyForwardEnterTransition(): EnterTransition {
    return slideIntoContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Left,
        animationSpec = tween(durationMillis = SPENDLY_NAV_DURATION, easing = FastOutSlowInEasing)
    ) + fadeIn(animationSpec = tween(durationMillis = SPENDLY_NAV_FADE_DURATION)) +
        scaleIn(
            initialScale = 0.985f,
            animationSpec = tween(durationMillis = SPENDLY_NAV_DURATION, easing = FastOutSlowInEasing)
        )
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.spendlyForwardExitTransition(): ExitTransition {
    return slideOutOfContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Left,
        animationSpec = tween(durationMillis = SPENDLY_NAV_DURATION, easing = FastOutSlowInEasing)
    ) + fadeOut(animationSpec = tween(durationMillis = SPENDLY_NAV_FADE_DURATION)) +
        scaleOut(
            targetScale = 0.99f,
            animationSpec = tween(durationMillis = SPENDLY_NAV_DURATION, easing = FastOutSlowInEasing)
        )
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.spendlyBackEnterTransition(): EnterTransition {
    return slideIntoContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Right,
        animationSpec = tween(durationMillis = SPENDLY_NAV_DURATION, easing = FastOutSlowInEasing)
    ) + fadeIn(animationSpec = tween(durationMillis = SPENDLY_NAV_FADE_DURATION)) +
        scaleIn(
            initialScale = 0.985f,
            animationSpec = tween(durationMillis = SPENDLY_NAV_DURATION, easing = FastOutSlowInEasing)
        )
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.spendlyBackExitTransition(): ExitTransition {
    return slideOutOfContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Right,
        animationSpec = tween(durationMillis = SPENDLY_NAV_DURATION, easing = FastOutSlowInEasing)
    ) + fadeOut(animationSpec = tween(durationMillis = SPENDLY_NAV_FADE_DURATION)) +
        scaleOut(
            targetScale = 0.99f,
            animationSpec = tween(durationMillis = SPENDLY_NAV_DURATION, easing = FastOutSlowInEasing)
        )
}
