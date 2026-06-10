package com.spendly.financetracker.ui.screen.profile

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spendly.financetracker.ui.components.SpendlyRadius
import com.spendly.financetracker.ui.components.SpendlySizing
import com.spendly.financetracker.ui.components.SpendlySpacing
import com.spendly.financetracker.ui.theme.SpendlyGreen
import com.spendly.financetracker.ui.theme.SpendlyGreenDark
import com.spendly.financetracker.ui.theme.SpendlyGreenLight
import com.spendly.financetracker.ui.theme.SpendlyRed
import com.spendly.financetracker.ui.theme.AccentColorKey
import com.spendly.financetracker.ui.theme.ThemeMode
import com.spendly.financetracker.ui.theme.spendlyAccentPalette
import com.spendly.financetracker.ui.util.displayNameFromEmail
import com.spendly.financetracker.ui.util.initialsFromEmail
import com.spendly.financetracker.data.model.UserProfile
import com.spendly.financetracker.ui.viewmodel.AnalyticsViewModel
import com.spendly.financetracker.ui.viewmodel.FinanceUiState
import com.spendly.financetracker.ui.viewmodel.spendlyCurrencies
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    state: FinanceUiState,
    onUpdateProfile: (UserProfile) -> Unit,
    onChangePassword: (String, String, String) -> Unit,
    onDeleteAccount: (String) -> Unit,
    onOpenBudget: () -> Unit = {},
    onOpenRecurring: () -> Unit = {},
    onSignOut: () -> Unit
) {
    val analyticsViewModel: AnalyticsViewModel = hiltViewModel()
    val analyticsState by analyticsViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val email = state.session?.email.orEmpty()
    val name = state.profile?.name?.takeIf { it.isNotBlank() } ?: displayNameFromEmail(email)
    val initials = initialsFromEmail(name)
    val profileImage = rememberProfileBitmap(state.profile?.profileImageUri)
    var showEditProfile by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showAppearanceDialog by remember { mutableStateOf(false) }
    var showAccentDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showBudgetAlertDialog by remember { mutableStateOf(false) }
    var showDailyReminderDialog by remember { mutableStateOf(false) }
    var showChangePassword by remember { mutableStateOf(false) }
    var showDeleteAccount by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }
    var pendingProfileImageUri by remember { mutableStateOf<String?>(null) }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) pendingProfileImageUri = uri.toString()
    }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    fun openImagePicker() {
        imagePicker.launch("image/*")
    }
    fun UserProfile.withReminderSettingsFrom(current: UserProfile?): UserProfile = copy(
        dailyRemindersEnabled = current?.dailyRemindersEnabled ?: false,
        reminderTime = current?.reminderTime ?: "20:00",
        notificationFrequency = current?.notificationFrequency,
        remindExpenses = current?.remindExpenses ?: true,
        remindIncome = current?.remindIncome ?: true,
        smartReminderMode = current?.smartReminderMode ?: true
    )

    androidx.compose.runtime.LaunchedEffect(analyticsState.exportedReport) {
        val report = analyticsState.exportedReport ?: return@LaunchedEffect
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = report.mimeType
            putExtra(Intent.EXTRA_STREAM, report.uri)
            putExtra(Intent.EXTRA_SUBJECT, report.fileName)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Spendly backup"))
        analyticsViewModel.clearExport()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Profile",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
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
                    bottom = SpendlySpacing.mainScreenBottomPadding
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SpendlySpacing.sectionGap)
        ) {
            ProfileHeroCard(
                name = name,
                email = email.ifBlank { "No email available" },
                initials = initials,
                profileImage = profileImage,
                accentColorKey = state.profile?.accentColorKey,
                onEditImage = ::openImagePicker
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ProfileSectionLabel("Preferences")
                ProfileItem(
                    icon = Icons.Default.Edit,
                    label = "Edit Profile",
                    onClick = { showEditProfile = true }
                )
                ProfileItem(
                    icon = Icons.Default.CurrencyExchange,
                    label = "Default Currency",
                    value = state.profile?.defaultCurrency ?: "LKR",
                    onClick = { showCurrencyDialog = true }
                )
                ProfileItem(
                    icon = Icons.Default.DarkMode,
                    label = "Appearance",
                    value = ThemeMode.fromStorage(state.profile?.themeMode).label,
                    onClick = { showAppearanceDialog = true }
                )
                ProfileItem(
                    icon = Icons.Default.Settings,
                    label = "Accent Color",
                    value = AccentColorKey.fromStorage(state.profile?.accentColorKey).label,
                    onClick = { showAccentDialog = true }
                )
                ProfileItem(
                    icon = Icons.Default.Notifications,
                    label = "Daily Reminders",
                    value = if (state.profile?.dailyRemindersEnabled == true) state.profile.reminderTime ?: "20:00" else "Off",
                    onClick = { showDailyReminderDialog = true }
                )
                ProfileItem(
                    icon = Icons.Default.Notifications,
                    label = "Budget Alerts",
                    value = if (state.profile?.budgetAlertsEnabled != false) "${state.profile?.budgetAlertThresholdPercent ?: 80}%" else "Off",
                    onClick = { showBudgetAlertDialog = true }
                )
                ProfileItem(
                    icon = Icons.Default.AccountBalanceWallet,
                    label = "Budget Settings",
                    onClick = onOpenBudget
                )
                ProfileItem(
                    icon = Icons.Default.EventRepeat,
                    label = "Recurring Transactions",
                    onClick = onOpenRecurring
                )
                ProfileItem(
                    icon = Icons.Default.AccountBalanceWallet,
                    label = "Backup / Export",
                    value = "CSV, PDF",
                    onClick = { showExportDialog = true }
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ProfileSectionLabel("Account")
                ProfileItem(
                    icon = Icons.Default.Settings,
                    label = "Change Password",
                    onClick = { showChangePassword = true }
                )
                ProfileItem(
                    icon = Icons.AutoMirrored.Filled.Logout,
                    label = "Logout",
                    labelColor = SpendlyRed,
                    showChevron = false,
                    onClick = { showLogoutConfirm = true }
                )
                ProfileItem(
                    icon = Icons.Default.Delete,
                    label = "Delete Account",
                    labelColor = SpendlyRed,
                    showChevron = false,
                    onClick = { showDeleteAccount = true }
                )
            }
        }
    }

    if (showEditProfile) {
        var editedName by remember(showEditProfile) { mutableStateOf(state.profile?.name ?: name) }
        AlertDialog(
            onDismissRequest = { showEditProfile = false },
            title = { Text("Edit Profile") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        label = { Text("Name") },
                        singleLine = true
                    )
                    TextButton(onClick = { openImagePicker() }) {
                        Text("Change profile picture")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val current = state.profile
                        val uid = state.session?.uid.orEmpty()
                        if (uid.isNotBlank() && editedName.isNotBlank()) {
                            onUpdateProfile(
                                UserProfile(
                                    uid = current?.uid ?: uid,
                                    name = editedName.trim(),
                                    email = current?.email ?: email,
                                    defaultCurrency = current?.defaultCurrency ?: "LKR",
                                    createdAtMillis = current?.createdAtMillis ?: 0L,
                                    updatedAtMillis = current?.updatedAtMillis ?: 0L,
                                    isSynced = false,
                                    profileImageUri = current?.profileImageUri,
                                    exchangeRateSettings = current?.exchangeRateSettings.orEmpty(),
                                    notificationFrequency = current?.notificationFrequency,
                                    reminderTime = current?.reminderTime,
                                    categorySettingsJson = current?.categorySettingsJson.orEmpty(),
                                    themeMode = current?.themeMode ?: ThemeMode.SYSTEM.storageValue,
                                    budgetAlertsEnabled = current?.budgetAlertsEnabled ?: true,
                                    budgetAlertThresholdPercent = current?.budgetAlertThresholdPercent ?: 80,
                                    profileImageStoragePath = current?.profileImageStoragePath,
                                    accentColorKey = current?.accentColorKey ?: AccentColorKey.GREEN.storageValue,
                                    dailyRemindersEnabled = current?.dailyRemindersEnabled ?: false,
                                    remindExpenses = current?.remindExpenses ?: true,
                                    remindIncome = current?.remindIncome ?: true,
                                    smartReminderMode = current?.smartReminderMode ?: true
                                )
                            )
                            showEditProfile = false
                        }
                    }
                ) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showEditProfile = false }) { Text("Cancel") } }
        )
    }

    if (showCurrencyDialog) {
        AlertDialog(
            onDismissRequest = { showCurrencyDialog = false },
            title = { Text("Default Currency") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    spendlyCurrencies.forEach { currency ->
                        Surface(
                            onClick = {
                                val current = state.profile
                                val uid = state.session?.uid.orEmpty()
                                if (uid.isNotBlank()) {
                                    onUpdateProfile(
                                        UserProfile(
                                            uid = current?.uid ?: uid,
                                            name = current?.name ?: name,
                                            email = current?.email ?: email,
                                            defaultCurrency = currency,
                                            createdAtMillis = current?.createdAtMillis ?: 0L,
                                            updatedAtMillis = current?.updatedAtMillis ?: 0L,
                                            isSynced = false,
                                            profileImageUri = current?.profileImageUri,
                                            exchangeRateSettings = current?.exchangeRateSettings.orEmpty(),
                                            notificationFrequency = current?.notificationFrequency,
                                            reminderTime = current?.reminderTime,
                                            categorySettingsJson = current?.categorySettingsJson.orEmpty(),
                                            themeMode = current?.themeMode ?: ThemeMode.SYSTEM.storageValue,
                                            budgetAlertsEnabled = current?.budgetAlertsEnabled ?: true,
                                            budgetAlertThresholdPercent = current?.budgetAlertThresholdPercent ?: 80,
                                            profileImageStoragePath = current?.profileImageStoragePath,
                                            accentColorKey = current?.accentColorKey ?: AccentColorKey.GREEN.storageValue,
                                    dailyRemindersEnabled = current?.dailyRemindersEnabled ?: false,
                                    remindExpenses = current?.remindExpenses ?: true,
                                    remindIncome = current?.remindIncome ?: true,
                                    smartReminderMode = current?.smartReminderMode ?: true
                                        )
                                    )
                                }
                                showCurrencyDialog = false
                            },
                            shape = RoundedCornerShape(SpendlyRadius.dialogOption),
                            color = if ((state.profile?.defaultCurrency ?: "LKR") == currency) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                currency,
                                modifier = Modifier.padding(14.dp),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showCurrencyDialog = false }) { Text("Cancel") } }
        )
    }

    if (showAppearanceDialog) {
        AlertDialog(
            onDismissRequest = { showAppearanceDialog = false },
            title = { Text("Appearance") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeMode.entries.forEach { mode ->
                        val selected = ThemeMode.fromStorage(state.profile?.themeMode) == mode
                        Surface(
                            onClick = {
                                val current = state.profile
                                val uid = state.session?.uid.orEmpty()
                                if (uid.isNotBlank()) {
                                    onUpdateProfile(
                                        UserProfile(
                                            uid = current?.uid ?: uid,
                                            name = current?.name ?: name,
                                            email = current?.email ?: email,
                                            defaultCurrency = current?.defaultCurrency ?: "LKR",
                                            createdAtMillis = current?.createdAtMillis ?: 0L,
                                            updatedAtMillis = current?.updatedAtMillis ?: 0L,
                                            isSynced = false,
                                            profileImageUri = current?.profileImageUri,
                                            exchangeRateSettings = current?.exchangeRateSettings.orEmpty(),
                                            notificationFrequency = current?.notificationFrequency,
                                            reminderTime = current?.reminderTime,
                                            categorySettingsJson = current?.categorySettingsJson.orEmpty(),
                                            themeMode = mode.storageValue,
                                            budgetAlertsEnabled = current?.budgetAlertsEnabled ?: true,
                                            budgetAlertThresholdPercent = current?.budgetAlertThresholdPercent ?: 80,
                                            profileImageStoragePath = current?.profileImageStoragePath,
                                            accentColorKey = current?.accentColorKey ?: AccentColorKey.GREEN.storageValue,
                                    dailyRemindersEnabled = current?.dailyRemindersEnabled ?: false,
                                    remindExpenses = current?.remindExpenses ?: true,
                                    remindIncome = current?.remindIncome ?: true,
                                    smartReminderMode = current?.smartReminderMode ?: true
                                        )
                                    )
                                }
                                showAppearanceDialog = false
                            },
                            shape = RoundedCornerShape(SpendlyRadius.dialogOption),
                            color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                mode.label,
                                modifier = Modifier.padding(14.dp),
                                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showAppearanceDialog = false }) { Text("Cancel") } }
        )
    }

    if (showAccentDialog) {
        AlertDialog(
            onDismissRequest = { showAccentDialog = false },
            title = { Text("Accent Color") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AccentColorKey.entries.forEach { accent ->
                        val palette = spendlyAccentPalette(accent.storageValue)
                        val selected = AccentColorKey.fromStorage(state.profile?.accentColorKey) == accent
                        Surface(
                            onClick = {
                                val current = state.profile
                                val uid = state.session?.uid.orEmpty()
                                if (uid.isNotBlank()) {
                                    onUpdateProfile(
                                        UserProfile(
                                            uid = current?.uid ?: uid,
                                            name = current?.name ?: name,
                                            email = current?.email ?: email,
                                            defaultCurrency = current?.defaultCurrency ?: "LKR",
                                            createdAtMillis = current?.createdAtMillis ?: 0L,
                                            updatedAtMillis = current?.updatedAtMillis ?: 0L,
                                            isSynced = false,
                                            profileImageUri = current?.profileImageUri,
                                            exchangeRateSettings = current?.exchangeRateSettings.orEmpty(),
                                            notificationFrequency = current?.notificationFrequency,
                                            reminderTime = current?.reminderTime,
                                            categorySettingsJson = current?.categorySettingsJson.orEmpty(),
                                            themeMode = current?.themeMode ?: ThemeMode.SYSTEM.storageValue,
                                            budgetAlertsEnabled = current?.budgetAlertsEnabled ?: true,
                                            budgetAlertThresholdPercent = current?.budgetAlertThresholdPercent ?: 80,
                                            profileImageStoragePath = current?.profileImageStoragePath,
                                            accentColorKey = accent.storageValue,
                                            dailyRemindersEnabled = current?.dailyRemindersEnabled ?: false,
                                            remindExpenses = current?.remindExpenses ?: true,
                                            remindIncome = current?.remindIncome ?: true,
                                            smartReminderMode = current?.smartReminderMode ?: true
                                        )
                                    )
                                }
                                showAccentDialog = false
                            },
                            shape = RoundedCornerShape(SpendlyRadius.dialogOption),
                            color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(palette.primary)
                                )
                                Text(
                                    accent.label,
                                    color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showAccentDialog = false }) { Text("Cancel") } }
        )
    }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Backup / Export") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Create a temporary Spendly report for the selected analytics month. PDF includes colored analytics, budgets, goals, and recent transactions.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        onClick = {
                            analyticsViewModel.exportCsv()
                            showExportDialog = false
                        },
                        shape = RoundedCornerShape(SpendlyRadius.dialogOption),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Export CSV", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("Transaction backup for spreadsheet review.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Surface(
                        onClick = {
                            analyticsViewModel.exportPdf()
                            showExportDialog = false
                        },
                        shape = RoundedCornerShape(SpendlyRadius.dialogOption),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Export Detailed PDF", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("Colored monthly analytics report for sharing or backup.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showExportDialog = false }) { Text("Cancel") } }
        )
    }

    if (showBudgetAlertDialog) {
        var enabled by remember(showBudgetAlertDialog) { mutableStateOf(state.profile?.budgetAlertsEnabled != false) }
        var threshold by remember(showBudgetAlertDialog) { mutableStateOf((state.profile?.budgetAlertThresholdPercent ?: 80).toString()) }
        AlertDialog(
            onDismissRequest = { showBudgetAlertDialog = false },
            title = { Text("Budget Alerts") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Enable local budget alerts", modifier = Modifier.weight(1f))
                        Switch(checked = enabled, onCheckedChange = { enabled = it })
                    }
                    OutlinedTextField(
                        value = threshold,
                        onValueChange = { value -> if (value.all { it.isDigit() } && value.length <= 3) threshold = value },
                        label = { Text("Warning threshold %") },
                        singleLine = true,
                        enabled = enabled
                    )
                    Text("Alerts appear once per budget, month, and threshold.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                Button(onClick = {
                    val current = state.profile
                    val uid = state.session?.uid.orEmpty()
                    val safeThreshold = threshold.toIntOrNull()?.coerceIn(1, 100) ?: 80
                    if (uid.isNotBlank()) {
                        if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        onUpdateProfile(
                            UserProfile(
                                uid = current?.uid ?: uid,
                                name = current?.name ?: name,
                                email = current?.email ?: email,
                                defaultCurrency = current?.defaultCurrency ?: "LKR",
                                createdAtMillis = current?.createdAtMillis ?: 0L,
                                updatedAtMillis = current?.updatedAtMillis ?: 0L,
                                isSynced = false,
                                profileImageUri = current?.profileImageUri,
                                exchangeRateSettings = current?.exchangeRateSettings.orEmpty(),
                                notificationFrequency = current?.notificationFrequency,
                                reminderTime = current?.reminderTime,
                                categorySettingsJson = current?.categorySettingsJson.orEmpty(),
                                themeMode = current?.themeMode ?: ThemeMode.SYSTEM.storageValue,
                                budgetAlertsEnabled = enabled,
                                budgetAlertThresholdPercent = safeThreshold,
                                profileImageStoragePath = current?.profileImageStoragePath,
                                accentColorKey = current?.accentColorKey ?: AccentColorKey.GREEN.storageValue,
                                    dailyRemindersEnabled = current?.dailyRemindersEnabled ?: false,
                                    remindExpenses = current?.remindExpenses ?: true,
                                    remindIncome = current?.remindIncome ?: true,
                                    smartReminderMode = current?.smartReminderMode ?: true
                            )
                        )
                    }
                    showBudgetAlertDialog = false
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showBudgetAlertDialog = false }) { Text("Cancel") } }
        )
    }

    if (showDailyReminderDialog) {
        var enabled by remember(showDailyReminderDialog) { mutableStateOf(state.profile?.dailyRemindersEnabled == true) }
        var reminderTime by remember(showDailyReminderDialog) { mutableStateOf(state.profile?.reminderTime ?: "20:00") }
        var remindExpenses by remember(showDailyReminderDialog) { mutableStateOf(state.profile?.remindExpenses != false) }
        var remindIncome by remember(showDailyReminderDialog) { mutableStateOf(state.profile?.remindIncome != false) }
        var smartMode by remember(showDailyReminderDialog) { mutableStateOf(state.profile?.smartReminderMode != false) }
        val isValidTime = Regex("^([01]\\d|2[0-3]):[0-5]\\d$").matches(reminderTime)
        AlertDialog(
            onDismissRequest = { showDailyReminderDialog = false },
            title = { Text("Daily Reminders") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Enable daily reminders", modifier = Modifier.weight(1f))
                        Switch(checked = enabled, onCheckedChange = { enabled = it })
                    }
                    OutlinedTextField(
                        value = reminderTime,
                        onValueChange = { value ->
                            if (value.length <= 5 && value.all { it.isDigit() || it == ':' }) reminderTime = value
                        },
                        label = { Text("Reminder time (HH:mm)") },
                        singleLine = true,
                        enabled = enabled,
                        isError = enabled && !isValidTime
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Remind for expenses", modifier = Modifier.weight(1f))
                        Switch(checked = remindExpenses, onCheckedChange = { remindExpenses = it }, enabled = enabled)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Remind for income", modifier = Modifier.weight(1f))
                        Switch(checked = remindIncome, onCheckedChange = { remindIncome = it }, enabled = enabled)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Smart mode", modifier = Modifier.weight(1f))
                        Switch(checked = smartMode, onCheckedChange = { smartMode = it }, enabled = enabled)
                    }
                    Text(
                        "Smart mode skips reminders when the selected transaction types were already added today.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = !enabled || isValidTime,
                    onClick = {
                        val current = state.profile
                        val uid = state.session?.uid.orEmpty()
                        if (uid.isNotBlank()) {
                            if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                            onUpdateProfile(
                                UserProfile(
                                    uid = current?.uid ?: uid,
                                    name = current?.name ?: name,
                                    email = current?.email ?: email,
                                    defaultCurrency = current?.defaultCurrency ?: "LKR",
                                    createdAtMillis = current?.createdAtMillis ?: 0L,
                                    updatedAtMillis = current?.updatedAtMillis ?: 0L,
                                    isSynced = false,
                                    profileImageUri = current?.profileImageUri,
                                    exchangeRateSettings = current?.exchangeRateSettings.orEmpty(),
                                    notificationFrequency = if (enabled) "DAILY" else current?.notificationFrequency,
                                    reminderTime = reminderTime,
                                    categorySettingsJson = current?.categorySettingsJson.orEmpty(),
                                    themeMode = current?.themeMode ?: ThemeMode.SYSTEM.storageValue,
                                    budgetAlertsEnabled = current?.budgetAlertsEnabled ?: true,
                                    budgetAlertThresholdPercent = current?.budgetAlertThresholdPercent ?: 80,
                                    profileImageStoragePath = current?.profileImageStoragePath,
                                    accentColorKey = current?.accentColorKey ?: AccentColorKey.GREEN.storageValue,
                                    dailyRemindersEnabled = enabled,
                                    remindExpenses = remindExpenses,
                                    remindIncome = remindIncome,
                                    smartReminderMode = smartMode
                                )
                            )
                        }
                        showDailyReminderDialog = false
                    }
                ) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showDailyReminderDialog = false }) { Text("Cancel") } }
        )
    }

    if (showChangePassword) {
        var currentPassword by remember(showChangePassword) { mutableStateOf("") }
        var newPassword by remember(showChangePassword) { mutableStateOf("") }
        var confirmPassword by remember(showChangePassword) { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showChangePassword = false },
            title = { Text("Change Password") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        label = { Text("Current password") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New password") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm new password") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onChangePassword(currentPassword, newPassword, confirmPassword)
                    showChangePassword = false
                }) { Text("Update") }
            },
            dismissButton = { TextButton(onClick = { showChangePassword = false }) { Text("Cancel") } }
        )
    }

    if (showDeleteAccount) {
        var currentPassword by remember(showDeleteAccount) { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showDeleteAccount = false },
            title = { Text("Delete account?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("This permanently deletes your Spendly account. Enter your current password to continue.")
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        label = { Text("Current password") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteAccount(currentPassword)
                    showDeleteAccount = false
                }) { Text("Delete", color = SpendlyRed) }
            },
            dismissButton = { TextButton(onClick = { showDeleteAccount = false }) { Text("Cancel") } }
        )
    }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("Log out?") },
            text = { Text("You will need to sign in again to access Spendly.") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutConfirm = false
                    onSignOut()
                }) { Text("Logout", color = SpendlyRed) }
            },
            dismissButton = { TextButton(onClick = { showLogoutConfirm = false }) { Text("Cancel") } }
        )
    }

    pendingProfileImageUri?.let { uri ->
        ProfileImageEditorDialog(
            imageUri = uri,
            onDismiss = { pendingProfileImageUri = null },
            onSave = { finalUri ->
                val current = state.profile
                val uid = state.session?.uid.orEmpty()
                if (uid.isNotBlank()) {
                    onUpdateProfile(
                        UserProfile(
                            uid = current?.uid ?: uid,
                            name = current?.name ?: name,
                            email = current?.email ?: email,
                            defaultCurrency = current?.defaultCurrency ?: "LKR",
                            createdAtMillis = current?.createdAtMillis ?: 0L,
                            updatedAtMillis = current?.updatedAtMillis ?: 0L,
                            isSynced = false,
                            profileImageUri = finalUri,
                            exchangeRateSettings = current?.exchangeRateSettings.orEmpty(),
                            notificationFrequency = current?.notificationFrequency,
                            reminderTime = current?.reminderTime,
                            categorySettingsJson = current?.categorySettingsJson.orEmpty(),
                            themeMode = current?.themeMode ?: ThemeMode.SYSTEM.storageValue,
                            budgetAlertsEnabled = current?.budgetAlertsEnabled ?: true,
                            budgetAlertThresholdPercent = current?.budgetAlertThresholdPercent ?: 80,
                            profileImageStoragePath = current?.profileImageStoragePath,
                            accentColorKey = current?.accentColorKey ?: AccentColorKey.GREEN.storageValue,
                                    dailyRemindersEnabled = current?.dailyRemindersEnabled ?: false,
                                    remindExpenses = current?.remindExpenses ?: true,
                                    remindIncome = current?.remindIncome ?: true,
                                    smartReminderMode = current?.smartReminderMode ?: true
                        )
                    )
                }
                pendingProfileImageUri = null
            }
        )
    }
}

@Composable
private fun ProfileHeroCard(
    name: String,
    email: String,
    initials: String,
    profileImage: ImageBitmap?,
    accentColorKey: String?,
    onEditImage: () -> Unit
) {
    val accentPalette = spendlyAccentPalette(accentColorKey)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(238.dp),
        shape = RoundedCornerShape(28.dp),
        color = Color.Transparent,
        shadowElevation = 6.dp
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(accentPalette.dark, accentPalette.primary, accentPalette.gradientEnd)
                    )
                )
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier.size(116.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(104.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.92f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (profileImage != null) {
                            Image(
                                bitmap = profileImage,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = initials,
                                style = MaterialTheme.typography.displaySmall,
                                color = accentPalette.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(36.dp)
                            .clickable { onEditImage() },
                        shape = CircleShape,
                        color = Color.White
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit profile image",
                                tint = accentPalette.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.size(12.dp))
                Text(
                    text = name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.78f)
                )
            }
        }
    }
}

@Composable
private fun rememberProfileBitmap(uri: String?): ImageBitmap? {
    val context = LocalContext.current
    val image by produceState<ImageBitmap?>(initialValue = null, key1 = uri) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                uri?.takeIf { it.isNotBlank() }?.let {
                    val openStream = {
                        if (it.startsWith("http://") || it.startsWith("https://")) {
                            URL(it).openStream()
                        } else {
                            context.contentResolver.openInputStream(android.net.Uri.parse(it))
                        }
                    }
                    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    openStream()?.use { input -> BitmapFactory.decodeStream(input, null, bounds) }
                    val options = BitmapFactory.Options().apply {
                        var sample = 1
                        while (bounds.outWidth / sample > 512 || bounds.outHeight / sample > 512) sample *= 2
                        inSampleSize = sample
                    }
                    openStream()?.use { input -> BitmapFactory.decodeStream(input, null, options)?.asImageBitmap() }
                }
            }.getOrNull()
        }
    }
    return image
}

@Composable
private fun ProfileImageEditorDialog(
    imageUri: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var rotation by remember(imageUri) { mutableStateOf(0) }
    val preview by produceState<ImageBitmap?>(initialValue = null, key1 = imageUri, key2 = rotation) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                val source = decodeScaledBitmap(context, imageUri, 1200) ?: return@runCatching null
                val edited = centerCropSquare(rotateBitmap(source, rotation))
                edited.asImageBitmap()
            }.getOrNull()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit profile image") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (preview != null) {
                        Image(
                            bitmap = preview!!,
                            contentDescription = "Profile image preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text("Preview unavailable", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TextButton(onClick = { rotation = (rotation + 270) % 360 }) {
                        Text("Rotate left")
                    }
                    TextButton(onClick = { rotation = (rotation + 90) % 360 }) {
                        Text("Rotate right")
                    }
                }
                Text(
                    "The image is saved as a square crop for a clean avatar.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                enabled = preview != null,
                onClick = {
                    scope.launch {
                        val finalUri = saveEditedProfileImage(context, imageUri, rotation)
                        onSave(finalUri)
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

private suspend fun saveEditedProfileImage(
    context: Context,
    imageUri: String,
    rotation: Int
): String = withContext(Dispatchers.IO) {
    val source = decodeScaledBitmap(context, imageUri, 1600) ?: return@withContext imageUri
    val edited = centerCropSquare(rotateBitmap(source, rotation))
    val dir = File(context.cacheDir, "images").apply { mkdirs() }
    val file = File(dir, "profile-${System.currentTimeMillis()}.jpg")
    FileOutputStream(file).use { out ->
        edited.compress(Bitmap.CompressFormat.JPEG, 90, out)
    }
    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file).toString()
}

private fun decodeScaledBitmap(context: Context, uri: String, maxSize: Int): Bitmap? {
    fun openStream() = context.contentResolver.openInputStream(Uri.parse(uri))
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    openStream()?.use { input -> BitmapFactory.decodeStream(input, null, bounds) }
    val options = BitmapFactory.Options().apply {
        var sample = 1
        while (bounds.outWidth / sample > maxSize || bounds.outHeight / sample > maxSize) sample *= 2
        inSampleSize = sample
    }
    return openStream()?.use { input -> BitmapFactory.decodeStream(input, null, options) }
}

private fun rotateBitmap(source: Bitmap, degrees: Int): Bitmap {
    if (degrees % 360 == 0) return source
    val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
    return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
}

private fun centerCropSquare(source: Bitmap): Bitmap {
    val size = minOf(source.width, source.height)
    val x = ((source.width - size) / 2).coerceAtLeast(0)
    val y = ((source.height - size) / 2).coerceAtLeast(0)
    return Bitmap.createBitmap(source, x, y, size, size)
}

@Composable
private fun ProfileSectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun ProfileItem(
    icon: ImageVector,
    label: String,
    value: String? = null,
    labelColor: Color = Color.Unspecified,
    showChevron: Boolean = true,
    onClick: () -> Unit = {}
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(SpendlyRadius.card),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(SpendlySpacing.cardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                null,
                tint = if (labelColor == SpendlyRed) SpendlyRed else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(SpendlySizing.iconMedium)
            )
            Spacer(Modifier.width(16.dp))
            Text(
                label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (labelColor == Color.Unspecified) MaterialTheme.colorScheme.onSurface else labelColor,
                modifier = Modifier.weight(1f)
            )
            if (value != null) {
                Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (showChevron) {
                Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(20.dp))
            }
        }
    }
}
