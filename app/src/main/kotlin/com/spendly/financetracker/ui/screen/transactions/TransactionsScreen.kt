package com.spendly.financetracker.ui.screen.transactions

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.spendly.financetracker.ui.components.SpendlyAddActionMenu
import com.spendly.financetracker.ui.components.TransactionListItem
import com.spendly.financetracker.ui.navigation.Screen
import com.spendly.financetracker.ui.theme.SpendlyGray100
import com.spendly.financetracker.ui.theme.SpendlyGray300
import com.spendly.financetracker.ui.theme.SpendlyGray500
import com.spendly.financetracker.ui.theme.SpendlyGray700
import com.spendly.financetracker.ui.theme.SpendlyGreen
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
    val state by viewModel.uiState.collectAsState()
    var monthMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0.dp),
                title = { Text("History", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
                actions = {
                    Box(modifier = Modifier.padding(end = 16.dp)) {
                        Surface(color = SpendlyGray100, shape = RoundedCornerShape(20.dp), onClick = { monthMenuExpanded = true }) {
                            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarMonth, null, tint = SpendlyGray700, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.size(4.dp))
                                Text(state.selectedMonthLabel, style = MaterialTheme.typography.labelMedium, color = SpendlyGray700)
                                Icon(Icons.Default.ArrowDropDown, null, tint = SpendlyGray700, modifier = Modifier.size(16.dp))
                            }
                        }
                        DropdownMenu(expanded = monthMenuExpanded, onDismissRequest = { monthMenuExpanded = false }) {
                            state.monthOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.label) },
                                    onClick = {
                                        viewModel.selectMonth(option.startMillis)
                                        monthMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TransactionTab.values().forEach { tab ->
                            FilterChip(
                                selected = state.filter == tab,
                                onClick = { viewModel.setFilter(tab) },
                                label = { Text(tab.title) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SpendlyGreen,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                    Text(
                        "${state.filtered.size} transactions",
                        style = MaterialTheme.typography.labelSmall,
                        color = SpendlyGray500
                    )
                }

                HorizontalDivider(thickness = 0.5.dp, color = SpendlyGray300)

                if (state.filtered.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        state.groupedTransactions.forEach { group ->
                            stickyHeader {
                                Box(
                                    modifier = Modifier.fillMaxWidth().background(SpendlyGray100).padding(horizontal = 16.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        "${group.label} - ${group.transactions.size}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SpendlyGray500,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                            items(items = group.transactions, key = FinanceTransaction::id) { transaction ->
                                TransactionListItem(
                                    transaction = transaction,
                                    showContainer = false,
                                    onEdit = {
                                        val route = when (transaction.type) {
                                            TransactionType.INCOME -> Screen.AddIncome.editRoute(transaction.id)
                                            TransactionType.EXPENSE -> Screen.AddExpense.editRoute(transaction.id)
                                        }
                                        navController.navigate(route)
                                    },
                                    onDelete = { viewModel.requestDelete(transaction) }
                                )
                                HorizontalDivider(thickness = 0.5.dp, color = SpendlyGray100)
                            }
                        }
                    }
                }
            }

            SpendlyAddActionMenu(
                onAddIncome = { navController.navigate(Screen.AddIncome.route) },
                onAddExpense = { navController.navigate(Screen.AddExpense.route) },
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 24.dp)
            )
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
