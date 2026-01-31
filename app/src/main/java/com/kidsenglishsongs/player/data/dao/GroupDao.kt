package com.kidsenglishsongs.player.data.dao

import androidx.room.*
import com.kidsenglishsongs.player.data.entity.GroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {
    
    @Query("SELECT * FROM `groups` ORDER BY sortOrder ASC, createdAt DESC")
    fun getAllGroups(): Flow<List<GroupEntity>>
    
    @Query("SELECT * FROM `groups` WHERE id = :id")
    suspend fun getGroupById(id: String): GroupEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: GroupEntity)
    
    @Update
    suspend fun updateGroup(group: GroupEntity)
    
    @Delete
    suspend fun deleteGroup(group: GroupEntity)
    
    @Query("DELETE FROM `groups` WHERE id = :id")
    suspend fun deleteGroupById(id: String)
    
    @Query("SELECT COUNT(*) FROM `groups`")
    suspend fun getGroupCount(): Int
    
    @Query("SELECT MAX(sortOrder) FROM `groups`")
    suspend fun getMaxSortOrder(): Int?
}
