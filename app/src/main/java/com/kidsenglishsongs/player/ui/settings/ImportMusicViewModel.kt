package com.kidsenglishsongs.player.ui.settings

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidsenglishsongs.player.data.entity.SongEntity
import com.kidsenglishsongs.player.data.repository.SongRepository
import com.kidsenglishsongs.player.util.AudioMetadataReader
import com.kidsenglishsongs.player.util.FileHashUtils
import com.kidsenglishsongs.player.util.LocalMusicScanner
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ImportMusicViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val songRepository: SongRepository,
    private val localMusicScanner: LocalMusicScanner,
    private val fileHashUtils: FileHashUtils,
    private val audioMetadataReader: AudioMetadataReader
) : ViewModel() {
    
    /**
     * 导入状态
     */
    sealed class ImportState {
        object Idle : ImportState()
        object Scanning : ImportState()
        data class ScanProgress(val current: Int, val total: Int) : ImportState()
        data class ScanCompleted(val items: List<LocalMusicScanner.LocalMusicItem>, val duplicateCount: Int) : ImportState()
        data class Importing(val current: Int, val total: Int, val currentSong: String) : ImportState()
        data class ImportCompleted(val successCount: Int, val failCount: Int, val duplicateCount: Int) : ImportState()
        data class Error(val message: String) : ImportState()
    }
    
    private val _importState = MutableStateFlow<ImportState>(ImportState.Idle)
    val importState: StateFlow<ImportState> = _importState.asStateFlow()
    
    // 扫描到的音乐列表
    private val _scannedMusic = MutableStateFlow<List<LocalMusicScanner.LocalMusicItem>>(emptyList())
    val scannedMusic: StateFlow<List<LocalMusicScanner.LocalMusicItem>> = _scannedMusic.asStateFlow()
    
    // 选中的音乐ID
    private val _selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedIds: StateFlow<Set<Long>> = _selectedIds.asStateFlow()
    
    // 已存在的哈希值（用于标记重复）
    private val _existingHashes = MutableStateFlow<Set<String>>(emptySet())
    
    // 重复文件的ID
    private val _duplicateIds = MutableStateFlow<Set<Long>>(emptySet())
    val duplicateIds: StateFlow<Set<Long>> = _duplicateIds.asStateFlow()
    
    /**
     * 扫描本机音乐
     */
    fun scanLocalMusic() {
        viewModelScope.launch {
            // 先获取已存在的哈希值
            val existingHashes = songRepository.getAllFileHashes().toSet()
            _existingHashes.value = existingHashes
            
            localMusicScanner.scanAllMusic(context).collect { state ->
                when (state) {
                    is LocalMusicScanner.ScanState.Idle -> {
                        _importState.value = ImportState.Idle
                    }
                    is LocalMusicScanner.ScanState.Scanning -> {
                        _importState.value = ImportState.Scanning
                    }
                    is LocalMusicScanner.ScanState.Progress -> {
                        _importState.value = ImportState.ScanProgress(state.current, state.total)
                    }
                    is LocalMusicScanner.ScanState.Completed -> {
                        _scannedMusic.value = state.items
                        
                        // 检查重复（需要计算哈希）
                        checkDuplicates(state.items)
                    }
                    is LocalMusicScanner.ScanState.Error -> {
                        _importState.value = ImportState.Error(state.message)
                    }
                }
            }
        }
    }
    
    /**
     * 检查重复文件
     */
    private suspend fun checkDuplicates(items: List<LocalMusicScanner.LocalMusicItem>) {
        withContext(Dispatchers.IO) {
            val duplicates = mutableSetOf<Long>()
            val existingHashes = _existingHashes.value
            
            items.forEach { item ->
                val hash = fileHashUtils.calculateMD5(item.path)
                if (hash != null && existingHashes.contains(hash)) {
                    duplicates.add(item.id)
                }
            }
            
            _duplicateIds.value = duplicates
            _importState.value = ImportState.ScanCompleted(items, duplicates.size)
        }
    }
    
    /**
     * 切换选择
     */
    fun toggleSelection(id: Long) {
        _selectedIds.update { currentIds ->
            if (currentIds.contains(id)) {
                currentIds - id
            } else {
                currentIds + id
            }
        }
    }
    
    /**
     * 全选/取消全选
     */
    fun selectAll(selectAll: Boolean) {
        if (selectAll) {
            // 全选（排除重复的）
            val nonDuplicateIds = _scannedMusic.value
                .filter { !_duplicateIds.value.contains(it.id) }
                .map { it.id }
                .toSet()
            _selectedIds.value = nonDuplicateIds
        } else {
            _selectedIds.value = emptySet()
        }
    }
    
    /**
     * 导入选中的歌曲
     */
    fun importSelectedSongs() {
        viewModelScope.launch {
            val selectedItems = _scannedMusic.value.filter { _selectedIds.value.contains(it.id) }
            
            if (selectedItems.isEmpty()) {
                _importState.value = ImportState.Error("请先选择要导入的歌曲")
                return@launch
            }
            
            var successCount = 0
            var failCount = 0
            var duplicateCount = 0
            
            selectedItems.forEachIndexed { index, item ->
                _importState.value = ImportState.Importing(
                    current = index + 1,
                    total = selectedItems.size,
                    currentSong = item.title
                )
                
                try {
                    val result = importSingleSong(item)
                    when (result) {
                        ImportResult.SUCCESS -> successCount++
                        ImportResult.DUPLICATE -> duplicateCount++
                        ImportResult.FAILED -> failCount++
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    failCount++
                }
            }
            
            _importState.value = ImportState.ImportCompleted(successCount, failCount, duplicateCount)
            
            // 清空选择
            _selectedIds.value = emptySet()
        }
    }
    
    /**
     * 导入单首歌曲
     */
    private suspend fun importSingleSong(item: LocalMusicScanner.LocalMusicItem): ImportResult = withContext(Dispatchers.IO) {
        try {
            // 计算文件哈希
            val fileHash = fileHashUtils.calculateMD5(item.path)
            
            // 检查是否已存在
            if (fileHash != null && songRepository.existsByHash(fileHash)) {
                return@withContext ImportResult.DUPLICATE
            }
            
            // 复制文件到应用私有目录
            val sourceFile = File(item.path)
            if (!sourceFile.exists()) {
                return@withContext ImportResult.FAILED
            }
            
            val fileName = sourceFile.name
            val destFile = File(context.filesDir, "songs/$fileName")
            
            // 如果目标文件已存在，添加时间戳
            val finalDestFile = if (destFile.exists()) {
                val nameWithoutExt = fileName.substringBeforeLast('.')
                val ext = fileName.substringAfterLast('.', "")
                File(context.filesDir, "songs/${nameWithoutExt}_${System.currentTimeMillis()}.$ext")
            } else {
                destFile
            }
            
            finalDestFile.parentFile?.mkdirs()
            
            // 复制文件
            sourceFile.inputStream().use { input ->
                FileOutputStream(finalDestFile).use { output ->
                    input.copyTo(output)
                }
            }
            
            // 创建歌曲实体
            val song = SongEntity(
                id = UUID.randomUUID().toString(),
                title = item.title,
                filePath = finalDestFile.absolutePath,
                duration = item.duration,
                createdAt = System.currentTimeMillis(),
                fileHash = fileHash,
                artist = item.artist,
                album = item.album,
                fileSize = item.size
            )
            
            songRepository.insertSong(song)
            
            // 更新已存在的哈希集合
            if (fileHash != null) {
                _existingHashes.update { it + fileHash }
            }
            
            ImportResult.SUCCESS
            
        } catch (e: Exception) {
            e.printStackTrace()
            ImportResult.FAILED
        }
    }
    
    /**
     * 从文件选择器导入（支持单个文件或文件夹）
     */
    fun importFromUri(uris: List<Uri>) {
        viewModelScope.launch {
            val allUris = mutableListOf<Uri>()
            
            // 处理每个 URI，如果是目录则递归获取所有音频文件
            uris.forEach { uri ->
                val documentFile = DocumentFile.fromSingleUri(context, uri)
                    ?: DocumentFile.fromTreeUri(context, uri)
                
                if (documentFile != null) {
                    if (documentFile.isDirectory) {
                        collectAudioFilesFromDirectory(documentFile, allUris)
                    } else if (isAudioFile(documentFile)) {
                        allUris.add(uri)
                    }
                }
            }
            
            if (allUris.isEmpty()) {
                _importState.value = ImportState.Error("未找到音频文件")
                return@launch
            }
            
            importFromUriList(allUris)
        }
    }
    
    /**
     * 从目录中递归收集音频文件
     */
    private fun collectAudioFilesFromDirectory(directory: DocumentFile, result: MutableList<Uri>) {
        directory.listFiles().forEach { file ->
            if (file.isDirectory) {
                collectAudioFilesFromDirectory(file, result)
            } else if (isAudioFile(file)) {
                result.add(file.uri)
            }
        }
    }
    
    /**
     * 检查是否是音频文件
     */
    private fun isAudioFile(file: DocumentFile): Boolean {
        val mimeType = file.type ?: return false
        if (mimeType.startsWith("audio/")) return true
        
        val name = file.name ?: return false
        val ext = name.substringAfterLast('.', "").lowercase()
        return ext in listOf("mp3", "m4a", "wav", "flac", "ogg", "aac", "wma")
    }
    
    /**
     * 导入 URI 列表
     */
    private suspend fun importFromUriList(uris: List<Uri>) {
        var successCount = 0
        var failCount = 0
        var duplicateCount = 0
        
        uris.forEachIndexed { index, uri ->
            _importState.value = ImportState.Importing(
                current = index + 1,
                total = uris.size,
                currentSong = uri.lastPathSegment ?: "未知"
            )
            
            try {
                val result = importFromSingleUri(uri)
                when (result) {
                    ImportResult.SUCCESS -> successCount++
                    ImportResult.DUPLICATE -> duplicateCount++
                    ImportResult.FAILED -> failCount++
                }
            } catch (e: Exception) {
                e.printStackTrace()
                failCount++
            }
        }
        
        _importState.value = ImportState.ImportCompleted(successCount, failCount, duplicateCount)
    }
    
    /**
     * 从单个 URI 导入
     */
    private suspend fun importFromSingleUri(uri: Uri): ImportResult = withContext(Dispatchers.IO) {
        try {
            // 计算哈希
            val fileHash = fileHashUtils.calculateMD5(context, uri)
            
            // 检查是否已存在
            if (fileHash != null && songRepository.existsByHash(fileHash)) {
                return@withContext ImportResult.DUPLICATE
            }
            
            // 读取元数据并复制文件
            val metadata = audioMetadataReader.readMetadata(context, uri)
                ?: return@withContext ImportResult.FAILED
            
            // 创建歌曲实体
            val song = SongEntity(
                id = UUID.randomUUID().toString(),
                title = metadata.title ?: uri.lastPathSegment ?: "未知歌曲",
                filePath = metadata.filePath,
                duration = metadata.duration,
                createdAt = System.currentTimeMillis(),
                fileHash = fileHash,
                artist = metadata.artist,
                album = metadata.album
            )
            
            songRepository.insertSong(song)
            
            ImportResult.SUCCESS
            
        } catch (e: Exception) {
            e.printStackTrace()
            ImportResult.FAILED
        }
    }
    
    /**
     * 重置状态
     */
    fun resetState() {
        _importState.value = ImportState.Idle
        _scannedMusic.value = emptyList()
        _selectedIds.value = emptySet()
        _duplicateIds.value = emptySet()
    }
    
    /**
     * 导入结果
     */
    private enum class ImportResult {
        SUCCESS, DUPLICATE, FAILED
    }
}
