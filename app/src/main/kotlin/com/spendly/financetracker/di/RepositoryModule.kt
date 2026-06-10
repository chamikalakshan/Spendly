package com.spendly.financetracker.di

import com.spendly.financetracker.data.remote.BudgetRepositoryImpl
import com.spendly.financetracker.data.remote.ExpenseRepositoryImpl
import com.spendly.financetracker.data.remote.GoalRepositoryImpl
import com.spendly.financetracker.data.remote.IncomeRepositoryImpl
import com.spendly.financetracker.data.remote.NotificationRepositoryImpl
import com.spendly.financetracker.data.remote.RecurringTransactionRepositoryImpl
import com.spendly.financetracker.data.remote.UserRepositoryImpl
import com.spendly.financetracker.data.repository.AuthRepository
import com.spendly.financetracker.data.repository.BudgetRepository
import com.spendly.financetracker.data.repository.ExpenseRepository
import com.spendly.financetracker.data.repository.FirebaseAuthRepository
import com.spendly.financetracker.data.repository.FirebaseTransactionRepository
import com.spendly.financetracker.data.repository.GoalRepository
import com.spendly.financetracker.data.repository.IncomeRepository
import com.spendly.financetracker.data.repository.NotificationRepository
import com.spendly.financetracker.data.repository.RecurringTransactionRepository
import com.spendly.financetracker.data.repository.TransactionRepository
import com.spendly.financetracker.data.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: FirebaseAuthRepository): AuthRepository

    @Binds
    @Singleton
    abstract fun bindIncomeRepository(impl: IncomeRepositoryImpl): IncomeRepository

    @Binds
    @Singleton
    abstract fun bindExpenseRepository(impl: ExpenseRepositoryImpl): ExpenseRepository

    @Binds
    @Singleton
    abstract fun bindGoalRepository(impl: GoalRepositoryImpl): GoalRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(impl: FirebaseTransactionRepository): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindBudgetRepository(impl: BudgetRepositoryImpl): BudgetRepository

    @Binds
    @Singleton
    abstract fun bindRecurringTransactionRepository(impl: RecurringTransactionRepositoryImpl): RecurringTransactionRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(impl: NotificationRepositoryImpl): NotificationRepository
}
