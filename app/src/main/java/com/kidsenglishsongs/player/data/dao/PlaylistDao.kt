package com.kidsenglishsongs.player.data.dao

import androidx.room.*
import com.kidsenglishsongs.player.data.entity.PlaylistEntity
import com.kidsenglishsongs.player.data.entity.PlaylistSongCrossRef
import com.kidsenglishsongs.player.data.entity.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    
    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>
    
    @Query("SELECT * FROM playlists WHERE id = :id")
    suspend fun getPlaylistById(id: String): PlaylistEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity)
    
    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)
    
    @Delete
    suspend fun deletePlaylist(playlist: PlaylistEntity)
    
    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun deletePlaylistById(id: String)
    
    // 播放列表-歌曲关联
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistSongCrossRef(crossRef: PlaylistSongCrossRef)
    
    @Delete
    suspend fun deletePlaylistSongCrossRef(crossRef: PlaylistSongCrossRef)
    
    @Query("DELETE FROM playlist_song_cross_ref WHERE playlistId = :playlistId")
    suspend fun deleteAllSongsFromPlaylist(playlistId: String)
    
    @Query("DELETE FROM playlist_song_cross_ref WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun removeSongFromPlaylist(playlistId: String, songId: String)
    
    @Query("""
        SELECT s.* FROM songs s
        INNER JOIN playlist_song_cross_ref psc ON s.id = psc.songId
        WHERE psc.playlistId = :playlistId
        ORDER BY psc.sortOrder ASC
    """)
    fun getSongsInPlaylist(playlistId: String): Flow<List<SongEntity>>
    
    @Query("SELECT MAX(sortOrder) FROM playlist_song_cross_ref WHERE playlistId = :playlistId")
    suspend fun getMaxSortOrderInPlaylist(playlistId: String): Int?
    
    @Query("SELECT COUNT(*) FROM playlist_song_cross_ref WHERE playlistId = :playlistId")
    suspend fun getSongCountInPlaylist(playlistId: String): Int
    
    @Query("SELECT COUNT(*) FROM playlists")
    suspend fun getPlaylistCount(): Int
}
