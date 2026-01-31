package com.kidsenglishsongs.player.ui.library

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kidsenglishsongs.player.data.entity.GroupEntity
import com.kidsenglishsongs.player.data.entity.SongEntity
import com.kidsenglishsongs.player.data.entity.TagEntity
import com.kidsenglishsongs.player.ui.components.*
import com.kidsenglishsongs.player.ui.theme.*

/**
 * 歌曲库界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onNavigateBack: () -> Unit,
    onNavigateToParentControl: () -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val songs by viewModel.filteredSongs.collectAsState()
    val groups by viewModel.groups.collectAsState()
    val tags by viewModel.tags.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedGroupId by viewModel.selectedGroupId.collectAsState()
    val selectedTagIds by viewModel.selectedTagIds.collectAsState()
    val currentPlayingSongId by viewModel.currentPlayingSongId.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    // 文件选择器
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            viewModel.importSongs(uris)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("歌曲库") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    // 搜索按钮
                    IconButton(onClick = { viewModel.toggleSearchMode() }) {
                        Icon(Icons.Default.Search, contentDescription = "搜索")
                    }
                    // 家长控制入口
                    IconButton(onClick = onNavigateToParentControl) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = "家长控制")
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    filePickerLauncher.launch(arrayOf("audio/*"))
                },
                containerColor = Secondary,
                contentColor = OnSecondary
            ) {
                Icon(Icons.Default.Add, contentDescription = "导入歌曲")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 搜索栏
            if (viewModel.isSearchMode.collectAsState().value) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { viewModel.setSearchQuery(it) },
                    onClose = { viewModel.toggleSearchMode() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
            
            // 分组筛选
            if (groups.isNotEmpty()) {
                GroupFilter(
                    groups = groups,
                    selectedGroupId = selectedGroupId,
                    onGroupSelected = { viewModel.setSelectedGroup(it) },
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            // 标签筛选
            if (tags.isNotEmpty()) {
                TagFilter(
                    tags = tags,
                    selectedTagIds = selectedTagIds,
                    onTagToggle = { viewModel.toggleTag(it) },
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            // 歌曲列表
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator(message = "加载中...")
                }
            } else if (songs.isEmpty()) {
                EmptyStateView(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.MusicOff,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = OnSurfaceVariant
                        )
                    },
                    title = "还没有歌曲",
                    subtitle = "点击右下角按钮导入歌曲",
                    action = {
                        Button(
                            onClick = { filePickerLauncher.launch(arrayOf("audio/*")) }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("导入歌曲")
                        }
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(songs, key = { it.id }) { song ->
                        SongCard(
                            song = song,
                            onClick = { viewModel.playSong(song) },
                            onFavoriteClick = { viewModel.toggleFavorite(song.id) },
                            isPlaying = song.id == currentPlayingSongId
                        )
                    }
                    
                    // 底部留白，避免被 FAB 遮挡
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text("搜索歌曲...") },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null)
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "清除")
                }
            } else {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "关闭搜索")
                }
            }
        },
        singleLine = true,
        shape = MaterialTheme.shapes.medium
    )
}

@Composable
private fun GroupFilter(
    groups: List<GroupEntity>,
    selectedGroupId: String?,
    onGroupSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // "全部" 选项
        item {
            FilterChip(
                selected = selectedGroupId == null,
                onClick = { onGroupSelected(null) },
                label = { Text("全部") }
            )
        }
        
        items(groups, key = { it.id }) { group ->
            FilterChip(
                selected = selectedGroupId == group.id,
                onClick = { onGroupSelected(group.id) },
                label = { Text(group.name) }
            )
        }
    }
}

@Composable
private fun TagFilter(
    tags: List<TagEntity>,
    selectedTagIds: Set<String>,
    onTagToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(tags, key = { it.id }) { tag ->
            SelectableTagChip(
                tag = tag,
                isSelected = selectedTagIds.contains(tag.id),
                onSelectionChange = { onTagToggle(tag.id) }
            )
        }
    }
}
