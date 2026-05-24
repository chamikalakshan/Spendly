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
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Subscriptions
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spendly.financetracker.ui.theme.SpendlyGray300
import com.spendly.financetracker.ui.theme.SpendlyGray500
import com.spendly.financetracker.ui.theme.SpendlyRed
import com.spendly.financetracker.data.model.ExpenseType
import com.spendly.financetracker.ui.util.AmountVisualTransformation
import com.spendly.financetracker.ui.util.formatMoney
import com.spendly.financetracker.ui.viewmodel.AddExpenseViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddExpenseScreen(
    onBack: () -> Unit
) {
    val viewModel: AddExpenseViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()
    var showCategoryDialog by remember { mutableStateOf(false) }
    var customCategory by remember { mutableStateOf("") }
    var currencyMenuExpanded by remember { mutableStateOf(false) }
    var editCategories by remember { mutableStateOf(false) }

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
	                title = { Text(if (state.editId == null) "Add Expense" else "Edit Expense") },
                actions = {
                    Button(
                        onClick = viewModel::save,
                        enabled = !state.isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = SpendlyRed, contentColor = Color.White),
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
            if (state.isLoading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = SpendlyRed)
            if (state.error != null) {
                Text(state.error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelMedium)
            }

            // Amount
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AddExpenseLabel("Amount")
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
                                    .clickable { currencyMenuExpanded = true }
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(state.selectedCurrency, style = MaterialTheme.typography.titleSmall, color = SpendlyRed, fontWeight = FontWeight.Bold)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select currency", tint = SpendlyGray500, modifier = Modifier.size(18.dp))
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
                                color = SpendlyRed
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

            // Category
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Category", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                TextButton(onClick = { editCategories = !editCategories }) {
                    Text(if (editCategories) "Done" else "Edit")
                }
            }
            FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.categories.forEach { cat ->
                    FilterChip(
                        selected = state.selectedCategory == cat,
                        onClick = { viewModel.onCategorySelected(cat) },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(expenseCategoryIcon(cat), null, Modifier.size(14.dp))
                                Text(cat)
                                if (editCategories && state.categories.size > 1) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Delete category",
                                        modifier = Modifier.size(14.dp).clickable { viewModel.deleteCategory(cat) }
                                    )
                                }
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SpendlyRed,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White
                        )
                    )
                }
                FilterChip(
                    selected = false,
                    onClick = { showCategoryDialog = true },
                    label = { Text("+ Create") }
                )
            }

            if (state.needsExchangeRate) {
                OutlinedTextField(
                    value = state.exchangeRate,
                    onValueChange = viewModel::onExchangeRateChanged,
                    label = { Text("${state.selectedCurrency} to ${state.defaultCurrency} rate") },
                    placeholder = { Text("0.00") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                Text(
                    "Converted: ${formatMoney(state.convertedAmountCents, state.defaultCurrency)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = SpendlyGray500
                )
            }

            AddExpenseLabel("Expense Type")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ExpenseType.values().forEach { type ->
                    FilterChip(
                        selected = state.expenseType == type,
                        onClick = { viewModel.onExpenseTypeSelected(type) },
                        label = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SpendlyRed,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            AddExpenseLabel("Payment Method")
            FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.paymentMethods.forEach { method ->
                    FilterChip(
                        selected = state.selectedPaymentMethod == method,
                        onClick = { viewModel.onPaymentMethodSelected(method) },
                        label = { Text(method) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SpendlyRed,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Name
            AddExpenseLabel("Name")
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::onNameChanged,
                placeholder = { Text("e.g. Dinner, Gym fee") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Date
            AddExpenseLabel("Date")
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
            AddExpenseLabel("Note (optional)")
            OutlinedTextField(
                value = state.note,
                onValueChange = viewModel::onNoteChanged,
                placeholder = { Text("What was this for?") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Spacer(Modifier.height(24.dp))
        }

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SpendlyRed)
            }
        }
    }

    if (showCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showCategoryDialog = false },
            title = { Text("Custom Category") },
            text = {
                OutlinedTextField(
                    value = customCategory,
                    onValueChange = { customCategory = it },
                    label = { Text("Category") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.addCustomCategory(customCategory)
                    customCategory = ""
                    showCategoryDialog = false
                }) { Text("Create") }
            },
            dismissButton = { TextButton(onClick = { showCategoryDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun AddExpenseLabel(text: String) {
    Text(text, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
}

private fun expenseCategoryIcon(category: String): ImageVector = when (category) {
    "Food" -> Icons.Default.Restaurant
    "Transport" -> Icons.Default.DirectionsCar
    "Rent" -> Icons.Default.Home
    "Subscriptions" -> Icons.Default.Subscriptions
    "Entertainment" -> Icons.Default.Movie
    "Gym" -> Icons.Default.FitnessCenter
    "Goal" -> Icons.Default.Flag
    else -> Icons.Default.Category
}
