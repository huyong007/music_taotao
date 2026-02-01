package com.kidsenglishsongs.player.util

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 文件哈希工具类
 * 用于计算文件的 MD5 哈希值，实现去重
 */
@Singleton
class FileHashUtils @Inject constructor() {
    
    /**
     * 计算文件的 MD5 哈希值
     */
    suspend fun calculateMD5(file: File): String? = withContext(Dispatchers.IO) {
        try {
            FileInputStream(file).use { inputStream ->
                calculateMD5FromStream(inputStream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * 从 URI 计算 MD5 哈希值
     */
    suspend fun calculateMD5(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                calculateMD5FromStream(inputStream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * 从文件路径计算 MD5 哈希值
     */
    suspend fun calculateMD5(filePath: String): String? = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (file.exists()) {
                calculateMD5(file)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * 从输入流计算 MD5 哈希值
     */
    private fun calculateMD5FromStream(inputStream: InputStream): String {
        val digest = MessageDigest.getInstance("MD5")
        val buffer = ByteArray(8192)
        var bytesRead: Int
        
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            digest.update(buffer, 0, bytesRead)
        }
        
        val hashBytes = digest.digest()
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
    
    /**
     * 快速哈希：只读取文件的头部和尾部
     * 用于大文件的快速去重判断
     */
    suspend fun calculateQuickHash(file: File, chunkSize: Int = 64 * 1024): String? = withContext(Dispatchers.IO) {
        try {
            val fileSize = file.length()
            
            // 如果文件小于 chunk 大小的两倍，直接计算完整哈希
            if (fileSize <= chunkSize * 2) {
                return@withContext calculateMD5(file)
            }
            
            val digest = MessageDigest.getInstance("MD5")
            
            FileInputStream(file).use { fis ->
                val buffer = ByteArray(chunkSize)
                
                // 读取文件头部
                val headBytesRead = fis.read(buffer)
                if (headBytesRead > 0) {
                    digest.update(buffer, 0, headBytesRead)
                }
                
                // 跳到文件尾部
                fis.channel.position(fileSize - chunkSize)
                
                // 读取文件尾部
                val tailBytesRead = fis.read(buffer)
                if (tailBytesRead > 0) {
                    digest.update(buffer, 0, tailBytesRead)
                }
                
                // 添加文件大小作为哈希的一部分
                digest.update(fileSize.toString().toByteArray())
            }
            
            val hashBytes = digest.digest()
            hashBytes.joinToString("") { "%02x".format(it) }
            
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * 批量计算哈希值
     */
    suspend fun calculateHashes(files: List<File>): Map<File, String?> = withContext(Dispatchers.IO) {
        files.associateWith { file ->
            calculateMD5(file)
        }
    }
}
