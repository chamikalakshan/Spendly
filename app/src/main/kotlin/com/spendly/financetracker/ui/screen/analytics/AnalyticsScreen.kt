package com.spendly.financetracker.ui.screen.analytics

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spendly.financetracker.ui.components.NoAnalyticsState
import com.spendly.financetracker.ui.components.SpendlyMonthPicker
import com.spendly.financetracker.ui.components.SpendlyRadius
import com.spendly.financetracker.ui.components.SpendlySpacing
import com.spendly.financetracker.ui.theme.ChartColors
import com.spendly.financetracker.ui.theme.ChartPurple
import com.spendly.financetracker.ui.theme.SpendlyGreen
import com.spendly.financetracker.ui.theme.SpendlyRed
import com.spendly.financetracker.ui.util.formatMoney
import com.spendly.financetracker.ui.viewmodel.AnalyticsSlice
import com.spendly.financetracker.ui.viewmodel.AnalyticsUiState
import com.spendly.financetracker.ui.viewmodel.AnalyticsViewModel
import com.spendly.financetracker.data.service.SmartInsight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen() {
    val viewModel: AnalyticsViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Analytics", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
                    actions = {
                        SpendlyMonthPicker(
                            selectedLabel = state.selectedMonthLabel,
                            options = state.monthOptions,
                            onMonthSelected = viewModel::selectMonth,
                            modifier = Modifier.padding(end = SpendlySpacing.screenHorizontal)
                        )
                    }
                )
                if (state.isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
            }
        }
    ) { padding ->
        if (!state.isLoading && state.totalIncome == 0L && state.totalExpense == 0L) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(bottom = SpendlySpacing.mainScreenBottomPadding),
                contentAlignment = Alignment.Center
            ) {
                NoAnalyticsState()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(
                    start = SpendlySpacing.screenHorizontal,
                    top = SpendlySpacing.screenTop,
                    end = SpendlySpacing.screenHorizontal,
                    bottom = SpendlySpacing.mainScreenBottomPadding
                ),
                verticalArrangement = Arrangement.spacedBy(SpendlySpacing.cardGap)
            ) {
                item { SummaryTiles(state) }
                if (state.smartInsights.isNotEmpty()) {
                    item { SmartInsightsCard(state.smartInsights) }
                }
                item { SpendingByCategoryCard(state) }
                item { SpendingSplitCard(state) }
                item { MonthlyOverviewCard(state) }
                if (state.incomeSources.isNotEmpty()) {
                    item { IncomeSourcesCard(state) }
                }
            }
        }
    }
}

@Composable
private fun SmartInsightsCard(insights: List<SmartInsight>) {
    AnalyticsCard {
        Text("Smart Insights", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        insights.forEach { insight ->
            val color = when (insight.severity) {
                "DANGER" -> SpendlyRed
                "WARNING" -> ChartPurple
                else -> SpendlyGreen
            }
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.size(10.dp).padding(top = 5.dp).clip(CircleShape).background(color))
                Column {
                    Text(insight.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text(insight.message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun SummaryTiles(state: AnalyticsUiState) {
    Row(horizontalArrangement = Arrangement.spacedBy(SpendlySpacing.cardGap)) {
        SummaryTile("Total Income", formatMoney(state.totalIncome), MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer, Modifier.weight(1f))
        SummaryTile("Total Expenses", formatMoney(state.totalExpense), MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer, Modifier.weight(1f))
    }
}

@Composable
private fun SummaryTile(label: String, value: String, background: Color, textColor: Color, modifier: Modifier) {
    Card(modifier = modifier.height(88.dp), colors = CardDefaults.cardColors(containerColor = background), shape = RoundedCornerShape(SpendlyRadius.card)) {
        Column(modifier = Modifier.padding(SpendlySpacing.cardPadding), verticalArrangement = Arrangement.Center) {
            Text(label, style = MaterialTheme.typography.titleSmall, color = textColor, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, color = textColor, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SpendingByCategoryCard(state: AnalyticsUiState) {
    AnalyticsCard {
        Text("Spending by Category", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        Row(modifier = Modifier.fillMaxWidth().height(210.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.weight(0.9f), contentAlignment = Alignment.Center) {
                DonutChart(state.spendingByCategory, Modifier.size(132.dp))
                Text("Spent\n${formatCompactAmount(state.totalExpense)}", textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
            }
            Column(modifier = Modifier.weight(1.1f), verticalArrangement = Arrangement.spacedBy(11.dp)) {
                state.spendingByCategory.take(7).forEachIndexed { index, item ->
                    LegendRow(item, ChartColors[index % ChartColors.size])
                }
            }
        }
    }
}

@Composable
private fun DonutChart(categories: List<AnalyticsSlice>, modifier: Modifier = Modifier) {
    val emptyColor = MaterialTheme.colorScheme.surfaceVariant
    Canvas(modifier = modifier) {
        val stroke = Stroke(width = 22.dp.toPx(), cap = StrokeCap.Butt)
        val arcSize = Size(size.width - stroke.width, size.height - stroke.width)
        var start = -90f
        if (categories.isEmpty()) {
            drawArc(emptyColor, start, 360f, false, topLeft = androidx.compose.ui.geometry.Offset(stroke.width / 2, stroke.width / 2), size = arcSize, style = stroke)
        } else {
            categories.forEachIndexed { index, item ->
                val sweep = (item.percent.coerceAtLeast(1.0).toFloat() / 100f) * 360f
                drawArc(
                    color = ChartColors[index % ChartColors.size],
                    startAngle = start,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = androidx.compose.ui.geometry.Offset(stroke.width / 2, stroke.width / 2),
                    size = arcSize,
                    style = stroke
                )
                start += sweep
            }
        }
    }
}

@Composable
private fun LegendRow(item: AnalyticsSlice, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(3.dp)).background(color))
        Text(item.label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(formatPercent(item.percent), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SpendingSplitCard(state: AnalyticsUiState) {
    val split = state.spendingSplit
    AnalyticsCard {
        Text("Committed vs Discretionary", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        SplitBar("Committed", "Rent • Gym • Subscriptions", split.committedCents, split.committedPercent, ChartPurple)
        SplitBar("Discretionary", "Food • Transport • Entertainment", split.discretionaryCents, split.discretionaryPercent, SpendlyGreen)
    }
}

@Composable
private fun SplitBar(label: String, subtitle: String, amount: Long, percent: Double, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.Bottom) {
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("${formatMoney(amount)} (${formatPercent(percent)})", style = MaterialTheme.typography.titleSmall, color = color, fontWeight = FontWeight.Bold)
        }
        LinearProgressIndicator(
            progress = { (percent.toFloat() / 100f).coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(8.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun MonthlyOverviewCard(state: AnalyticsUiState) {
    AnalyticsCard {
        Text("Monthly Overview", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Income", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(formatMoney(state.totalIncome), style = MaterialTheme.typography.titleMedium, color = SpendlyGreen, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Expenses", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(formatMoney(state.totalExpense), style = MaterialTheme.typography.titleMedium, color = SpendlyRed, fontWeight = FontWeight.Bold)
            }
        }
        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Net Cash Flow", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            val net = state.totalIncome - state.totalExpense
            Text(formatMoney(net), style = MaterialTheme.typography.titleMedium, color = if (net >= 0) SpendlyGreen else SpendlyRed, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun IncomeSourcesCard(state: AnalyticsUiState) {
    AnalyticsCard {
        Text("Income Sources", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        state.incomeSources.forEach { source ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(source.label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.End) {
                    Text(formatMoney(source.amountCents), style = MaterialTheme.typography.titleSmall, color = SpendlyGreen, fontWeight = FontWeight.Bold)
                    Text(formatPercent(source.percent), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
        }
    }
}

@Composable
private fun AnalyticsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SpendlyRadius.card),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(SpendlySpacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(SpendlySpacing.cardGap),
            content = content
        )
    }
}

private fun formatPercent(percent: Double): String = "%.1f%%".format(percent)

private fun formatCompactAmount(cents: Long): String {
    val amount = cents / 100.0
    return when {
        amount >= 1_000_000 -> "$%.1fM".format(amount / 1_000_000)
        amount >= 1_000 -> "$%.1fK".format(amount / 1_000)
        else -> "$%.0f".format(amount)
    }
}
