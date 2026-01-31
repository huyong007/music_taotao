package com.kidsenglishsongs.player.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kidsenglishsongs.player.data.dao.*
import com.kidsenglishsongs.player.data.entity.*

@Database(
    entities = [
        SongEntity::class,
        GroupEntity::class,
        TagEntity::class,
        SongTagCrossRef::class,
        PlaylistEntity::class,
        PlaylistSongCrossRef::class,
        PlayHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun songDao(): SongDao
    abstract fun groupDao(): GroupDao
    abstract fun tagDao(): TagDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun playHistoryDao(): PlayHistoryDao
    
    companion object {
        const val DATABASE_NAME = "kids_english_songs_db"
    }
}
