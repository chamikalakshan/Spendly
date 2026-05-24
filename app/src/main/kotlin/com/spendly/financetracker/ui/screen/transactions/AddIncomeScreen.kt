package com.spendly.financetracker.ui.screen.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.spendly.financetracker.ui.theme.SpendlyGray300
import com.spendly.financetracker.ui.theme.SpendlyGray500
import com.spendly.financetracker.ui.theme.SpendlyGreen
import com.spendly.financetracker.ui.viewmodel.AddIncomeViewModel
import com.spendly.financetracker.ui.viewmodel.RateStatus
import com.spendly.financetracker.ui.viewmodel.cryptoCoins
import com.spendly.financetracker.ui.util.AmountVisualTransformation
import com.spendly.financetracker.ui.util.formatMoney
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddIncomeScreen(
    onBack: () -> Unit
) {
    val viewModel: AddIncomeViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()
    var showSourceDialog by remember { mutableStateOf(false) }
    var customSource by remember { mutableStateOf("") }
    var currencyMenuExpanded by remember { mutableStateOf(false) }
    var editSources by remember { mutableStateOf(false) }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0.dp),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
	                title = { Text(if (state.editId == null) "Add Income" else "Edit Income") },
                actions = {
                    Button(
                        onClick = viewModel::save,
                        enabled = !state.isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = SpendlyGreen, contentColor = Color.White),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.padding(end = 8.dp).height(36.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                    ) { Text("Save", style = MaterialTheme.typography.labelMedium) }
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
            if (state.isLoading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = SpendlyGreen)
            if (state.error != null) {
                Text(state.error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelMedium)
            }

            // Amount
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AddIncomeLabel("Amount")
                val amountCurrency = if (state.isCrypto) state.customCryptoCoin.ifBlank { state.selectedCoin } else state.selectedCurrency
                Surface(
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(0.8.dp, SpendlyGray300)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box {
                            Row(
                                modifier = Modifier
                                    .width(104.dp)
                                    .clickable(enabled = !state.isCrypto) { currencyMenuExpanded = true }
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(amountCurrency, style = MaterialTheme.typography.titleSmall, color = SpendlyGreen, fontWeight = FontWeight.Bold)
                                if (!state.isCrypto) Icon(Icons.Default.ArrowDropDown, contentDescription = "Select currency", tint = SpendlyGray500, modifier = Modifier.size(18.dp))
                            }
                            DropdownMenu(expanded = currencyMenuExpanded, onDismissRequest = { currencyMenuExpanded = false }) {
                                state.visibleCurrencies.forEach { currency ->
                                    DropdownMenuItem(
                                        text = { Text(currency) },
                                        onClick = {
                                            viewModel.onCurrencySelected(currency)
                                            currencyMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        Box(modifier = Modifier.width(1.dp).height(58.dp).background(SpendlyGray300))
                        BasicTextField(
                            value = state.amount,
                            onValueChange = viewModel::onAmountChanged,
                            modifier = Modifier.weight(1f).padding(horizontal = 14.dp),
                            textStyle = TextStyle(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = SpendlyGreen
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            visualTransformation = AmountVisualTransformation,
                            decorationBox = { inner ->
                                if (state.amount.isEmpty()) Text("Enter Amount", fontSize = 16.sp, color = SpendlyGray500)
                                inner()
                            }
                        )
                    }
                }
            }

            // Source
            Row(verticalAlignment = Alignment.CenterVertically) {
                AddIncomeLabel("Income Source", modifier = Modifier.weight(1f))
                TextButton(onClick = { editSources = !editSources }) {
                    Text(if (editSources) "Done" else "Edit")
                }
            }
            FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.incomeSources.forEach { source ->
                    FilterChip(
                        selected = state.selectedSource == source,
                        onClick = { viewModel.onSourceSelected(source) },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(source)
                                if (editSources && state.incomeSources.size > 1) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Delete source",
                                        modifier = Modifier.size(14.dp).clickable { viewModel.deleteSource(source) }
                                    )
                                }
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SpendlyGreen, selectedLabelColor = Color.White)
                    )
                }
                FilterChip(selected = false, onClick = { showSourceDialog = true }, label = { Text("+ Create") })
            }

            if (state.needsExchangeRate && !state.isCrypto) {
                OutlinedTextField(
                    value = state.exchangeRate,
                    onValueChange = viewModel::onExchangeRateChanged,
                    label = { Text("${state.selectedCurrency} to ${state.defaultCurrency} rate") },
                    placeholder = { Text("0.00") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    trailingIcon = {
                        TextButton(onClick = viewModel::fetchExchangeRate, enabled = !state.isFetchingRate) {
                            Text(if (state.fiatRateStatus == RateStatus.UPDATING) "Updating..." else "Update")
                        }
                    }
                )
                RateStatusText(state.fiatRateStatus)
            }

            if (state.isCrypto) {
                AddIncomeLabel("Crypto Coin")
                FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    cryptoCoins.forEach { coin ->
                        FilterChip(
                            selected = state.selectedCoin == coin,
                            onClick = { viewModel.onCoinSelected(coin) },
                            label = { Text(coin) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SpendlyGreen, selectedLabelColor = Color.White)
                        )
                    }
                }
                if (state.selectedCoin == "Other") {
                    OutlinedTextField(
                        value = state.customCryptoCoin,
                        onValueChange = viewModel::onCustomCryptoCoinChanged,
                        label = { Text("Coin name") },
                        placeholder = { Text("e.g. ADA, MATIC") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                OutlinedTextField(
                    value = state.cryptoRate,
                    onValueChange = viewModel::onCryptoRateChanged,
                    label = { Text("${state.selectedCoin} rate in ${state.defaultCurrency}") },
                    placeholder = { Text("0.00") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    trailingIcon = {
                        TextButton(onClick = viewModel::fetchCryptoRate, enabled = !state.isFetchingRate) {
                            Text(if (state.cryptoRateStatus == RateStatus.UPDATING) "Updating..." else "Update")
                        }
                    }
                )
                RateStatusText(state.cryptoRateStatus)
            }

            Text(
                "Converted: ${formatMoney(state.convertedAmountCents, state.defaultCurrency)}",
                style = MaterialTheme.typography.labelMedium,
                color = SpendlyGray500
            )

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("Recurring monthly", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                Switch(checked = state.isRecurring, onCheckedChange = viewModel::onRecurringChanged)
            }

            // Name
            AddIncomeLabel("Name")
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::onNameChanged,
                placeholder = { Text("e.g. May Salary, Client ABC") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Date
            AddIncomeLabel("Date")
            var showDatePicker by remember { mutableStateOf(false) }
            Box {
                OutlinedTextField(
                    value = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(state.selectedDate)),
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = { Icon(Icons.Default.CalendarMonth, null) }
                )
                Box(modifier = Modifier.matchParentSize().clickable { showDatePicker = true })
            }
            if (showDatePicker) {
                val dpState = rememberDatePickerState(initialSelectedDateMillis = state.selectedDate)
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = { TextButton(onClick = { dpState.selectedDateMillis?.let { viewModel.onDateSelected(it) }; showDatePicker = false }) { Text("OK") } },
                    dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
                ) { DatePicker(state = dpState) }
            }

            // Note
            AddIncomeLabel("Note (optional)")
            OutlinedTextField(
                value = state.note,
                onValueChange = viewModel::onNoteChanged,
                placeholder = { Text("Add a note...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Spacer(Modifier.height(24.dp))
        }

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SpendlyGreen)
            }
        }
    }

    if (showSourceDialog) {
        AlertDialog(
            onDismissRequest = { showSourceDialog = false },
            title = { Text("Create income source") },
            text = {
                OutlinedTextField(
                    value = customSource,
                    onValueChange = { customSource = it },
                    label = { Text("Source") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.addCustomSource(customSource)
                    customSource = ""
                    showSourceDialog = false
                }) { Text("Create") }
            },
            dismissButton = { TextButton(onClick = { showSourceDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun AddIncomeLabel(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier = modifier, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
}

@Composable
private fun RateStatusText(status: RateStatus) {
    val text = when (status) {
        RateStatus.IDLE -> null
        RateStatus.UPDATING -> "Updating..."
        RateStatus.UPDATED -> "Updated just now"
        RateStatus.MANUAL_REQUIRED -> "Manual rate required"
        RateStatus.UNAVAILABLE -> "Rate unavailable"
    } ?: return
    Text(text, style = MaterialTheme.typography.labelSmall, color = SpendlyGray500)
}
