package com.spendly.app.ui.screens

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
import androidx.compose.material.icons.filled.*
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
import com.spendly.app.data.model.enums.ExpenseCategory
import com.spendly.app.data.model.enums.ExpenseType
import com.spendly.app.data.model.enums.PaymentMethod
import com.spendly.app.ui.components.ErrorBanner
import com.spendly.app.ui.components.LoadingOverlay
import com.spendly.app.ui.theme.*
import com.spendly.app.viewmodel.AddExpenseViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddExpenseScreen(
    navController: NavController,
    editExpenseId: String? = null,
    viewModel: AddExpenseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(editExpenseId) {
        viewModel.loadExpenseForEdit(editExpenseId)
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            navController.popBackStack()
        }
    }

    if (uiState.showAddPaymentDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissAddPaymentDialog() },
            title = { Text("Add payment method") },
            text = {
                OutlinedTextField(
                    value = uiState.newPaymentInput,
                    onValueChange = { viewModel.onNewPaymentInputChanged(it) },
                    label = { Text("Payment method") },
                    placeholder = { Text("e.g. Bank Transfer, Cheque") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmAddPayment() }) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissAddPaymentDialog() }) { Text("Cancel") }
            }
        )
    }

    if (uiState.showAddCategoryDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissAddCategoryDialog() },
            title = { Text("Add category") },
            text = {
                OutlinedTextField(
                    value = uiState.newCategoryInput,
                    onValueChange = { viewModel.onNewCategoryInputChanged(it) },
                    label = { Text("Category name") },
                    placeholder = { Text("e.g. Medical, Education") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmAddCategory() }) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissAddCategoryDialog() }) { Text("Cancel") }
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
                title = { Text(if (editExpenseId.isNullOrBlank()) "Add Expense" else "Edit Expense") },
                actions = {
                    Button(
                        onClick = { viewModel.saveExpense() },
                        enabled = !uiState.isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SpendlyRed,
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

            // Large red amount display
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Amount (LKR)",
                    style = SpendlyTypography.labelSmall,
                    color = SpendlyGray500,
                    textAlign = TextAlign.Center
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "LKR",
                            style = MaterialTheme.typography.headlineMedium,
                            color = SpendlyRed,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        BasicTextField(
                            value = uiState.amount,
                            onValueChange = { viewModel.onAmountChanged(it) },
                            textStyle = TextStyle(
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (uiState.amount.isEmpty()) SpendlyGray300 else SpendlyRed,
                                textAlign = TextAlign.Center
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            decorationBox = { inner ->
                                if (uiState.amount.isEmpty()) {
                                    Text(
                                        "0",
                                        fontSize = 48.sp,
                                        color = SpendlyGray300,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                }
                                inner()
                            }
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(SpendlyRed)
                )
                if (uiState.amountError != null) {
                    Text(uiState.amountError!!, color = SpendlyRed, style = SpendlyTypography.labelSmall)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Category section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Category", style = SpendlyTypography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    TextButton(onClick = { viewModel.showAddCategoryDialog() }) {
                        Text("Edit", color = SpendlyGreen, style = SpendlyTypography.labelMedium)
                    }
                }

                val categoryIconMap = mapOf(
                    ExpenseCategory.FOOD to Icons.Default.Restaurant,
                    ExpenseCategory.TRANSPORT to Icons.Default.DirectionsCar,
                    ExpenseCategory.RENT to Icons.Default.Home,
                    ExpenseCategory.SUBSCRIPTIONS to Icons.Default.Subscriptions,
                    ExpenseCategory.ENTERTAINMENT to Icons.Default.Movie,
                    ExpenseCategory.GYM to Icons.Default.FitnessCenter,
                    ExpenseCategory.OTHER to Icons.Default.Category
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExpenseCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = uiState.selectedCategory == cat,
                            onClick = { viewModel.onCategorySelected(cat) },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(categoryIconMap[cat] ?: Icons.Default.Category, null, Modifier.size(14.dp))
                                    Text(cat.displayName)
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SpendlyRed,
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White
                            )
                        )
                    }
                    uiState.customCategories.forEach { custom ->
                        FilterChip(
                            selected = uiState.selectedCustomCategory == custom,
                            onClick = { viewModel.onCustomCategorySelected(custom) },
                            label = { Text(custom) }
                        )
                    }
                }
                if (uiState.categoryError != null) {
                    Text(uiState.categoryError!!, color = SpendlyRed, style = SpendlyTypography.labelSmall)
                }
            }

            // Name field
            SectionLabel("Name")
            OutlinedTextField(
                value = uiState.name,
                onValueChange = { viewModel.onNameChanged(it) },
                placeholder = { Text("e.g. Dinner at Arcade, Gym fee") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Expense Type
            SectionLabel("Expense Type")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                ExpenseType.entries.forEach { type ->
                    FilterChip(
                        selected = uiState.expenseType == type,
                        onClick = { viewModel.onExpenseTypeSelected(type) },
                        label = { Text(type.displayName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (type == ExpenseType.COMMITTED) SpendlyBlue else SpendlyGreen,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
            Text(
                text = when (uiState.expenseType) {
                    ExpenseType.DISCRETIONARY -> "Variable, optional spending"
                    ExpenseType.COMMITTED -> "Fixed, essential spending"
                },
                style = SpendlyTypography.labelSmall,
                color = SpendlyGray500,
                modifier = Modifier.padding(start = 4.dp)
            )

            // Payment Method
            SectionLabel("Payment Method")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    PaymentMethod.CARD to "Card",
                    PaymentMethod.CASH to "Cash",
                    PaymentMethod.AUTODEBIT to "Auto-debit"
                ).forEach { (method, label) ->
                    FilterChip(
                        selected = uiState.paymentMethod == method,
                        onClick = { viewModel.onPaymentMethodSelected(method) },
                        label = { Text(label) }
                    )
                }
                uiState.customPaymentMethods.forEach { custom ->
                    FilterChip(selected = false, onClick = {}, label = { Text(custom) })
                }
                AssistChip(
                    onClick = { viewModel.showAddPaymentDialog() },
                    label = { Text("Add +") },
                    leadingIcon = { Icon(Icons.Default.Add, null, Modifier.size(14.dp)) }
                )
            }

            // Date
            SectionLabel("Date")
            var showPicker by remember { mutableStateOf(false) }
            OutlinedTextField(
                value = SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date(uiState.selectedDate)),
                onValueChange = {},
                enabled = false,
                modifier = Modifier.fillMaxWidth().clickable { showPicker = true },
                trailingIcon = { Icon(Icons.Default.CalendarMonth, null) },
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            if (showPicker) {
                val state = rememberDatePickerState(uiState.selectedDate)
                DatePickerDialog(
                    onDismissRequest = { showPicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            state.selectedDateMillis?.let { viewModel.onDateSelected(it) }
                            showPicker = false
                        }) { Text("OK") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showPicker = false }) { Text("Cancel") }
                    }
                ) {
                    DatePicker(state)
                }
            }

            // Note
            SectionLabel("Note (optional)")
            OutlinedTextField(
                value = uiState.note,
                onValueChange = { viewModel.onNoteChanged(it) },
                placeholder = { Text("What was this for?") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
        LoadingOverlay(uiState.isLoading)
    }
}

@Composable
private fun SectionLabel(t: String) = Text(t, style = SpendlyTypography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
