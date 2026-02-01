package com.kidsenglishsongs.player.data.repository

import com.kidsenglishsongs.player.data.dao.SongDao
import com.kidsenglishsongs.player.data.dao.PlayHistoryDao
import com.kidsenglishsongs.player.data.entity.PlayHistoryEntity
import com.kidsenglishsongs.player.data.entity.SongEntity
import com.kidsenglishsongs.player.data.entity.SongTagCrossRef
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SongRepository @Inject constructor(
    private val songDao: SongDao,
    private val playHistoryDao: PlayHistoryDao
) {
    
    fun getAllSongs(): Flow<List<SongEntity>> = songDao.getAllSongs()
    
    fun getSongsByGroup(groupId: String): Flow<List<SongEntity>> = songDao.getSongsByGroup(groupId)
    
    fun getFavoriteSongs(): Flow<List<SongEntity>> = songDao.getFavoriteSongs()
    
    fun searchSongs(query: String): Flow<List<SongEntity>> = songDao.searchSongs(query)
    
    fun getMostPlayedSongs(limit: Int = 10): Flow<List<SongEntity>> = songDao.getMostPlayedSongs(limit)
    
    fun getRecentlyPlayedSongs(limit: Int = 10): Flow<List<SongEntity>> = songDao.getRecentlyPlayedSongs(limit)
    
    fun getSongsByTag(tagId: String): Flow<List<SongEntity>> = songDao.getSongsByTag(tagId)
    
    suspend fun getSongById(id: String): SongEntity? = songDao.getSongById(id)
    
    suspend fun insertSong(song: SongEntity) = songDao.insertSong(song)
    
    suspend fun insertSongs(songs: List<SongEntity>) = songDao.insertSongs(songs)
    
    suspend fun updateSong(song: SongEntity) = songDao.updateSong(song)
    
    suspend fun deleteSong(song: SongEntity) = songDao.deleteSong(song)
    
    suspend fun deleteSongById(id: String) = songDao.deleteSongById(id)
    
    suspend fun toggleFavorite(id: String) {
        val song = songDao.getSongById(id)
        song?.let {
            songDao.updateFavorite(id, !it.isFavorite)
        }
    }
    
    suspend fun setFavorite(id: String, isFavorite: Boolean) {
        songDao.updateFavorite(id, isFavorite)
    }
    
    suspend fun updateCover(id: String, coverPath: String?) {
        songDao.updateCover(id, coverPath)
    }
    
    suspend fun updateLyrics(id: String, lyricsPath: String?) {
        songDao.updateLyrics(id, lyricsPath)
    }
    
    suspend fun updateGroup(id: String, groupId: String?) {
        songDao.updateGroup(id, groupId)
    }
    
    suspend fun recordPlay(songId: String, playDuration: Long) {
        val playedAt = System.currentTimeMillis()
        songDao.incrementPlayCount(songId, playedAt)
        
        val playHistory = PlayHistoryEntity(
            id = UUID.randomUUID().toString(),
            songId = songId,
            playedAt = playedAt,
            playDuration = playDuration
        )
        playHistoryDao.insertPlayHistory(playHistory)
    }
    
    // 标签关联
    suspend fun addTagToSong(songId: String, tagId: String) {
        songDao.insertSongTagCrossRef(SongTagCrossRef(songId, tagId))
    }
    
    suspend fun removeTagFromSong(songId: String, tagId: String) {
        songDao.deleteSongTagCrossRef(SongTagCrossRef(songId, tagId))
    }
    
    suspend fun clearTagsForSong(songId: String) {
        songDao.deleteAllTagsForSong(songId)
    }
    
    // 统计
    suspend fun getSongCount(): Int = songDao.getSongCount()
    
    suspend fun getTotalDuration(): Long = songDao.getTotalDuration() ?: 0L
    
    suspend fun getTotalPlayCount(): Int = songDao.getTotalPlayCount() ?: 0
    
    // 文件哈希去重相关
    suspend fun getSongByHash(hash: String): SongEntity? = songDao.getSongByHash(hash)
    
    suspend fun getAllFileHashes(): List<String> = songDao.getAllFileHashes()
    
    suspend fun existsByHash(hash: String): Boolean = songDao.existsByHash(hash)
}
