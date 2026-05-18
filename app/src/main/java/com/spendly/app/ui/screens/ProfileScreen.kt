package com.spendly.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.spendly.app.navigation.Screen
import com.spendly.app.ui.theme.*
import com.spendly.app.utils.FormatUtils
import com.spendly.app.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isLoggedOut) {
        if (uiState.isLoggedOut) {
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", style = SpendlyTypography.titleLarge, fontWeight = FontWeight.Bold) }
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
            // User Avatar and basic info
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(SpendlyGreenLight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.user?.name?.take(1)?.uppercase() ?: "U",
                        style = MaterialTheme.typography.displayMedium,
                        color = SpendlyGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    text = uiState.user?.name?.takeIf { it.isNotBlank() } ?: "Spendly User",
                    style = SpendlyTypography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = uiState.user?.email?.takeIf { it.isNotBlank() } ?: "No email available",
                    style = SpendlyTypography.bodyMedium,
                    color = SpendlyGray500
                )
            }

            // Settings sections
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ProfileSectionLabel("Preferences")
                ProfileItem(
                    icon = Icons.Default.CurrencyExchange,
                    label = "Default Currency",
                    value = uiState.user?.defaultCurrency?.name ?: "LKR"
                )
                ProfileItem(
                    icon = Icons.Default.Settings,
                    label = "USD to LKR Rate",
                    value = FormatUtils.formatLKR(uiState.user?.usdToLkrRate ?: 320.5)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ProfileSectionLabel("Account")
                ProfileItem(
                    icon = Icons.AutoMirrored.Filled.Logout,
                    label = "Logout",
                    labelColor = SpendlyRed,
                    showChevron = false,
                    onClick = { viewModel.logout() }
                )
            }
        }
    }
}

@Composable
private fun ProfileSectionLabel(text: String) {
    Text(
        text = text,
        style = SpendlyTypography.labelSmall,
        color = SpendlyGray500,
        modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
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
            Icon(icon, null, tint = if (labelColor == SpendlyRed) SpendlyRed else SpendlyGray700, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(16.dp))
            Text(label, style = SpendlyTypography.bodyLarge, color = labelColor, modifier = Modifier.weight(1f))
            if (value != null) {
                Text(value, style = SpendlyTypography.bodyMedium, color = SpendlyGray500)
            }
            if (showChevron) {
                Icon(Icons.Default.ChevronRight, null, tint = SpendlyGray300, modifier = Modifier.size(20.dp))
            }
        }
    }
}
