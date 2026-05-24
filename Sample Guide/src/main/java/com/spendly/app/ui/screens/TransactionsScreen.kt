package com.spendly.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.spendly.app.data.model.TransactionItem
import com.spendly.app.data.model.amountLKR
import com.spendly.app.data.model.dateMs
import com.spendly.app.data.model.displayCategory
import com.spendly.app.data.model.displayName
import com.spendly.app.data.model.id
import com.spendly.app.data.model.isCommitted
import com.spendly.app.data.model.isIncome
import com.spendly.app.data.model.enums.TransactionFilter
import com.spendly.app.navigation.Screen
import com.spendly.app.ui.components.NoTransactionsState
import com.spendly.app.ui.components.TransactionIcon
import com.spendly.app.ui.theme.*
import com.spendly.app.utils.FormatUtils
import com.spendly.app.viewmodel.TransactionsViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TransactionsScreen(
    navController: NavController,
    viewModel: TransactionsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val filteredTransactions by viewModel.filteredTransactions.collectAsState()
    val groupedByDate by viewModel.groupedByDate.collectAsState()

    var monthDropdownExpanded by remember { mutableStateOf(false) }

    // Delete confirmation dialog
    uiState.deleteConfirmItem?.let { item ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissDelete() },
            title = { Text("Delete Transaction") },
            text = { Text("Are you sure you want to delete this transaction? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.deleteTransaction(item) },
                    colors = ButtonDefaults.textButtonColors(contentColor = SpendlyRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDelete() }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Transactions", style = SpendlyTypography.titleLarge, fontWeight = FontWeight.Bold)
                },
                actions = {
                    // Month selector dropdown chip
                    Box {
                        Surface(
                            color = SpendlyGray100,
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.clickable { monthDropdownExpanded = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CalendarMonth, null, tint = SpendlyGray700, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(uiState.selectedMonthLabel, style = SpendlyTypography.labelMedium, color = SpendlyGray700)
                                Icon(
                                    if (monthDropdownExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                    null,
                                    tint = SpendlyGray700,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = monthDropdownExpanded,
                            onDismissRequest = { monthDropdownExpanded = false }
                        ) {
                            viewModel.availableMonths.forEach { (label, start, end) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        viewModel.setMonth(start, end, label)
                                        monthDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                }
            )
        }
        // bottomBar removed - handled by NavGraph
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filter chips + transaction count
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TransactionFilter.entries.forEach { f ->
                        FilterChip(
                            selected = uiState.filter == f,
                            onClick = { viewModel.setFilter(f) },
                            label = { Text(f.displayName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SpendlyGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "${filteredTransactions.size} transactions",
                    style = SpendlyTypography.labelSmall,
                    color = SpendlyGray500
                )
            }

            HorizontalDivider(thickness = 0.5.dp, color = SpendlyGray300)

            if (groupedByDate.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    NoTransactionsState(onAddExpense = { navController.navigate(Screen.AddExpense.route) })
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    groupedByDate.forEach { (dateLabel, txList) ->
                        stickyHeader {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SpendlyGray50)
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "$dateLabel · ${txList.size}",
                                    style = SpendlyTypography.labelSmall,
                                    color = SpendlyGray500,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }

                        items(txList, key = { it.id }) { tx ->
                            TransactionListItem(
                                item = tx,
                                isExpanded = uiState.expandedItemId == tx.id,
                                onTap = { viewModel.toggleExpand(tx.id) },
                                onEdit = {
                                    navController.navigate(
                                        if (tx.isIncome) {
                                            Screen.AddIncome.editRoute(tx.id)
                                        } else {
                                            Screen.AddExpense.editRoute(tx.id)
                                        }
                                    )
                                },
                                onDelete = { viewModel.confirmDelete(tx) }
                            )
                            HorizontalDivider(thickness = 0.5.dp, color = SpendlyGray100)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionListItem(
    item: TransactionItem,
    isExpanded: Boolean,
    onTap: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        // Collapsed row
        Row(
            modifier = Modifier
                .clickable { onTap() }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TransactionIcon(item.displayCategory, item.isIncome, size = 38.dp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.displayName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(item.displayCategory, style = SpendlyTypography.labelSmall, color = SpendlyGray500)
                    if (!item.isIncome) {
                        Surface(
                            color = if (item.isCommitted) SpendlyBlueLight else SpendlyAmberLight,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                if (item.isCommitted) "COMMITTED" else "DISCRETIONARY",
                                style = SpendlyTypography.labelSmall,
                                fontSize = 9.sp,
                                color = if (item.isCommitted) SpendlyBlue else SpendlyAmber,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (item.isIncome) "+${formatLKR(item.amountLKR)}" else "−${formatLKR(item.amountLKR)}",
                    color = if (item.isIncome) SpendlyGreen else SpendlyRed,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = SpendlyGray500,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        // Expanded section
        AnimatedVisibility(visible = isExpanded) {
            Column(
                modifier = Modifier
                    .padding(start = 66.dp, end = 16.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val note = when(item) {
                    is TransactionItem.Income -> item.entry.note
                    is TransactionItem.Expense -> item.entry.note
                }
                
                if (!note.isNullOrBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Notes, null, tint = SpendlyGray500, modifier = Modifier.size(14.dp))
                        Text(note, style = MaterialTheme.typography.bodySmall, color = SpendlyGray500)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.CalendarToday, null, tint = SpendlyGray500, modifier = Modifier.size(14.dp))
                    Text(formatDateFull(item.dateMs), style = MaterialTheme.typography.bodySmall, color = SpendlyGray500)
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = onEdit,
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SpendlyGreen),
                        border = BorderStroke(1.dp, SpendlyGreen)
                    ) {
                        Text("Edit", style = SpendlyTypography.labelMedium)
                    }
                    Spacer(Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = onDelete,
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SpendlyRed),
                        border = BorderStroke(1.dp, SpendlyRed)
                    ) {
                        Text("Delete", style = SpendlyTypography.labelMedium)
                    }
                }
            }
        }
    }
}

private fun formatLKR(amount: Double): String {
    return FormatUtils.formatLKR(amount)
}

private fun formatDateFull(timeMs: Long): String {
    return FormatUtils.formatDateShort(timeMs)
}
