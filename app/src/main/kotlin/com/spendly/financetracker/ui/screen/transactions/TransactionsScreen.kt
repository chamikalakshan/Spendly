package com.spendly.financetracker.ui.screen.transactions

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.spendly.financetracker.data.model.FinanceTransaction
import com.spendly.financetracker.data.model.TransactionType
import com.spendly.financetracker.ui.components.NoRecordsState
import com.spendly.financetracker.ui.components.SpendlyMonthPicker
import com.spendly.financetracker.ui.components.SpendlySpacing
import com.spendly.financetracker.ui.components.TransactionListItem
import com.spendly.financetracker.ui.navigation.Screen
import com.spendly.financetracker.ui.theme.SpendlyRed
import com.spendly.financetracker.ui.viewmodel.TransactionTab
import com.spendly.financetracker.ui.viewmodel.TransactionsViewModel

typealias OnTransactionTabSelected = (TransactionTab) -> Unit

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TransactionsScreen(
    navController: NavController
) {
    val viewModel: TransactionsViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("History", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
                    actions = {
                        SpendlyMonthPicker(
                            selectedLabel = state.selectedMonthLabel,
                            options = state.monthOptions,
                            onMonthSelected = viewModel::selectMonth,
                            modifier = Modifier.padding(end = SpendlySpacing.screenHorizontal)
                        )
                    }
                )
                if (state.isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 16.dp)
                        .padding(top = SpendlySpacing.screenTop, bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TransactionTab.values().forEach { tab ->
                            FilterChip(
                                selected = state.filter == tab,
                                onClick = { viewModel.setFilter(tab) },
                                label = { Text(tab.title) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }
                    Text(
                        "${state.filtered.size} transactions",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))

                if (state.filtered.isEmpty()) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .padding(bottom = SpendlySpacing.mainScreenBottomPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        when (state.filter) {
                            TransactionTab.ALL -> NoRecordsState()
                            TransactionTab.EXPENSES -> NoRecordsState(
                                actionLabel = "Add expense",
                                onAction = { navController.navigate(Screen.AddExpense.route) }
                            )
                            TransactionTab.INCOMES -> NoRecordsState(
                                actionLabel = "Add income",
                                onAction = { navController.navigate(Screen.AddIncome.route) }
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                        contentPadding = PaddingValues(
                            start = SpendlySpacing.screenHorizontal,
                            top = 12.dp,
                            end = SpendlySpacing.screenHorizontal,
                            bottom = SpendlySpacing.mainScreenBottomPadding
                        ),
                        verticalArrangement = Arrangement.spacedBy(SpendlySpacing.cardGap)
                    ) {
                        state.groupedTransactions.forEach { group ->
                            stickyHeader {
                                Box(
                                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background).padding(vertical = 4.dp)
                                ) {
                                    Text(
                                        "${group.label} - ${group.transactions.size}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                            items(items = group.transactions, key = FinanceTransaction::id) { transaction ->
                                TransactionListItem(
                                    transaction = transaction,
                                    showContainer = true,
                                    onEdit = {
                                        val route = when (transaction.type) {
                                            TransactionType.INCOME -> Screen.AddIncome.editRoute(transaction.id)
                                            TransactionType.EXPENSE -> Screen.AddExpense.editRoute(transaction.id)
                                        }
                                        navController.navigate(route)
                                    },
                                    onDelete = { viewModel.requestDelete(transaction) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    state.transactionPendingDelete?.let { transaction ->
        AlertDialog(
            onDismissRequest = viewModel::cancelDelete,
            title = { Text("Delete transaction?") },
            text = { Text("This will remove ${transaction.title} from your records.") },
            confirmButton = {
                TextButton(onClick = { viewModel.delete(transaction) }) {
                    Text("Delete", color = SpendlyRed)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelDelete) {
                    Text("Cancel")
                }
            }
        )
    }
}
