package com.kidsenglishsongs.player.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kidsenglishsongs.player.ui.theme.*

/**
 * 设置界面 - 网易云风格
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToImportMusic: () -> Unit = {}
) {
    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background,
                    titleContentColor = OnBackground,
                    navigationIconContentColor = OnBackground
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 音乐管理
            item {
                Text(
                    text = "音乐管理",
                    style = MaterialTheme.typography.titleSmall,
                    color = Primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.LibraryMusic,
                    title = "导入音乐",
                    subtitle = "从本机扫描或选择文件夹导入",
                    onClick = onNavigateToImportMusic
                )
            }
            
            // 播放设置
            item {
                Text(
                    text = "播放设置",
                    style = MaterialTheme.typography.titleSmall,
                    color = Primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Timer,
                    title = "默认睡眠定时",
                    subtitle = "设置默认的睡眠定时时长",
                    onClick = { /* TODO */ }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Speed,
                    title = "播放速度",
                    subtitle = "调整默认播放速度",
                    onClick = { /* TODO */ }
                )
            }
            
            // 界面设置
            item {
                Text(
                    text = "界面设置",
                    style = MaterialTheme.typography.titleSmall,
                    color = Primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.GridView,
                    title = "歌曲显示方式",
                    subtitle = "列表 / 网格",
                    onClick = { /* TODO */ }
                )
            }
            
            // 数据管理
            item {
                Text(
                    text = "数据管理",
                    style = MaterialTheme.typography.titleSmall,
                    color = Primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.History,
                    title = "清除播放历史",
                    subtitle = "删除所有播放记录",
                    onClick = { /* TODO */ }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.DeleteSweep,
                    title = "清除所有数据",
                    subtitle = "删除所有歌曲和设置",
                    onClick = { /* TODO */ },
                    isDangerous = true
                )
            }
            
            // 关于
            item {
                Text(
                    text = "关于",
                    style = MaterialTheme.typography.titleSmall,
                    color = Primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "版本",
                    subtitle = "1.0.0",
                    onClick = { }
                )
            }
        }
    }
}

@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isDangerous: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Surface
        )
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
                tint = if (isDangerous) Error else Primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isDangerous) Error else OnSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = OnSurfaceVariant
            )
        }
    }
}
