package com.ivy.data.db.dao.write

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ivy.data.db.entity.SplitSettlementEntity
import java.util.UUID

@Dao
interface WriteSplitSettlementDao {
    @Upsert
    suspend fun save(value: SplitSettlementEntity)

    @Upsert
    suspend fun saveMany(value: List<SplitSettlementEntity>)

    @Query("DELETE FROM split_settlements WHERE id = :id")
    suspend fun deleteById(id: UUID)

    @Query("DELETE FROM split_settlements")
    suspend fun deleteAll()
}
