package com.ivy.data.db.dao.write

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ivy.data.db.entity.GroupMemberEntity
import java.util.UUID

@Dao
interface WriteGroupMemberDao {
    @Upsert
    suspend fun save(value: GroupMemberEntity)

    @Upsert
    suspend fun saveMany(value: List<GroupMemberEntity>)

    @Query("DELETE FROM group_members WHERE id = :id")
    suspend fun deleteById(id: UUID)

    @Query("DELETE FROM group_members")
    suspend fun deleteAll()
}
