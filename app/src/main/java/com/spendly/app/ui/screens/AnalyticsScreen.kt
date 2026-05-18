package com.spendly.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import co.yml.charts.axis.AxisData
import co.yml.charts.common.model.PlotType
import co.yml.charts.common.model.Point
import co.yml.charts.ui.barchart.GroupBarChart
import co.yml.charts.ui.barchart.models.BarData
import co.yml.charts.ui.barchart.models.BarPlotData
import co.yml.charts.ui.barchart.models.BarStyle
import co.yml.charts.ui.barchart.models.GroupBar
import co.yml.charts.ui.barchart.models.GroupBarChartData
import co.yml.charts.ui.piechart.charts.DonutPieChart
import co.yml.charts.ui.piechart.models.PieChartConfig
import co.yml.charts.ui.piechart.models.PieChartData
import com.spendly.app.data.model.enums.IncomeSource
import com.spendly.app.ui.components.NoAnalyticsState
import com.spendly.app.ui.theme.*
import com.spendly.app.utils.FormatUtils
import com.spendly.app.viewmodel.AnalyticsViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    navController: NavController,
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var monthDropdownExpanded by remember { mutableStateOf(false) }

    val sourceColorMap = mapOf(
        IncomeSource.SALARY to SpendlyGreen,
        IncomeSource.FREELANCE to SpendlyBlue,
        IncomeSource.CRYPTO to ChartPurple,
        IncomeSource.ADSENSE to SpendlyAmber
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analytics", style = SpendlyTypography.titleLarge, fontWeight = FontWeight.Bold) },
                actions = {
                    Box {
                        Surface(
                            color = SpendlyGray100,
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.clickable { monthDropdownExpanded = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CalendarMonth, null, tint = SpendlyGray700, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(uiState.selectedMonthLabel, style = SpendlyTypography.labelMedium, color = SpendlyGray700)
                                Icon(
                                    if (monthDropdownExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                    null,
                                    tint = SpendlyGray700,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = monthDropdownExpanded,
                            onDismissRequest = { monthDropdownExpanded = false }
                        ) {
                            FormatUtils.getLast6Months().forEach { (label, start, end) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        viewModel.setMonth(start, end, label)
                                        monthDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                }
            )
        }
    ) { padding ->
        if (uiState.totalIncome == 0.0 && uiState.totalExpense == 0.0 && !uiState.isLoading) {
            NoAnalyticsState()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Total Income + Total Expenses banners
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = SpendlyGreenLight)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Total Income", style = SpendlyTypography.labelSmall, color = SpendlyGreenDark)
                                Text(FormatUtils.formatLKR(uiState.totalIncome), style = SpendlyTypography.titleMedium, color = SpendlyGreenDark, fontWeight = FontWeight.Bold)
                            }
                        }
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = SpendlyRedLight)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Total Expenses", style = SpendlyTypography.labelSmall, color = SpendlyRedDark)
                                Text(FormatUtils.formatLKR(uiState.totalExpense), style = SpendlyTypography.titleMedium, color = SpendlyRedDark, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Spending by Category (Donut Chart)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(0.5.dp, SpendlyGray300)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Spending by Category", style = SpendlyTypography.titleSmall, fontWeight = FontWeight.Bold)

                            if (uiState.expensesByCategory.isNotEmpty()) {
                                val donutChartData = PieChartData(
                                    slices = uiState.expensesByCategory.toList().mapIndexed { index, (cat, amt) ->
                                        PieChartData.Slice(
                                            label = cat.displayName,
                                            value = amt.toFloat(),
                                            color = ChartColors[index % ChartColors.size]
                                        )
                                    },
                                    plotType = PlotType.Donut
                                )

                                val donutChartConfig = PieChartConfig(
                                    isAnimationEnable = true,
                                    showSliceLabels = false,
                                    labelVisible = false,
                                    strokeWidth = 40f
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    DonutPieChart(
                                        modifier = Modifier.size(200.dp),
                                        pieChartData = donutChartData,
                                        pieChartConfig = donutChartConfig
                                    )
                                    Text(
                                        text = "Spent\n${FormatUtils.formatLKR(uiState.totalExpense)}",
                                        textAlign = TextAlign.Center,
                                        style = SpendlyTypography.labelMedium
                                    )
                                }

                                // Legend — percentage only
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    uiState.expensesByCategory.toList().sortedByDescending { it.second }.forEachIndexed { index, (cat, amt) ->
                                        val pct = if (uiState.totalExpense > 0) ((amt / uiState.totalExpense) * 100).toInt() else 0
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(ChartColors[index % ChartColors.size]))
                                            Text(cat.displayName, style = SpendlyTypography.bodySmall, modifier = Modifier.weight(1f))
                                            Text("$pct%", style = SpendlyTypography.bodySmall, fontWeight = FontWeight.Bold, color = SpendlyGray700)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Committed vs Discretionary
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(0.5.dp, SpendlyGray300)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Committed vs Discretionary", style = SpendlyTypography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("Your spending split — committed costs cannot easily be reduced.", style = SpendlyTypography.labelSmall, color = SpendlyGray500)

                            val committedPct = uiState.committedPercent / 100f
                            val discretionaryPct = uiState.discretionaryPercent / 100f

                            // Committed bar
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Committed", style = SpendlyTypography.bodySmall, fontWeight = FontWeight.Bold)
                                        Text(uiState.committedSubcategories.ifEmpty { "None" }, style = SpendlyTypography.labelSmall, color = SpendlyGray500)
                                    }
                                    Text("${FormatUtils.formatLKR(uiState.committedTotal)} (${uiState.committedPercent}%)", style = SpendlyTypography.bodySmall, fontWeight = FontWeight.Bold)
                                }
                                LinearProgressIndicator(
                                    progress = { committedPct },
                                    modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                                    color = ChartPurple,
                                    trackColor = ChartPurple.copy(alpha = 0.15f)
                                )
                            }

                            // Discretionary bar
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Discretionary", style = SpendlyTypography.bodySmall, fontWeight = FontWeight.Bold)
                                        Text(uiState.discretionarySubcategories.ifEmpty { "None" }, style = SpendlyTypography.labelSmall, color = SpendlyGray500)
                                    }
                                    Text("${FormatUtils.formatLKR(uiState.discretionaryTotal)} (${uiState.discretionaryPercent}%)", style = SpendlyTypography.bodySmall, fontWeight = FontWeight.Bold)
                                }
                                LinearProgressIndicator(
                                    progress = { discretionaryPct },
                                    modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                                    color = SpendlyAmber,
                                    trackColor = SpendlyAmber.copy(alpha = 0.15f)
                                )
                            }
                        }
                    }
                }

                // Monthly Overview
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(0.5.dp, SpendlyGray300)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Monthly Overview (5 months)", style = SpendlyTypography.titleSmall, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.align(Alignment.End)) {
                                LegendItem("Income", SpendlyGreen)
                                LegendItem("Expenses", SpendlyRed)
                            }

                            if (uiState.monthlyOverviewData.isNotEmpty()) {
                                val maxVal = uiState.monthlyOverviewData.maxOf { it.income.coerceAtLeast(it.expense) }.toFloat().coerceAtLeast(1f)
                                
                                val groupBarList = uiState.monthlyOverviewData.mapIndexed { index, item ->
                                    GroupBar(
                                        label = item.monthLabel,
                                        barList = listOf(
                                            BarData(point = Point(index.toFloat(), item.income.toFloat()), color = SpendlyGreen, label = "Income"),
                                            BarData(point = Point(index.toFloat(), item.expense.toFloat()), color = SpendlyRed, label = "Expense")
                                        )
                                    )
                                }

                                GroupBarChart(
                                    modifier = Modifier.height(200.dp).fillMaxWidth(),
                                    groupBarChartData = GroupBarChartData(
                                        barPlotData = BarPlotData(
                                            groupBarList = groupBarList,
                                            barStyle = BarStyle(barWidth = 10.dp)
                                        ),
                                        xAxisData = AxisData.Builder()
                                            .axisStepSize(45.dp)
                                            .steps(groupBarList.size - 1)
                                            .labelData { i -> groupBarList.getOrNull(i)?.label ?: "" }
                                            .build(),
                                        yAxisData = AxisData.Builder()
                                            .steps(5)
                                            .labelAndAxisLinePadding(20.dp)
                                            .labelData { i -> (i * (maxVal / 5)).toInt().toString() }
                                            .build()
                                    )
                                )
                            }
                        }
                    }
                }

                // Income Sources
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(0.5.dp, SpendlyGray300)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            val monthLabel = uiState.selectedMonthLabel.split(" ").firstOrNull() ?: ""
                            Text("Income Sources · $monthLabel", style = SpendlyTypography.titleSmall, fontWeight = FontWeight.Bold)

                            uiState.incomeBySource.forEach { (source, amt) ->
                                val pct = if (uiState.totalIncome > 0) ((amt / uiState.totalIncome) * 100).toInt() else 0
                                val barColor = sourceColorMap[source] ?: SpendlyGreen
                                
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(bottom = 12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(when(source) {
                                            IncomeSource.SALARY -> "💼"
                                            IncomeSource.FREELANCE -> "🖥️"
                                            IncomeSource.CRYPTO -> "🪙"
                                            IncomeSource.ADSENSE -> "📢"
                                        }, fontSize = 18.sp)
                                        Spacer(Modifier.width(8.dp))
                                        Text(source.displayName, style = SpendlyTypography.bodyMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                        Text(FormatUtils.formatLKR(amt), style = SpendlyTypography.bodyMedium, fontWeight = FontWeight.Bold)
                                    }
                                    LinearProgressIndicator(
                                        progress = { if (uiState.totalIncome > 0) (amt / uiState.totalIncome).toFloat() else 0f },
                                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                        color = barColor,
                                        trackColor = barColor.copy(alpha = 0.15f)
                                    )
                                    Text("$pct% of total income", style = SpendlyTypography.labelSmall, color = SpendlyGray500)
                                }
                            }
                        }
                    }
                }
                
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
        Text(label, style = SpendlyTypography.labelSmall, color = SpendlyGray500)
    }
}
