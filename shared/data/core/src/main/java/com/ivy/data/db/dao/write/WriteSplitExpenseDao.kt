package com.ivy.data.db.dao.write

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ivy.data.db.entity.SplitExpenseEntity
import java.util.UUID

@Dao
interface WriteSplitExpenseDao {
    @Upsert
    suspend fun save(value: SplitExpenseEntity)

    @Upsert
    suspend fun saveMany(value: List<SplitExpenseEntity>)

    @Query("DELETE FROM split_expenses WHERE id = :id")
    suspend fun deleteById(id: UUID)

    @Query("DELETE FROM split_expenses")
    suspend fun deleteAll()
}
