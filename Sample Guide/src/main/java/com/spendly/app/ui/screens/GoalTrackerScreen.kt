package com.spendly.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.spendly.app.ui.components.NoGoalState
import com.spendly.app.ui.theme.SpendlyGray300
import com.spendly.app.ui.theme.SpendlyGray500
import com.spendly.app.ui.theme.SpendlyGray700
import com.spendly.app.ui.theme.SpendlyGray900
import com.spendly.app.ui.theme.SpendlyGreen
import com.spendly.app.ui.theme.SpendlyGreenDark
import com.spendly.app.ui.theme.SpendlyGreenLight
import com.spendly.app.ui.theme.SpendlyTypography
import com.spendly.app.utils.FormatUtils
import com.spendly.app.viewmodel.GoalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalTrackerScreen(
    navController: NavController,
    viewModel: GoalViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Goal Tracker", fontWeight = FontWeight.Bold) })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                OutlinedButton(
                    onClick = { navController.navigate(Screen.EditGoal.route) },
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, SpendlyGreen),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SpendlyGreen)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Add New Goal")
                }
            }

            if (uiState.allGoals.isEmpty()) {
                item {
                    NoGoalState(onSetGoal = { navController.navigate(Screen.EditGoal.route) })
                }
            } else {
                uiState.primaryGoal?.let { goal ->
                    item {
                        Text("Primary Goal", style = SpendlyTypography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    item {
                        PrimaryGoalCard(
                            goal = goal,
                            savedAmount = uiState.savedAmount,
                            progress = uiState.progressPercent,
                            remaining = uiState.remainingAmount,
                            isOnTrack = uiState.isOnTrack,
                            onClick = { navController.navigate(Screen.PrimaryGoal.detailRoute(goal.id)) }
                        )
                    }
                }

                if (uiState.otherGoals.isNotEmpty()) {
                    item {
                        Text("Other Goals", style = SpendlyTypography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    items(uiState.otherGoals, key = { it.id }) { goal ->
                        OtherGoalRow(
                            goal = goal,
                            savedAmount = uiState.savedAmount,
                            onClick = { navController.navigate(Screen.PrimaryGoal.detailRoute(goal.id)) }
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun PrimaryGoalCard(
    goal: SavingsGoal,
    savedAmount: Double,
    progress: Float,
    remaining: Double,
    isOnTrack: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SpendlyGreen),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    goal.goalName,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = if (isOnTrack) SpendlyGreenDark else Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                        Text("ON TRACK", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    }
                }
            }

            Text(
                "${FormatUtils.formatMonthYear(goal.targetDate)} - ${FormatUtils.formatLKR(remaining)} remaining",
                style = SpendlyTypography.bodySmall,
                color = Color.White.copy(alpha = 0.82f)
            )

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.3f)
            )

            Row {
                Text(
                    FormatUtils.formatLKR(savedAmount.coerceAtMost(goal.targetAmount)),
                    style = SpendlyTypography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    " / ${FormatUtils.formatLKR(goal.targetAmount)}",
                    style = SpendlyTypography.labelMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun OtherGoalRow(
    goal: SavingsGoal,
    savedAmount: Double,
    onClick: () -> Unit
) {
    val progress = if (goal.targetAmount > 0.0) {
        (savedAmount / goal.targetAmount).coerceIn(0.0, 1.0).toFloat()
    } else {
        0f
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(0.5.dp, SpendlyGray300),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(SpendlyGreenLight, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (goal.goalName.contains("laptop", ignoreCase = true)) Icons.Default.Laptop else Icons.Default.Flag,
                    contentDescription = null,
                    tint = SpendlyGreen
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row {
                    Text(goal.goalName, modifier = Modifier.weight(1f), color = SpendlyGray900, fontWeight = FontWeight.Bold)
                    Text("${(progress * 100).toInt()}%", color = SpendlyGreen, fontWeight = FontWeight.Bold)
                }
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = SpendlyGreen,
                    trackColor = SpendlyGreenLight
                )
                Text(
                    "${FormatUtils.formatLKR(savedAmount.coerceAtMost(goal.targetAmount))} / ${FormatUtils.formatLKR(goal.targetAmount)}",
                    style = SpendlyTypography.labelSmall,
                    color = SpendlyGray500
                )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = SpendlyGray700)
        }
    }
}
