package com.ivy.data.db.dao.read

import androidx.room.Dao
import androidx.room.Query
import com.ivy.data.db.entity.GroupEntity
import java.util.*

@Dao
interface GroupDao {
    @Query("SELECT * FROM `groups` ORDER BY createdAt DESC")
    suspend fun findAll(): List<GroupEntity>

    @Query("SELECT * FROM `groups` WHERE archived = 0 ORDER BY createdAt DESC")
    suspend fun findActive(): List<GroupEntity>

    @Query("SELECT * FROM `groups` WHERE id = :id")
    suspend fun findById(id: UUID): GroupEntity?
}
