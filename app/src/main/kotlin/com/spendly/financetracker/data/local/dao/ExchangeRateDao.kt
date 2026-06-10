package com.spendly.financetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.spendly.financetracker.data.local.entity.ExchangeRateEntity

@Dao
interface ExchangeRateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ExchangeRateEntity)

    @Query("SELECT * FROM exchange_rates WHERE fromCurrency = :fromCurrency AND toCurrency = :toCurrency LIMIT 1")
    suspend fun getRate(fromCurrency: String, toCurrency: String): ExchangeRateEntity?
}
