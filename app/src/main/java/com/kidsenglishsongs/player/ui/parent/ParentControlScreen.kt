package com.kidsenglishsongs.player.ui.parent

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.kidsenglishsongs.player.ui.theme.*
import kotlin.random.Random

/**
 * 家长控制界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentControlScreen(
    onNavigateBack: () -> Unit,
    viewModel: ParentControlViewModel = hiltViewModel()
) {
    var showVerificationDialog by remember { mutableStateOf(true) }
    var isVerified by remember { mutableStateOf(false) }
    
    val songs by viewModel.songs.collectAsState()
    val groups by viewModel.groups.collectAsState()
    val tags by viewModel.tags.collectAsState()
    val statistics by viewModel.statistics.collectAsState()
    
    // 验证对话框
    if (showVerificationDialog && !isVerified) {
        ParentVerificationDialog(
            onVerified = {
                isVerified = true
                showVerificationDialog = false
            },
            onDismiss = onNavigateBack
        )
    }
    
    if (!isVerified) return
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("家长控制") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = OnPrimary,
                    navigationIconContentColor = OnPrimary
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 统计信息卡片
            item {
                StatisticsCard(statistics = statistics)
            }
            
            // 分组管理
            item {
                ManagementCard(
                    title = "分组管理",
                    icon = Icons.Default.Folder,
                    itemCount = groups.size,
                    onAddClick = { viewModel.showCreateGroupDialog() },
                    onManageClick = { /* TODO: 导航到分组管理 */ }
                )
            }
            
            // 标签管理
            item {
                ManagementCard(
                    title = "标签管理",
                    icon = Icons.Default.Label,
                    itemCount = tags.size,
                    onAddClick = { viewModel.showCreateTagDialog() },
                    onManageClick = { /* TODO: 导航到标签管理 */ }
                )
            }
            
            // 歌曲管理标题
            item {
                Text(
                    text = "歌曲管理 (${songs.size}首)",
                    style = MaterialTheme.typography.titleMedium,
                    color = OnSurface
                )
            }
            
            // 歌曲列表 - 可删除
            items(songs, key = { it.id }) { song ->
                SongManagementItem(
                    title = song.title,
                    playCount = song.playCount,
                    onDeleteClick = { viewModel.showDeleteConfirmDialog(song) }
                )
            }
        }
    }
    
    // 创建分组对话框
    if (viewModel.showCreateGroupDialog.collectAsState().value) {
        CreateGroupDialog(
            onDismiss = { viewModel.hideCreateGroupDialog() },
            onCreate = { name -> viewModel.createGroup(name) }
        )
    }
    
    // 创建标签对话框
    if (viewModel.showCreateTagDialog.collectAsState().value) {
        CreateTagDialog(
            onDismiss = { viewModel.hideCreateTagDialog() },
            onCreate = { name, color -> viewModel.createTag(name, color) }
        )
    }
    
    // 删除确认对话框
    viewModel.songToDelete.collectAsState().value?.let { song ->
        DeleteConfirmDialog(
            songTitle = song.title,
            onDismiss = { viewModel.hideDeleteConfirmDialog() },
            onConfirm = { viewModel.deleteSong(song) }
        )
    }
}

@Composable
private fun ParentVerificationDialog(
    onVerified: () -> Unit,
    onDismiss: () -> Unit
) {
    var answer by remember { mutableStateOf("") }
    val num1 = remember { Random.nextInt(1, 10) }
    val num2 = remember { Random.nextInt(1, 10) }
    val correctAnswer = num1 + num2
    var isError by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("家长验证") },
        text = {
            Column {
                Text("请计算以下算式来验证您是家长：")
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "$num1 + $num2 = ?",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = answer,
                    onValueChange = {
                        answer = it
                        isError = false
                    },
                    label = { Text("答案") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = isError,
                    singleLine = true
                )
                
                if (isError) {
                    Text(
                        text = "答案错误，请重试",
                        color = Error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (answer.toIntOrNull() == correctAnswer) {
                        onVerified()
                    } else {
                        isError = true
                    }
                }
            ) {
                Text("确认")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun StatisticsCard(
    statistics: ParentControlViewModel.Statistics
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PrimaryLight.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "播放统计",
                style = MaterialTheme.typography.titleMedium,
                color = OnSurface
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatItem(label = "歌曲数", value = "${statistics.songCount}")
                StatItem(label = "播放次数", value = "${statistics.totalPlayCount}")
                StatItem(label = "总时长", value = formatDuration(statistics.totalDuration))
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = Primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = OnSurfaceVariant
        )
    }
}

@Composable
private fun ManagementCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    itemCount: Int,
    onAddClick: () -> Unit,
    onManageClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "$itemCount 个",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
            }
            
            IconButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "添加")
            }
        }
    }
}

@Composable
private fun SongManagementItem(
    title: String,
    playCount: Int,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = Primary
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "播放 $playCount 次",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
            }
            
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = Error
                )
            }
        }
    }
}

@Composable
private fun CreateGroupDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("创建分组") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("分组名称") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onCreate(name) },
                enabled = name.isNotBlank()
            ) {
                Text("创建")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun CreateTagDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#4FC3F7") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("创建标签") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("标签名称") },
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("选择颜色", style = MaterialTheme.typography.bodyMedium)
                
                Spacer(modifier = Modifier.height(8.dp))
                
                com.kidsenglishsongs.player.ui.components.TagColorPicker(
                    selectedColor = selectedColor,
                    onColorSelected = { selectedColor = it }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onCreate(name, selectedColor) },
                enabled = name.isNotBlank()
            ) {
                Text("创建")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun DeleteConfirmDialog(
    songTitle: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("确认删除") },
        text = { Text("确定要删除歌曲 \"$songTitle\" 吗？此操作不可撤销。") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Error)
            ) {
                Text("删除")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

private fun formatDuration(millis: Long): String {
    val hours = millis / (1000 * 60 * 60)
    val minutes = (millis / (1000 * 60)) % 60
    
    return if (hours > 0) {
        "${hours}小时${minutes}分"
    } else {
        "${minutes}分钟"
    }
}
