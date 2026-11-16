package com.ivy.data.db.dao.read

import androidx.room.Dao
import androidx.room.Query
import com.ivy.data.db.entity.GroupMemberEntity
import java.util.*

@Dao
interface GroupMemberDao {
    @Query("SELECT * FROM group_members ORDER BY orderNum ASC, id ASC")
    suspend fun findAll(): List<GroupMemberEntity>

    @Query("SELECT * FROM group_members WHERE groupId = :groupId ORDER BY orderNum ASC, id ASC")
    suspend fun findByGroupId(groupId: UUID): List<GroupMemberEntity>

    @Query("SELECT * FROM group_members WHERE groupId = :groupId AND archived = 0 ORDER BY orderNum ASC, id ASC")
    suspend fun findActiveByGroupId(groupId: UUID): List<GroupMemberEntity>

    @Query("SELECT * FROM group_members WHERE id = :id")
    suspend fun findById(id: UUID): GroupMemberEntity?
}
