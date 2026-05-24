package com.spendly.financetracker.ui.screen.home

import android.app.Activity
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendly.financetracker.ui.components.NoTransactionsState
import com.spendly.financetracker.ui.components.SpendlyAddActionMenu
import com.spendly.financetracker.ui.components.TransactionListItem
import com.spendly.financetracker.ui.theme.SpendlyGray100
import com.spendly.financetracker.ui.theme.SpendlyGray300
import com.spendly.financetracker.ui.theme.SpendlyGray500
import com.spendly.financetracker.ui.theme.SpendlyGray700
import com.spendly.financetracker.ui.theme.SpendlyGray900
import com.spendly.financetracker.ui.theme.SpendlyGreen
import com.spendly.financetracker.ui.theme.SpendlyGreenLight
import com.spendly.financetracker.ui.theme.SpendlyRed
import com.spendly.financetracker.ui.util.currentMonthLabel
import com.spendly.financetracker.ui.util.displayNameFromEmail
import com.spendly.financetracker.ui.util.formatMoney
import com.spendly.financetracker.ui.util.greetingForNow
import com.spendly.financetracker.ui.util.goalIconForKey
import com.spendly.financetracker.ui.util.initialsFromEmail
import com.spendly.financetracker.ui.viewmodel.FinanceUiState
import androidx.core.view.WindowInsetsControllerCompat

@Composable
fun HomeScreen(
    state: FinanceUiState,
    onOpenProfile: () -> Unit,
    onOpenTransactions: () -> Unit,
    onOpenGoal: () -> Unit,
    onAddIncome: () -> Unit,
    onAddExpense: () -> Unit
) {
    val view = LocalView.current
    DisposableEffect(view) {
        val window = (view.context as? Activity)?.window
        val previousColor = window?.statusBarColor
        if (window != null) {
            window.statusBarColor = SpendlyGreen.toArgb()
            WindowInsetsControllerCompat(window, view).isAppearanceLightStatusBars = false
        }
        onDispose {
            if (window != null && previousColor != null) {
                window.statusBarColor = previousColor
                WindowInsetsControllerCompat(window, view).isAppearanceLightStatusBars = true
            }
        }
    }
    val recentTransactions = state.recentTransactions
    val userName = state.profile?.name?.takeIf { it.isNotBlank() } ?: displayNameFromEmail(state.session?.email)
    val userInitials = initialsFromEmail(userName)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            DashboardHeader(
                userName = userName,
                userInitials = userInitials,
                balanceCents = state.balanceCents,
                onOpenProfile = onOpenProfile
            )

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        DashboardSummaryCard(
                            label = "Monthly Income",
                            amount = formatMoney(state.currentMonthIncomeCents),
                            amountColor = SpendlyGreen,
                            modifier = Modifier.weight(1f)
                        )
                        DashboardSummaryCard(
                            label = "Monthly expense",
                            amount = formatMoney(state.currentMonthExpenseCents),
                            amountColor = SpendlyRed,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    NetSavingsCard(state = state)
                }

                state.primaryGoal?.let { goal ->
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = onOpenGoal),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(0.5.dp, SpendlyGray300),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(SpendlyGray100, RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            goalIconForKey(goal.iconKey),
                                            contentDescription = null,
                                            tint = SpendlyGray700,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            goal.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            "Target: ${goal.dueDate}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = SpendlyGray500
                                        )
                                    }
                                    Surface(
                                        color = SpendlyGreenLight,
                                        shape = RoundedCornerShape(20.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = null,
                                                tint = SpendlyGreen,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Text(
                                                "ON TRACK",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = SpendlyGreen,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }

                                LinearProgressIndicator(
                                    progress = { goal.progressPercent / 100f },
                                    color = SpendlyGreen,
                                    trackColor = SpendlyGreenLight,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                )

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "${formatMoney(goal.savedCents)} (${goal.progressPercent}%)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SpendlyGray500
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(
                                        "Goal: ${formatMoney(goal.targetCents)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SpendlyGray500
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(SpendlyGray100, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Need: ${formatMoney(state.primaryGoalMonthlyNeedCents)}/mo",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SpendlyGray700,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Row(
                                        modifier = Modifier.clickable(onClick = onOpenGoal),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(
                                            "View",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = SpendlyGreen
                                        )
                                        Icon(
                                            Icons.Default.ChevronRight,
                                            contentDescription = null,
                                            tint = SpendlyGreen,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Recent Transactions",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "See all ->",
                            style = MaterialTheme.typography.labelSmall,
                            color = SpendlyGreen,
                            modifier = Modifier.clickable(onClick = onOpenTransactions)
                        )
                    }
                }

                if (recentTransactions.isEmpty()) {
                    item {
                        NoTransactionsState(onAddExpense = onAddExpense)
                    }
                } else {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(0.5.dp, SpendlyGray300),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column {
                                recentTransactions.forEachIndexed { index, transaction ->
                                    TransactionListItem(
                                        transaction = transaction,
                                        showContainer = false
                                    )
                                    if (index < recentTransactions.lastIndex) {
                                        HorizontalDivider(thickness = 0.5.dp, color = SpendlyGray100)
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(92.dp))
                }
            }
        }

        SpendlyAddActionMenu(
            onAddIncome = onAddIncome,
            onAddExpense = onAddExpense,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
    }
}

@Composable
private fun DashboardHeader(
    userName: String,
    userInitials: String,
    balanceCents: Long,
    onOpenProfile: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(SpendlyGreen)
            .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 24.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "${greetingForNow()},",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Text(
                        userName,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f))
                        .clickable(onClick = onOpenProfile),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        userInitials,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SpendlyGreenLight.copy(alpha = 0.92f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Total Balance",
                        style = MaterialTheme.typography.labelMedium,
                        color = SpendlyGray700,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        formatMoney(balanceCents),
                        style = MaterialTheme.typography.headlineSmall,
                        color = if (balanceCents >= 0L) SpendlyGreen else SpendlyRed,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardSummaryCard(
    label: String,
    amount: String,
    amountColor: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(0.5.dp, SpendlyGray300),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = SpendlyGray500
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                amount,
                style = MaterialTheme.typography.titleLarge,
                color = amountColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun NetSavingsCard(state: FinanceUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(0.5.dp, SpendlyGray300),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                "Monthly Net savings",
                style = MaterialTheme.typography.titleMedium,
                color = SpendlyGray900,
                fontWeight = FontWeight.Bold
            )
            Text(
                currentMonthLabel(),
                style = MaterialTheme.typography.labelSmall,
                color = SpendlyGray500
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    formatMoney(state.currentMonthNetSavingsCents),
                    style = MaterialTheme.typography.headlineMedium,
                    color = if (state.currentMonthNetSavingsCents >= 0L) SpendlyGreen else SpendlyRed,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "${state.savingsRate}%",
                    style = MaterialTheme.typography.titleLarge,
                    color = SpendlyGreen,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                "Savings rate",
                style = MaterialTheme.typography.labelSmall,
                color = SpendlyGray500,
                modifier = Modifier.align(Alignment.End)
            )
            Text(
                "Yearly net savings - ${formatMoney(state.currentYearSavingsCents)}",
                style = MaterialTheme.typography.labelSmall,
                color = SpendlyGray500
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { state.savingsRate / 100f },
                color = SpendlyGreen,
                trackColor = SpendlyGreenLight,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            )
        }
    }
}
