package com.kidsenglishsongs.player.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.kidsenglishsongs.player.ui.components.*
import com.kidsenglishsongs.player.ui.theme.*
import java.io.File

/**
 * 儿童播放界面 - 大按钮、简洁设计
 */
@Composable
fun PlayerScreen(
    onNavigateToLibrary: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val playbackState by viewModel.playbackState.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()
    val sleepTimerRemaining by viewModel.sleepTimerRemaining.collectAsState()
    val showSleepTimerDialog by viewModel.showSleepTimerDialog.collectAsState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Background, SurfaceVariant)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 顶部工具栏
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 睡眠定时器指示器
                if (sleepTimerRemaining != null && sleepTimerRemaining!! > 0) {
                    SleepTimerIndicator(
                        remainingMillis = sleepTimerRemaining!!,
                        onClick = { viewModel.showSleepTimerDialog() }
                    )
                } else {
                    IconButton(onClick = { viewModel.showSleepTimerDialog() }) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "睡眠定时器",
                            tint = OnSurfaceVariant
                        )
                    }
                }
                
                // 设置按钮
                IconButton(onClick = onNavigateToSettings) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "设置",
                        tint = OnSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 封面图片
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Primary, PrimaryLight)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (currentSong?.coverPath != null) {
                    AsyncImage(
                        model = File(currentSong!!.coverPath!!),
                        contentDescription = currentSong?.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        modifier = Modifier.size(120.dp),
                        tint = OnPrimary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 歌曲名称
            Text(
                text = currentSong?.title ?: "还没有播放歌曲",
                style = MaterialTheme.typography.headlineMedium,
                color = OnSurface,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 进度条
            PlaybackProgressBar(
                progress = playbackState.progress,
                currentPosition = playbackState.currentPosition,
                duration = playbackState.duration,
                onSeek = { progress ->
                    viewModel.seekToProgress(progress)
                },
                enabled = currentSong != null
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 播放控制
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 随机播放
                ShuffleButton(
                    isEnabled = playbackState.isShuffleEnabled,
                    onClick = { viewModel.toggleShuffle() },
                    size = 48.dp
                )
                
                // 上一首
                CircleControlButton(
                    icon = Icons.Default.SkipPrevious,
                    contentDescription = "上一首",
                    onClick = { viewModel.seekToPrevious() },
                    size = 64.dp,
                    enabled = currentSong != null
                )
                
                // 播放/暂停
                BigPlayButton(
                    isPlaying = playbackState.isPlaying,
                    onClick = { viewModel.togglePlayPause() },
                    size = 100.dp
                )
                
                // 下一首
                CircleControlButton(
                    icon = Icons.Default.SkipNext,
                    contentDescription = "下一首",
                    onClick = { viewModel.seekToNext() },
                    size = 64.dp,
                    enabled = currentSong != null
                )
                
                // 循环模式
                RepeatModeButton(
                    repeatMode = playbackState.repeatMode,
                    onClick = { viewModel.toggleRepeatMode() },
                    size = 48.dp
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 收藏按钮
            BigFavoriteButton(
                isFavorite = currentSong?.isFavorite == true,
                onClick = { viewModel.toggleFavorite() },
                size = 64.dp
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // 底部歌曲库入口
            Button(
                onClick = onNavigateToLibrary,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Icon(
                    imageVector = Icons.Default.LibraryMusic,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "选择歌曲",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
    
    // 睡眠定时器对话框
    if (showSleepTimerDialog) {
        SleepTimerDialog(
            onDismiss = { viewModel.hideSleepTimerDialog() },
            onTimerSet = { minutes -> viewModel.setSleepTimer(minutes) },
            currentTimerRemaining = sleepTimerRemaining
        )
    }
}
