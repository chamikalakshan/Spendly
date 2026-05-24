package com.spendly.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.spendly.app.data.model.SavingsGoal
import com.spendly.app.navigation.Screen
import com.spendly.app.ui.components.ErrorBanner
import com.spendly.app.ui.components.LoadingOverlay
import com.spendly.app.ui.theme.*
import com.spendly.app.utils.FormatUtils
import com.spendly.app.viewmodel.GoalViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditGoalScreen(
    navController: NavController,
    goalId: String? = null,
    viewModel: GoalViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val primaryGoal = if (goalId.isNullOrBlank()) null else uiState.primaryGoal

    // Local form state
    var goalName by remember { mutableStateOf("") }
    var targetAmount by remember { mutableStateOf("") }
    var targetDateMs by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(goalId) {
        if (!goalId.isNullOrBlank()) {
            viewModel.selectGoal(goalId)
        }
    }

    // Sync local state only when editing an existing goal.
    LaunchedEffect(goalId, primaryGoal) {
        if (!goalId.isNullOrBlank()) {
            primaryGoal?.let {
                goalName = it.goalName
                targetAmount = it.targetAmount.toLong().toString()
                targetDateMs = it.targetDate
            }
        } else {
            goalName = ""
            targetAmount = ""
            targetDateMs = System.currentTimeMillis()
        }
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            navController.popBackStack()
        }
    }

    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) {
            navController.navigate(Screen.GoalTracker.route) {
                popUpTo(Screen.GoalTracker.route) { inclusive = false }
            }
        }
    }

    // Status computed locally
    val statusText = if (uiState.isOnTrack) "On Track" else "Behind Schedule"
    val statusColor = if (uiState.isOnTrack) SpendlyGreen else SpendlyRed

    // Delete confirmation dialog
    if (uiState.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelDeleteGoal() },
            title = { Text("Delete this goal?") },
            text = { Text("This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.executeDeleteGoal() },
                    colors = ButtonDefaults.textButtonColors(contentColor = SpendlyRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelDeleteGoal() }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = { Text("Edit Goal", style = SpendlyTypography.titleLarge, fontWeight = FontWeight.Bold) },
                actions = {
                    Button(
                        onClick = {
                            viewModel.saveGoal(
                                SavingsGoal(
                                    id = primaryGoal?.id ?: UUID.randomUUID().toString(),
                                    goalName = goalName,
                                    targetAmount = targetAmount.toDoubleOrNull() ?: 0.0,
                                    targetDate = targetDateMs
                                )
                            )
                        },
                        enabled = !uiState.isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SpendlyGreen,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.padding(end = 8.dp).height(36.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                    ) {
                        Text("Save", style = SpendlyTypography.labelMedium)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (uiState.error != null) {
                ErrorBanner(message = uiState.error!!, onDismiss = { viewModel.clearError() })
            }

            // Goal Name
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Goal Name", style = SpendlyTypography.labelSmall, color = SpendlyGray500)
                OutlinedTextField(
                    value = goalName,
                    onValueChange = { goalName = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g. MacBook Pro M4") },
                    singleLine = true
                )
            }

            // Status field — NEW
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Status", style = SpendlyTypography.labelSmall, color = SpendlyGray500)
                OutlinedTextField(
                    value = statusText,
                    onValueChange = {},
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = statusColor,
                        disabledBorderColor = statusColor.copy(alpha = 0.5f),
                        disabledContainerColor = Color.Transparent
                    )
                )
            }

            // Target Amount
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Target Amount (LKR)", style = SpendlyTypography.labelSmall, color = SpendlyGray500)
                OutlinedTextField(
                    value = targetAmount,
                    onValueChange = { targetAmount = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("0.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }

            // Target Date
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Target Date", style = SpendlyTypography.labelSmall, color = SpendlyGray500)
                var showDatePicker by remember { mutableStateOf(false) }
                OutlinedTextField(
                    value = SimpleDateFormat("yyyy-MM", Locale.US).format(Date(targetDateMs)),
                    onValueChange = {},
                    enabled = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true },
                    trailingIcon = { Icon(Icons.Default.CalendarMonth, null) },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledContainerColor = Color.Transparent
                    )
                )

                if (showDatePicker) {
                    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = targetDateMs)
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                datePickerState.selectedDateMillis?.let { targetDateMs = it }
                                showDatePicker = false
                            }) { Text("OK") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                        }
                    ) {
                        DatePicker(state = datePickerState)
                    }
                }
            }

            // Current savings — with progress bar — UPDATED
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Current Savings (auto-calculated)", style = SpendlyTypography.labelSmall, color = SpendlyGray500)
                OutlinedTextField(
                    value = FormatUtils.formatLKR(uiState.savedAmount),
                    onValueChange = {},
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = SpendlyGray700,
                        disabledBorderColor = SpendlyGray300,
                        disabledContainerColor = SpendlyGray100
                    )
                )
                
                // Progress bar below savings field — NEW
                Column(
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { uiState.progressPercent },
                        color = SpendlyGreen,
                        trackColor = SpendlyGreenLight,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(SpendlyGreenLight, RoundedCornerShape(3.dp))
                    )
                    Text(
                        text = "${uiState.progressDisplay}% of ${FormatUtils.formatLKR(primaryGoal?.targetAmount ?: 0.0)}",
                        style = SpendlyTypography.labelSmall,
                        color = SpendlyGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Delete goal button
            if (primaryGoal != null) {
                OutlinedButton(
                    onClick = { viewModel.confirmDeleteGoal() },
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, SpendlyRed),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SpendlyRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("Delete Goal", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        LoadingOverlay(isLoading = uiState.isLoading)
    }
}
