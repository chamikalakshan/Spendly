package com.spendly.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.spendly.app.data.model.SavingsGoal
import com.spendly.app.navigation.Screen
import com.spendly.app.navigation.SpendlyBottomNavBar
import com.spendly.app.ui.theme.*
import com.spendly.app.viewmodel.GoalViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalTrackerScreen(
    navController: NavController,
    viewModel: GoalViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val goals = uiState.allGoals
    val totalSavedAmount = goals.sumOf { it.savedAmount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Savings Goals", style = SpendlyTypography.titleLarge, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { /* Help or Info */ }) {
                        Icon(Icons.Default.Info, contentDescription = null)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.EditGoal.route) },
                containerColor = SpendlyGreen,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Goal")
            }
        },
        bottomBar = {
            SpendlyBottomNavBar(navController = navController, currentRoute = Screen.GoalTracker.route)
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Total Savings Available Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SpendlyGreenDark),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "Total Accumulated Savings",
                            style = SpendlyTypography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            formatLKR(totalSavedAmount),
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "This is your total income minus all expenses across all time.",
                            style = SpendlyTypography.labelSmall,
                            color = Color.White.copy(alpha = 0.5f),
                            lineHeight = 14.sp
                        )
                    }
                }
            }

            item {
                Text(
                    "Active Goals",
                    style = SpendlyTypography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (goals.isEmpty()) {
                item {
                    EmptyGoalsState { navController.navigate(Screen.EditGoal.route) }
                }
            } else {
                items(goals, key = { it.id }) { goal ->
                    GoalProgressItem(
                        goal = goal,
                        totalAvailable = totalSavedAmount,
                        onDelete = { viewModel.deleteGoal(goal.id) }
                    )
                }
            }
            
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun GoalProgressItem(
    goal: SavingsGoal,
    totalAvailable: Double,
    onDelete: () -> Unit
) {
    // Logic: Each goal "claims" from the total accumulated savings.
    // In a real app, you might reorder goals to prioritize them.
    // For now, we show how much of the total savings goes towards this specific target.
    val progress = if (goal.targetAmount > 0) (totalAvailable / goal.targetAmount).coerceIn(0.0, 1.0).toFloat() else 0f
    val savedAmount = totalAvailable.coerceAtMost(goal.targetAmount)
    val remaining = (goal.targetAmount - totalAvailable).coerceAtLeast(0.0)
    
    // Calculate months left
    val monthsLeft = calculateMonthsLeft(goal.targetDate)
    val requiredPerMonth = if (monthsLeft > 0) remaining / monthsLeft else remaining

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(0.5.dp, SpendlyGray300)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(SpendlyGray100, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Flag, null, tint = SpendlyGreen, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(goal.goalName, style = SpendlyTypography.bodyLarge, fontWeight = FontWeight.Bold)
                    Text(
                        "Target: ${formatMonthYear(goal.targetDate)}",
                        style = SpendlyTypography.labelSmall,
                        color = SpendlyGray500
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.DeleteOutline, null, tint = SpendlyRed, modifier = Modifier.size(20.dp))
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = SpendlyGreen,
                    trackColor = SpendlyGreenLight
                )
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "${(progress * 100).toInt()}% achieved",
                        style = SpendlyTypography.labelSmall,
                        color = SpendlyGreen,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${formatLKR(savedAmount)} / ${formatLKR(goal.targetAmount)}",
                        style = SpendlyTypography.labelSmall,
                        color = SpendlyGray500
                    )
                }
            }

            if (remaining > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SpendlyGray50, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.TrendingUp, null, tint = SpendlyGray700, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            "Saving required per month",
                            style = SpendlyTypography.labelSmall,
                            color = SpendlyGray500,
                            fontSize = 10.sp
                        )
                        Text(
                            "${formatLKR(requiredPerMonth)} /mo",
                            style = SpendlyTypography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = SpendlyGray900
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SpendlyGreenLight, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("GOAL ACHIEVED! 🥳", style = SpendlyTypography.labelSmall, color = SpendlyGreen, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun EmptyGoalsState(onAdd: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.TrackChanges, null, tint = SpendlyGray300, modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(16.dp))
        Text("No savings goals yet", style = SpendlyTypography.bodyLarge, color = SpendlyGray700)
        Text("Track your progress for a car, house or travel.", style = SpendlyTypography.bodyMedium, color = SpendlyGray500)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onAdd, colors = ButtonDefaults.buttonColors(containerColor = SpendlyGreen)) {
            Text("Set your first goal")
        }
    }
}

private fun calculateMonthsLeft(targetDate: Long): Int {
    val now = Calendar.getInstance()
    val target = Calendar.getInstance().apply { timeInMillis = targetDate }
    val years = target.get(Calendar.YEAR) - now.get(Calendar.YEAR)
    val months = target.get(Calendar.MONTH) - now.get(Calendar.MONTH)
    return (years * 12 + months).coerceAtLeast(0)
}

private fun formatLKR(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US)
    return "Rs. " + formatter.format(amount)
}

private fun formatMonthYear(timeMs: Long): String {
    return SimpleDateFormat("MMMM yyyy", Locale.US).format(Date(timeMs))
}
