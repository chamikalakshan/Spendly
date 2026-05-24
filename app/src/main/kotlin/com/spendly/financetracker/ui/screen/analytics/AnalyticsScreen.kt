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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.spendly.financetracker.ui.theme.ChartColors
import com.spendly.financetracker.ui.theme.ChartPurple
import com.spendly.financetracker.ui.theme.SpendlyGray100
import com.spendly.financetracker.ui.theme.SpendlyGray300
import com.spendly.financetracker.ui.theme.SpendlyGray500
import com.spendly.financetracker.ui.theme.SpendlyGray700
import com.spendly.financetracker.ui.theme.SpendlyGray900
import com.spendly.financetracker.ui.theme.SpendlyGreen
import com.spendly.financetracker.ui.theme.SpendlyGreenDark
import com.spendly.financetracker.ui.theme.SpendlyGreenLight
import com.spendly.financetracker.ui.theme.SpendlyRed
import com.spendly.financetracker.ui.theme.SpendlyRedDark
import com.spendly.financetracker.ui.theme.SpendlyRedLight
import com.spendly.financetracker.ui.util.formatMoney
import com.spendly.financetracker.ui.viewmodel.AnalyticsSlice
import com.spendly.financetracker.ui.viewmodel.AnalyticsUiState
import com.spendly.financetracker.ui.viewmodel.AnalyticsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen() {
    val viewModel: AnalyticsViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()
    var monthMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    windowInsets = WindowInsets(0.dp),
                    title = { Text("Analytics", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) },
                    actions = {
                        Box(modifier = Modifier.padding(end = 16.dp)) {
                            Surface(color = SpendlyGray100, shape = RoundedCornerShape(18.dp), onClick = { monthMenuExpanded = true }) {
                                Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CalendarMonth, null, tint = SpendlyGray700, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(state.selectedMonthLabel, style = MaterialTheme.typography.titleSmall, color = SpendlyGray900)
                                    Icon(Icons.Default.ArrowDropDown, null, tint = SpendlyGray700, modifier = Modifier.size(18.dp))
                                }
                            }
                            DropdownMenu(expanded = monthMenuExpanded, onDismissRequest = { monthMenuExpanded = false }) {
                                state.monthOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option.label) },
                                        onClick = {
                                            viewModel.selectMonth(option.startMillis)
                                            monthMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                )
                HorizontalDivider(thickness = 0.5.dp, color = SpendlyGray300)
            }
        }
    ) { padding ->
        if (!state.isLoading && state.totalIncome == 0L && state.totalExpense == 0L) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                NoAnalyticsState()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).background(Color.White),
                contentPadding = PaddingValues(start = 16.dp, top = 14.dp, end = 16.dp, bottom = 104.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item { SummaryTiles(state) }
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
private fun SummaryTiles(state: AnalyticsUiState) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SummaryTile("Total Income", formatMoney(state.totalIncome), SpendlyGreenLight, SpendlyGreenDark, Modifier.weight(1f))
        SummaryTile("Total Expenses", formatMoney(state.totalExpense), SpendlyRedLight, SpendlyRedDark, Modifier.weight(1f))
    }
}

@Composable
private fun SummaryTile(label: String, value: String, background: Color, textColor: Color, modifier: Modifier) {
    Card(modifier = modifier.height(88.dp), colors = CardDefaults.cardColors(containerColor = background), shape = RoundedCornerShape(18.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.Center) {
            Text(label, style = MaterialTheme.typography.titleSmall, color = textColor, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, color = textColor, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SpendingByCategoryCard(state: AnalyticsUiState) {
    AnalyticsCard {
        Text("Spending by Category", style = MaterialTheme.typography.titleLarge, color = SpendlyGray900, fontWeight = FontWeight.Bold)
        Row(modifier = Modifier.fillMaxWidth().height(210.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.weight(0.9f), contentAlignment = Alignment.Center) {
                DonutChart(state.spendingByCategory, Modifier.size(132.dp))
                Text("Spent\n${formatCompactAmount(state.totalExpense)}", textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium, color = SpendlyGray700, fontWeight = FontWeight.Bold)
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
    Canvas(modifier = modifier) {
        val stroke = Stroke(width = 22.dp.toPx(), cap = StrokeCap.Butt)
        val arcSize = Size(size.width - stroke.width, size.height - stroke.width)
        var start = -90f
        if (categories.isEmpty()) {
            drawArc(SpendlyGray100, start, 360f, false, topLeft = androidx.compose.ui.geometry.Offset(stroke.width / 2, stroke.width / 2), size = arcSize, style = stroke)
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
        Text(item.label, style = MaterialTheme.typography.bodyMedium, color = SpendlyGray700, modifier = Modifier.weight(1f))
        Text(formatPercent(item.percent), style = MaterialTheme.typography.bodyMedium, color = SpendlyGray700, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SpendingSplitCard(state: AnalyticsUiState) {
    val split = state.spendingSplit
    AnalyticsCard {
        Text("Committed vs Discretionary", style = MaterialTheme.typography.titleLarge, color = SpendlyGray900, fontWeight = FontWeight.Bold)
        SplitBar("Committed", "Rent • Gym • Subscriptions", split.committedCents, split.committedPercent, ChartPurple)
        SplitBar("Discretionary", "Food • Transport • Entertainment", split.discretionaryCents, split.discretionaryPercent, SpendlyGreen)
    }
}

@Composable
private fun SplitBar(label: String, subtitle: String, amount: Long, percent: Double, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.Bottom) {
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.titleMedium, color = SpendlyGray900, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = SpendlyGray500)
            }
            Text("${formatMoney(amount)} (${formatPercent(percent)})", style = MaterialTheme.typography.titleSmall, color = color, fontWeight = FontWeight.Bold)
        }
        LinearProgressIndicator(
            progress = { (percent.toFloat() / 100f).coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(8.dp)),
            color = color,
            trackColor = SpendlyGray100
        )
    }
}

@Composable
private fun MonthlyOverviewCard(state: AnalyticsUiState) {
    val data = state.monthlyOverview
    val maxValue = data.maxOfOrNull { maxOf(it.income, it.expense) }?.coerceAtLeast(1L) ?: 1L
    AnalyticsCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Monthly Overview (5 months)", style = MaterialTheme.typography.titleLarge, color = SpendlyGray900, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LegendMini("Income", SpendlyGreen)
                LegendMini("Expenses", SpendlyRed)
            }
        }
        Row(modifier = Modifier.fillMaxWidth().height(190.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Bottom) {
            Column(modifier = Modifier.height(148.dp), verticalArrangement = Arrangement.SpaceBetween, horizontalAlignment = Alignment.End) {
                Text(formatCompactAmount(maxValue), style = MaterialTheme.typography.labelSmall, color = SpendlyGray500)
                Text(formatCompactAmount(maxValue / 2), style = MaterialTheme.typography.labelSmall, color = SpendlyGray500)
                Text("0", style = MaterialTheme.typography.labelSmall, color = SpendlyGray500)
            }
            data.forEach { item ->
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                    Row(modifier = Modifier.height(148.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.Bottom) {
                        ChartBar(item.income, maxValue, SpendlyGreen, Modifier.weight(1f))
                        ChartBar(item.expense, maxValue, SpendlyRed, Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(item.label, style = MaterialTheme.typography.labelSmall, color = SpendlyGray500)
                }
            }
        }
    }
}

@Composable
private fun ChartBar(value: Long, maxValue: Long, color: Color, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxHeight(), contentAlignment = Alignment.BottomCenter) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight((value.toFloat() / maxValue.toFloat()).coerceIn(0.03f, 1f))
                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                .background(color)
        )
    }
}

@Composable
private fun IncomeSourcesCard(state: AnalyticsUiState) {
    AnalyticsCard {
        Text("Income Sources", style = MaterialTheme.typography.titleLarge, color = SpendlyGray900, fontWeight = FontWeight.Bold)
        state.incomeSources.forEach { item ->
            SplitBar(item.label, "${formatPercent(item.percent)} of total income", item.amountCents, item.percent, SpendlyGreen)
        }
    }
}

@Composable
private fun LegendMini(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
        Text(label, style = MaterialTheme.typography.labelSmall, color = SpendlyGray500)
    }
}

@Composable
private fun AnalyticsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(0.5.dp, SpendlyGray300),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp), content = content)
    }
}

private fun formatCompactAmount(cents: Long): String {
    val amount = cents / 100L
    return when {
        amount >= 1_000_000L -> "LKR ${(amount + 500_000L) / 1_000_000L}m"
        amount >= 1_000L -> "LKR ${(amount + 500L) / 1_000L}k"
        else -> "LKR $amount"
    }
}

private fun formatPercent(percent: Double): String =
    "${String.format(java.util.Locale.US, "%.1f", percent)}%"
