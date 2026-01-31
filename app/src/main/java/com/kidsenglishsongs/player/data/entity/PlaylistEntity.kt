package com.kidsenglishsongs.player.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 播放列表实体
 */
@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey val id: String,
    val name: String,
    val createdAt: Long
)

/**
 * 播放列表-歌曲 关联表
 */
@Entity(
    tableName = "playlist_song_cross_ref",
    primaryKeys = ["playlistId", "songId"]
)
data class PlaylistSongCrossRef(
    val playlistId: String,
    val songId: String,
    val sortOrder: Int
)
