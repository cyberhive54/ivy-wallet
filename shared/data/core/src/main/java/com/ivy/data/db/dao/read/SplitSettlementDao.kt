package com.ivy.data.db.dao.read

import androidx.room.Dao
import androidx.room.Query
import com.ivy.data.db.entity.SplitSettlementEntity
import java.util.*

@Dao
interface SplitSettlementDao {
    @Query("SELECT * FROM split_settlements ORDER BY createdAt DESC")
    suspend fun findAll(): List<SplitSettlementEntity>

    @Query("SELECT * FROM split_settlements WHERE groupId = :groupId ORDER BY createdAt DESC")
    suspend fun findByGroupId(groupId: UUID): List<SplitSettlementEntity>
}
