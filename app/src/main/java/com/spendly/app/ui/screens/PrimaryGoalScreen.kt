package com.spendly.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import co.yml.charts.axis.AxisData
import co.yml.charts.common.model.Point
import co.yml.charts.ui.barchart.BarChart
import co.yml.charts.ui.barchart.models.BarChartData
import co.yml.charts.ui.barchart.models.BarData
import com.spendly.app.navigation.Screen
import com.spendly.app.ui.components.NoGoalState
import com.spendly.app.ui.theme.*
import com.spendly.app.utils.FormatUtils
import com.spendly.app.viewmodel.GoalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrimaryGoalScreen(
    navController: NavController,
    goalId: String? = null,
    viewModel: GoalViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(goalId) {
        viewModel.selectGoal(goalId)
    }

    if (uiState.showAddSavingsDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissAddSavingsDialog() },
            title = { Text("Add savings") },
            text = {
                Column {
                    Text("How much are you adding toward this goal?")
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = uiState.addSavingsAmount,
                        onValueChange = { viewModel.onAddSavingsAmountChanged(it) },
                        label = { Text("Amount (LKR)") },
                        prefix = { Text("LKR ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmAddSavings() }) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissAddSavingsDialog() }) { Text("Cancel") }
            }
        )
    }

    if (uiState.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelDeleteGoal() },
            title = { Text("Delete Goal") },
            text = { Text("Are you sure you want to delete this goal?") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.executeDeleteGoal() },
                    colors = ButtonDefaults.textButtonColors(contentColor = SpendlyRed)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelDeleteGoal() }) { Text("Cancel") }
            }
        )
    }

    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = { Text("Primary Goal", style = SpendlyTypography.titleLarge, fontWeight = FontWeight.Bold) },
                actions = {
                    TextButton(
                        onClick = {
                            val id = uiState.primaryGoal?.id
                            navController.navigate(
                                if (id.isNullOrBlank()) Screen.EditGoal.route else Screen.EditGoal.editRoute(id)
                            )
                        }
                    ) {
                        Text("Edit", color = SpendlyGreen)
                    }
                    IconButton(onClick = { viewModel.confirmDeleteGoal() }) {
                        Icon(Icons.Default.DeleteOutline, null, tint = SpendlyRed)
                    }
                }
            )
        }
    ) { padding ->
        val goal = uiState.primaryGoal
        if (goal == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                NoGoalState(onSetGoal = { navController.navigate(Screen.EditGoal.route) })
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Goal header card
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SpendlyGreen),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = SpendlyGreenDark,
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(12.dp))
                                        Text("ON TRACK", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                OutlinedButton(
                                    onClick = { viewModel.showAddSavingsDialog() },
                                    border = BorderStroke(1.dp, Color.White),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier.height(32.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(Icons.Default.Add, null, modifier = Modifier.size(14.dp))
                                        Text("Add Savings", style = SpendlyTypography.labelMedium)
                                    }
                                }
                            }

                            Text(goal.goalName, style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
                            Text(
                                "Target: ${FormatUtils.formatMonthYear(goal.targetDate)}   Remaining: ${formatLKR(uiState.remainingAmount)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )

                            LinearProgressIndicator(
                                progress = { uiState.progressPercent },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                color = Color.White,
                                trackColor = Color.White.copy(alpha = 0.3f)
                            )

                            Row {
                                Text(
                                    "${uiState.progressDisplay}% (${formatLKR(uiState.savedAmount)})",
                                    style = SpendlyTypography.labelSmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    formatLKR(goal.targetAmount),
                                    style = SpendlyTypography.labelSmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }

                // Stats grid
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(0.5.dp, SpendlyGray300)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            val stats = listOf(
                                "Saved" to formatLKR(uiState.savedAmount),
                                "Target" to formatLKR(goal.targetAmount),
                                "Saving/month" to formatLKR(uiState.requiredMonthly),
                                "Achieved" to "${uiState.progressDisplay}%",
                                "Remaining" to formatLKR(uiState.remainingAmount),
                                "Months left" to "${uiState.monthsLeft}"
                            )
                            
                            stats.chunked(2).forEachIndexed { index, rowStats ->
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    rowStats.forEachIndexed { rowIndex, stat ->
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(stat.first, style = SpendlyTypography.labelSmall, color = SpendlyGray500)
                                            Text(stat.second, style = SpendlyTypography.bodyLarge, fontWeight = FontWeight.Bold, color = SpendlyGray900)
                                        }
                                    }
                                }
                                if (index < 2) {
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp, color = SpendlyGray100)
                                }
                            }
                        }
                    }
                }

                // Monthly Savings chart
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(0.5.dp, SpendlyGray300)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Monthly Savings", style = SpendlyTypography.titleSmall, fontWeight = FontWeight.Bold)
                            
                            if (uiState.monthlySavings.isNotEmpty()) {
                                val maxVal = uiState.monthlySavings.maxOrNull()?.toFloat()?.coerceAtLeast(1f) ?: 1f
                                val barData = uiState.monthlySavings.mapIndexed { index, value ->
                                    BarData(
                                        point = Point(index.toFloat(), value.toFloat()),
                                        color = if (value >= 0) SpendlyGreen else SpendlyRed,
                                        label = uiState.monthLabels.getOrNull(index) ?: ""
                                    )
                                }

                                val xAxisData = AxisData.Builder()
                                    .axisStepSize(40.dp)
                                    .steps(barData.size - 1)
                                    .labelData { i -> uiState.monthLabels.getOrNull(i) ?: "" }
                                    .build()

                                val yAxisData = AxisData.Builder()
                                    .steps(4)
                                    .labelAndAxisLinePadding(20.dp)
                                    .labelData { i -> (i * (maxVal / 4)).toInt().toString() }
                                    .build()

                                BarChart(
                                    modifier = Modifier.height(180.dp).fillMaxWidth(),
                                    barChartData = BarChartData(
                                        chartData = barData,
                                        xAxisData = xAxisData,
                                        yAxisData = yAxisData
                                    )
                                )
                            }
                        }
                    }
                }

                // Live Projection section
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SpendlyGreenLight),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.AutoMirrored.Filled.TrendingUp, null, tint = SpendlyGreen, modifier = Modifier.size(16.dp))
                                Text("Live Projection (optional only)", style = SpendlyTypography.labelSmall, color = SpendlyGreen, fontWeight = FontWeight.Bold)
                            }
                            
                            Text(uiState.projectedDate, style = MaterialTheme.typography.headlineMedium, color = SpendlyGreenDark, fontWeight = FontWeight.Bold)
                            
                            Text(uiState.projectionText, style = SpendlyTypography.bodySmall, color = SpendlyGreenDark.copy(alpha = 0.8f))

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp, color = SpendlyGreen.copy(alpha = 0.2f))
                            
                            Row {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Remaining to save", style = SpendlyTypography.labelSmall, color = SpendlyGray500)
                                    Text(formatLKR(uiState.remainingAmount), style = SpendlyTypography.bodyMedium, fontWeight = FontWeight.Bold)
                                }
                                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                    Text("Required per month", style = SpendlyTypography.labelSmall, color = SpendlyGray500)
                                    Text(formatLKR(uiState.requiredMonthly), style = SpendlyTypography.bodyMedium, fontWeight = FontWeight.Bold, color = SpendlyGreen)
                                }
                            }
                        }
                    }
                }
                
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

private fun formatLKR(amount: Double): String {
    return FormatUtils.formatLKR(amount)
}
