package com.kidsenglishsongs.player.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 歌曲实体
 */
@Entity(
    tableName = "songs",
    indices = [Index(value = ["fileHash"], unique = true)]
)
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
    val lastPlayedAt: Long? = null,        // 最后播放时间
    val fileHash: String? = null,          // 文件MD5哈希，用于去重
    val artist: String? = null,            // 艺术家
    val album: String? = null,             // 专辑
    val fileSize: Long = 0                 // 文件大小（字节）
)
