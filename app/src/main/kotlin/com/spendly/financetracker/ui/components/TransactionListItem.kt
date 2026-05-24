package com.spendly.financetracker.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.spendly.financetracker.data.model.FinanceTransaction
import com.spendly.financetracker.data.model.TransactionType
import com.spendly.financetracker.ui.theme.SpendlyGray100
import com.spendly.financetracker.ui.theme.SpendlyGray300
import com.spendly.financetracker.ui.theme.SpendlyGray500
import com.spendly.financetracker.ui.theme.SpendlyGreen
import com.spendly.financetracker.ui.theme.SpendlyRed
import com.spendly.financetracker.ui.util.formatDateFull
import com.spendly.financetracker.ui.util.formatDateShort
import com.spendly.financetracker.ui.util.formatMoney

@Composable
fun TransactionListItem(
    transaction: FinanceTransaction,
    modifier: Modifier = Modifier,
    showContainer: Boolean = true,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    var expanded by remember(transaction.id) { mutableStateOf(false) }
    val amountPrefix = if (transaction.type == TransactionType.INCOME) "+" else "-"
    val amountColor = transactionAmountColor(transaction.type)

    val content: @Composable () -> Unit = {
        TransactionItemContent(
            transaction = transaction,
            expanded = expanded,
            onToggle = { expanded = !expanded },
            amountPrefix = amountPrefix,
            amountColor = amountColor,
            onEdit = onEdit,
            onDelete = onDelete
        )
    }

    if (showContainer) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(0.5.dp, SpendlyGray300),
            shape = MaterialTheme.shapes.medium
        ) {
            content()
        }
    } else {
        Column(modifier = modifier.fillMaxWidth()) {
            content()
        }
    }
}

@Composable
private fun TransactionItemContent(
    transaction: FinanceTransaction,
    expanded: Boolean,
    onToggle: () -> Unit,
    amountPrefix: String,
    amountColor: Color,
    onEdit: (() -> Unit)?,
    onDelete: (() -> Unit)?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TransactionIcon(
                transactionType = transaction.type,
                label = transactionCategoryLabel(transaction.type),
                size = 38.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = transactionCategoryLabel(transaction.type),
                        style = MaterialTheme.typography.labelSmall,
                        color = SpendlyGray500
                    )
                    TrackBadge(
                        label = transactionTrackLabel(transaction.type),
                        isIncome = transaction.type == TransactionType.INCOME
                    )
                    Text(
                        text = formatDateShort(transaction.dateMillis),
                        style = MaterialTheme.typography.labelSmall,
                        color = SpendlyGray500
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$amountPrefix${formatMoney(transaction.amountCents)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = amountColor,
                    textAlign = TextAlign.End
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = SpendlyGray500,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier.padding(start = 66.dp, end = 16.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (transaction.note.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Notes, null, tint = SpendlyGray500, modifier = Modifier.size(14.dp))
                        Text(transaction.note, style = MaterialTheme.typography.bodySmall, color = SpendlyGray500)
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.CalendarToday, null, tint = SpendlyGray500, modifier = Modifier.size(14.dp))
                    Text(
                        formatDateFull(transaction.dateMillis),
                        style = MaterialTheme.typography.bodySmall,
                        color = SpendlyGray500
                    )
                }

                if (onEdit != null || onDelete != null) {
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { onEdit?.invoke() },
                            enabled = onEdit != null,
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SpendlyGreen),
                            border = BorderStroke(1.dp, SpendlyGray100)
                        ) {
                            Text("Edit", style = MaterialTheme.typography.labelMedium)
                        }
                        Spacer(Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = { onDelete?.invoke() },
                            enabled = onDelete != null,
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SpendlyRed),
                            border = BorderStroke(1.dp, SpendlyGray100)
                        ) {
                            Text("Delete", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}
