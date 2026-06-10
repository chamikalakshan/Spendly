package com.spendly.financetracker.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.spendly.financetracker.ui.theme.SpendlyGreen
import com.spendly.financetracker.ui.theme.SpendlyRed

@Composable
fun SpendlyAddActionMenu(
    onAddIncome: () -> Unit,
    onAddExpense: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AnimatedVisibility(visible = expanded) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SpendlyAddActionItem(
                    label = "Add Income",
                    color = SpendlyGreen,
                    onClick = {
                        expanded = false
                        onAddIncome()
                    }
                )
                SpendlyAddActionItem(
                    label = "Add Expense",
                    color = SpendlyRed,
                    onClick = {
                        expanded = false
                        onAddExpense()
                    }
                )
            }
        }

        SpendlyFab(
            onClick = { expanded = !expanded },
            icon = if (expanded) Icons.Default.Close else Icons.Default.Add,
            contentDescription = if (expanded) "Close add menu" else "Add transaction"
        )
    }
}

@Composable
private fun SpendlyAddActionItem(
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(SpendlyRadius.card),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = color,
            contentColor = Color.White,
            modifier = Modifier.size(SpendlySizing.miniFabSize)
        ) {
            Icon(Icons.Default.Add, contentDescription = label, modifier = Modifier.size(SpendlySizing.iconMedium))
        }
    }
}

@Composable
fun SpendlyFab(
    onClick: () -> Unit,
    icon: ImageVector = Icons.Default.Add,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
    containerColor: Color = SpendlyGreen
) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = containerColor,
        contentColor = Color.White,
        shape = CircleShape,
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
        modifier = modifier.size(SpendlySizing.fabSize)
    ) {
        Icon(icon, contentDescription = contentDescription, modifier = Modifier.size(SpendlySizing.fabIcon))
    }
}
