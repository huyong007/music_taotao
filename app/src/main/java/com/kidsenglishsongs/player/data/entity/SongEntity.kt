package com.kidsenglishsongs.player.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 歌曲实体
 */
@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey val id: String,           // UUID
    val title: String,                     // 歌曲名称
    val filePath: String,                  // 文件路径
    val coverPath: String? = null,         // 封面图片路径
    val lyricsPath: String? = null,        // 歌词文件路径
    val duration: Long,                    // 时长（毫秒）
    val playCount: Int = 0,                // 播放次数
    val isFavorite: Boolean = false,       // 是否收藏
    val groupId: String? = null,           // 所属分组ID
    val createdAt: Long,                   // 添加时间
    val lastPlayedAt: Long? = null         // 最后播放时间
)
