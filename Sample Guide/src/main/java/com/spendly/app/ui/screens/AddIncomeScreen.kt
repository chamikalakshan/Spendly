package com.spendly.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.spendly.app.data.model.enums.Currency
import com.spendly.app.data.model.enums.IncomeSource
import com.spendly.app.data.model.enums.InvoiceStatus
import com.spendly.app.ui.components.ErrorBanner
import com.spendly.app.ui.theme.*
import com.spendly.app.viewmodel.AddIncomeViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddIncomeScreen(
    navController: NavController,
    editIncomeId: String? = null,
    viewModel: AddIncomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(editIncomeId) {
        viewModel.loadIncomeForEdit(editIncomeId)
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            navController.popBackStack()
        }
    }

    if (uiState.showAddSourceDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissAddSourceDialog() },
            title = { Text("Add income source") },
            text = {
                OutlinedTextField(
                    value = uiState.newSourceInput,
                    onValueChange = { viewModel.onNewSourceInputChanged(it) },
                    label = { Text("Source name") },
                    placeholder = { Text("e.g. YouTube, Dividends") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmAddSource() }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissAddSourceDialog() }) {
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
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                title = { Text(if (editIncomeId.isNullOrBlank()) "Add Income" else "Edit Income") },
                actions = {
                    Button(
                        onClick = { viewModel.saveIncome() },
                        enabled = !uiState.isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SpendlyGreen,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        modifier = Modifier.padding(end = 8.dp).height(36.dp)
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

            // Income Source section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Income Source",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { viewModel.showAddSourceDialog() }) {
                        Text("Edit", color = SpendlyGreen, style = MaterialTheme.typography.labelMedium)
                    }
                }

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IncomeSource.entries.forEach { source ->
                        FilterChip(
                            selected = uiState.selectedSource == source,
                            onClick = { viewModel.onSourceSelected(source) },
                            label = { Text(source.displayName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SpendlyGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                    uiState.customSources.forEach { custom ->
                        FilterChip(
                            selected = uiState.selectedCustomSource == custom,
                            onClick = { viewModel.onCustomSourceSelected(custom) },
                            label = { Text(custom) }
                        )
                    }
                    AssistChip(
                        onClick = { viewModel.showAddSourceDialog() },
                        label = { Text("Add +") },
                        leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp)) }
                    )
                }
            }

            // Name field
            SectionLabel("Name")
            OutlinedTextField(
                value = uiState.name,
                onValueChange = { viewModel.onNameChanged(it) },
                placeholder = { Text("e.g. May Salary, Client ABC") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Amount field
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Amount", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.weight(1f))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        FilterChip(
                            selected = uiState.currency == Currency.LKR,
                            onClick = { viewModel.onCurrencyChanged(Currency.LKR) },
                            label = { Text("LKR") },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SpendlyGreen, selectedLabelColor = Color.White)
                        )
                        FilterChip(
                            selected = uiState.currency == Currency.USD,
                            onClick = { viewModel.onCurrencyChanged(Currency.USD) },
                            label = { Text("USD") },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SpendlyGreen, selectedLabelColor = Color.White)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SpendlyGray300, RoundedCornerShape(12.dp))
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    BasicTextField(
                        value = uiState.amount,
                        onValueChange = { viewModel.onAmountChanged(it) },
                        textStyle = TextStyle(
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = if (uiState.amount == "0.00" || uiState.amount.isEmpty()) SpendlyGray300 else SpendlyGray900
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { innerTextField ->
                            if (uiState.amount.isEmpty()) {
                                Text(
                                    "0.00",
                                    fontSize = 36.sp,
                                    color = SpendlyGray300,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            innerTextField()
                        }
                    )
                }
            }

            // USD fields
            AnimatedVisibility(visible = uiState.currency == Currency.USD) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = uiState.exchangeRate,
                        onValueChange = { viewModel.onExchangeRateChanged(it) },
                        label = { Text("Exchange rate (1 USD = LKR)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = uiState.amountLKR,
                        onValueChange = {},
                        enabled = false,
                        label = { Text("LKR equivalent (auto-calculated)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Conditional source fields
            AnimatedVisibility(visible = uiState.selectedSource == IncomeSource.FREELANCE) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = uiState.projectName,
                        onValueChange = { viewModel.onProjectNameChanged(it) },
                        label = { Text("Project name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        InvoiceStatus.entries.forEach { status ->
                            FilterChip(
                                selected = uiState.invoiceStatus == status,
                                onClick = { viewModel.onInvoiceStatusSelected(status) },
                                label = { Text(status.displayName) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SpendlyGreen,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(visible = uiState.selectedSource == IncomeSource.CRYPTO) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionLabel("Coin")
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("USDT", "ETH", "BTC", "Other").forEach { c ->
                            FilterChip(
                                selected = uiState.coin == c,
                                onClick = { viewModel.onCoinSelected(c) },
                                label = { Text(c) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SpendlyGreen,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // Date field
            var showDatePicker by remember { mutableStateOf(false) }
            SectionLabel("Date")
            OutlinedTextField(
                value = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(uiState.selectedDate)),
                onValueChange = {},
                enabled = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true },
                trailingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            if (showDatePicker) {
                val datePickerState = rememberDatePickerState(initialSelectedDateMillis = uiState.selectedDate)
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let { viewModel.onDateSelected(it) }
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

            // Recurring toggle
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Recurring monthly income", style = MaterialTheme.typography.bodyMedium)
                    Text("Log automatically each month", style = MaterialTheme.typography.labelSmall, color = SpendlyGray500)
                }
                Switch(
                    checked = uiState.isRecurring,
                    onCheckedChange = { viewModel.onRecurringToggled(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = SpendlyGreen)
                )
            }

            // Note
            SectionLabel("Note (optional)")
            OutlinedTextField(
                value = uiState.note,
                onValueChange = { viewModel.onNoteChanged(it) },
                placeholder = { Text("Add a note...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
        
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = SpendlyGreen)
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}
