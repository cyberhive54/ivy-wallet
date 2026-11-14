package com.ivy.data.db.dao.write

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ivy.data.db.entity.PlannedPaymentPriceHistoryEntity
import java.util.UUID

@Dao
interface WritePlannedPaymentPriceHistoryDao {
    @Upsert
    suspend fun save(value: PlannedPaymentPriceHistoryEntity)

    @Upsert
    suspend fun saveMany(value: List<PlannedPaymentPriceHistoryEntity>)

    @Query("DELETE FROM planned_payment_price_history WHERE ruleId = :ruleId")
    suspend fun deleteByRuleId(ruleId: UUID)

    @Query("DELETE FROM planned_payment_price_history")
    suspend fun deleteAll()
}
