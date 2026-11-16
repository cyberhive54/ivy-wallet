package com.ivy.data.db.dao.read

import androidx.room.Dao
import androidx.room.Query
import com.ivy.data.db.entity.SplitExpensePayerEntity
import java.util.UUID

@Dao
interface SplitExpensePayerDao {
    @Query("SELECT * FROM split_expense_payers WHERE expenseId = :expenseId")
    suspend fun findByExpenseId(expenseId: UUID): List<SplitExpensePayerEntity>

    @Query("SELECT * FROM split_expense_payers WHERE memberId = :memberId")
    suspend fun findByMemberId(memberId: UUID): List<SplitExpensePayerEntity>

    @Query("SELECT * FROM split_expense_payers")
    suspend fun findAll(): List<SplitExpensePayerEntity>
}
