package com.ivy.data.db.dao.read

import androidx.room.Dao
import androidx.room.Query
import com.ivy.data.db.entity.PlannedPaymentPriceHistoryEntity
import java.util.*

@Dao
interface PlannedPaymentPriceHistoryDao {
    @Query("SELECT * FROM planned_payment_price_history ORDER BY effectiveFrom DESC")
    suspend fun findAll(): List<PlannedPaymentPriceHistoryEntity>

    @Query("SELECT * FROM planned_payment_price_history WHERE ruleId = :ruleId ORDER BY effectiveFrom DESC")
    suspend fun findByRuleId(ruleId: UUID): List<PlannedPaymentPriceHistoryEntity>

    @Query("SELECT * FROM planned_payment_price_history WHERE ruleId = :ruleId ORDER BY effectiveFrom DESC LIMIT 1")
    suspend fun findLatestByRuleId(ruleId: UUID): PlannedPaymentPriceHistoryEntity?
}
