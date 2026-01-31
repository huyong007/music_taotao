package com.kidsenglishsongs.player.data.repository

import com.kidsenglishsongs.player.data.dao.PlaylistDao
import com.kidsenglishsongs.player.data.entity.PlaylistEntity
import com.kidsenglishsongs.player.data.entity.PlaylistSongCrossRef
import com.kidsenglishsongs.player.data.entity.SongEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistRepository @Inject constructor(
    private val playlistDao: PlaylistDao
) {
    
    fun getAllPlaylists(): Flow<List<PlaylistEntity>> = playlistDao.getAllPlaylists()
    
    suspend fun getPlaylistById(id: String): PlaylistEntity? = playlistDao.getPlaylistById(id)
    
    suspend fun insertPlaylist(playlist: PlaylistEntity) = playlistDao.insertPlaylist(playlist)
    
    suspend fun updatePlaylist(playlist: PlaylistEntity) = playlistDao.updatePlaylist(playlist)
    
    suspend fun deletePlaylist(playlist: PlaylistEntity) {
        playlistDao.deleteAllSongsFromPlaylist(playlist.id)
        playlistDao.deletePlaylist(playlist)
    }
    
    suspend fun deletePlaylistById(id: String) {
        playlistDao.deleteAllSongsFromPlaylist(id)
        playlistDao.deletePlaylistById(id)
    }
    
    fun getSongsInPlaylist(playlistId: String): Flow<List<SongEntity>> =
        playlistDao.getSongsInPlaylist(playlistId)
    
    suspend fun addSongToPlaylist(playlistId: String, songId: String) {
        val maxOrder = playlistDao.getMaxSortOrderInPlaylist(playlistId) ?: -1
        val crossRef = PlaylistSongCrossRef(
            playlistId = playlistId,
            songId = songId,
            sortOrder = maxOrder + 1
        )
        playlistDao.insertPlaylistSongCrossRef(crossRef)
    }
    
    suspend fun removeSongFromPlaylist(playlistId: String, songId: String) {
        playlistDao.removeSongFromPlaylist(playlistId, songId)
    }
    
    suspend fun clearPlaylist(playlistId: String) {
        playlistDao.deleteAllSongsFromPlaylist(playlistId)
    }
    
    suspend fun getSongCountInPlaylist(playlistId: String): Int =
        playlistDao.getSongCountInPlaylist(playlistId)
    
    suspend fun getPlaylistCount(): Int = playlistDao.getPlaylistCount()
}
