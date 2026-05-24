package com.spendly.financetracker.ui.screen.profile

import android.Manifest
import android.os.Build
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spendly.financetracker.ui.theme.SpendlyGray300
import com.spendly.financetracker.ui.theme.SpendlyGray50
import com.spendly.financetracker.ui.theme.SpendlyGray500
import com.spendly.financetracker.ui.theme.SpendlyGray700
import com.spendly.financetracker.ui.theme.SpendlyGray900
import com.spendly.financetracker.ui.theme.SpendlyGreen
import com.spendly.financetracker.ui.theme.SpendlyGreenLight
import com.spendly.financetracker.ui.theme.SpendlyRed
import com.spendly.financetracker.ui.util.displayNameFromEmail
import com.spendly.financetracker.ui.util.initialsFromEmail
import com.spendly.financetracker.data.model.UserProfile
import com.spendly.financetracker.ui.viewmodel.FinanceUiState
import com.spendly.financetracker.ui.viewmodel.spendlyCurrencies

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    state: FinanceUiState,
    onUpdateProfile: (UserProfile) -> Unit,
    onChangePassword: (String) -> Unit,
    onSignOut: () -> Unit
) {
    val email = state.session?.email.orEmpty()
    val name = state.profile?.name?.takeIf { it.isNotBlank() } ?: displayNameFromEmail(email)
    val initials = initialsFromEmail(name)
    val profileImage = rememberProfileBitmap(state.profile?.profileImageUri)
    var showEditProfile by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showExchangeRate by remember { mutableStateOf(false) }
    var showChangePassword by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        val current = state.profile
        val uid = state.session?.uid.orEmpty()
        if (uri != null && uid.isNotBlank()) {
            onUpdateProfile(
                UserProfile(
                    uid = current?.uid ?: uid,
                    name = current?.name ?: name,
                    email = current?.email ?: email,
                    defaultCurrency = current?.defaultCurrency ?: "LKR",
                    createdAtMillis = current?.createdAtMillis ?: 0L,
                    updatedAtMillis = current?.updatedAtMillis ?: 0L,
                    isSynced = false,
                    profileImageUri = uri.toString(),
                    exchangeRateSettings = current?.exchangeRateSettings.orEmpty(),
                    notificationFrequency = current?.notificationFrequency,
                    reminderTime = current?.reminderTime,
                    categorySettingsJson = current?.categorySettingsJson.orEmpty()
                )
            )
        }
    }
    val imagePermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) imagePicker.launch("image/*")
    }
    fun openImagePicker() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            imagePermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            imagePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(112.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(SpendlyGreenLight),
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
                                style = MaterialTheme.typography.displayMedium,
                                color = SpendlyGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(34.dp)
                            .clickable { openImagePicker() },
                        shape = CircleShape,
                        color = SpendlyGreen
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit profile image", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }
                Spacer(Modifier.size(16.dp))
                Text(
                    text = name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = email.ifBlank { "No email available" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = SpendlyGray500
                )
            }

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
                    icon = Icons.Default.CurrencyExchange,
                    label = "Exchange Rate",
                    onClick = { showExchangeRate = true }
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
                    icon = Icons.Default.Logout,
                    label = "Logout",
                    labelColor = SpendlyRed,
                    showChevron = false,
                    onClick = { showLogoutConfirm = true }
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
                                    categorySettingsJson = current?.categorySettingsJson.orEmpty()
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
                                            categorySettingsJson = current?.categorySettingsJson.orEmpty()
                                        )
                                    )
                                }
                                showCurrencyDialog = false
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = if ((state.profile?.defaultCurrency ?: "LKR") == currency) SpendlyGreenLight else SpendlyGray50,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(currency, modifier = Modifier.padding(14.dp), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showCurrencyDialog = false }) { Text("Cancel") } }
        )
    }

    if (showExchangeRate) {
        AlertDialog(
            onDismissRequest = { showExchangeRate = false },
            title = { Text("Exchange Rate Settings") },
            text = { Text("Rates are saved with each transaction. Manual rates can be entered from Add Income when live rates are unavailable.") },
            confirmButton = { TextButton(onClick = { showExchangeRate = false }) { Text("OK") } }
        )
    }

    if (showChangePassword) {
        var newPassword by remember(showChangePassword) { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showChangePassword = false },
            title = { Text("Change Password") },
            text = {
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New password") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onChangePassword(newPassword)
                    showChangePassword = false
                }) { Text("Update") }
            },
            dismissButton = { TextButton(onClick = { showChangePassword = false }) { Text("Cancel") } }
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
}

@Composable
private fun rememberProfileBitmap(uri: String?): ImageBitmap? {
    val context = LocalContext.current
    return remember(uri) {
        runCatching {
        uri?.takeIf { it.isNotBlank() }?.let {
            context.contentResolver.openInputStream(android.net.Uri.parse(it))?.use { stream ->
                BitmapFactory.decodeStream(stream)?.asImageBitmap()
            }
        }
        }.getOrNull()
    }
}

@Composable
private fun ProfileSectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = SpendlyGray500,
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
    labelColor: Color = SpendlyGray900,
    showChevron: Boolean = true,
    onClick: () -> Unit = {}
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = SpendlyGray50,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                null,
                tint = if (labelColor == SpendlyRed) SpendlyRed else SpendlyGray700,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(16.dp))
            Text(
                label,
                style = MaterialTheme.typography.bodyLarge,
                color = labelColor,
                modifier = Modifier.weight(1f)
            )
            if (value != null) {
                Text(value, style = MaterialTheme.typography.bodyMedium, color = SpendlyGray500)
            }
            if (showChevron) {
                Icon(Icons.Default.ChevronRight, null, tint = SpendlyGray300, modifier = Modifier.size(20.dp))
            }
        }
    }
}
