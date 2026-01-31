package com.kidsenglishsongs.player.util

import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import javax.inject.Inject
import javax.inject.Singleton

/**
 * LRC 歌词解析器
 */
@Singleton
class LrcParser @Inject constructor() {
    
    /**
     * 歌词行数据
     */
    data class LyricLine(
        val timeMs: Long,      // 时间（毫秒）
        val text: String       // 歌词文本
    )
    
    /**
     * 解析结果
     */
    data class LrcData(
        val title: String?,
        val artist: String?,
        val album: String?,
        val lines: List<LyricLine>
    )
    
    /**
     * 从文件解析 LRC 歌词
     */
    fun parse(file: File): LrcData? {
        return try {
            val content = file.readText()
            parse(content)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * 从文本内容解析 LRC 歌词
     */
    fun parse(content: String): LrcData {
        var title: String? = null
        var artist: String? = null
        var album: String? = null
        val lines = mutableListOf<LyricLine>()
        
        val timeRegex = Regex("""\[(\d{2}):(\d{2})\.(\d{2,3})](.*)""")
        val metaRegex = Regex("""\[(ti|ar|al):(.+)]""")
        
        content.lines().forEach { line ->
            // 尝试解析元数据
            metaRegex.find(line)?.let { match ->
                val key = match.groupValues[1]
                val value = match.groupValues[2].trim()
                when (key) {
                    "ti" -> title = value
                    "ar" -> artist = value
                    "al" -> album = value
                }
                return@forEach
            }
            
            // 尝试解析歌词行
            timeRegex.find(line)?.let { match ->
                val minutes = match.groupValues[1].toLongOrNull() ?: 0
                val seconds = match.groupValues[2].toLongOrNull() ?: 0
                val millisStr = match.groupValues[3]
                val millis = when (millisStr.length) {
                    2 -> (millisStr.toLongOrNull() ?: 0) * 10
                    3 -> millisStr.toLongOrNull() ?: 0
                    else -> 0
                }
                val text = match.groupValues[4].trim()
                
                if (text.isNotEmpty()) {
                    val timeMs = minutes * 60 * 1000 + seconds * 1000 + millis
                    lines.add(LyricLine(timeMs, text))
                }
            }
            
            // 处理多时间标签的情况，如 [00:01.00][00:30.00]歌词
            val multiTimeRegex = Regex("""\[(\d{2}):(\d{2})\.(\d{2,3})]""")
            val matches = multiTimeRegex.findAll(line).toList()
            if (matches.size > 1) {
                val text = line.replace(multiTimeRegex, "").trim()
                if (text.isNotEmpty()) {
                    matches.forEach { match ->
                        val minutes = match.groupValues[1].toLongOrNull() ?: 0
                        val seconds = match.groupValues[2].toLongOrNull() ?: 0
                        val millisStr = match.groupValues[3]
                        val millis = when (millisStr.length) {
                            2 -> (millisStr.toLongOrNull() ?: 0) * 10
                            3 -> millisStr.toLongOrNull() ?: 0
                            else -> 0
                        }
                        val timeMs = minutes * 60 * 1000 + seconds * 1000 + millis
                        lines.add(LyricLine(timeMs, text))
                    }
                }
            }
        }
        
        // 按时间排序
        lines.sortBy { it.timeMs }
        
        return LrcData(
            title = title,
            artist = artist,
            album = album,
            lines = lines
        )
    }
    
    /**
     * 根据当前播放时间获取当前歌词行索引
     */
    fun getCurrentLineIndex(lines: List<LyricLine>, currentTimeMs: Long): Int {
        if (lines.isEmpty()) return -1
        
        for (i in lines.indices.reversed()) {
            if (lines[i].timeMs <= currentTimeMs) {
                return i
            }
        }
        
        return -1
    }
}
