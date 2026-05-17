package com.spendly.app.di

import com.spendly.app.data.remote.AuthRepositoryImpl
import com.spendly.app.data.remote.ExpenseRepositoryImpl
import com.spendly.app.data.remote.GoalRepositoryImpl
import com.spendly.app.data.remote.IncomeRepositoryImpl
import com.spendly.app.repository.AuthRepository
import com.spendly.app.repository.ExpenseRepository
import com.spendly.app.repository.GoalRepository
import com.spendly.app.repository.IncomeRepository
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
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindIncomeRepository(
        incomeRepositoryImpl: IncomeRepositoryImpl
    ): IncomeRepository

    @Binds
    @Singleton
    abstract fun bindExpenseRepository(
        expenseRepositoryImpl: ExpenseRepositoryImpl
    ): ExpenseRepository

    @Binds
    @Singleton
    abstract fun bindGoalRepository(
        goalRepositoryImpl: GoalRepositoryImpl
    ): GoalRepository
}
