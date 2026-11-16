package com.ivy.data.db.dao.write

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ivy.data.db.entity.SplitExpensePayerEntity
import java.util.UUID

@Dao
interface WriteSplitExpensePayerDao {
    @Upsert
    suspend fun save(value: SplitExpensePayerEntity)

    @Upsert
    suspend fun saveMany(value: List<SplitExpensePayerEntity>)

    @Query("DELETE FROM split_expense_payers WHERE expenseId = :expenseId")
    suspend fun deleteByExpenseId(expenseId: UUID)

    @Query("DELETE FROM split_expense_payers")
    suspend fun deleteAll()
}
