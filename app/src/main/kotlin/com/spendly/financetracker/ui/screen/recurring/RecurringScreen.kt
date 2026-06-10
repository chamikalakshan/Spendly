package com.spendly.financetracker.ui.screen.recurring

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spendly.financetracker.data.model.RecurringFrequency
import com.spendly.financetracker.data.model.RecurringRule
import com.spendly.financetracker.data.model.TransactionType
import com.spendly.financetracker.ui.components.EmptyState
import com.spendly.financetracker.ui.components.SectionHeader
import com.spendly.financetracker.ui.components.SpendlyRadius
import com.spendly.financetracker.ui.components.SpendlySizing
import com.spendly.financetracker.ui.components.SpendlySpacing
import com.spendly.financetracker.ui.theme.SpendlyGreen
import com.spendly.financetracker.ui.theme.SpendlyRed
import com.spendly.financetracker.ui.util.formatDateFull
import com.spendly.financetracker.ui.util.formatMoney
import com.spendly.financetracker.ui.viewmodel.RecurringViewModel
import com.spendly.financetracker.ui.viewmodel.defaultIncomeSources
import com.spendly.financetracker.ui.viewmodel.expenseCategories

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringScreen(onBack: () -> Unit = {}) {
    val viewModel: RecurringViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showEditor by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = { Text("Recurring", fontWeight = FontWeight.Bold) },
                actions = {
                    TextButton(onClick = viewModel::generateDueNow) { Text("Generate") }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(
                start = SpendlySpacing.screenHorizontal,
                top = SpendlySpacing.screenTop,
                end = SpendlySpacing.screenHorizontal,
                bottom = SpendlySpacing.sectionGap
            ),
            verticalArrangement = Arrangement.spacedBy(SpendlySpacing.sectionGap)
        ) {
            item {
                Button(
                    onClick = { showEditor = true },
                    shape = RoundedCornerShape(SpendlyRadius.input),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(SpendlySizing.buttonHeight)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("Add Recurring", modifier = Modifier.padding(start = 8.dp), fontWeight = FontWeight.Bold)
                }
            }
            if (state.rules.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Default.EventRepeat,
                        title = "No recurring rules yet",
                        subtitle = "Automate monthly salaries, bills, subscriptions, and regular expenses",
                        actionLabel = "Add recurring",
                        onAction = { showEditor = true }
                    )
                }
            } else {
                if (state.activeRules.isNotEmpty()) {
                    item { SectionHeader("Active Rules", subtitle = "Upcoming automatic transactions") }
                    items(state.activeRules, key = { it.id }) { rule ->
                        RecurringRuleCard(
                            rule = rule,
                            onPauseResume = { viewModel.pause(rule.id) },
                            onDelete = { viewModel.delete(rule.id) }
                        )
                    }
                }
                if (state.pausedRules.isNotEmpty()) {
                    item { SectionHeader("Paused Rules", subtitle = "Not generating until resumed") }
                    items(state.pausedRules, key = { it.id }) { rule ->
                        RecurringRuleCard(
                            rule = rule,
                            onPauseResume = { viewModel.resume(rule.id) },
                            onDelete = { viewModel.delete(rule.id) }
                        )
                    }
                }
            }
        }
    }

    if (showEditor) {
        RecurringEditorDialog(
            onDismiss = { showEditor = false },
            onSave = { type, name, amount, category, frequency ->
                viewModel.saveRule(
                    type = type,
                    name = name,
                    amountInput = amount,
                    categoryOrSource = category,
                    frequency = frequency,
                    startDateMillis = System.currentTimeMillis()
                )
                showEditor = false
            }
        )
    }
}

@Composable
private fun RecurringRuleCard(
    rule: RecurringRule,
    onPauseResume: () -> Unit,
    onDelete: () -> Unit
) {
    val accent = if (rule.type == TransactionType.INCOME) SpendlyGreen else SpendlyRed
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        shape = RoundedCornerShape(SpendlyRadius.panel)
    ) {
        Column(modifier = Modifier.padding(SpendlySpacing.cardPadding), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(rule.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("${rule.frequency.name.lowercase().replaceFirstChar { it.uppercase() }} • Next ${formatDateFull(rule.nextRunDateMillis)}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Surface(shape = RoundedCornerShape(50), color = accent.copy(alpha = 0.12f)) {
                    Text(rule.type.name, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), style = MaterialTheme.typography.labelSmall, color = accent, fontWeight = FontWeight.Bold)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(formatMoney(rule.amountCents, rule.defaultCurrency), style = MaterialTheme.typography.titleMedium, color = accent, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                IconButton(onClick = onPauseResume) {
                    Icon(if (rule.isActive) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = "Pause or resume", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete recurring", tint = SpendlyRed)
                }
            }
        }
    }
}

@Composable
private fun RecurringEditorDialog(
    onDismiss: () -> Unit,
    onSave: (TransactionType, String, String, String, RecurringFrequency) -> Unit
) {
    var type by remember { mutableStateOf(TransactionType.EXPENSE) }
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(expenseCategories.first()) }
    var frequency by remember { mutableStateOf(RecurringFrequency.MONTHLY) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var frequencyExpanded by remember { mutableStateOf(false) }
    val categoryOptions = if (type == TransactionType.INCOME) defaultIncomeSources else expenseCategories

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add recurring") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TransactionType.values().forEach { option ->
                        FilterChip(
                            selected = type == option,
                            onClick = {
                                type = option
                                category = if (option == TransactionType.INCOME) defaultIncomeSources.first() else expenseCategories.first()
                            },
                            label = { Text(option.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary, selectedLabelColor = MaterialTheme.colorScheme.onPrimary)
                        )
                    }
                }
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, singleLine = true)
                OutlinedTextField(
                    value = amount,
                    onValueChange = { if (it.isEmpty() || it.all { ch -> ch.isDigit() || ch == '.' }) amount = it },
                    label = { Text("Amount") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                Box {
                    OutlinedTextField(value = category, onValueChange = {}, readOnly = true, label = { Text(if (type == TransactionType.INCOME) "Source" else "Category") }, modifier = Modifier.fillMaxWidth())
                    Surface(onClick = { categoryExpanded = true }, color = Color.Transparent, modifier = Modifier.matchParentSize()) {}
                    DropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                        categoryOptions.forEach { item ->
                            DropdownMenuItem(text = { Text(item) }, onClick = { category = item; categoryExpanded = false })
                        }
                    }
                }
                Box {
                    OutlinedTextField(value = frequency.name.lowercase().replaceFirstChar { it.uppercase() }, onValueChange = {}, readOnly = true, label = { Text("Frequency") }, modifier = Modifier.fillMaxWidth())
                    Surface(onClick = { frequencyExpanded = true }, color = Color.Transparent, modifier = Modifier.matchParentSize()) {}
                    DropdownMenu(expanded = frequencyExpanded, onDismissRequest = { frequencyExpanded = false }) {
                        RecurringFrequency.values().forEach { item ->
                            DropdownMenuItem(text = { Text(item.name.lowercase().replaceFirstChar { it.uppercase() }) }, onClick = { frequency = item; frequencyExpanded = false })
                        }
                    }
                }
            }
        },
        confirmButton = { Button(onClick = { onSave(type, name, amount, category, frequency) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
