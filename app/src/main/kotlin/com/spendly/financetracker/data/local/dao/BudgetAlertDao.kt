package com.spendly.financetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.spendly.financetracker.data.local.entity.BudgetAlertEntity

@Dao
interface BudgetAlertDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: BudgetAlertEntity)

    @Query(
        "SELECT * FROM budget_alerts WHERE userId = :userId AND budgetId = :budgetId " +
            "AND monthStartMillis = :monthStartMillis AND thresholdType = :thresholdType LIMIT 1"
    )
    suspend fun getExisting(userId: String, budgetId: String, monthStartMillis: Long, thresholdType: String): BudgetAlertEntity?
}
