package com.spendly.financetracker.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.spendly.financetracker.data.model.FinanceTransaction
import com.spendly.financetracker.data.model.TransactionType
import com.spendly.financetracker.ui.components.SpendlyRadius
import com.spendly.financetracker.ui.components.SpendlySpacing
import com.spendly.financetracker.ui.viewmodel.FinanceUiState

@Composable
fun DashboardScreen(
    state: FinanceUiState,
    contentPadding: PaddingValues,
    onSignOut: () -> Unit,
    onTitleChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onTypeChange: (TransactionType) -> Unit,
    onAddTransaction: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(contentPadding),
        contentPadding = PaddingValues(
            start = 20.dp,
            top = SpendlySpacing.screenTop,
            end = 20.dp,
            bottom = 20.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Header(
                email = state.session?.email,
                onSignOut = onSignOut
            )
        }

        item {
            SummaryCards(
                balanceCents = state.balanceCents,
                incomeCents = state.incomeCents,
                expenseCents = state.expenseCents
            )
        }

        item {
            TransactionForm(
                state = state,
                onTitleChange = onTitleChange,
                onAmountChange = onAmountChange,
                onNoteChange = onNoteChange,
                onTypeChange = onTypeChange,
                onAddTransaction = onAddTransaction
            )
        }

        item {
            Text(
                text = "Transactions",
                style = MaterialTheme.typography.titleLarge
            )
        }

        if (state.transactions.isEmpty()) {
            item {
                Text(
                    text = "No transactions yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(
                items = state.transactions,
                key = { it.id }
            ) { transaction ->
                TransactionRow(transaction = transaction)
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun Header(
    email: String?,
    onSignOut: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Finance Tracker",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = email.orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        TextButton(onClick = onSignOut) {
            Text("Sign out")
        }
    }
}

@Composable
private fun SummaryCards(
    balanceCents: Long,
    incomeCents: Long,
    expenseCents: Long
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SummaryCard(
            label = "Balance",
            amount = formatMoney(balanceCents),
            isPrimary = true
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SummaryCard(
                label = "Income",
                amount = formatMoney(incomeCents),
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                label = "Expenses",
                amount = formatMoney(expenseCents),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SummaryCard(
    label: String,
    amount: String,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SpendlyRadius.card)
    ) {
        Column(
            modifier = Modifier.padding(SpendlySpacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = amount,
                style = if (isPrimary) {
                    MaterialTheme.typography.headlineMedium
                } else {
                    MaterialTheme.typography.titleLarge
                },
                color = if (isPrimary) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TransactionForm(
    state: FinanceUiState,
    onTitleChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onTypeChange: (TransactionType) -> Unit,
    onAddTransaction: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 720.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Add transaction",
            style = MaterialTheme.typography.titleLarge
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = state.transactionType == TransactionType.EXPENSE,
                onClick = { onTypeChange(TransactionType.EXPENSE) },
                label = { Text("Expense") }
            )
            FilterChip(
                selected = state.transactionType == TransactionType.INCOME,
                onClick = { onTypeChange(TransactionType.INCOME) },
                label = { Text("Income") }
            )
        }

        OutlinedTextField(
            value = state.transactionTitle,
            onValueChange = onTitleChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Title") },
            singleLine = true
        )

        OutlinedTextField(
            value = state.transactionAmount,
            onValueChange = onAmountChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Amount") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )

        OutlinedTextField(
            value = state.transactionNote,
            onValueChange = onNoteChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Note") },
            minLines = 2
        )

        if (state.isBusy) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        Button(
            onClick = onAddTransaction,
            enabled = !state.isBusy,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save transaction")
        }
    }
}

@Composable
private fun TransactionRow(transaction: FinanceTransaction) {
    val amountPrefix = if (transaction.type == TransactionType.INCOME) "+" else "-"
    val supportingText = buildString {
        append(transaction.type.name.lowercase().replaceFirstChar { it.uppercase() })
        if (transaction.note.isNotBlank()) {
            append(" - ")
            append(transaction.note)
        }
    }

    ListItem(
        headlineContent = {
            Text(
                text = transaction.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            Text(
                text = supportingText,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        },
        trailingContent = {
            Text(
                text = "$amountPrefix${formatMoney(transaction.amountCents)}",
                color = if (transaction.type == TransactionType.INCOME) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                },
                fontWeight = FontWeight.SemiBold
            )
        }
    )
}

private fun formatMoney(cents: Long): String {
    val sign = if (cents < 0L) "-" else ""
    val absolute = kotlin.math.abs(cents)
    val whole = absolute / 100L
    val fraction = (absolute % 100L).toString().padStart(2, '0')
    return "${sign}Rs $whole.$fraction"
}
