package com.ivy.data.db.dao.write

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ivy.data.db.entity.SplitExpenseParticipantEntity
import java.util.UUID

@Dao
interface WriteSplitExpenseParticipantDao {
    @Upsert
    suspend fun save(value: SplitExpenseParticipantEntity)

    @Upsert
    suspend fun saveMany(value: List<SplitExpenseParticipantEntity>)

    @Query("DELETE FROM split_expense_participants WHERE id = :id")
    suspend fun deleteById(id: UUID)

    @Query("DELETE FROM split_expense_participants WHERE expenseId = :expenseId")
    suspend fun deleteByExpenseId(expenseId: UUID)

    @Query("DELETE FROM split_expense_participants")
    suspend fun deleteAll()
}
