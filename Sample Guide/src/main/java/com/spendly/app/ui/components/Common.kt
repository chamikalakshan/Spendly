package com.spendly.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.spendly.app.ui.theme.SpendlyGreen
import com.spendly.app.ui.theme.SpendlyGray300
import com.spendly.app.ui.theme.SpendlyGray500
import com.spendly.app.ui.theme.SpendlyGray700
import com.spendly.app.ui.theme.SpendlyRed
import com.spendly.app.ui.theme.SpendlyRedDark
import com.spendly.app.ui.theme.SpendlyRedLight

@Composable
fun ErrorBanner(message: String, onDismiss: () -> Unit) {
    Surface(
        color = SpendlyRedLight,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = SpendlyRed,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = message,
                color = SpendlyRedDark,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = SpendlyRed
                )
            }
        }
    }
}

@Composable
fun NoTransactionsState(
    title: String = "No transactions found",
    subtitle: String = "Add your first expense to get started",
    actionLabel: String = "Add Expense",
    onAction: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(24.dp)
    ) {
        Icon(
            Icons.AutoMirrored.Filled.ReceiptLong,
            null,
            tint = SpendlyGray300,
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.bodyLarge, color = SpendlyGray700)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = SpendlyGray500)
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onAction,
            colors = ButtonDefaults.buttonColors(containerColor = SpendlyGreen)
        ) {
            Text(actionLabel)
        }
    }
}
