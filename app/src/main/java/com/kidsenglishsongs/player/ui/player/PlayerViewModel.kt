package com.kidsenglishsongs.player.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidsenglishsongs.player.data.entity.SongEntity
import com.kidsenglishsongs.player.data.repository.SongRepository
import com.kidsenglishsongs.player.player.PlaybackState
import com.kidsenglishsongs.player.player.RepeatMode
import com.kidsenglishsongs.player.player.controller.PlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerController: PlayerController,
    private val songRepository: SongRepository
) : ViewModel() {
    
    val playbackState: StateFlow<PlaybackState> = playerController.playbackState
    
    val currentPlaylist: StateFlow<List<SongEntity>> = playerController.currentPlaylist
    
    val sleepTimerRemaining: StateFlow<Long?> = playerController.sleepTimerRemaining
    
    private val _currentSong = MutableStateFlow<SongEntity?>(null)
    val currentSong: StateFlow<SongEntity?> = _currentSong.asStateFlow()
    
    private val _showSleepTimerDialog = MutableStateFlow(false)
    val showSleepTimerDialog: StateFlow<Boolean> = _showSleepTimerDialog.asStateFlow()
    
    init {
        // 连接到播放服务
        playerController.connect()
        
        // 监听当前播放的歌曲ID，获取完整歌曲信息
        viewModelScope.launch {
            playbackState.collect { state ->
                state.currentSongId?.let { songId ->
                    val song = songRepository.getSongById(songId)
                    _currentSong.value = song
                }
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        // 注意：不要在这里断开连接，因为播放应该继续
    }
    
    fun play() = playerController.play()
    
    fun pause() = playerController.pause()
    
    fun togglePlayPause() = playerController.togglePlayPause()
    
    fun seekTo(position: Long) = playerController.seekTo(position)
    
    fun seekToProgress(progress: Float) {
        val position = (playbackState.value.duration * progress).toLong()
        playerController.seekTo(position)
    }
    
    fun seekToNext() = playerController.seekToNext()
    
    fun seekToPrevious() = playerController.seekToPrevious()
    
    fun toggleRepeatMode() = playerController.toggleRepeatMode()
    
    fun setRepeatMode(mode: RepeatMode) = playerController.setRepeatMode(mode)
    
    fun toggleShuffle() = playerController.toggleShuffle()
    
    fun toggleFavorite() {
        viewModelScope.launch {
            currentSong.value?.let { song ->
                songRepository.toggleFavorite(song.id)
                // 更新本地状态
                _currentSong.value = song.copy(isFavorite = !song.isFavorite)
            }
        }
    }
    
    fun showSleepTimerDialog() {
        _showSleepTimerDialog.value = true
    }
    
    fun hideSleepTimerDialog() {
        _showSleepTimerDialog.value = false
    }
    
    fun setSleepTimer(minutes: Int) {
        playerController.setSleepTimer(minutes)
        hideSleepTimerDialog()
    }
    
    fun cancelSleepTimer() {
        playerController.cancelSleepTimer()
    }
    
    fun playSong(song: SongEntity) {
        playerController.playSong(song)
    }
    
    fun setPlaylist(songs: List<SongEntity>, startIndex: Int = 0) {
        playerController.setPlaylist(songs, startIndex)
    }
    
    fun addToQueue(song: SongEntity) {
        playerController.addToQueue(song)
    }
}
