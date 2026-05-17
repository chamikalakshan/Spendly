package com.spendly.app.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.spendly.app.data.local.dao.ExpenseDao
import com.spendly.app.data.local.dao.GoalDao
import com.spendly.app.data.local.dao.IncomeDao
import com.spendly.app.data.local.entity.ExpenseEntryEntity
import com.spendly.app.data.local.entity.IncomeEntryEntity
import com.spendly.app.data.local.entity.SavingsGoalEntity

@Database(
    entities = [
        IncomeEntryEntity::class,
        ExpenseEntryEntity::class,
        SavingsGoalEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(SpendlyTypeConverters::class)
abstract class SpendlyDatabase : RoomDatabase() {
    abstract fun incomeDao(): IncomeDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun goalDao(): GoalDao
}
