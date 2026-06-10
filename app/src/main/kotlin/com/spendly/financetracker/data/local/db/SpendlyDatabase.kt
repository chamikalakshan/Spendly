package com.spendly.financetracker.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.spendly.financetracker.data.local.dao.BudgetDao
import com.spendly.financetracker.data.local.dao.BudgetAlertDao
import com.spendly.financetracker.data.local.dao.ExpenseDao
import com.spendly.financetracker.data.local.dao.ExchangeRateDao
import com.spendly.financetracker.data.local.dao.GoalDao
import com.spendly.financetracker.data.local.dao.IncomeDao
import com.spendly.financetracker.data.local.dao.NotificationDao
import com.spendly.financetracker.data.local.dao.RecurringRuleDao
import com.spendly.financetracker.data.local.dao.SyncConflictDao
import com.spendly.financetracker.data.local.dao.SyncMetadataDao
import com.spendly.financetracker.data.local.dao.UserProfileDao
import com.spendly.financetracker.data.local.entity.BudgetAlertEntity
import com.spendly.financetracker.data.local.entity.BudgetEntity
import com.spendly.financetracker.data.local.entity.ExchangeRateEntity
import com.spendly.financetracker.data.local.entity.ExpenseEntryEntity
import com.spendly.financetracker.data.local.entity.IncomeEntryEntity
import com.spendly.financetracker.data.local.entity.NotificationEntity
import com.spendly.financetracker.data.local.entity.RecurringRuleEntity
import com.spendly.financetracker.data.local.entity.SavingsGoalEntity
import com.spendly.financetracker.data.local.entity.SyncConflictEntity
import com.spendly.financetracker.data.local.entity.SyncMetadataEntity
import com.spendly.financetracker.data.local.entity.UserProfileEntity

@Database(
    entities = [
        IncomeEntryEntity::class,
        ExpenseEntryEntity::class,
        SavingsGoalEntity::class,
        UserProfileEntity::class,
        BudgetEntity::class,
        RecurringRuleEntity::class,
        BudgetAlertEntity::class,
        ExchangeRateEntity::class,
        SyncConflictEntity::class,
        SyncMetadataEntity::class,
        NotificationEntity::class
    ],
    version = 10,
    exportSchema = false
)
abstract class SpendlyDatabase : RoomDatabase() {
    abstract fun incomeDao(): IncomeDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun goalDao(): GoalDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun budgetDao(): BudgetDao
    abstract fun recurringRuleDao(): RecurringRuleDao
    abstract fun budgetAlertDao(): BudgetAlertDao
    abstract fun exchangeRateDao(): ExchangeRateDao
    abstract fun syncConflictDao(): SyncConflictDao
    abstract fun syncMetadataDao(): SyncMetadataDao
    abstract fun notificationDao(): NotificationDao

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

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_profiles ADD COLUMN themeMode TEXT NOT NULL DEFAULT 'SYSTEM'")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE income_entries ADD COLUMN recurringRuleId TEXT")
                db.execSQL("ALTER TABLE income_entries ADD COLUMN recurringPeriodKey TEXT")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_income_entries_userId_recurringRuleId_recurringPeriodKey ON income_entries(userId, recurringRuleId, recurringPeriodKey)")

                db.execSQL("ALTER TABLE expense_entries ADD COLUMN recurringRuleId TEXT")
                db.execSQL("ALTER TABLE expense_entries ADD COLUMN recurringPeriodKey TEXT")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_expense_entries_userId_recurringRuleId_recurringPeriodKey ON expense_entries(userId, recurringRuleId, recurringPeriodKey)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS budget_entries (
                        id TEXT NOT NULL PRIMARY KEY,
                        userId TEXT NOT NULL,
                        category TEXT NOT NULL,
                        monthStartMillis INTEGER NOT NULL,
                        limitCents INTEGER NOT NULL,
                        defaultCurrency TEXT NOT NULL DEFAULT 'LKR',
                        alertThresholdPercent INTEGER NOT NULL DEFAULT 80,
                        note TEXT,
                        isSynced INTEGER NOT NULL,
                        createdAtMillis INTEGER NOT NULL,
                        updatedAtMillis INTEGER NOT NULL,
                        deletedAtMillis INTEGER
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_budget_entries_userId ON budget_entries(userId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_budget_entries_monthStartMillis ON budget_entries(monthStartMillis)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_budget_entries_userId_monthStartMillis ON budget_entries(userId, monthStartMillis)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_budget_entries_userId_category_monthStartMillis ON budget_entries(userId, category, monthStartMillis)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS recurring_rules (
                        id TEXT NOT NULL PRIMARY KEY,
                        userId TEXT NOT NULL,
                        type TEXT NOT NULL,
                        name TEXT NOT NULL,
                        amountCents INTEGER NOT NULL,
                        originalAmount REAL NOT NULL DEFAULT 0.0,
                        originalCurrency TEXT NOT NULL DEFAULT 'LKR',
                        defaultCurrency TEXT NOT NULL DEFAULT 'LKR',
                        exchangeRate REAL,
                        source TEXT,
                        category TEXT,
                        paymentMethod TEXT,
                        expenseType TEXT,
                        note TEXT,
                        frequency TEXT NOT NULL DEFAULT 'MONTHLY',
                        interval INTEGER NOT NULL DEFAULT 1,
                        startDateMillis INTEGER NOT NULL,
                        nextRunDateMillis INTEGER NOT NULL,
                        endDateMillis INTEGER,
                        isActive INTEGER NOT NULL DEFAULT 1,
                        lastGeneratedAtMillis INTEGER,
                        isSynced INTEGER NOT NULL,
                        createdAtMillis INTEGER NOT NULL,
                        updatedAtMillis INTEGER NOT NULL,
                        deletedAtMillis INTEGER
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_recurring_rules_userId ON recurring_rules(userId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_recurring_rules_nextRunDateMillis ON recurring_rules(nextRunDateMillis)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_recurring_rules_userId_isActive_nextRunDateMillis ON recurring_rules(userId, isActive, nextRunDateMillis)")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_profiles ADD COLUMN budgetAlertsEnabled INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE user_profiles ADD COLUMN budgetAlertThresholdPercent INTEGER NOT NULL DEFAULT 80")
                db.execSQL("ALTER TABLE user_profiles ADD COLUMN profileImageStoragePath TEXT")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS budget_alerts (
                        id TEXT NOT NULL PRIMARY KEY,
                        userId TEXT NOT NULL,
                        budgetId TEXT NOT NULL,
                        category TEXT NOT NULL,
                        monthStartMillis INTEGER NOT NULL,
                        thresholdType TEXT NOT NULL,
                        progressPercent INTEGER NOT NULL,
                        notifiedAtMillis INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_budget_alerts_userId ON budget_alerts(userId)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_budget_alerts_userId_budgetId_monthStartMillis_thresholdType ON budget_alerts(userId, budgetId, monthStartMillis, thresholdType)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS exchange_rates (
                        id TEXT NOT NULL PRIMARY KEY,
                        fromCurrency TEXT NOT NULL,
                        toCurrency TEXT NOT NULL,
                        rate REAL NOT NULL,
                        source TEXT NOT NULL,
                        fetchedAtMillis INTEGER NOT NULL,
                        expiresAtMillis INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_exchange_rates_fromCurrency_toCurrency ON exchange_rates(fromCurrency, toCurrency)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS sync_conflicts (
                        id TEXT NOT NULL PRIMARY KEY,
                        userId TEXT NOT NULL,
                        collectionName TEXT NOT NULL,
                        documentId TEXT NOT NULL,
                        localUpdatedAtMillis INTEGER NOT NULL,
                        remoteUpdatedAtMillis INTEGER NOT NULL,
                        status TEXT NOT NULL,
                        createdAtMillis INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_sync_conflicts_userId ON sync_conflicts(userId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_sync_conflicts_userId_collectionName_documentId_status ON sync_conflicts(userId, collectionName, documentId, status)")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS sync_metadata (
                        userId TEXT NOT NULL,
                        collectionName TEXT NOT NULL,
                        lastPullMillis INTEGER NOT NULL DEFAULT 0,
                        lastSuccessfulSyncMillis INTEGER NOT NULL DEFAULT 0,
                        lastError TEXT,
                        isSyncing INTEGER NOT NULL DEFAULT 0,
                        PRIMARY KEY(userId, collectionName)
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_sync_metadata_userId ON sync_metadata(userId)")
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_profiles ADD COLUMN accentColorKey TEXT NOT NULL DEFAULT 'GREEN'")
                db.execSQL("ALTER TABLE savings_goals ADD COLUMN iconAccentColorKey TEXT NOT NULL DEFAULT 'GREEN'")
                db.execSQL("ALTER TABLE savings_goals ADD COLUMN goalImageUri TEXT")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS notifications (
                        id TEXT NOT NULL PRIMARY KEY,
                        userId TEXT NOT NULL,
                        title TEXT NOT NULL,
                        body TEXT NOT NULL,
                        type TEXT NOT NULL,
                        isRead INTEGER NOT NULL DEFAULT 0,
                        createdAtMillis INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_notifications_userId ON notifications(userId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_notifications_userId_createdAtMillis ON notifications(userId, createdAtMillis)")
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_profiles ADD COLUMN dailyRemindersEnabled INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE user_profiles ADD COLUMN remindExpenses INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE user_profiles ADD COLUMN remindIncome INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE user_profiles ADD COLUMN smartReminderMode INTEGER NOT NULL DEFAULT 1")
            }
        }
    }
}
