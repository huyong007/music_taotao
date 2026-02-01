package com.kidsenglishsongs.player.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 2,
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
        
        // 数据库迁移 1 -> 2: 添加 fileHash, artist, album, fileSize 字段
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 添加新字段
                database.execSQL("ALTER TABLE songs ADD COLUMN fileHash TEXT")
                database.execSQL("ALTER TABLE songs ADD COLUMN artist TEXT")
                database.execSQL("ALTER TABLE songs ADD COLUMN album TEXT")
                database.execSQL("ALTER TABLE songs ADD COLUMN fileSize INTEGER NOT NULL DEFAULT 0")
                
                // 创建唯一索引
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_songs_fileHash ON songs(fileHash)")
            }
        }
    }
}
