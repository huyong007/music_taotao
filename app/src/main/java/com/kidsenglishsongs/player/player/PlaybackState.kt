package com.kidsenglishsongs.player.player

/**
 * 循环播放模式
 */
enum class RepeatMode {
    OFF,        // 不循环
    ONE,        // 单曲循环
    ALL         // 列表循环
}

/**
 * 播放状态
 */
data class PlaybackState(
    val isPlaying: Boolean = false,
    val currentSongId: String? = null,
    val currentSongTitle: String = "",
    val currentSongCover: String? = null,
    val duration: Long = 0L,
    val currentPosition: Long = 0L,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val isShuffleEnabled: Boolean = false,
    val playbackSpeed: Float = 1.0f,
    val isBuffering: Boolean = false,
    val error: String? = null
) {
    val progress: Float
        get() = if (duration > 0) currentPosition.toFloat() / duration else 0f
    
    val hasNext: Boolean
        get() = true // 简化处理，实际根据播放列表判断
    
    val hasPrevious: Boolean
        get() = true // 简化处理，实际根据播放列表判断
}

/**
 * 播放器事件
 */
sealed class PlayerEvent {
    data class SongChanged(val songId: String) : PlayerEvent()
    data class PlaybackStateChanged(val isPlaying: Boolean) : PlayerEvent()
    data class PositionChanged(val position: Long) : PlayerEvent()
    data class DurationChanged(val duration: Long) : PlayerEvent()
    data class Error(val message: String) : PlayerEvent()
    data object PlaybackCompleted : PlayerEvent()
}
