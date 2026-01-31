package com.kidsenglishsongs.player.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 播放历史实体
 */
@Entity(tableName = "play_history")
data class PlayHistoryEntity(
    @PrimaryKey val id: String,
    val songId: String,
    val playedAt: Long,
    val playDuration: Long                 // 实际播放时长
)
