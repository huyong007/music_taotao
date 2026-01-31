package com.kidsenglishsongs.player.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 标签实体
 */
@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey val id: String,
    val name: String,
    val color: String                      // 十六进制颜色
)

/**
 * 歌曲-标签 多对多关联表
 */
@Entity(
    tableName = "song_tag_cross_ref",
    primaryKeys = ["songId", "tagId"]
)
data class SongTagCrossRef(
    val songId: String,
    val tagId: String
)
