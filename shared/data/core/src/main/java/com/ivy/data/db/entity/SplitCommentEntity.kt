package com.ivy.data.db.entity

import androidx.annotation.Keep
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
@Entity(tableName = "split_comments")
data class SplitCommentEntity(
    @SerialName("expenseId")
    @Serializable(with = KSerializerUUID::class)
    val expenseId: UUID,
    @SerialName("authorMemberId")
    @Serializable(with = KSerializerUUID::class)
    val authorMemberId: UUID,
    @SerialName("text")
    val text: String,
    @SerialName("createdAt")
    @Serializable(with = KSerializerInstant::class)
    val createdAt: Instant = Instant.now(),
    // PUBLIC = visible to everyone in group; PRIVATE = visible to author only
    @SerialName("visibility")
    val visibility: String = "PUBLIC",

    @PrimaryKey
    @SerialName("id")
    @Serializable(with = KSerializerUUID::class)
    val id: UUID = UUID.randomUUID(),
)
