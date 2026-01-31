package com.kidsenglishsongs.player.data.repository

import com.kidsenglishsongs.player.data.dao.GroupDao
import com.kidsenglishsongs.player.data.entity.GroupEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupRepository @Inject constructor(
    private val groupDao: GroupDao
) {
    
    fun getAllGroups(): Flow<List<GroupEntity>> = groupDao.getAllGroups()
    
    suspend fun getGroupById(id: String): GroupEntity? = groupDao.getGroupById(id)
    
    suspend fun insertGroup(group: GroupEntity) = groupDao.insertGroup(group)
    
    suspend fun updateGroup(group: GroupEntity) = groupDao.updateGroup(group)
    
    suspend fun deleteGroup(group: GroupEntity) = groupDao.deleteGroup(group)
    
    suspend fun deleteGroupById(id: String) = groupDao.deleteGroupById(id)
    
    suspend fun getGroupCount(): Int = groupDao.getGroupCount()
    
    suspend fun getNextSortOrder(): Int = (groupDao.getMaxSortOrder() ?: -1) + 1
}
