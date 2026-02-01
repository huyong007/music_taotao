package com.kidsenglishsongs.player.data.dao

import androidx.room.*
import com.kidsenglishsongs.player.data.entity.SongEntity
import com.kidsenglishsongs.player.data.entity.SongTagCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    
    @Query("SELECT * FROM songs ORDER BY createdAt DESC")
    fun getAllSongs(): Flow<List<SongEntity>>
    
    @Query("SELECT * FROM songs WHERE groupId = :groupId ORDER BY createdAt DESC")
    fun getSongsByGroup(groupId: String): Flow<List<SongEntity>>
    
    @Query("SELECT * FROM songs WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteSongs(): Flow<List<SongEntity>>
    
    @Query("SELECT * FROM songs WHERE title LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchSongs(query: String): Flow<List<SongEntity>>
    
    @Query("SELECT * FROM songs WHERE id = :id")
    suspend fun getSongById(id: String): SongEntity?
    
    @Query("SELECT * FROM songs ORDER BY playCount DESC LIMIT :limit")
    fun getMostPlayedSongs(limit: Int = 10): Flow<List<SongEntity>>
    
    @Query("SELECT * FROM songs ORDER BY lastPlayedAt DESC LIMIT :limit")
    fun getRecentlyPlayedSongs(limit: Int = 10): Flow<List<SongEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: SongEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<SongEntity>)
    
    @Update
    suspend fun updateSong(song: SongEntity)
    
    @Delete
    suspend fun deleteSong(song: SongEntity)
    
    @Query("DELETE FROM songs WHERE id = :id")
    suspend fun deleteSongById(id: String)
    
    @Query("UPDATE songs SET playCount = playCount + 1, lastPlayedAt = :playedAt WHERE id = :id")
    suspend fun incrementPlayCount(id: String, playedAt: Long = System.currentTimeMillis())
    
    @Query("UPDATE songs SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: String, isFavorite: Boolean)
    
    @Query("UPDATE songs SET coverPath = :coverPath WHERE id = :id")
    suspend fun updateCover(id: String, coverPath: String?)
    
    @Query("UPDATE songs SET lyricsPath = :lyricsPath WHERE id = :id")
    suspend fun updateLyrics(id: String, lyricsPath: String?)
    
    @Query("UPDATE songs SET groupId = :groupId WHERE id = :id")
    suspend fun updateGroup(id: String, groupId: String?)
    
    // 标签相关
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongTagCrossRef(crossRef: SongTagCrossRef)
    
    @Delete
    suspend fun deleteSongTagCrossRef(crossRef: SongTagCrossRef)
    
    @Query("DELETE FROM song_tag_cross_ref WHERE songId = :songId")
    suspend fun deleteAllTagsForSong(songId: String)
    
    @Query("""
        SELECT s.* FROM songs s
        INNER JOIN song_tag_cross_ref stc ON s.id = stc.songId
        WHERE stc.tagId = :tagId
        ORDER BY s.createdAt DESC
    """)
    fun getSongsByTag(tagId: String): Flow<List<SongEntity>>
    
    @Query("SELECT COUNT(*) FROM songs")
    suspend fun getSongCount(): Int
    
    @Query("SELECT SUM(duration) FROM songs")
    suspend fun getTotalDuration(): Long?
    
    @Query("SELECT SUM(playCount) FROM songs")
    suspend fun getTotalPlayCount(): Int?
    
    // 通过文件哈希检查是否已存在
    @Query("SELECT * FROM songs WHERE fileHash = :hash LIMIT 1")
    suspend fun getSongByHash(hash: String): SongEntity?
    
    // 获取所有已存在的文件哈希
    @Query("SELECT fileHash FROM songs WHERE fileHash IS NOT NULL")
    suspend fun getAllFileHashes(): List<String>
    
    // 检查哈希是否存在
    @Query("SELECT EXISTS(SELECT 1 FROM songs WHERE fileHash = :hash)")
    suspend fun existsByHash(hash: String): Boolean
}
