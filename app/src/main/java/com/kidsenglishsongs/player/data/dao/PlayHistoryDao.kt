package com.kidsenglishsongs.player.data.dao

import androidx.room.*
import com.kidsenglishsongs.player.data.entity.PlayHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayHistoryDao {
    
    @Query("SELECT * FROM play_history ORDER BY playedAt DESC")
    fun getAllPlayHistory(): Flow<List<PlayHistoryEntity>>
    
    @Query("SELECT * FROM play_history ORDER BY playedAt DESC LIMIT :limit")
    fun getRecentPlayHistory(limit: Int = 50): Flow<List<PlayHistoryEntity>>
    
    @Query("SELECT * FROM play_history WHERE songId = :songId ORDER BY playedAt DESC")
    fun getPlayHistoryForSong(songId: String): Flow<List<PlayHistoryEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayHistory(playHistory: PlayHistoryEntity)
    
    @Delete
    suspend fun deletePlayHistory(playHistory: PlayHistoryEntity)
    
    @Query("DELETE FROM play_history")
    suspend fun clearAllPlayHistory()
    
    @Query("DELETE FROM play_history WHERE songId = :songId")
    suspend fun clearPlayHistoryForSong(songId: String)
    
    @Query("SELECT SUM(playDuration) FROM play_history")
    suspend fun getTotalPlayDuration(): Long?
    
    @Query("SELECT COUNT(*) FROM play_history")
    suspend fun getPlayHistoryCount(): Int
    
    @Query("""
        SELECT SUM(playDuration) FROM play_history 
        WHERE playedAt >= :startTime AND playedAt <= :endTime
    """)
    suspend fun getPlayDurationInRange(startTime: Long, endTime: Long): Long?
}
