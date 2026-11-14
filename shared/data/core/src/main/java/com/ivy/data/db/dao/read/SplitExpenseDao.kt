package com.ivy.data.db.dao.read

import androidx.room.Dao
import androidx.room.Query
import com.ivy.data.db.entity.SplitExpenseEntity
import java.util.*

@Dao
interface SplitExpenseDao {
    @Query("SELECT * FROM split_expenses ORDER BY date DESC")
    suspend fun findAll(): List<SplitExpenseEntity>

    @Query("SELECT * FROM split_expenses WHERE groupId = :groupId ORDER BY date DESC")
    suspend fun findByGroupId(groupId: UUID): List<SplitExpenseEntity>

    @Query("SELECT * FROM split_expenses WHERE groupId = :groupId AND settled = 0")
    suspend fun findUnsettledByGroup(groupId: UUID): List<SplitExpenseEntity>

    @Query("SELECT * FROM split_expenses WHERE id = :id")
    suspend fun findById(id: UUID): SplitExpenseEntity?
}
