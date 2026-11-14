package com.ivy.data.db.dao.read

import androidx.room.Dao
import androidx.room.Query
import com.ivy.data.db.entity.SplitCommentEntity
import java.util.*

@Dao
interface SplitCommentDao {
    @Query("SELECT * FROM split_comments ORDER BY createdAt ASC")
    suspend fun findAll(): List<SplitCommentEntity>

    @Query("SELECT * FROM split_comments WHERE expenseId = :expenseId ORDER BY createdAt ASC")
    suspend fun findByExpenseId(expenseId: UUID): List<SplitCommentEntity>
}
