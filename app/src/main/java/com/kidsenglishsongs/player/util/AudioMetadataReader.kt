package com.kidsenglishsongs.player.util

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 音频文件元数据读取器
 */
@Singleton
class AudioMetadataReader @Inject constructor() {
    
    /**
     * 音频文件元数据
     */
    data class AudioMetadata(
        val title: String?,
        val artist: String?,
        val album: String?,
        val duration: Long,
        val filePath: String
    )
    
    /**
     * 从 URI 读取音频元数据
     */
    fun readMetadata(context: Context, uri: Uri): AudioMetadata? {
        return try {
            // 复制文件到应用私有目录
            val fileName = getFileName(context, uri) ?: "audio_${System.currentTimeMillis()}"
            val destFile = File(context.filesDir, "songs/$fileName")
            destFile.parentFile?.mkdirs()
            
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            
            // 读取元数据
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(destFile.absolutePath)
            
            val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            val album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val duration = durationStr?.toLongOrNull() ?: 0L
            
            retriever.release()
            
            AudioMetadata(
                title = title ?: getFileNameWithoutExtension(fileName),
                artist = artist,
                album = album,
                duration = duration,
                filePath = destFile.absolutePath
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * 从 URI 获取文件名
     */
    private fun getFileName(context: Context, uri: Uri): String? {
        var result: String? = null
        
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index >= 0) {
                        result = cursor.getString(index)
                    }
                }
            }
        }
        
        if (result == null) {
            result = uri.path?.let { path ->
                val cut = path.lastIndexOf('/')
                if (cut != -1) path.substring(cut + 1) else path
            }
        }
        
        return result
    }
    
    /**
     * 获取不带扩展名的文件名
     */
    private fun getFileNameWithoutExtension(fileName: String): String {
        val dotIndex = fileName.lastIndexOf('.')
        return if (dotIndex > 0) fileName.substring(0, dotIndex) else fileName
    }
}
