package com.ivy.data.db.dao.write

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ivy.data.db.entity.GroupEntity
import java.util.UUID

@Dao
interface WriteGroupDao {
    @Upsert
    suspend fun save(value: GroupEntity)

    @Upsert
    suspend fun saveMany(value: List<GroupEntity>)

    @Query("DELETE FROM `groups` WHERE id = :id")
    suspend fun deleteById(id: UUID)

    @Query("DELETE FROM `groups`")
    suspend fun deleteAll()
}
