package com.kidsenglishsongs.player.util

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 本机音乐扫描器
 * 使用 MediaStore 扫描设备上所有音频文件
 */
@Singleton
class LocalMusicScanner @Inject constructor() {
    
    /**
     * 本地音乐项目
     */
    data class LocalMusicItem(
        val id: Long,
        val title: String,
        val artist: String?,
        val album: String?,
        val duration: Long,
        val size: Long,
        val path: String,
        val uri: Uri,
        val mimeType: String?,
        val dateAdded: Long
    )
    
    /**
     * 扫描状态
     */
    sealed class ScanState {
        object Idle : ScanState()
        object Scanning : ScanState()
        data class Progress(val current: Int, val total: Int) : ScanState()
        data class Completed(val items: List<LocalMusicItem>) : ScanState()
        data class Error(val message: String) : ScanState()
    }
    
    /**
     * 支持的音频格式
     */
    private val supportedMimeTypes = setOf(
        "audio/mpeg",       // mp3
        "audio/mp4",        // m4a
        "audio/x-m4a",      // m4a
        "audio/wav",        // wav
        "audio/x-wav",      // wav
        "audio/flac",       // flac
        "audio/ogg",        // ogg
        "audio/vorbis",     // ogg
        "audio/aac"         // aac
    )
    
    /**
     * 支持的文件扩展名
     */
    private val supportedExtensions = setOf(
        "mp3", "m4a", "wav", "flac", "ogg", "aac", "wma"
    )
    
    /**
     * 扫描本机所有音乐
     */
    fun scanAllMusic(context: Context): Flow<ScanState> = flow {
        emit(ScanState.Scanning)
        
        try {
            val musicList = mutableListOf<LocalMusicItem>()
            
            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }
            
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.MIME_TYPE,
                MediaStore.Audio.Media.DATE_ADDED
            )
            
            // 过滤条件：排除太短的音频（如通知音）
            val selection = "${MediaStore.Audio.Media.DURATION} > ?"
            val selectionArgs = arrayOf("30000") // 大于30秒
            
            val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"
            
            context.contentResolver.query(
                collection,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                val totalCount = cursor.count
                var currentIndex = 0
                
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
                val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
                
                while (cursor.moveToNext()) {
                    currentIndex++
                    
                    // 每扫描10个更新一次进度
                    if (currentIndex % 10 == 0) {
                        emit(ScanState.Progress(currentIndex, totalCount))
                    }
                    
                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn) ?: "未知歌曲"
                    val artist = cursor.getStringOrNull(artistColumn)
                    val album = cursor.getStringOrNull(albumColumn)
                    val duration = cursor.getLong(durationColumn)
                    val size = cursor.getLong(sizeColumn)
                    val path = cursor.getString(dataColumn) ?: continue
                    val mimeType = cursor.getStringOrNull(mimeTypeColumn)
                    val dateAdded = cursor.getLong(dateAddedColumn)
                    
                    // 检查是否是支持的格式
                    if (!isSupportedFormat(path, mimeType)) {
                        continue
                    }
                    
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    )
                    
                    musicList.add(
                        LocalMusicItem(
                            id = id,
                            title = title,
                            artist = artist,
                            album = album,
                            duration = duration,
                            size = size,
                            path = path,
                            uri = contentUri,
                            mimeType = mimeType,
                            dateAdded = dateAdded
                        )
                    )
                }
            }
            
            emit(ScanState.Completed(musicList))
            
        } catch (e: Exception) {
            emit(ScanState.Error(e.message ?: "扫描失败"))
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * 检查是否是支持的格式
     */
    private fun isSupportedFormat(path: String, mimeType: String?): Boolean {
        // 检查 MIME 类型
        if (mimeType != null && supportedMimeTypes.contains(mimeType.lowercase())) {
            return true
        }
        
        // 检查文件扩展名
        val extension = path.substringAfterLast('.', "").lowercase()
        return supportedExtensions.contains(extension)
    }
    
    /**
     * 安全获取字符串，处理 "<unknown>" 等占位符
     */
    private fun Cursor.getStringOrNull(columnIndex: Int): String? {
        val value = getString(columnIndex)
        return if (value.isNullOrBlank() || value == "<unknown>") null else value
    }
}
