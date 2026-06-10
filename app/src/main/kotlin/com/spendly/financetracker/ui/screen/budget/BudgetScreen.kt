package com.spendly.financetracker.ui.screen.budget

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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spendly.financetracker.data.model.BudgetProgress
import com.spendly.financetracker.ui.components.EmptyState
import com.spendly.financetracker.ui.components.SectionHeader
import com.spendly.financetracker.ui.components.SpendlyMonthPicker
import com.spendly.financetracker.ui.components.SpendlyRadius
import com.spendly.financetracker.ui.components.SpendlySizing
import com.spendly.financetracker.ui.components.SpendlySpacing
import com.spendly.financetracker.ui.theme.SpendlyAmber
import com.spendly.financetracker.ui.theme.SpendlyRed
import com.spendly.financetracker.ui.util.formatMoney
import com.spendly.financetracker.ui.viewmodel.BudgetViewModel
import com.spendly.financetracker.ui.viewmodel.expenseCategories

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(onBack: () -> Unit = {}) {
    val viewModel: BudgetViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<BudgetProgress?>(null) }
    var showEditor by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = { Text("Budget", fontWeight = FontWeight.Bold) },
                actions = {
                    SpendlyMonthPicker(
                        selectedLabel = state.selectedMonthLabel,
                        options = state.monthOptions,
                        onMonthSelected = viewModel::selectMonth,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(
                start = SpendlySpacing.screenHorizontal,
                top = SpendlySpacing.screenTop,
                end = SpendlySpacing.screenHorizontal,
                bottom = SpendlySpacing.sectionGap
            ),
            verticalArrangement = Arrangement.spacedBy(SpendlySpacing.sectionGap)
        ) {
            item {
                BudgetHero(
                    totalLimit = state.totalLimitCents,
                    totalSpent = state.totalSpentCents,
                    remaining = (state.totalLimitCents - state.totalSpentCents).coerceAtLeast(0L)
                )
            }
            item {
                Button(
                    onClick = {
                        editing = null
                        showEditor = true
                    },
                    shape = RoundedCornerShape(SpendlyRadius.input),
                    modifier = Modifier.fillMaxWidth().height(SpendlySizing.buttonHeight),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("Create Budget", modifier = Modifier.padding(start = 8.dp), fontWeight = FontWeight.Bold)
                }
            }
            item {
                SectionHeader(
                    title = "Category Budgets",
                    subtitle = "Progress updates from real expenses"
                )
            }
            if (state.budgets.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Default.AccountBalanceWallet,
                        title = "No budgets yet",
                        subtitle = "Create a monthly category budget to track spending",
                        actionLabel = "Add budget",
                        onAction = { showEditor = true }
                    )
                }
            } else {
                items(state.budgets, key = { it.budget.id }) { item ->
                    BudgetCard(
                        item = item,
                        onEdit = {
                            editing = item
                            showEditor = true
                        },
                        onDelete = { viewModel.deleteBudget(item.budget.id) }
                    )
                }
            }
        }
    }

    if (showEditor) {
        BudgetEditorDialog(
            editing = editing,
            onDismiss = { showEditor = false },
            onSave = { category, amount, note ->
                viewModel.saveBudget(category, amount, note, editing?.budget?.id)
                showEditor = false
            }
        )
    }

    LaunchedEffect(state.message, state.error) {
        if (state.message != null || state.error != null) viewModel.clearMessage()
    }
}

@Composable
private fun BudgetHero(totalLimit: Long, totalSpent: Long, remaining: Long) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.92f),
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary
                        )
                    )
                )
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Monthly Budget", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                BudgetMetric("Limit", formatMoney(totalLimit), Modifier.weight(1f))
                BudgetMetric("Spent", formatMoney(totalSpent), Modifier.weight(1f))
                BudgetMetric("Left", formatMoney(remaining), Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun BudgetMetric(label: String, value: String, modifier: Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(16.dp), color = Color.White.copy(alpha = 0.14f)) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.72f))
            Text(value, style = MaterialTheme.typography.labelMedium, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun BudgetCard(item: BudgetProgress, onEdit: () -> Unit, onDelete: () -> Unit) {
    val color = when {
        item.isExceeded -> SpendlyRed
        item.isWarning -> SpendlyAmber
        else -> MaterialTheme.colorScheme.primary
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(SpendlyRadius.panel),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(SpendlySpacing.cardPadding), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.budget.category, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("${formatMoney(item.spentCents)} spent of ${formatMoney(item.budget.limitCents)}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                StatusPill(if (item.isExceeded) "Exceeded" else if (item.isWarning) "Warning" else "On track", color)
            }
            LinearProgressIndicator(
                progress = { (item.progressPercent / 100f).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(8.dp)),
                color = color,
                trackColor = color.copy(alpha = 0.16f)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Remaining ${formatMoney(item.remainingCents)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Edit budget", tint = MaterialTheme.colorScheme.primary) }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Delete budget", tint = SpendlyRed) }
            }
        }
    }
}

@Composable
private fun StatusPill(label: String, color: Color) {
    Surface(shape = RoundedCornerShape(50), color = color.copy(alpha = 0.12f)) {
        Text(label, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun BudgetEditorDialog(
    editing: BudgetProgress?,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var category by remember(editing?.budget?.id) { mutableStateOf(editing?.budget?.category ?: expenseCategories.first()) }
    var amount by remember(editing?.budget?.id) { mutableStateOf(editing?.budget?.limitCents?.let { (it / 100L).toString() } ?: "") }
    var note by remember(editing?.budget?.id) { mutableStateOf(editing?.budget?.note.orEmpty()) }
    var expanded by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (editing == null) "Add budget" else "Edit budget") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box {
                    OutlinedTextField(value = category, onValueChange = {}, readOnly = true, label = { Text("Category") }, modifier = Modifier.fillMaxWidth())
                    Box(modifier = Modifier.matchParentSize().padding(top = 8.dp).background(Color.Transparent).padding(1.dp)) {
                        Surface(onClick = { expanded = true }, color = Color.Transparent, modifier = Modifier.fillMaxSize()) {}
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        expenseCategories.forEach {
                            DropdownMenuItem(text = { Text(it) }, onClick = { category = it; expanded = false })
                        }
                    }
                }
                OutlinedTextField(
                    value = amount,
                    onValueChange = { if (it.isEmpty() || it.all { ch -> ch.isDigit() || ch == '.' }) amount = it },
                    label = { Text("Limit amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Note") })
            }
        },
        confirmButton = { Button(onClick = { onSave(category, amount, note) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
