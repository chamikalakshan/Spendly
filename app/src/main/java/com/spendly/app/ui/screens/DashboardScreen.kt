package com.spendly.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.spendly.app.data.model.*
import com.spendly.app.navigation.Screen
import com.spendly.app.ui.components.NoTransactionsState
import com.spendly.app.ui.components.TransactionIcon
import com.spendly.app.ui.theme.*
import com.spendly.app.utils.FormatUtils
import com.spendly.app.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val userName by viewModel.userName.collectAsState()
    val userInitials by viewModel.userInitials.collectAsState()
    val income by viewModel.currentMonthIncome.collectAsState()
    val expenses by viewModel.currentMonthExpenses.collectAsState()
    val netSavings by viewModel.netSavings.collectAsState()
    val savingsRate by viewModel.savingsRate.collectAsState()
    val recentTransactions by viewModel.recentTransactions.collectAsState()
    val activeGoal by viewModel.activeGoal.collectAsState()
    val goalProgressPercent by viewModel.goalProgressPercent.collectAsState()
    val requiredMonthly by viewModel.requiredMonthlySavings.collectAsState()
    val isOnTrack by viewModel.isOnTrack.collectAsState()

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Add expense") },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                containerColor = SpendlyGreen,
                contentColor = Color.White,
                onClick = { navController.navigate(Screen.AddExpense.route) }
            )
        }
        // bottomBar REMOVED - handled by NavGraph
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // SECTION 1 — Full green header
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
                                "${FormatUtils.getGreeting()},",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    userName,
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text("👋", fontSize = 22.sp)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.25f))
                                .clickable { navController.navigate(Screen.Profile.route) },
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

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    "Income",
                                    style = SpendlyTypography.labelSmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    formatLKR(income),
                                    style = SpendlyTypography.titleLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    "Expenses",
                                    style = SpendlyTypography.labelSmall,
                                    color = SpendlyAmber.copy(alpha = 0.9f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    formatLKR(expenses),
                                    style = SpendlyTypography.titleLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(0.5.dp, SpendlyGray300)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                "Net Savings · May 2026",
                                style = SpendlyTypography.labelSmall,
                                color = SpendlyGray500
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    formatLKR(netSavings),
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = SpendlyGreen,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    "$savingsRate%",
                                    style = SpendlyTypography.titleLarge,
                                    color = SpendlyGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                "Savings rate",
                                style = SpendlyTypography.labelSmall,
                                color = SpendlyGray500,
                                modifier = Modifier.align(Alignment.End)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { savingsRate / 100f },
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

                activeGoal?.let { goal ->
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { navController.navigate(Screen.PrimaryGoal.route) },
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(0.5.dp, SpendlyGray300)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(SpendlyGray100, RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Laptop,
                                            contentDescription = null,
                                            tint = SpendlyGray700,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(goal.goalName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Text(
                                            "Target: ${FormatUtils.formatMonthYear(goal.targetDate)}",
                                            style = SpendlyTypography.labelSmall,
                                            color = SpendlyGray500
                                        )
                                    }
                                    if (isOnTrack) {
                                        Surface(
                                            color = SpendlyGreenLight,
                                            shape = RoundedCornerShape(20.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(Icons.Default.Check, contentDescription = null, tint = SpendlyGreen, modifier = Modifier.size(12.dp))
                                                Text(
                                                    "ON TRACK",
                                                    style = SpendlyTypography.labelSmall,
                                                    color = SpendlyGreen,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }
                                    }
                                }

                                LinearProgressIndicator(
                                    progress = { goalProgressPercent },
                                    color = SpendlyGreen,
                                    trackColor = SpendlyGreenLight,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                )

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val savedAmount = goal.targetAmount * goalProgressPercent
                                    Text(
                                        "${formatLKR(savedAmount)} (${(goalProgressPercent * 100).toInt()}%)",
                                        style = SpendlyTypography.labelSmall,
                                        color = SpendlyGray500
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(
                                        "Goal: ${formatLKR(goal.targetAmount)}",
                                        style = SpendlyTypography.labelSmall,
                                        color = SpendlyGray500
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(SpendlyGray50)
                                        .padding(horizontal = 10.dp, vertical = 8.dp)
                                        .clip(RoundedCornerShape(6.dp)),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Need: ${formatLKR(requiredMonthly)}/mo",
                                        style = SpendlyTypography.labelSmall,
                                        color = SpendlyGray700,
                                        modifier = Modifier.weight(1f)
                                    )
                                    TextButton(
                                        onClick = { navController.navigate(Screen.PrimaryGoal.route) },
                                        contentPadding = PaddingValues(0.dp),
                                        modifier = Modifier.height(24.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text("View", style = SpendlyTypography.labelSmall, color = SpendlyGreen)
                                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = SpendlyGreen, modifier = Modifier.size(14.dp))
                                        }
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
                        Text("Recent Transactions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(
                            "See all →",
                            style = SpendlyTypography.labelSmall,
                            color = SpendlyGreen,
                            modifier = Modifier.clickable { navController.navigate(Screen.Transactions.route) }
                        )
                    }
                }

                if (recentTransactions.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            NoTransactionsState(onAction = { navController.navigate(Screen.AddExpense.route) })
                        }
                    }
                } else {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(0.5.dp, SpendlyGray300)
                        ) {
                            Column {
                                recentTransactions.take(6).forEachIndexed { index, tx ->
                                    TransactionRow(tx)
                                    if (index < recentTransactions.take(6).size - 1) {
                                        HorizontalDivider(thickness = 0.5.dp, color = SpendlyGray100)
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
}

@Composable
private fun TransactionRow(tx: TransactionItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TransactionIcon(tx.displayCategory, tx.isIncome)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(tx.displayName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(tx.displayCategory, style = SpendlyTypography.labelSmall, color = SpendlyGray500)
                
                val badgeColor = if (tx.isCommitted) SpendlyBlue else SpendlyAmber
                val badgeBg = if (tx.isCommitted) SpendlyBlueLight else SpendlyAmberLight
                
                val badgeText = if (tx.isCommitted) "COMMITTED" else if (!tx.isIncome) "DISCRETIONARY" else ""
                
                if (badgeText.isNotEmpty()) {
                    Surface(
                        color = badgeBg,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            badgeText,
                            style = SpendlyTypography.labelSmall,
                            fontSize = 9.sp,
                            color = badgeColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(formatDateShort(tx.dateMs), style = SpendlyTypography.labelSmall, color = SpendlyGray500)
            }
        }
        Text(
            text = if (tx.isIncome) "+${formatLKR(tx.amountLKR)}" else "−${formatLKR(tx.amountLKR)}",
            color = if (tx.isIncome) SpendlyGreen else SpendlyRed,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun formatLKR(amount: Double): String {
    return FormatUtils.formatLKR(amount)
}

private fun formatDateShort(timeMs: Long): String {
    return FormatUtils.formatDateShort(timeMs)
}
