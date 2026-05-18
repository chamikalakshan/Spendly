package com.spendly.app.di

import android.content.Context
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.spendly.app.data.local.dao.ExpenseDao
import com.spendly.app.data.local.dao.GoalDao
import com.spendly.app.data.local.dao.IncomeDao
import com.spendly.app.data.local.db.SpendlyDatabase
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
    fun provideFirebaseFirestore(): FirebaseFirestore {
        val firestore = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
            .build()
        firestore.firestoreSettings = settings
        return firestore
    }

    @Provides
    @Singleton
    fun provideSpendlyDatabase(
        @ApplicationContext context: Context
    ): SpendlyDatabase {
        return Room.databaseBuilder(
            context,
            SpendlyDatabase::class.java,
            "spendly_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideIncomeDao(db: SpendlyDatabase): IncomeDao = db.incomeDao()

    @Provides
    @Singleton
    fun provideExpenseDao(db: SpendlyDatabase): ExpenseDao = db.expenseDao()

    @Provides
    @Singleton
    fun provideGoalDao(db: SpendlyDatabase): GoalDao = db.goalDao()
}
