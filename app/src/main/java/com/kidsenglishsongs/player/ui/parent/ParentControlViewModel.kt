package com.kidsenglishsongs.player.ui.parent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidsenglishsongs.player.data.entity.GroupEntity
import com.kidsenglishsongs.player.data.entity.SongEntity
import com.kidsenglishsongs.player.data.entity.TagEntity
import com.kidsenglishsongs.player.data.repository.GroupRepository
import com.kidsenglishsongs.player.data.repository.SongRepository
import com.kidsenglishsongs.player.data.repository.TagRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ParentControlViewModel @Inject constructor(
    private val songRepository: SongRepository,
    private val groupRepository: GroupRepository,
    private val tagRepository: TagRepository
) : ViewModel() {
    
    val songs: StateFlow<List<SongEntity>> = songRepository.getAllSongs()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    val groups: StateFlow<List<GroupEntity>> = groupRepository.getAllGroups()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    val tags: StateFlow<List<TagEntity>> = tagRepository.getAllTags()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    private val _statistics = MutableStateFlow(Statistics())
    val statistics: StateFlow<Statistics> = _statistics.asStateFlow()
    
    private val _showCreateGroupDialog = MutableStateFlow(false)
    val showCreateGroupDialog: StateFlow<Boolean> = _showCreateGroupDialog.asStateFlow()
    
    private val _showCreateTagDialog = MutableStateFlow(false)
    val showCreateTagDialog: StateFlow<Boolean> = _showCreateTagDialog.asStateFlow()
    
    private val _songToDelete = MutableStateFlow<SongEntity?>(null)
    val songToDelete: StateFlow<SongEntity?> = _songToDelete.asStateFlow()
    
    init {
        loadStatistics()
    }
    
    private fun loadStatistics() {
        viewModelScope.launch {
            val songCount = songRepository.getSongCount()
            val totalDuration = songRepository.getTotalDuration()
            val totalPlayCount = songRepository.getTotalPlayCount()
            
            _statistics.value = Statistics(
                songCount = songCount,
                totalDuration = totalDuration,
                totalPlayCount = totalPlayCount
            )
        }
    }
    
    fun showCreateGroupDialog() {
        _showCreateGroupDialog.value = true
    }
    
    fun hideCreateGroupDialog() {
        _showCreateGroupDialog.value = false
    }
    
    fun createGroup(name: String) {
        viewModelScope.launch {
            val sortOrder = groupRepository.getNextSortOrder()
            val group = GroupEntity(
                id = UUID.randomUUID().toString(),
                name = name,
                sortOrder = sortOrder,
                createdAt = System.currentTimeMillis()
            )
            groupRepository.insertGroup(group)
            hideCreateGroupDialog()
        }
    }
    
    fun showCreateTagDialog() {
        _showCreateTagDialog.value = true
    }
    
    fun hideCreateTagDialog() {
        _showCreateTagDialog.value = false
    }
    
    fun createTag(name: String, color: String) {
        viewModelScope.launch {
            val tag = TagEntity(
                id = UUID.randomUUID().toString(),
                name = name,
                color = color
            )
            tagRepository.insertTag(tag)
            hideCreateTagDialog()
        }
    }
    
    fun showDeleteConfirmDialog(song: SongEntity) {
        _songToDelete.value = song
    }
    
    fun hideDeleteConfirmDialog() {
        _songToDelete.value = null
    }
    
    fun deleteSong(song: SongEntity) {
        viewModelScope.launch {
            songRepository.deleteSong(song)
            hideDeleteConfirmDialog()
            loadStatistics()
        }
    }
    
    data class Statistics(
        val songCount: Int = 0,
        val totalDuration: Long = 0,
        val totalPlayCount: Int = 0
    )
}
