package com.spendly.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.spendly.app.ui.theme.SpendlyGray300

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = SpendlyGray300
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            maxLines = 3
        )
        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = onAction) {
                Text(text = actionLabel)
            }
        }
    }
}

@Composable
fun NoTransactionsState(onAddExpense: () -> Unit) {
    EmptyState(
        icon = Icons.AutoMirrored.Filled.ReceiptLong,
        title = "No transactions yet",
        subtitle = "Start logging your income and expenses",
        actionLabel = "Add expense",
        onAction = onAddExpense
    )
}

@Composable
fun NoGoalState(onSetGoal: () -> Unit) {
    EmptyState(
        icon = Icons.Default.TrackChanges,
        title = "No savings goal set",
        subtitle = "Set a goal to stay motivated and track your progress",
        actionLabel = "Set a goal",
        onAction = onSetGoal
    )
}

@Composable
fun NetworkErrorState(onRetry: () -> Unit) {
    EmptyState(
        icon = Icons.Default.WifiOff,
        title = "No internet connection",
        subtitle = "Data saved locally and will sync automatically when you reconnect",
        actionLabel = "Retry",
        onAction = onRetry
    )
}

@Composable
fun NoAnalyticsState() {
    EmptyState(
        icon = Icons.Default.BarChart,
        title = "Not enough data yet",
        subtitle = "Log at least one income and one expense to see your analytics"
    )
}
