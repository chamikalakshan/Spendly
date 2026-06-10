package com.spendly.financetracker.util

import com.spendly.financetracker.data.local.entity.BudgetEntity
import com.spendly.financetracker.data.local.entity.ExpenseEntryEntity
import com.spendly.financetracker.data.local.entity.IncomeEntryEntity
import com.spendly.financetracker.data.local.entity.RecurringRuleEntity
import com.spendly.financetracker.data.local.entity.SavingsGoalEntity
import com.spendly.financetracker.data.local.entity.UserProfileEntity
import com.spendly.financetracker.data.model.Budget
import com.spendly.financetracker.data.model.FinanceTransaction
import com.spendly.financetracker.data.model.RecurringFrequency
import com.spendly.financetracker.data.model.RecurringRule
import com.spendly.financetracker.data.model.SavingsGoal
import com.spendly.financetracker.data.model.ExpenseType
import com.spendly.financetracker.data.model.TransactionType
import com.spendly.financetracker.data.model.UserProfile

fun IncomeEntryEntity.toTransaction(): FinanceTransaction = FinanceTransaction(
    id = id,
    userId = userId,
    title = name,
    amountCents = amountCents,
    type = TransactionType.INCOME,
    category = "",
    source = source,
    note = note,
    dateMillis = dateMillis,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis,
    isSynced = isSynced,
    originalAmount = if (originalAmount > 0.0) originalAmount else amountCents / 100.0,
    originalCurrency = originalCurrency,
    defaultCurrency = defaultCurrency,
    exchangeRate = exchangeRate,
    isRecurring = isRecurring,
    cryptoCoin = cryptoCoin,
    cryptoAmount = cryptoAmount,
    cryptoRate = cryptoRate,
    cryptoRateSource = cryptoRateSource,
    cryptoRateFetchedAt = cryptoRateFetchedAt,
    recurringRuleId = recurringRuleId,
    recurringPeriodKey = recurringPeriodKey
)

fun ExpenseEntryEntity.toTransaction(): FinanceTransaction = FinanceTransaction(
    id = id,
    userId = userId,
    title = name,
    amountCents = amountCents,
    type = TransactionType.EXPENSE,
    category = category,
    source = "",
    note = note,
    dateMillis = dateMillis,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis,
    isSynced = isSynced,
    originalAmount = if (originalAmount > 0.0) originalAmount else amountCents / 100.0,
    originalCurrency = originalCurrency,
    defaultCurrency = defaultCurrency,
    exchangeRate = exchangeRate,
    paymentMethod = paymentMethod,
    expenseType = expenseType?.let { runCatching { ExpenseType.valueOf(it) }.getOrNull() },
    goalId = goalId,
    recurringRuleId = recurringRuleId,
    recurringPeriodKey = recurringPeriodKey
)

fun SavingsGoalEntity.toModel(): SavingsGoal = SavingsGoal(
    id = id,
    userId = userId,
    title = title,
    status = status,
    targetCents = targetCents,
    savedCents = savedCents,
    dueDateMillis = dueDateMillis,
    category = category,
    isPrimary = isPrimary,
    isSynced = isSynced,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis,
    initialSavedCents = initialSavedCents,
    defaultCurrency = defaultCurrency,
    iconKey = iconKey,
    iconAccentColorKey = iconAccentColorKey,
    goalImageUri = goalImageUri
)

fun SavingsGoal.toEntity(): SavingsGoalEntity = SavingsGoalEntity(
    id = id,
    userId = userId,
    title = title,
    status = status,
    targetCents = targetCents,
    savedCents = savedCents,
    dueDateMillis = dueDateMillis,
    category = category,
    isPrimary = isPrimary,
    isSynced = isSynced,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis,
    initialSavedCents = initialSavedCents,
    defaultCurrency = defaultCurrency,
    iconKey = iconKey,
    iconAccentColorKey = iconAccentColorKey,
    goalImageUri = goalImageUri
)

fun UserProfileEntity.toModel(): UserProfile = UserProfile(
    uid = uid,
    name = name,
    email = email,
    defaultCurrency = defaultCurrency,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis,
    isSynced = isSynced,
    profileImageUri = profileImageUri,
    exchangeRateSettings = exchangeRateSettings,
    notificationFrequency = notificationFrequency,
    reminderTime = reminderTime,
    categorySettingsJson = categorySettingsJson,
    themeMode = themeMode,
    budgetAlertsEnabled = budgetAlertsEnabled,
    budgetAlertThresholdPercent = budgetAlertThresholdPercent,
    profileImageStoragePath = profileImageStoragePath,
    accentColorKey = accentColorKey,
    dailyRemindersEnabled = dailyRemindersEnabled,
    remindExpenses = remindExpenses,
    remindIncome = remindIncome,
    smartReminderMode = smartReminderMode
)

fun UserProfile.toEntity(): UserProfileEntity = UserProfileEntity(
    uid = uid,
    name = name,
    email = email,
    defaultCurrency = defaultCurrency,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis,
    isSynced = isSynced,
    profileImageUri = profileImageUri,
    exchangeRateSettings = exchangeRateSettings,
    notificationFrequency = notificationFrequency,
    reminderTime = reminderTime,
    categorySettingsJson = categorySettingsJson,
    themeMode = themeMode,
    budgetAlertsEnabled = budgetAlertsEnabled,
    budgetAlertThresholdPercent = budgetAlertThresholdPercent,
    profileImageStoragePath = profileImageStoragePath,
    accentColorKey = accentColorKey,
    dailyRemindersEnabled = dailyRemindersEnabled,
    remindExpenses = remindExpenses,
    remindIncome = remindIncome,
    smartReminderMode = smartReminderMode
)

fun BudgetEntity.toModel(): Budget = Budget(
    id = id,
    userId = userId,
    category = category,
    monthStartMillis = monthStartMillis,
    limitCents = limitCents,
    defaultCurrency = defaultCurrency,
    alertThresholdPercent = alertThresholdPercent,
    note = note.orEmpty(),
    isSynced = isSynced,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis,
    deletedAtMillis = deletedAtMillis
)

fun Budget.toEntity(): BudgetEntity = BudgetEntity(
    id = id,
    userId = userId,
    category = category,
    monthStartMillis = monthStartMillis,
    limitCents = limitCents,
    defaultCurrency = defaultCurrency,
    alertThresholdPercent = alertThresholdPercent,
    note = note,
    isSynced = isSynced,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis,
    deletedAtMillis = deletedAtMillis
)

fun RecurringRuleEntity.toModel(): RecurringRule = RecurringRule(
    id = id,
    userId = userId,
    type = runCatching { TransactionType.valueOf(type) }.getOrDefault(TransactionType.EXPENSE),
    name = name,
    amountCents = amountCents,
    originalAmount = originalAmount,
    originalCurrency = originalCurrency,
    defaultCurrency = defaultCurrency,
    exchangeRate = exchangeRate,
    source = source,
    category = category,
    paymentMethod = paymentMethod,
    expenseType = expenseType?.let { runCatching { ExpenseType.valueOf(it) }.getOrNull() },
    note = note.orEmpty(),
    frequency = runCatching { RecurringFrequency.valueOf(frequency) }.getOrDefault(RecurringFrequency.MONTHLY),
    interval = interval,
    startDateMillis = startDateMillis,
    nextRunDateMillis = nextRunDateMillis,
    endDateMillis = endDateMillis,
    isActive = isActive,
    lastGeneratedAtMillis = lastGeneratedAtMillis,
    isSynced = isSynced,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis,
    deletedAtMillis = deletedAtMillis
)

fun RecurringRule.toEntity(): RecurringRuleEntity = RecurringRuleEntity(
    id = id,
    userId = userId,
    type = type.name,
    name = name,
    amountCents = amountCents,
    originalAmount = originalAmount,
    originalCurrency = originalCurrency,
    defaultCurrency = defaultCurrency,
    exchangeRate = exchangeRate,
    source = source,
    category = category,
    paymentMethod = paymentMethod,
    expenseType = expenseType?.name,
    note = note,
    frequency = frequency.name,
    interval = interval,
    startDateMillis = startDateMillis,
    nextRunDateMillis = nextRunDateMillis,
    endDateMillis = endDateMillis,
    isActive = isActive,
    lastGeneratedAtMillis = lastGeneratedAtMillis,
    isSynced = isSynced,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis,
    deletedAtMillis = deletedAtMillis
)
