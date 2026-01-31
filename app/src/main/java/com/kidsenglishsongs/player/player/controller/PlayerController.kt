package com.kidsenglishsongs.player.player.controller

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.kidsenglishsongs.player.data.entity.SongEntity
import com.kidsenglishsongs.player.player.PlaybackState
import com.kidsenglishsongs.player.player.RepeatMode
import com.kidsenglishsongs.player.player.service.PlaybackService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerController @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null
    
    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()
    
    private val _currentPlaylist = MutableStateFlow<List<SongEntity>>(emptyList())
    val currentPlaylist: StateFlow<List<SongEntity>> = _currentPlaylist.asStateFlow()
    
    private var positionUpdateJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)
    
    private var sleepTimerJob: Job? = null
    private val _sleepTimerRemaining = MutableStateFlow<Long?>(null)
    val sleepTimerRemaining: StateFlow<Long?> = _sleepTimerRemaining.asStateFlow()
    
    fun connect() {
        if (controllerFuture != null) return
        
        val sessionToken = SessionToken(
            context,
            ComponentName(context, PlaybackService::class.java)
        )
        
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            mediaController = controllerFuture?.get()
            setupPlayerListener()
        }, MoreExecutors.directExecutor())
    }
    
    fun disconnect() {
        positionUpdateJob?.cancel()
        sleepTimerJob?.cancel()
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controllerFuture = null
        mediaController = null
    }
    
    private fun setupPlayerListener() {
        mediaController?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _playbackState.update { it.copy(isPlaying = isPlaying) }
                if (isPlaying) {
                    startPositionUpdate()
                } else {
                    stopPositionUpdate()
                }
            }
            
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                mediaItem?.let { item ->
                    _playbackState.update { state ->
                        state.copy(
                            currentSongId = item.mediaId,
                            currentSongTitle = item.mediaMetadata.title?.toString() ?: "",
                            currentSongCover = item.mediaMetadata.artworkUri?.toString(),
                            duration = mediaController?.duration ?: 0L
                        )
                    }
                }
            }
            
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        _playbackState.update { it.copy(isBuffering = true) }
                    }
                    Player.STATE_READY -> {
                        _playbackState.update { state ->
                            state.copy(
                                isBuffering = false,
                                duration = mediaController?.duration ?: 0L
                            )
                        }
                    }
                    Player.STATE_ENDED -> {
                        _playbackState.update { it.copy(isBuffering = false) }
                    }
                    Player.STATE_IDLE -> {
                        _playbackState.update { it.copy(isBuffering = false) }
                    }
                }
            }
            
            override fun onRepeatModeChanged(repeatMode: Int) {
                val mode = when (repeatMode) {
                    Player.REPEAT_MODE_OFF -> RepeatMode.OFF
                    Player.REPEAT_MODE_ONE -> RepeatMode.ONE
                    Player.REPEAT_MODE_ALL -> RepeatMode.ALL
                    else -> RepeatMode.OFF
                }
                _playbackState.update { it.copy(repeatMode = mode) }
            }
            
            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                _playbackState.update { it.copy(isShuffleEnabled = shuffleModeEnabled) }
            }
            
            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                _playbackState.update { it.copy(error = error.message) }
            }
        })
    }
    
    private fun startPositionUpdate() {
        positionUpdateJob?.cancel()
        positionUpdateJob = scope.launch {
            while (isActive) {
                mediaController?.let { controller ->
                    _playbackState.update { it.copy(currentPosition = controller.currentPosition) }
                }
                delay(500)
            }
        }
    }
    
    private fun stopPositionUpdate() {
        positionUpdateJob?.cancel()
        positionUpdateJob = null
    }
    
    // 播放控制方法
    fun play() {
        mediaController?.play()
    }
    
    fun pause() {
        mediaController?.pause()
    }
    
    fun togglePlayPause() {
        mediaController?.let { controller ->
            if (controller.isPlaying) {
                controller.pause()
            } else {
                controller.play()
            }
        }
    }
    
    fun seekTo(position: Long) {
        mediaController?.seekTo(position)
    }
    
    fun seekToNext() {
        mediaController?.seekToNext()
    }
    
    fun seekToPrevious() {
        mediaController?.seekToPrevious()
    }
    
    fun setRepeatMode(mode: RepeatMode) {
        val playerMode = when (mode) {
            RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
        }
        mediaController?.repeatMode = playerMode
    }
    
    fun toggleRepeatMode() {
        val currentMode = _playbackState.value.repeatMode
        val nextMode = when (currentMode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        setRepeatMode(nextMode)
    }
    
    fun toggleShuffle() {
        mediaController?.let { controller ->
            controller.shuffleModeEnabled = !controller.shuffleModeEnabled
        }
    }
    
    fun setPlaybackSpeed(speed: Float) {
        mediaController?.setPlaybackSpeed(speed)
        _playbackState.update { it.copy(playbackSpeed = speed) }
    }
    
    // 播放列表管理
    fun setPlaylist(songs: List<SongEntity>, startIndex: Int = 0) {
        _currentPlaylist.value = songs
        
        val mediaItems = songs.map { song ->
            createMediaItem(song)
        }
        
        mediaController?.let { controller ->
            controller.setMediaItems(mediaItems, startIndex, 0L)
            controller.prepare()
            controller.play()
        }
    }
    
    fun playSong(song: SongEntity) {
        val mediaItem = createMediaItem(song)
        
        mediaController?.let { controller ->
            controller.setMediaItem(mediaItem)
            controller.prepare()
            controller.play()
        }
        
        _currentPlaylist.value = listOf(song)
    }
    
    fun addToQueue(song: SongEntity) {
        val mediaItem = createMediaItem(song)
        mediaController?.addMediaItem(mediaItem)
        _currentPlaylist.update { it + song }
    }
    
    fun clearQueue() {
        mediaController?.clearMediaItems()
        _currentPlaylist.value = emptyList()
    }
    
    private fun createMediaItem(song: SongEntity): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle(song.title)
            .setArtworkUri(song.coverPath?.let { Uri.fromFile(File(it)) })
            .build()
        
        return MediaItem.Builder()
            .setMediaId(song.id)
            .setUri(Uri.fromFile(File(song.filePath)))
            .setMediaMetadata(metadata)
            .build()
    }
    
    // 睡眠定时器
    fun setSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        
        if (minutes <= 0) {
            _sleepTimerRemaining.value = null
            return
        }
        
        val totalMillis = minutes * 60 * 1000L
        _sleepTimerRemaining.value = totalMillis
        
        sleepTimerJob = scope.launch {
            var remaining = totalMillis
            while (remaining > 0 && isActive) {
                delay(1000)
                remaining -= 1000
                _sleepTimerRemaining.value = remaining
            }
            
            if (isActive) {
                pause()
                _sleepTimerRemaining.value = null
            }
        }
    }
    
    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        _sleepTimerRemaining.value = null
    }
    
    // 获取当前播放歌曲 ID
    fun getCurrentSongId(): String? = _playbackState.value.currentSongId
}
