package com.kidsenglishsongs.player.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 分组实体
 */
@Entity(tableName = "groups")
data class GroupEntity(
    @PrimaryKey val id: String,
    val name: String,
    val coverPath: String? = null,
    val sortOrder: Int = 0,
    val createdAt: Long
)
