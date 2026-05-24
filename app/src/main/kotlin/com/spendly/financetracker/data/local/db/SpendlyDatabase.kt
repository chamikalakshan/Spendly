package com.spendly.financetracker.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.spendly.financetracker.data.local.dao.ExpenseDao
import com.spendly.financetracker.data.local.dao.GoalDao
import com.spendly.financetracker.data.local.dao.IncomeDao
import com.spendly.financetracker.data.local.dao.UserProfileDao
import com.spendly.financetracker.data.local.entity.ExpenseEntryEntity
import com.spendly.financetracker.data.local.entity.IncomeEntryEntity
import com.spendly.financetracker.data.local.entity.SavingsGoalEntity
import com.spendly.financetracker.data.local.entity.UserProfileEntity

@Database(
    entities = [
        IncomeEntryEntity::class,
        ExpenseEntryEntity::class,
        SavingsGoalEntity::class,
        UserProfileEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class SpendlyDatabase : RoomDatabase() {
    abstract fun incomeDao(): IncomeDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun goalDao(): GoalDao
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE income_entries ADD COLUMN originalAmount REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE income_entries ADD COLUMN originalCurrency TEXT NOT NULL DEFAULT 'LKR'")
                db.execSQL("ALTER TABLE income_entries ADD COLUMN defaultCurrency TEXT NOT NULL DEFAULT 'LKR'")
                db.execSQL("ALTER TABLE income_entries ADD COLUMN exchangeRate REAL")
                db.execSQL("ALTER TABLE income_entries ADD COLUMN isRecurring INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE income_entries ADD COLUMN cryptoCoin TEXT")
                db.execSQL("ALTER TABLE income_entries ADD COLUMN cryptoAmount REAL")
                db.execSQL("ALTER TABLE income_entries ADD COLUMN cryptoRate REAL")
                db.execSQL("ALTER TABLE income_entries ADD COLUMN cryptoRateSource TEXT")
                db.execSQL("ALTER TABLE income_entries ADD COLUMN cryptoRateFetchedAt INTEGER")

                db.execSQL("ALTER TABLE expense_entries ADD COLUMN originalAmount REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE expense_entries ADD COLUMN originalCurrency TEXT NOT NULL DEFAULT 'LKR'")
                db.execSQL("ALTER TABLE expense_entries ADD COLUMN defaultCurrency TEXT NOT NULL DEFAULT 'LKR'")
                db.execSQL("ALTER TABLE expense_entries ADD COLUMN exchangeRate REAL")
                db.execSQL("ALTER TABLE expense_entries ADD COLUMN paymentMethod TEXT")
                db.execSQL("ALTER TABLE expense_entries ADD COLUMN expenseType TEXT")
                db.execSQL("ALTER TABLE expense_entries ADD COLUMN goalId TEXT")

                db.execSQL("ALTER TABLE user_profiles ADD COLUMN profileImageUri TEXT")
                db.execSQL("ALTER TABLE user_profiles ADD COLUMN exchangeRateSettings TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE user_profiles ADD COLUMN notificationFrequency TEXT")
                db.execSQL("ALTER TABLE user_profiles ADD COLUMN reminderTime TEXT")

                db.execSQL("ALTER TABLE savings_goals ADD COLUMN initialSavedCents INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE savings_goals ADD COLUMN defaultCurrency TEXT NOT NULL DEFAULT 'LKR'")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_profiles ADD COLUMN categorySettingsJson TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE savings_goals ADD COLUMN iconKey TEXT NOT NULL DEFAULT 'goal'")
            }
        }
    }
}
