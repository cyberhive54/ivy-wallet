package com.ivy.data.db.entity

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ivy.base.kotlinxserilzation.KSerializerInstant
import com.ivy.base.kotlinxserilzation.KSerializerUUID
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

@Suppress("DataClassDefaultValues")
@Keep
@Serializable
@Entity(tableName = "group_members")
data class GroupMemberEntity(
    @SerialName("groupId")
    @Serializable(with = KSerializerUUID::class)
    val groupId: UUID,
    @SerialName("memberName")
    val memberName: String,
    @SerialName("memberIsYou")
    val memberIsYou: Boolean = false,
    @SerialName("memberColor")
    val memberColor: Int = 0,
    @SerialName("memberPhone")
    val memberPhone: String? = null,
    @SerialName("avatarId")
    val avatarId: String? = null,
    @SerialName("orderNum")
    val orderNum: Double = 0.0,
    // v133: leaving a group keeps the row (history is preserved), it just hides
    // the member from the active list.
    @SerialName("archived")
    @ColumnInfo(defaultValue = "0")
    val archived: Boolean = false,
    // v133: membership audit - when the member joined the group
    @SerialName("createdAt")
    @Serializable(with = KSerializerInstant::class)
    val createdAt: Instant? = null,

    @PrimaryKey
    @SerialName("id")
    @Serializable(with = KSerializerUUID::class)
    val id: UUID = UUID.randomUUID(),
)