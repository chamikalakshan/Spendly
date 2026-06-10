package com.spendly.financetracker.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.storage.FirebaseStorage
import com.spendly.financetracker.data.local.dao.BudgetAlertDao
import com.spendly.financetracker.data.local.dao.BudgetDao
import com.spendly.financetracker.data.local.dao.ExpenseDao
import com.spendly.financetracker.data.local.dao.ExchangeRateDao
import com.spendly.financetracker.data.local.dao.GoalDao
import com.spendly.financetracker.data.local.dao.IncomeDao
import com.spendly.financetracker.data.local.dao.NotificationDao
import com.spendly.financetracker.data.local.dao.RecurringRuleDao
import com.spendly.financetracker.data.local.dao.SyncConflictDao
import com.spendly.financetracker.data.local.dao.SyncMetadataDao
import com.spendly.financetracker.data.local.dao.UserProfileDao
import com.spendly.financetracker.data.local.db.SpendlyDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance().apply {
            firestoreSettings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build()
        }
    }

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SpendlyDatabase =
        Room.databaseBuilder(context, SpendlyDatabase::class.java, "spendly_database")
            .addMigrations(
                SpendlyDatabase.MIGRATION_1_2,
                SpendlyDatabase.MIGRATION_2_3,
                SpendlyDatabase.MIGRATION_3_4,
                SpendlyDatabase.MIGRATION_4_5,
                SpendlyDatabase.MIGRATION_5_6,
                SpendlyDatabase.MIGRATION_6_7,
                SpendlyDatabase.MIGRATION_7_8,
                SpendlyDatabase.MIGRATION_8_9,
                SpendlyDatabase.MIGRATION_9_10
            )
            .build()

    @Provides
    fun provideIncomeDao(database: SpendlyDatabase): IncomeDao = database.incomeDao()

    @Provides
    fun provideExpenseDao(database: SpendlyDatabase): ExpenseDao = database.expenseDao()

    @Provides
    fun provideGoalDao(database: SpendlyDatabase): GoalDao = database.goalDao()

    @Provides
    fun provideUserProfileDao(database: SpendlyDatabase): UserProfileDao = database.userProfileDao()

    @Provides
    fun provideBudgetDao(database: SpendlyDatabase): BudgetDao = database.budgetDao()

    @Provides
    fun provideRecurringRuleDao(database: SpendlyDatabase): RecurringRuleDao = database.recurringRuleDao()

    @Provides
    fun provideBudgetAlertDao(database: SpendlyDatabase): BudgetAlertDao = database.budgetAlertDao()

    @Provides
    fun provideExchangeRateDao(database: SpendlyDatabase): ExchangeRateDao = database.exchangeRateDao()

    @Provides
    fun provideSyncConflictDao(database: SpendlyDatabase): SyncConflictDao = database.syncConflictDao()

    @Provides
    fun provideSyncMetadataDao(database: SpendlyDatabase): SyncMetadataDao = database.syncMetadataDao()

    @Provides
    fun provideNotificationDao(database: SpendlyDatabase): NotificationDao = database.notificationDao()

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager = WorkManager.getInstance(context)
}
