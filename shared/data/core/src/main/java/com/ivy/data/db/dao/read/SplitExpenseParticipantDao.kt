package com.ivy.data.db.dao.read

import androidx.room.Dao
import androidx.room.Query
import com.ivy.data.db.entity.SplitExpenseParticipantEntity
import java.util.*

@Dao
interface SplitExpenseParticipantDao {
    @Query("SELECT * FROM split_expense_participants")
    suspend fun findAll(): List<SplitExpenseParticipantEntity>

    @Query("SELECT * FROM split_expense_participants WHERE expenseId = :expenseId")
    suspend fun findByExpenseId(expenseId: UUID): List<SplitExpenseParticipantEntity>

    @Query("SELECT * FROM split_expense_participants WHERE memberId = :memberId")
    suspend fun findByMemberId(memberId: UUID): List<SplitExpenseParticipantEntity>
}
