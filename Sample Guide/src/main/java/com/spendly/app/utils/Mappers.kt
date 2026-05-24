package com.spendly.app.utils

import com.spendly.app.data.local.entity.ExpenseEntryEntity
import com.spendly.app.data.local.entity.IncomeEntryEntity
import com.spendly.app.data.local.entity.SavingsGoalEntity
import com.spendly.app.data.model.ExpenseEntry
import com.spendly.app.data.model.IncomeEntry
import com.spendly.app.data.model.SavingsGoal

fun IncomeEntry.toEntity(): IncomeEntryEntity = IncomeEntryEntity(
    id = id,
    userId = userId,
    name = name,
    sourceType = sourceType,
    amount = amount,
    currency = currency,
    amountLKR = amountLKR,
    exchangeRate = exchangeRate,
    coin = coin,
    projectName = projectName,
    invoiceStatus = invoiceStatus,
    date = date,
    isRecurring = isRecurring,
    note = note,
    isSynced = isSynced,
    createdAt = createdAt
)

fun IncomeEntryEntity.toModel(): IncomeEntry = IncomeEntry(
    id = id,
    userId = userId,
    name = name,
    sourceType = sourceType,
    amount = amount,
    currency = currency,
    amountLKR = amountLKR,
    exchangeRate = exchangeRate,
    coin = coin,
    projectName = projectName,
    invoiceStatus = invoiceStatus,
    date = date,
    isRecurring = isRecurring,
    note = note,
    isSynced = isSynced,
    createdAt = createdAt
)

fun ExpenseEntry.toEntity(): ExpenseEntryEntity = ExpenseEntryEntity(
    id = id,
    userId = userId,
    name = name,
    amount = amount,
    category = category,
    expenseType = expenseType,
    paymentMethod = paymentMethod,
    date = date,
    note = note,
    isSynced = isSynced,
    createdAt = createdAt
)

fun ExpenseEntryEntity.toModel(): ExpenseEntry = ExpenseEntry(
    id = id,
    userId = userId,
    name = name,
    amount = amount,
    category = category,
    expenseType = expenseType,
    paymentMethod = paymentMethod,
    date = date,
    note = note,
    isSynced = isSynced,
    createdAt = createdAt
)

fun SavingsGoal.toEntity(): SavingsGoalEntity = SavingsGoalEntity(
    id = id,
    userId = userId,
    goalName = goalName,
    targetAmount = targetAmount,
    targetDate = targetDate,
    isSynced = isSynced,
    createdAt = createdAt
)

fun SavingsGoalEntity.toModel(): SavingsGoal = SavingsGoal(
    id = id,
    userId = userId,
    goalName = goalName,
    targetAmount = targetAmount,
    targetDate = targetDate,
    isSynced = isSynced,
    createdAt = createdAt
)
