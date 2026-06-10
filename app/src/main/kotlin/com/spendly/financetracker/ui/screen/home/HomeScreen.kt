package com.spendly.financetracker.ui.screen.home

import android.app.Activity
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.spendly.financetracker.data.model.BudgetProgress
import com.spendly.financetracker.data.model.RecurringRule
import com.spendly.financetracker.data.model.SavingsGoal
import com.spendly.financetracker.ui.components.EmptyState
import com.spendly.financetracker.ui.components.SectionHeader
import com.spendly.financetracker.ui.components.SpendlyRadius
import com.spendly.financetracker.ui.components.SpendlySpacing
import com.spendly.financetracker.ui.components.TransactionListItem
import com.spendly.financetracker.ui.theme.SpendlyAmber
import com.spendly.financetracker.ui.theme.SpendlyGreen
import com.spendly.financetracker.ui.theme.SpendlyGreenDark
import com.spendly.financetracker.ui.theme.SpendlyGreenLight
import com.spendly.financetracker.ui.theme.SpendlyRed
import com.spendly.financetracker.ui.theme.SpendlyRedLight
import com.spendly.financetracker.ui.theme.AccentColorKey
import com.spendly.financetracker.ui.theme.spendlyAccentPalette
import com.spendly.financetracker.ui.util.currentMonthLabel
import com.spendly.financetracker.ui.util.displayNameFromEmail
import com.spendly.financetracker.ui.util.formatDateShort
import com.spendly.financetracker.ui.util.formatMoney
import com.spendly.financetracker.ui.util.goalIconForKey
import com.spendly.financetracker.ui.util.greetingForNow
import com.spendly.financetracker.ui.util.initialsFromEmail
import com.spendly.financetracker.ui.viewmodel.FinanceUiState
import com.spendly.financetracker.ui.viewmodel.BudgetViewModel
import com.spendly.financetracker.ui.viewmodel.RecurringViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

@Composable
fun HomeScreen(
    state: FinanceUiState,
    onOpenProfile: () -> Unit,
    onOpenTransactions: () -> Unit,
    onOpenGoal: () -> Unit,
    onOpenGoals: () -> Unit,
    onOpenBudget: () -> Unit,
    onOpenRecurring: () -> Unit,
    onOpenAnalytics: () -> Unit,
    onOpenNotifications: () -> Unit,
    onAddIncome: () -> Unit,
    onAddExpense: () -> Unit
) {
    val budgetViewModel: BudgetViewModel = hiltViewModel()
    val budgetState by budgetViewModel.uiState.collectAsStateWithLifecycle()
    val recurringViewModel: RecurringViewModel = hiltViewModel()
    val recurringState by recurringViewModel.uiState.collectAsStateWithLifecycle()
    val accentPalette = spendlyAccentPalette(state.profile?.accentColorKey)
    val view = LocalView.current
    val useDarkStatusIcons = MaterialTheme.colorScheme.background.luminance() > 0.5f
    DisposableEffect(view, useDarkStatusIcons, accentPalette.dark) {
        val window = (view.context as? Activity)?.window
        val previousColor = window?.statusBarColor
        if (window != null) {
            window.statusBarColor = accentPalette.dark.toArgb()
            WindowInsetsControllerCompat(window, view).isAppearanceLightStatusBars = false
        }
        onDispose {
            if (window != null && previousColor != null) {
                window.statusBarColor = previousColor
                WindowInsetsControllerCompat(window, view).isAppearanceLightStatusBars = useDarkStatusIcons
            }
        }
    }

    val userName = state.profile?.name?.takeIf { it.isNotBlank() } ?: displayNameFromEmail(state.session?.email)
    val userInitials = initialsFromEmail(userName)
    val profileBitmap = rememberHomeProfileBitmap(state.profile?.profileImageUri)
    val recentTransactions = state.recentTransactions.take(3)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(
            start = SpendlySpacing.screenHorizontal,
            top = SpendlySpacing.screenTop,
            end = SpendlySpacing.screenHorizontal,
            bottom = SpendlySpacing.homeScreenBottomPadding
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            HomeGreetingHeader(
                userName = userName,
                userInitials = userInitials,
                profileImage = profileBitmap,
                notificationCount = 0,
                onOpenProfile = onOpenProfile,
                onOpenNotifications = onOpenNotifications
            )
        }

        item {
            PremiumHeroCard(state = state)
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickActionCard(
                    title = "Add Income",
                    subtitle = "Record money in",
                    icon = Icons.Default.Add,
                    accent = SpendlyGreen,
                    container = SpendlyGreenLight,
                    modifier = Modifier.weight(1f),
                    onClick = onAddIncome
                )
                QuickActionCard(
                    title = "Add Expense",
                    subtitle = "Track spending",
                    icon = Icons.Default.Payments,
                    accent = SpendlyRed,
                    container = SpendlyRedLight,
                    modifier = Modifier.weight(1f),
                    onClick = onAddExpense
                )
            }
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                item { ShortcutTile("Goals", Icons.Default.TrackChanges, onOpenGoals, Modifier.width(86.dp)) }
                item { ShortcutTile("Budget", Icons.Default.AccountBalanceWallet, onOpenBudget, Modifier.width(86.dp)) }
                item { ShortcutTile("Recurring", Icons.Default.EventRepeat, onOpenRecurring, Modifier.width(96.dp)) }
                item { ShortcutTile("Analytics", Icons.Default.Analytics, onOpenAnalytics, Modifier.width(92.dp)) }
                item { ShortcutTile("History", Icons.Default.Receipt, onOpenTransactions, Modifier.width(86.dp)) }
                item { ShortcutTile("Profile", Icons.Default.Person, onOpenProfile, Modifier.width(86.dp)) }
            }
        }

        if (budgetState.budgets.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Budget Watch",
                    subtitle = "Top category limits this month",
                    actionLabel = "Open",
                    onAction = onOpenBudget
                )
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(budgetState.budgets.take(3), key = { it.budget.id }) { item ->
                        HomeBudgetPreviewCard(item = item, modifier = Modifier.width(230.dp), onClick = onOpenBudget)
                    }
                }
            }
        }

        recurringState.activeRules.minByOrNull { it.nextRunDateMillis }?.let { nextRule ->
            item {
                SectionHeader(
                    title = "Next Recurring",
                    subtitle = "Upcoming automated record",
                    actionLabel = "Manage",
                    onAction = onOpenRecurring
                )
            }
            item {
                HomeRecurringPreviewCard(rule = nextRule, onClick = onOpenRecurring)
            }
        }

        item {
            SectionHeader(
                title = "My Goals",
                subtitle = "Stay close to the targets that matter",
                actionLabel = "View all",
                onAction = onOpenGoals
            )
        }

        if (state.goals.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Default.TrackChanges,
                    title = "No goals yet",
                    subtitle = "Create a goal to track your next milestone",
                    actionLabel = "Add goal",
                    onAction = onOpenGoals
                )
            }
        } else {
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(end = 28.dp)
                ) {
                    items(state.goals.take(5), key = SavingsGoal::id) { goal ->
                        PremiumGoalCard(
                            goal = goal,
                            fallbackAccentColorKey = state.profile?.accentColorKey,
                            modifier = Modifier.width(292.dp),
                            onClick = {
                                if (goal.id == state.primaryGoal?.id) onOpenGoal() else onOpenGoals()
                            }
                        )
                    }
                }
            }
        }

        item {
            SectionHeader(
                title = "Recent Transactions",
                actionLabel = "See all",
                onAction = onOpenTransactions
            )
        }

        if (recentTransactions.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Default.Receipt,
                    title = "No transactions yet",
                    subtitle = "Add income or expenses to start your finance timeline",
                    actionLabel = "Add expense",
                    onAction = onAddExpense
                )
            }
        } else {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)),
                    shape = RoundedCornerShape(SpendlyRadius.panel),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    recentTransactions.forEachIndexed { index, transaction ->
                        TransactionListItem(transaction = transaction, showContainer = false)
                        if (index < recentTransactions.lastIndex) {
                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
                            )
                        }
                    }
                }
            }
        }
    }

}

@Composable
private fun HomeGreetingHeader(
    userName: String,
    userInitials: String,
    profileImage: ImageBitmap?,
    notificationCount: Int,
    onOpenProfile: () -> Unit,
    onOpenNotifications: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "${greetingForNow()},",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                userName,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Surface(
            onClick = onOpenProfile,
            modifier = Modifier.size(46.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (profileImage != null) {
                    Image(
                        bitmap = profileImage,
                        contentDescription = "Profile image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        userInitials,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Spacer(Modifier.width(8.dp))
        Surface(
            onClick = onOpenNotifications,
            modifier = Modifier.size(46.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                if (notificationCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(18.dp)
                            .background(SpendlyRed, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = notificationCount.coerceAtMost(9).toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PremiumHeroCard(state: FinanceUiState) {
    val progress = state.savingsRate / 100f
    val accentPalette = spendlyAccentPalette(state.profile?.accentColorKey)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(accentPalette.dark, accentPalette.primary, accentPalette.gradientEnd)
                    )
                )
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Total Balance", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.72f))
                    Text(
                        formatMoney(state.balanceCents),
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.16f)) {
                    Icon(
                        Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(12.dp).size(24.dp)
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HeroMetric("Income", formatMoney(state.currentMonthIncomeCents), SpendlyGreenLight, Modifier.weight(1f))
                HeroMetric("Expenses", formatMoney(state.currentMonthExpenseCents), Color(0xFFFFE7E7), Modifier.weight(1f))
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(currentMonthLabel(), style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.78f), modifier = Modifier.weight(1f))
                    Text("${state.savingsRate}% savings rate", style = MaterialTheme.typography.labelMedium, color = Color.White, fontWeight = FontWeight.Bold)
                }
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.22f)
                )
                Text(
                    "Monthly net savings ${formatMoney(state.currentMonthNetSavingsCents)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.86f)
                )
            }
        }
    }
}

@Composable
private fun HeroMetric(label: String, amount: String, tint: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.68f))
            Text(amount, style = MaterialTheme.typography.titleSmall, color = tint, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    container: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(92.dp),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, accent.copy(alpha = 0.16f)),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(container, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accent)
            }
            Column {
                Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ShortcutTile(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = MaterialTheme.colorScheme.primary
    Surface(
        onClick = onClick,
        modifier = modifier.height(76.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(22.dp))
            Spacer(Modifier.height(6.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun HomeBudgetPreviewCard(
    item: BudgetProgress,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val accent = when {
        item.isExceeded -> SpendlyRed
        item.isWarning -> SpendlyAmber
        else -> MaterialTheme.colorScheme.primary
    }
    Surface(
        onClick = onClick,
        modifier = modifier.height(134.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.5.dp, accent.copy(alpha = 0.22f)),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(accent.copy(alpha = 0.12f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        item.budget.category,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        if (item.isExceeded) "Limit exceeded" else "Remaining ${formatMoney(item.remainingCents)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                Text(
                    "${item.progressPercent}%",
                    style = MaterialTheme.typography.labelLarge,
                    color = accent,
                    fontWeight = FontWeight.Bold
                )
            }
            LinearProgressIndicator(
                progress = { (item.progressPercent / 100f).coerceIn(0f, 1f) },
                color = accent,
                trackColor = accent.copy(alpha = 0.14f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${formatMoney(item.spentCents)} spent",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "Limit ${formatMoney(item.budget.limitCents)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun HomeRecurringPreviewCard(
    rule: RecurringRule,
    onClick: () -> Unit
) {
    val isIncome = rule.type.name == "INCOME"
    val accent = if (isIncome) SpendlyGreen else SpendlyRed
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(112.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.5.dp, accent.copy(alpha = 0.2f)),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(accent.copy(alpha = 0.12f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.EventRepeat, contentDescription = null, tint = accent)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    rule.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${rule.frequency.name.lowercase().replaceFirstChar { char -> char.uppercase() }} • Next ${formatDateShort(rule.nextRunDateMillis)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Text(
                    rule.category ?: rule.source ?: if (isIncome) "Income" else "Expense",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    formatMoney(rule.amountCents),
                    style = MaterialTheme.typography.titleSmall,
                    color = accent,
                    fontWeight = FontWeight.Bold
                )
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun PremiumGoalCard(
    goal: SavingsGoal,
    fallbackAccentColorKey: String?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val progress = goal.progressPercent / 100f
    val accent = spendlyAccentPalette(
        goal.iconAccentColorKey
            .takeUnless { it.equals(AccentColorKey.GREEN.storageValue, ignoreCase = true) }
            ?: fallbackAccentColorKey
    )
    Surface(
        onClick = onClick,
        modifier = modifier.height(166.dp),
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(accent.light, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(goalIconForKey(goal.iconKey), contentDescription = null, tint = accent.primary)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(goal.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(goal.dueDate, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                StatusChip(goal.status.ifBlank { "Tracking" }, accent.primary)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(formatMoney(goal.savedCents), style = MaterialTheme.typography.titleSmall, color = accent.primary, fontWeight = FontWeight.Bold)
                Text(" / ${formatMoney(goal.targetCents)}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.weight(1f))
                Text("${goal.progressPercent}%", style = MaterialTheme.typography.titleSmall, color = accent.primary, fontWeight = FontWeight.Bold)
            }
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                color = accent.primary,
                trackColor = accent.light,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Need steady monthly savings", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = accent.primary, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun rememberHomeProfileBitmap(uri: String?): ImageBitmap? {
    val context = LocalContext.current
    val image by produceState<ImageBitmap?>(initialValue = null, key1 = uri) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                uri?.takeIf { it.isNotBlank() }?.let {
                    val openStream = {
                        if (it.startsWith("http://") || it.startsWith("https://")) {
                            URL(it).openStream()
                        } else {
                            context.contentResolver.openInputStream(Uri.parse(it))
                        }
                    }
                    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    openStream()?.use { input ->
                        BitmapFactory.decodeStream(input, null, bounds)
                    }
                    val options = BitmapFactory.Options().apply {
                        var sample = 1
                        while (bounds.outWidth / sample > 384 || bounds.outHeight / sample > 384) sample *= 2
                        inSampleSize = sample
                    }
                    openStream()?.use { input ->
                        BitmapFactory.decodeStream(input, null, options)?.asImageBitmap()
                    }
                }
            }.getOrNull()
        }
    }
    return image
}

@Composable
private fun StatusChip(status: String, fallbackColor: Color = MaterialTheme.colorScheme.primary) {
    val normalized = status.uppercase()
    val color = when {
        "ACHIEV" in normalized -> SpendlyAmber
        "OFF" in normalized -> SpendlyRed
        else -> fallbackColor
    }
    Surface(
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
            Text(
                if ("TRACK" in normalized) "ON TRACK" else normalized,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
        }
    }
}
