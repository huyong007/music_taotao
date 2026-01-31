package com.kidsenglishsongs.player.ui.library

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidsenglishsongs.player.data.entity.GroupEntity
import com.kidsenglishsongs.player.data.entity.SongEntity
import com.kidsenglishsongs.player.data.entity.TagEntity
import com.kidsenglishsongs.player.data.repository.GroupRepository
import com.kidsenglishsongs.player.data.repository.SongRepository
import com.kidsenglishsongs.player.data.repository.TagRepository
import com.kidsenglishsongs.player.player.controller.PlayerController
import com.kidsenglishsongs.player.util.AudioMetadataReader
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val songRepository: SongRepository,
    private val groupRepository: GroupRepository,
    private val tagRepository: TagRepository,
    private val playerController: PlayerController,
    private val audioMetadataReader: AudioMetadataReader
) : ViewModel() {
    
    private val _songs = MutableStateFlow<List<SongEntity>>(emptyList())
    
    val groups: StateFlow<List<GroupEntity>> = groupRepository.getAllGroups()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    val tags: StateFlow<List<TagEntity>> = tagRepository.getAllTags()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _selectedGroupId = MutableStateFlow<String?>(null)
    val selectedGroupId: StateFlow<String?> = _selectedGroupId.asStateFlow()
    
    private val _selectedTagIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedTagIds: StateFlow<Set<String>> = _selectedTagIds.asStateFlow()
    
    private val _isSearchMode = MutableStateFlow(false)
    val isSearchMode: StateFlow<Boolean> = _isSearchMode.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    val currentPlayingSongId: StateFlow<String?> = playerController.playbackState
        .map { it.currentSongId }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)
    
    // 过滤后的歌曲列表
    val filteredSongs: StateFlow<List<SongEntity>> = combine(
        songRepository.getAllSongs(),
        searchQuery,
        selectedGroupId,
        selectedTagIds
    ) { songs, query, groupId, tagIds ->
        var filtered = songs
        
        // 搜索过滤
        if (query.isNotBlank()) {
            filtered = filtered.filter { it.title.contains(query, ignoreCase = true) }
        }
        
        // 分组过滤
        if (groupId != null) {
            filtered = filtered.filter { it.groupId == groupId }
        }
        
        // 标签过滤 - 暂时简化处理
        // TODO: 实现标签过滤逻辑
        
        filtered
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun setSelectedGroup(groupId: String?) {
        _selectedGroupId.value = groupId
    }
    
    fun toggleTag(tagId: String) {
        _selectedTagIds.update { currentTags ->
            if (currentTags.contains(tagId)) {
                currentTags - tagId
            } else {
                currentTags + tagId
            }
        }
    }
    
    fun toggleSearchMode() {
        _isSearchMode.update { !it }
        if (!_isSearchMode.value) {
            _searchQuery.value = ""
        }
    }
    
    fun toggleFavorite(songId: String) {
        viewModelScope.launch {
            songRepository.toggleFavorite(songId)
        }
    }
    
    fun playSong(song: SongEntity) {
        val allSongs = filteredSongs.value
        val index = allSongs.indexOfFirst { it.id == song.id }
        if (index >= 0) {
            playerController.setPlaylist(allSongs, index)
        } else {
            playerController.playSong(song)
        }
    }
    
    fun importSongs(uris: List<Uri>) {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                val songs = uris.mapNotNull { uri ->
                    audioMetadataReader.readMetadata(context, uri)?.let { metadata ->
                        SongEntity(
                            id = UUID.randomUUID().toString(),
                            title = metadata.title ?: uri.lastPathSegment ?: "未知歌曲",
                            filePath = metadata.filePath,
                            duration = metadata.duration,
                            createdAt = System.currentTimeMillis()
                        )
                    }
                }
                
                songRepository.insertSongs(songs)
            } catch (e: Exception) {
                // TODO: 处理错误
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun deleteSong(song: SongEntity) {
        viewModelScope.launch {
            songRepository.deleteSong(song)
        }
    }
}
