package com.spendly.financetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.spendly.financetracker.data.model.TransactionType
import com.spendly.financetracker.ui.theme.SpendlyAmber
import com.spendly.financetracker.ui.theme.SpendlyAmberLight
import com.spendly.financetracker.ui.theme.SpendlyBlue
import com.spendly.financetracker.ui.theme.SpendlyBlueLight
import com.spendly.financetracker.ui.theme.SpendlyGreen
import com.spendly.financetracker.ui.theme.SpendlyGreenLight
import com.spendly.financetracker.ui.theme.SpendlyRed
import com.spendly.financetracker.ui.theme.SpendlyRedLight

@Composable
fun SpendlyStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = Color.Unspecified,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    leadingIcon: ImageVector? = null
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(SpendlyRadius.card)
    ) {
        Column(
            modifier = Modifier.padding(SpendlySpacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            if (actionLabel != null && onAction != null) {
                TextButton(
                    onClick = onAction,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = actionLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SpendlySectionLabel(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )
}

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
            tint = MaterialTheme.colorScheme.outline
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
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
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
fun NoRecordsState(
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    EmptyState(
        icon = Icons.Default.Receipt,
        title = "No records yet",
        subtitle = "Start logging your income and expenses",
        actionLabel = actionLabel,
        onAction = onAction
    )
}

@Composable
fun NoTransactionsState(onAddExpense: () -> Unit) {
    NoRecordsState(actionLabel = "Add expense", onAction = onAddExpense)
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
fun NoAnalyticsState() {
    EmptyState(
        icon = Icons.Default.BarChart,
        title = "Not enough data yet",
        subtitle = "Log at least one income and one expense to see your analytics"
    )
}

@Composable
fun TransactionIcon(
    transactionType: TransactionType,
    label: String,
    size: Dp = 40.dp,
    isRecurring: Boolean = false
) {
    val isIncome = transactionType == TransactionType.INCOME
    val bgColor = if (isIncome) SpendlyGreenLight else SpendlyRedLight
    val iconColor = if (isIncome) SpendlyGreen else SpendlyRed
    val icon = transactionIconFor(label, isIncome)

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconColor,
            modifier = Modifier.size(size * 0.5f)
        )
        if (isRecurring) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(size * 0.34f)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EventRepeat,
                    contentDescription = "Recurring",
                    tint = iconColor,
                    modifier = Modifier.size(size * 0.22f)
                )
            }
        }
    }
}

private fun transactionIconFor(label: String, isIncome: Boolean): ImageVector {
    val normalized = label.lowercase()
    return if (isIncome) {
        when {
            "salary" in normalized || "work" in normalized -> Icons.Default.Work
            "freelance" in normalized || "business" in normalized -> Icons.Default.BusinessCenter
            "crypto" in normalized || "sell" in normalized -> Icons.Default.Savings
            else -> Icons.Default.AccountBalanceWallet
        }
    } else {
        when {
            "food" in normalized || "restaurant" in normalized || "uber" in normalized || "pickme" in normalized -> Icons.Default.Fastfood
            "transport" in normalized || "vehicle" in normalized || "car" in normalized || "bike" in normalized -> Icons.Default.DirectionsCar
            "rent" in normalized || "house" in normalized || "home" in normalized -> Icons.Default.Home
            "subscription" in normalized || "netflix" in normalized -> Icons.Default.Subscriptions
            "entertainment" in normalized || "movie" in normalized -> Icons.Default.Movie
            "gym" in normalized || "fitness" in normalized -> Icons.Default.FitnessCenter
            "goal" in normalized || "saving" in normalized -> Icons.Default.TrackChanges
            "shopping" in normalized -> Icons.Default.ShoppingBag
            else -> Icons.Default.Payments
        }
    }
}

@Composable
fun TrackBadge(
    label: String,
    isIncome: Boolean,
    modifier: Modifier = Modifier
) {
    val badgeColor = if (isIncome) SpendlyBlue else SpendlyAmber
    val badgeBg = if (isIncome) SpendlyBlueLight else SpendlyAmberLight

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(badgeBg)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = badgeColor
        )
    }
}

@Composable
fun PrimaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = SpendlyGreen,
    icon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(SpendlySizing.buttonHeight),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(containerColor = containerColor),
        shape = RoundedCornerShape(SpendlyRadius.input)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text)
    }
}

fun transactionCategoryLabel(type: TransactionType): String =
    if (type == TransactionType.INCOME) "Income" else "Expense"

fun transactionTrackLabel(type: TransactionType): String =
    if (type == TransactionType.INCOME) "INCOME" else "DISCRETIONARY"

fun transactionAmountColor(type: TransactionType): Color =
    if (type == TransactionType.INCOME) SpendlyGreen else SpendlyRed
