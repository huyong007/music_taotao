package com.kidsenglishsongs.player.data.dao

import androidx.room.*
import com.kidsenglishsongs.player.data.entity.SongTagCrossRef
import com.kidsenglishsongs.player.data.entity.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    
    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAllTags(): Flow<List<TagEntity>>
    
    @Query("SELECT * FROM tags WHERE id = :id")
    suspend fun getTagById(id: String): TagEntity?
    
    @Query("""
        SELECT t.* FROM tags t
        INNER JOIN song_tag_cross_ref stc ON t.id = stc.tagId
        WHERE stc.songId = :songId
    """)
    fun getTagsForSong(songId: String): Flow<List<TagEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: TagEntity)
    
    @Update
    suspend fun updateTag(tag: TagEntity)
    
    @Delete
    suspend fun deleteTag(tag: TagEntity)
    
    @Query("DELETE FROM tags WHERE id = :id")
    suspend fun deleteTagById(id: String)
    
    @Query("SELECT COUNT(*) FROM tags")
    suspend fun getTagCount(): Int
}
