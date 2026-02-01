package com.kidsenglishsongs.player.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.kidsenglishsongs.player.ui.theme.*
import com.kidsenglishsongs.player.util.LocalMusicScanner

/**
 * 导入音乐界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportMusicScreen(
    onNavigateBack: () -> Unit,
    viewModel: ImportMusicViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val importState by viewModel.importState.collectAsState()
    val scannedMusic by viewModel.scannedMusic.collectAsState()
    val selectedIds by viewModel.selectedIds.collectAsState()
    val duplicateIds by viewModel.duplicateIds.collectAsState()
    
    // 权限请求
    var hasPermission by remember { mutableStateOf(false) }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (granted) {
            viewModel.scanLocalMusic()
        }
    }
    
    // 文件选择器（单个/多个文件）
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.importFromUri(uris)
        }
    }
    
    // 文件夹选择器
    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            viewModel.importFromUri(listOf(uri))
        }
    }
    
    // 检查权限
    LaunchedEffect(Unit) {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        
        hasPermission = ContextCompat.checkSelfPermission(
            context, permission
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("导入音乐") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    // 全选/取消全选按钮
                    if (scannedMusic.isNotEmpty() && importState is ImportMusicViewModel.ImportState.ScanCompleted) {
                        val allSelected = selectedIds.size == scannedMusic.size - duplicateIds.size
                        IconButton(onClick = { viewModel.selectAll(!allSelected) }) {
                            Icon(
                                if (allSelected) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                contentDescription = if (allSelected) "取消全选" else "全选"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = OnPrimary,
                    navigationIconContentColor = OnPrimary,
                    actionIconContentColor = OnPrimary
                )
            )
        },
        bottomBar = {
            // 底部操作栏
            AnimatedVisibility(
                visible = selectedIds.isNotEmpty() && importState is ImportMusicViewModel.ImportState.ScanCompleted
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "已选择 ${selectedIds.size} 首",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        
                        Button(
                            onClick = { viewModel.importSelectedSongs() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Secondary
                            )
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("导入")
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = importState) {
                is ImportMusicViewModel.ImportState.Idle -> {
                    // 初始状态 - 显示导入选项
                    ImportOptionsContent(
                        hasPermission = hasPermission,
                        onScanLocalMusic = {
                            val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                Manifest.permission.READ_MEDIA_AUDIO
                            } else {
                                Manifest.permission.READ_EXTERNAL_STORAGE
                            }
                            
                            if (hasPermission) {
                                viewModel.scanLocalMusic()
                            } else {
                                permissionLauncher.launch(permission)
                            }
                        },
                        onSelectFiles = {
                            filePickerLauncher.launch(arrayOf("audio/*"))
                        },
                        onSelectFolder = {
                            folderPickerLauncher.launch(null)
                        }
                    )
                }
                
                is ImportMusicViewModel.ImportState.Scanning,
                is ImportMusicViewModel.ImportState.ScanProgress -> {
                    // 扫描中
                    ScanningContent(state)
                }
                
                is ImportMusicViewModel.ImportState.ScanCompleted -> {
                    // 扫描完成 - 显示音乐列表
                    if (scannedMusic.isEmpty()) {
                        EmptyContent(
                            message = "未找到音乐文件",
                            onRetry = { viewModel.resetState() }
                        )
                    } else {
                        MusicListContent(
                            musicList = scannedMusic,
                            selectedIds = selectedIds,
                            duplicateIds = duplicateIds,
                            onToggleSelection = { viewModel.toggleSelection(it) }
                        )
                    }
                }
                
                is ImportMusicViewModel.ImportState.Importing -> {
                    // 导入中
                    ImportingContent(state)
                }
                
                is ImportMusicViewModel.ImportState.ImportCompleted -> {
                    // 导入完成
                    ImportCompletedContent(
                        state = state,
                        onDone = onNavigateBack,
                        onContinue = { viewModel.resetState() }
                    )
                }
                
                is ImportMusicViewModel.ImportState.Error -> {
                    // 错误
                    ErrorContent(
                        message = state.message,
                        onRetry = { viewModel.resetState() }
                    )
                }
            }
        }
    }
}

/**
 * 导入选项内容
 */
@Composable
private fun ImportOptionsContent(
    hasPermission: Boolean,
    onScanLocalMusic: () -> Unit,
    onSelectFiles: () -> Unit,
    onSelectFolder: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "选择导入方式",
            style = MaterialTheme.typography.titleMedium,
            color = Primary
        )
        
        // 扫描本机音乐
        ImportOptionCard(
            icon = Icons.Default.LibraryMusic,
            title = "扫描本机音乐",
            description = "扫描手机上的所有音乐文件",
            onClick = onScanLocalMusic
        )
        
        // 选择文件
        ImportOptionCard(
            icon = Icons.Default.AudioFile,
            title = "选择文件导入",
            description = "从文件管理器选择音频文件",
            onClick = onSelectFiles
        )
        
        // 选择文件夹
        ImportOptionCard(
            icon = Icons.Default.Folder,
            title = "选择文件夹导入",
            description = "导入整个文件夹中的音乐",
            onClick = onSelectFolder
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // 提示信息
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = Primary
                )
                Column {
                    Text(
                        text = "支持的格式",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurface
                    )
                    Text(
                        text = "MP3、M4A、WAV、FLAC、OGG、AAC",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * 导入选项卡片
 */
@Composable
private fun ImportOptionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
            }
            
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = OnSurfaceVariant
            )
        }
    }
}

/**
 * 扫描中内容
 */
@Composable
private fun ScanningContent(state: ImportMusicViewModel.ImportState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(64.dp),
            color = Primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "正在扫描...",
            style = MaterialTheme.typography.titleMedium
        )
        
        if (state is ImportMusicViewModel.ImportState.ScanProgress) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${state.current} / ${state.total}",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { state.current.toFloat() / state.total },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                color = Primary
            )
        }
    }
}

/**
 * 音乐列表内容
 */
@Composable
private fun MusicListContent(
    musicList: List<LocalMusicScanner.LocalMusicItem>,
    selectedIds: Set<Long>,
    duplicateIds: Set<Long>,
    onToggleSelection: (Long) -> Unit
) {
    Column {
        // 统计信息
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "共 ${musicList.size} 首歌曲",
                    style = MaterialTheme.typography.bodyMedium
                )
                if (duplicateIds.isNotEmpty()) {
                    Text(
                        text = "${duplicateIds.size} 首已存在",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Warning
                    )
                }
            }
        }
        
        // 音乐列表
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(musicList, key = { it.id }) { item ->
                val isDuplicate = duplicateIds.contains(item.id)
                val isSelected = selectedIds.contains(item.id)
                
                MusicListItem(
                    item = item,
                    isSelected = isSelected,
                    isDuplicate = isDuplicate,
                    onToggleSelection = { onToggleSelection(item.id) }
                )
            }
        }
    }
}

/**
 * 音乐列表项
 */
@Composable
private fun MusicListItem(
    item: LocalMusicScanner.LocalMusicItem,
    isSelected: Boolean,
    isDuplicate: Boolean,
    onToggleSelection: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isDuplicate, onClick = onToggleSelection)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 选择框
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onToggleSelection() },
            enabled = !isDuplicate,
            colors = CheckboxDefaults.colors(
                checkedColor = Secondary,
                disabledCheckedColor = OnSurfaceVariant,
                disabledUncheckedColor = OnSurfaceVariant
            )
        )
        
        // 歌曲图标
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (isDuplicate) OnSurfaceVariant.copy(alpha = 0.3f)
                    else Primary.copy(alpha = 0.1f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.MusicNote,
                contentDescription = null,
                tint = if (isDuplicate) OnSurfaceVariant else Primary
            )
        }
        
        // 歌曲信息
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isDuplicate) OnSurfaceVariant else OnSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                
                if (isDuplicate) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Warning.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "已存在",
                            style = MaterialTheme.typography.labelSmall,
                            color = Warning,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = item.artist ?: "未知艺术家",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "•",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
                Text(
                    text = formatDuration(item.duration),
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
            }
        }
    }
    
    HorizontalDivider(
        modifier = Modifier.padding(start = 76.dp),
        color = OnSurfaceVariant.copy(alpha = 0.1f)
    )
}

/**
 * 导入中内容
 */
@Composable
private fun ImportingContent(state: ImportMusicViewModel.ImportState.Importing) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(64.dp),
            color = Secondary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "正在导入...",
            style = MaterialTheme.typography.titleMedium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "${state.current} / ${state.total}",
            style = MaterialTheme.typography.bodyLarge,
            color = OnSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = state.currentSong,
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LinearProgressIndicator(
            progress = { state.current.toFloat() / state.total },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            color = Secondary
        )
    }
}

/**
 * 导入完成内容
 */
@Composable
private fun ImportCompletedContent(
    state: ImportMusicViewModel.ImportState.ImportCompleted,
    onDone: () -> Unit,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Success,
            modifier = Modifier.size(80.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "导入完成",
            style = MaterialTheme.typography.headlineSmall
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 统计信息
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (state.successCount > 0) {
                Text(
                    text = "成功导入 ${state.successCount} 首",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Success
                )
            }
            if (state.duplicateCount > 0) {
                Text(
                    text = "跳过 ${state.duplicateCount} 首重复歌曲",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Warning
                )
            }
            if (state.failCount > 0) {
                Text(
                    text = "失败 ${state.failCount} 首",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Error
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(onClick = onContinue) {
                Text("继续导入")
            }
            
            Button(
                onClick = onDone,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary
                )
            ) {
                Text("完成")
            }
        }
    }
}

/**
 * 空内容
 */
@Composable
private fun EmptyContent(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.MusicOff,
            contentDescription = null,
            tint = OnSurfaceVariant,
            modifier = Modifier.size(80.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = OnSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(onClick = onRetry) {
            Text("返回")
        }
    }
}

/**
 * 错误内容
 */
@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Error,
            contentDescription = null,
            tint = Error,
            modifier = Modifier.size(80.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = Error
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(onClick = onRetry) {
            Text("重试")
        }
    }
}

/**
 * 格式化时长
 */
private fun formatDuration(durationMs: Long): String {
    val seconds = (durationMs / 1000) % 60
    val minutes = (durationMs / 1000 / 60) % 60
    val hours = durationMs / 1000 / 60 / 60
    
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%d:%02d", minutes, seconds)
    }
}
