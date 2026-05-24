package com.spendly.financetracker.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.spendly.financetracker.data.local.dao.ExpenseDao
import com.spendly.financetracker.data.local.dao.GoalDao
import com.spendly.financetracker.data.local.dao.IncomeDao
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
    fun provideDatabase(@ApplicationContext context: Context): SpendlyDatabase =
        Room.databaseBuilder(context, SpendlyDatabase::class.java, "spendly_database")
            .addMigrations(SpendlyDatabase.MIGRATION_1_2, SpendlyDatabase.MIGRATION_2_3, SpendlyDatabase.MIGRATION_3_4)
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
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager = WorkManager.getInstance(context)
}
