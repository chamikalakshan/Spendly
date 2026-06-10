package com.spendly.financetracker.ui.screen.goals

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendly.financetracker.ui.components.NoGoalState
import com.spendly.financetracker.ui.components.SectionHeader
import com.spendly.financetracker.ui.components.SpendlyRadius
import com.spendly.financetracker.ui.components.SpendlySizing
import com.spendly.financetracker.ui.components.SpendlySpacing
import com.spendly.financetracker.ui.theme.SpendlyAmber
import com.spendly.financetracker.ui.theme.SpendlyGreen
import com.spendly.financetracker.ui.theme.SpendlyGreenDark
import com.spendly.financetracker.ui.theme.SpendlyRed
import com.spendly.financetracker.ui.theme.AccentColorKey
import com.spendly.financetracker.ui.theme.spendlyAccentPalette
import com.spendly.financetracker.ui.util.formatMoney
import com.spendly.financetracker.ui.util.goalIconForKey
import com.spendly.financetracker.ui.util.goalIconOptions
import com.spendly.financetracker.ui.util.suggestedGoalIconKey
import com.spendly.financetracker.ui.viewmodel.FinanceUiState
import com.spendly.financetracker.ui.viewmodel.Goal
import com.spendly.financetracker.ui.viewmodel.GoalDraft
import com.spendly.financetracker.ui.viewmodel.GoalMonthlySavingUi
import com.spendly.financetracker.ui.viewmodel.goalMonthlySavingsData
import com.spendly.financetracker.ui.viewmodel.requiredMonthlySavingsCents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

typealias OnAddGoal = () -> Unit
typealias OnGoalSelected = (String) -> Unit
typealias OnSaveGoal = (GoalDraft) -> Boolean
typealias OnUpdateGoal = (String, GoalDraft) -> Boolean
typealias OnDeleteGoal = (String) -> Boolean
typealias OnAddSavings = (String, String) -> Boolean

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun GoalsScreenContent(
    state: FinanceUiState,
    onAddGoal: OnAddGoal,
    onGoalSelected: OnGoalSelected
) {
    val achievedGoals = state.goals.filter { it.isAchievedGoal() }
    val primaryGoals = state.primaryGoals.filterNot { it.isAchievedGoal() }
    val otherGoals = state.otherGoals.filterNot { it.isAchievedGoal() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Goal Tracker", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = SpendlySpacing.screenHorizontal,
                    top = SpendlySpacing.screenTop,
                    end = SpendlySpacing.screenHorizontal,
                    bottom = SpendlySpacing.sectionGap
                ),
                verticalArrangement = Arrangement.spacedBy(SpendlySpacing.sectionGap)
            ) {
                item {
                    GoalOverviewHero(
                        totalGoals = state.goals.size,
                        primaryGoals = primaryGoals.size,
                        achievedGoals = achievedGoals.size,
                        accentColorKey = state.profile?.accentColorKey
                    )
                }
                item {
                    Button(
                        onClick = onAddGoal,
                        shape = RoundedCornerShape(SpendlyRadius.input),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.fillMaxWidth().height(SpendlySizing.buttonHeight)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Add Goal", fontWeight = FontWeight.Bold)
                    }
                }

                if (state.goals.isEmpty()) {
                    item {
                        NoGoalState(onSetGoal = onAddGoal)
                    }
                } else {
                    if (primaryGoals.isNotEmpty()) {
                        item {
                            SectionHeader(title = "Primary Goals", subtitle = "Your most important active targets")
                        }
                    }
                    items(primaryGoals, key = { it.id }) { goal ->
                        PrimaryGoalCard(
                            goal = goal,
                            fallbackAccentColorKey = state.profile?.accentColorKey,
                            onClick = { onGoalSelected(goal.id) }
                        )
                    }

                    if (otherGoals.isNotEmpty()) {
                        item {
                            SectionHeader(title = "Other Goals", subtitle = "Secondary goals with target dates")
                        }
                        items(otherGoals, key = { it.id }) { goal ->
                            OtherGoalRow(
                                goal = goal,
                                fallbackAccentColorKey = state.profile?.accentColorKey,
                                onClick = { onGoalSelected(goal.id) }
                            )
                        }
                    }
                    if (achievedGoals.isNotEmpty()) {
                        item {
                            SectionHeader(title = "Achieved Goals", subtitle = "Completed milestones")
                        }
                        items(achievedGoals, key = { it.id }) { goal ->
                            OtherGoalRow(
                                goal = goal,
                                fallbackAccentColorKey = state.profile?.accentColorKey,
                                onClick = { onGoalSelected(goal.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalOverviewHero(
    totalGoals: Int,
    primaryGoals: Int,
    achievedGoals: Int,
    accentColorKey: String?
) {
    val accent = spendlyAccentPalette(accentColorKey)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = Color.Transparent,
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(accent.dark, accent.primary, accent.gradientEnd)
                    )
                )
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(Color.White.copy(alpha = 0.18f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.TrackChanges, contentDescription = null, tint = Color.White)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Goal Tracker", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Plan, save, and track progress", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.78f))
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GoalHeroMetric("Total", totalGoals.toString(), Modifier.weight(1f))
                GoalHeroMetric("Primary", primaryGoals.toString(), Modifier.weight(1f))
                GoalHeroMetric("Achieved", achievedGoals.toString(), Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun GoalHeroMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f))
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.72f))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EditGoalScreenContent(
    onBack: () -> Unit,
    goal: Goal,
    onSave: OnUpdateGoal,
    onDelete: OnDeleteGoal
) {
    var goalName by rememberSaveable { mutableStateOf(goal.title) }
    var status by rememberSaveable { mutableStateOf(normalizedGoalStatus(goal.status)) }
    var targetAmount by rememberSaveable { mutableStateOf((goal.targetCents / 100L).toString()) }
    var targetDate by rememberSaveable { mutableStateOf(goal.dueDate) }
    var isPrimary by rememberSaveable { mutableStateOf(goal.isPrimary) }
    var selectedIconKey by rememberSaveable { mutableStateOf(goal.iconKey.ifBlank { suggestedGoalIconKey(goal.title) }) }
    var selectedIconAccentColorKey by rememberSaveable { mutableStateOf(goal.iconAccentColorKey.ifBlank { AccentColorKey.GREEN.storageValue }) }
    var goalImageUri by rememberSaveable { mutableStateOf(goal.goalImageUri.orEmpty()) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) goalImageUri = uri.toString()
    }
    val selectedGoalAccent = spendlyAccentPalette(selectedIconAccentColorKey)

    fun saveGoal() {
        onSave(goal.id, GoalDraft(
            title = goalName,
            status = status,
            targetAmount = targetAmount,
            targetDate = targetDate,
            initialSaved = "",
            isPrimary = isPrimary,
            iconKey = selectedIconKey,
            iconAccentColorKey = selectedIconAccentColorKey,
            goalImageUri = goalImageUri.takeIf { it.isNotBlank() }
        ))
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = { Text("Edit Goal", fontWeight = FontWeight.Bold) }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(SpendlySpacing.screenHorizontal),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { onDelete(goal.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = Color(0xFFEF5350)),
                    shape = RoundedCornerShape(SpendlyRadius.input),
                    modifier = Modifier
                        .weight(1f)
                        .height(SpendlySizing.buttonHeight),
                    border = BorderStroke(1.dp, Color(0xFFEF5350)),
                    contentPadding = PaddingValues(horizontal = 22.dp, vertical = 0.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF5350))
                    Spacer(Modifier.width(8.dp))
                    Text("Delete", style = MaterialTheme.typography.labelMedium)
                }

                Button(
                    onClick = ::saveGoal,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(SpendlyRadius.input),
                    modifier = Modifier
                        .weight(1f)
                        .height(SpendlySizing.buttonHeight),
                    contentPadding = PaddingValues(horizontal = 22.dp, vertical = 0.dp)
                ) {
                    Text("Save", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(
                    start = SpendlySpacing.screenHorizontal,
                    top = SpendlySpacing.screenTop,
                    end = SpendlySpacing.screenHorizontal,
                    bottom = SpendlySpacing.sectionGap
                ),
            verticalArrangement = Arrangement.spacedBy(SpendlySpacing.formGap)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(SpendlyRadius.card)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.TrackChanges,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        "Edit Goal",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Update your goal details",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            GoalFormField(
                label = "Goal Name",
                value = goalName,
                onValueChange = { goalName = it },
                placeholder = "e.g. Dream Vacation"
            )
            GoalIconSelector(
                selectedIconKey = selectedIconKey,
                onIconSelected = { selectedIconKey = it }
            )
            GoalAccentColorSelector(
                selectedColorKey = selectedIconAccentColorKey,
                onColorSelected = { selectedIconAccentColorKey = it }
            )
            GoalImageSelector(
                imageUri = goalImageUri.takeIf { it.isNotBlank() },
                onPickImage = { imagePicker.launch("image/*") },
                onClearImage = { goalImageUri = "" }
            )
            GoalStatusSelector(
                selectedStatus = status,
                onStatusSelected = { status = it }
            )
            GoalPrimarySwitch(
                checked = isPrimary,
                onCheckedChange = { isPrimary = it }
            )
            GoalFormField(
                label = "Target Amount (LKR)",
                value = targetAmount,
                onValueChange = { targetAmount = it },
                placeholder = "0",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            GoalDateField(
                label = "Target Date",
                value = targetDate,
                placeholder = "Select a date",
                onClick = { showDatePicker = true }
            )

            // Current savings card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(SpendlyRadius.card)
            ) {
                Column(modifier = Modifier.padding(SpendlySpacing.cardPadding)) {
                    Text("Current Savings (auto-calculated)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(formatMoney(goal.savedCents), style = MaterialTheme.typography.titleLarge, color = selectedGoalAccent.primary, fontWeight = FontWeight.Bold)
                    Text("${goal.progressPercent}% of ${formatMoney(goal.targetCents)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { goal.progressPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = selectedGoalAccent.primary,
                        trackColor = selectedGoalAccent.light
                    )
                }
            }

            // spacer where bottom bar provides actions
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            targetDate = formatGoalDate(millis)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddGoalScreenContent(
    onBack: () -> Unit,
    onSave: OnSaveGoal
) {
    var goalName by rememberSaveable { mutableStateOf("") }
    var status by rememberSaveable { mutableStateOf("Tracking") }
    var targetAmount by rememberSaveable { mutableStateOf("") }
    var targetDate by rememberSaveable { mutableStateOf("") }
    var initialSaved by rememberSaveable { mutableStateOf("") }
    var isPrimary by rememberSaveable { mutableStateOf(false) }
    var selectedIconKey by rememberSaveable { mutableStateOf("goal") }
    var selectedIconAccentColorKey by rememberSaveable { mutableStateOf(AccentColorKey.GREEN.storageValue) }
    var goalImageUri by rememberSaveable { mutableStateOf("") }
    var iconManuallySelected by rememberSaveable { mutableStateOf(false) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) goalImageUri = uri.toString()
    }

    LaunchedEffect(goalName) {
        if (!iconManuallySelected) selectedIconKey = suggestedGoalIconKey(goalName)
    }

    fun saveGoal() {
        onSave(
            GoalDraft(
                title = goalName,
                status = status,
                targetAmount = targetAmount,
                targetDate = targetDate,
                initialSaved = initialSaved,
                isPrimary = isPrimary,
                iconKey = selectedIconKey,
                iconAccentColorKey = selectedIconAccentColorKey,
                goalImageUri = goalImageUri.takeIf { it.isNotBlank() }
            )
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = { Text("Add New Goal", fontWeight = FontWeight.Bold) }
            )
        },
        floatingActionButton = {
            Button(
                onClick = ::saveGoal,
                    colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(SpendlyRadius.input),
                modifier = Modifier.height(SpendlySizing.buttonHeight),
                contentPadding = PaddingValues(horizontal = 22.dp, vertical = 0.dp)
            ) {
                Text("Save", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(
                    start = SpendlySpacing.screenHorizontal,
                    top = SpendlySpacing.screenTop,
                    end = SpendlySpacing.screenHorizontal,
                    bottom = SpendlySpacing.sectionGap
                ),
            verticalArrangement = Arrangement.spacedBy(SpendlySpacing.formGap)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(SpendlyRadius.card)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.TrackChanges,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        "Set a Target",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Plan your next big purchase",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            GoalFormField(
                label = "Goal Name",
                value = goalName,
                onValueChange = { goalName = it },
                placeholder = "e.g. Dream Vacation"
            )
            GoalIconSelector(
                selectedIconKey = selectedIconKey,
                onIconSelected = {
                    selectedIconKey = it
                    iconManuallySelected = true
                }
            )
            GoalAccentColorSelector(
                selectedColorKey = selectedIconAccentColorKey,
                onColorSelected = { selectedIconAccentColorKey = it }
            )
            GoalImageSelector(
                imageUri = goalImageUri.takeIf { it.isNotBlank() },
                onPickImage = { imagePicker.launch("image/*") },
                onClearImage = { goalImageUri = "" }
            )
            GoalStatusSelector(
                selectedStatus = status,
                onStatusSelected = { status = it }
            )
            GoalPrimarySwitch(
                checked = isPrimary,
                onCheckedChange = { isPrimary = it }
            )
            GoalFormField(
                label = "Target Amount",
                value = targetAmount,
                onValueChange = { targetAmount = it },
                placeholder = "0",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            GoalDateField(
                label = "Target Date",
                value = targetDate,
                placeholder = "Select a date",
                onClick = { showDatePicker = true }
            )
            GoalFormField(
                label = "Initial Saved (Optional)",
                value = initialSaved,
                onValueChange = { initialSaved = it },
                placeholder = "0",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            targetDate = formatGoalDate(millis)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun GoalStatusSelector(
    selectedStatus: String,
    onStatusSelected: (String) -> Unit
) {
    val statuses = listOf("Tracking", "Stopped", "Done")

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Status",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            statuses.forEach { status ->
                FilterChip(
                    selected = selectedStatus == status,
                    onClick = { onStatusSelected(status) },
                    label = { Text(status) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }
    }
}

@Composable
private fun GoalPrimarySwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(SpendlyRadius.card))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("Set as primary goal", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Text("Primary goals appear in the Primary Goals section.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun GoalDateField(
    label: String,
    value: String,
    placeholder: String,
    onClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        Box {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                trailingIcon = {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                singleLine = true,
                readOnly = true,
                shape = RoundedCornerShape(SpendlyRadius.input)
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(onClick = onClick)
            )
        }
    }
}

@Composable
private fun GoalIconSelector(
    selectedIconKey: String,
    onIconSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Icon",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(goalIconOptions, key = { it.key }) { option ->
                val selected = option.key == selectedIconKey
                Surface(
                    onClick = { onIconSelected(option.key) },
                    shape = RoundedCornerShape(SpendlyRadius.input),
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    border = if (selected) null else BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            option.icon,
                            contentDescription = option.label,
                            tint = if (selected) Color.White else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            option.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalAccentColorSelector(
    selectedColorKey: String,
    onColorSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Icon Color",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(AccentColorKey.entries, key = { it.storageValue }) { accent ->
                val palette = spendlyAccentPalette(accent.storageValue)
                val selected = AccentColorKey.fromStorage(selectedColorKey) == accent
                Surface(
                    onClick = { onColorSelected(accent.storageValue) },
                    shape = RoundedCornerShape(SpendlyRadius.pill),
                    color = if (selected) palette.light else MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, if (selected) palette.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(palette.primary)
                        )
                        Text(
                            accent.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalImageSelector(
    imageUri: String?,
    onPickImage: () -> Unit,
    onClearImage: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Goal Image",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        Surface(
            shape = RoundedCornerShape(SpendlyRadius.card),
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val bitmap = rememberGoalImageBitmap(imageUri)
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Default.TrackChanges,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        if (imageUri.isNullOrBlank()) "Use icon fallback" else "Custom image selected",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Optional image for goal cards and details.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TextButton(onClick = onPickImage) { Text("Choose") }
                if (!imageUri.isNullOrBlank()) {
                    TextButton(onClick = onClearImage) { Text("Clear") }
                }
            }
        }
    }
}

@Composable
private fun GoalFormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            singleLine = true,
            shape = RoundedCornerShape(SpendlyRadius.input),
            keyboardOptions = keyboardOptions
        )
    }
}

private fun formatGoalDate(timeMillis: Long): String =
    SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(timeMillis))

private fun resolvedGoalAccent(
    goal: Goal,
    fallbackAccentColorKey: String?
) = spendlyAccentPalette(
    goal.iconAccentColorKey
        .takeUnless { it.equals(AccentColorKey.GREEN.storageValue, ignoreCase = true) }
        ?: fallbackAccentColorKey
)

@Composable
private fun rememberGoalImageBitmap(uri: String?): ImageBitmap? {
    val context = LocalContext.current
    val image by produceState<ImageBitmap?>(initialValue = null, key1 = uri) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                uri?.takeIf { it.isNotBlank() }?.let {
                    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    context.contentResolver.openInputStream(Uri.parse(it))?.use { input ->
                        BitmapFactory.decodeStream(input, null, bounds)
                    }
                    val options = BitmapFactory.Options().apply {
                        var sample = 1
                        while (bounds.outWidth / sample > 512 || bounds.outHeight / sample > 512) sample *= 2
                        inSampleSize = sample
                    }
                    context.contentResolver.openInputStream(Uri.parse(it))?.use { input ->
                        BitmapFactory.decodeStream(input, null, options)?.asImageBitmap()
                    }
                }
            }.getOrNull()
        }
    }
    return image
}

@Composable
private fun GoalImageOrIcon(
    goal: Goal,
    modifier: Modifier = Modifier,
    containerColor: Color,
    iconTint: Color,
    iconSize: Int = 22
) {
    val bitmap = rememberGoalImageBitmap(goal.goalImageUri)
    Box(
        modifier = modifier.background(containerColor, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                goalIconForKey(goal.iconKey.ifBlank { suggestedGoalIconKey(goal.title) }),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(iconSize.dp)
            )
        }
    }
}

@Composable
private fun PrimaryGoalCard(
    goal: Goal,
    fallbackAccentColorKey: String?,
    onClick: () -> Unit
) {
    val accent = resolvedGoalAccent(goal, fallbackAccentColorKey)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = accent.primary),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                GoalImageOrIcon(
                    goal = goal,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    containerColor = Color.White.copy(alpha = 0.18f),
                    iconTint = Color.White
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    goal.title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = accent.dark,
                    shape = RoundedCornerShape(SpendlyRadius.pill)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                        Text(normalizedGoalStatus(goal.status).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    }
                }
            }

            Text(
                "${goal.dueDate} - ${formatMoney(goal.remainingCents)} remaining",
                style = MaterialTheme.typography.bodySmall,
                color = if (goal.isTargetDateExpired()) SpendlyRed else Color.White.copy(alpha = 0.82f)
            )

            LinearProgressIndicator(
                progress = { goal.progressPercent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = goalProgressColor(goal, Color.White),
                trackColor = Color.White.copy(alpha = 0.3f)
            )

            Row {
                Text(
                    formatMoney(goal.savedCents),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    " / ${formatMoney(goal.targetCents)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun OtherGoalRow(
    goal: Goal,
    fallbackAccentColorKey: String?,
    onClick: () -> Unit
) {
    val accent = resolvedGoalAccent(goal, fallbackAccentColorKey)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
        shape = RoundedCornerShape(SpendlyRadius.card)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GoalImageOrIcon(
                goal = goal,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp)),
                containerColor = accent.light,
                iconTint = accent.primary,
                iconSize = 20
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row {
                    Text(goal.title, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    Text("${goal.progressPercent}%", color = goalProgressColor(goal, accent.primary), fontWeight = FontWeight.Bold)
                }
                LinearProgressIndicator(
                    progress = { goal.progressPercent / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = goalProgressColor(goal, accent.primary),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Text(
                    "${formatMoney(goal.savedCents)} / ${formatMoney(goal.targetCents)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "${goal.dueDate} • ${normalizedGoalStatus(goal.status)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (goal.isTargetDateExpired()) SpendlyRed else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun GoalDetailScreenContent(
    state: FinanceUiState,
    goalId: String?,
    onAddSavings: OnAddSavings,
    onEdit: (String) -> Unit,
    onBack: () -> Unit
) {
    val goal = goalId?.let { selectedGoalId ->
        state.goals.firstOrNull { it.id == selectedGoalId }
    }
    var showAddSavingsDialog by rememberSaveable(goal?.id) { mutableStateOf(false) }
    var savingsAmount by rememberSaveable(goal?.id) { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = { Text(goal?.title ?: "Goal Details", fontWeight = FontWeight.Bold) },
                actions = {
                    if (goal != null) {
                        Button(
                            onClick = { onEdit(goal.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(SpendlyRadius.pill),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            modifier = Modifier.padding(end = 8.dp).height(34.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Edit", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (goal == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                NoGoalState(onSetGoal = onBack)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(
                    start = SpendlySpacing.screenHorizontal,
                    top = SpendlySpacing.screenTop,
                    end = SpendlySpacing.screenHorizontal,
                    bottom = SpendlySpacing.sectionGap
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    GoalDetailSummaryCard(
                        goal = goal,
                        fallbackAccentColorKey = state.profile?.accentColorKey,
                        onAddSavings = { showAddSavingsDialog = true }
                    )
                }
                item {
                    GoalMonthlySavingsCard(goal = goal, fallbackAccentColorKey = state.profile?.accentColorKey)
                }
            }
        }
    }

    if (goal != null && showAddSavingsDialog) {
        AddSavingsDialog(
            goalTitle = goal.title,
            amount = savingsAmount,
            onAmountChange = { savingsAmount = it },
            onDismiss = { showAddSavingsDialog = false },
            onApply = {
                if (onAddSavings(goal.id, savingsAmount)) {
                    savingsAmount = ""
                    showAddSavingsDialog = false
                }
            }
        )
    }
}

@Composable
private fun GoalDetailSummaryCard(
    goal: Goal,
    fallbackAccentColorKey: String?,
    onAddSavings: () -> Unit
) {
    val accent = resolvedGoalAccent(goal, fallbackAccentColorKey)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = accent.primary),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    GoalImageOrIcon(
                        goal = goal,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        containerColor = Color.White.copy(alpha = 0.18f),
                        iconTint = Color.White
                    )
                    Text(
                        goal.title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            "Target: ${goal.dueDate}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (goal.isTargetDateExpired()) SpendlyRed else Color.White.copy(alpha = 0.82f)
                        )
                        Text(
                            "Remaining: ${formatMoney(goal.remainingCents)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.82f)
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = accent.dark,
                        shape = RoundedCornerShape(SpendlyRadius.pill),
                        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.65f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                            Text(goal.status.uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        }
                    }
                        Button(
                            onClick = onAddSavings,
                            colors = ButtonDefaults.buttonColors(
                            containerColor = accent.dark,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(SpendlyRadius.pill),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add Savings", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            LinearProgressIndicator(
                progress = { goal.progressPercent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(9.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.3f)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${formatMoney(goal.savedCents)} (${goal.progressPercent}%)",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    formatMoney(goal.targetCents),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Saved: ${formatMoney(goal.savedCents)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "Saving / month: ${formatMoney(requiredMonthlySavingsCents(goal))}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
private fun GoalMonthlySavingsCard(goal: Goal, fallbackAccentColorKey: String?) {
    val savings = goalMonthlySavingsData(goal)
    val maxAmount = savings.maxOfOrNull { it.amountCents }?.coerceAtLeast(1L) ?: 1L
    val accent = resolvedGoalAccent(goal, fallbackAccentColorKey)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            Text(
                "Monthly Savings",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Column(
                    modifier = Modifier.height(138.dp).padding(top = 4.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    Text(formatCompactChartAmount(maxAmount), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(formatCompactChartAmount(maxAmount / 2), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("0", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                savings.forEach { item ->
                    GoalMonthlySavingsBar(
                        item = item,
                        maxAmount = maxAmount,
                        color = accent.primary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun GoalMonthlySavingsBar(
    item: GoalMonthlySavingUi,
    maxAmount: Long,
    color: Color,
    modifier: Modifier = Modifier
) {
    val percent = item.percent.takeIf { it > 0f }
        ?: (item.amountCents.toFloat() / maxAmount.toFloat()).coerceIn(0f, 1f)
    val barHeight = if (item.amountCents <= 0L) 0.dp else 14.dp + (112.dp * percent)

    Column(
        modifier = modifier.height(170.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Column(
            modifier = Modifier.height(138.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                formatCompactChartAmount(item.amountCents),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Spacer(Modifier.height(5.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.72f)
                    .height(barHeight)
                    .clip(RoundedCornerShape(6.dp))
                    .background(color)
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            item.month,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
private fun AddSavingsDialog(
    goalTitle: String,
    amount: String,
    onAmountChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onApply: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Savings") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Add the amount you saved for $goalTitle.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = onAmountChange,
                    placeholder = { Text("0") },
                    label = { Text("Saved amount") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onApply,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun formatCompactChartAmount(cents: Long): String {
    val amount = cents / 100L

    return when {
        amount >= 1_000_000L -> "${(amount + 500_000L) / 1_000_000L}M"
        amount >= 1_000L -> "${(amount + 500L) / 1_000L}K"
        else -> amount.toString()
    }
}

private fun Goal.isAchievedGoal(): Boolean =
    targetCents > 0L && (savedCents >= targetCents || normalizedGoalStatus(status) == "Done")

private fun Goal.isTargetDateExpired(): Boolean =
    dueDateMillis > 0L && dueDateMillis < System.currentTimeMillis() && !isAchievedGoal()

private fun normalizedGoalStatus(status: String): String =
    when (status.lowercase()) {
        "on track", "tracking" -> "Tracking"
        "not on track", "stopped" -> "Stopped"
        "done" -> "Done"
        else -> "Tracking"
    }

private fun goalProgressColor(goal: Goal, defaultColor: Color): Color =
    when (normalizedGoalStatus(goal.status)) {
        "Stopped" -> SpendlyAmber
        "Done" -> defaultColor
        else -> defaultColor
    }
