package com.ivy.data.db.entity

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ivy.base.kotlinxserilzation.KSerializerUUID
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
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

    @PrimaryKey
    @SerialName("id")
    @Serializable(with = KSerializerUUID::class)
    val id: UUID = UUID.randomUUID(),
)
