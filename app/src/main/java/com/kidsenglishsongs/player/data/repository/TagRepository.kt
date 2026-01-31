package com.kidsenglishsongs.player.data.repository

import com.kidsenglishsongs.player.data.dao.TagDao
import com.kidsenglishsongs.player.data.entity.TagEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TagRepository @Inject constructor(
    private val tagDao: TagDao
) {
    
    fun getAllTags(): Flow<List<TagEntity>> = tagDao.getAllTags()
    
    fun getTagsForSong(songId: String): Flow<List<TagEntity>> = tagDao.getTagsForSong(songId)
    
    suspend fun getTagById(id: String): TagEntity? = tagDao.getTagById(id)
    
    suspend fun insertTag(tag: TagEntity) = tagDao.insertTag(tag)
    
    suspend fun updateTag(tag: TagEntity) = tagDao.updateTag(tag)
    
    suspend fun deleteTag(tag: TagEntity) = tagDao.deleteTag(tag)
    
    suspend fun deleteTagById(id: String) = tagDao.deleteTagById(id)
    
    suspend fun getTagCount(): Int = tagDao.getTagCount()
}
