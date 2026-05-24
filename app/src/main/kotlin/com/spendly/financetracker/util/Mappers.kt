package com.spendly.financetracker.util

import com.spendly.financetracker.data.local.entity.ExpenseEntryEntity
import com.spendly.financetracker.data.local.entity.IncomeEntryEntity
import com.spendly.financetracker.data.local.entity.SavingsGoalEntity
import com.spendly.financetracker.data.local.entity.UserProfileEntity
import com.spendly.financetracker.data.model.FinanceTransaction
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
    cryptoRateFetchedAt = cryptoRateFetchedAt
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
    goalId = goalId
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
    iconKey = iconKey
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
    iconKey = iconKey
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
    categorySettingsJson = categorySettingsJson
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
    categorySettingsJson = categorySettingsJson
)
