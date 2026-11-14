package com.ivy.data.db.dao.write

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ivy.data.db.entity.SplitCommentEntity
import java.util.UUID

@Dao
interface WriteSplitCommentDao {
    @Upsert
    suspend fun save(value: SplitCommentEntity)

    @Upsert
    suspend fun saveMany(value: List<SplitCommentEntity>)

    @Query("DELETE FROM split_comments WHERE id = :id")
    suspend fun deleteById(id: UUID)

    @Query("DELETE FROM split_comments")
    suspend fun deleteAll()
}
