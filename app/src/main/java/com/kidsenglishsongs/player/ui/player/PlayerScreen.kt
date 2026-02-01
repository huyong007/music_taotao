package com.kidsenglishsongs.player.ui.player

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kidsenglishsongs.player.ui.components.*
import com.kidsenglishsongs.player.ui.theme.*
import com.kidsenglishsongs.player.player.RepeatMode
import java.io.File

// 网易云风格的深色主题色
private val NeteaseRed = Color(0xFFEC4141)
private val NeteaseDark = Color(0xFF1A1A1A)
private val NeteaseGray = Color(0xFF2A2A2A)
private val NeteaseTextPrimary = Color.White
private val NeteaseTextSecondary = Color(0xFFB3B3B3)
private val NeteaseProgress = Color(0xFFEC4141)
private val NeteaseProgressBg = Color(0xFF4A4A4A)

/**
 * 网易云音乐风格播放界面
 */
@OptIn(ExperimentalMaterial3Api::class)
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
    
    // 唱片旋转动画
    val infiniteTransition = rememberInfiniteTransition(label = "discRotation")
    val discRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 20000, easing = LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    // 当前旋转角度（暂停时保持）
    var currentRotation by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(playbackState.isPlaying, discRotation) {
        if (playbackState.isPlaying) {
            currentRotation = discRotation
        }
    }
    
    // 唱针旋转动画（播放时放下到唱片上，暂停时抬起）
    // 参考网易云：播放时约18度（唱针尖接触唱片），暂停时约-25度（唱针抬起）
    val tonearmRotation by animateFloatAsState(
        targetValue = if (playbackState.isPlaying) 18f else -25f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "tonearmRotation"
    )
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 背景层 - 模糊的封面图
        PlayerBackground(
            coverPath = currentSong?.coverPath
        )
        
        // 内容层
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // 顶部导航栏
            PlayerTopBar(
                songTitle = currentSong?.title ?: "Kids English Songs",
                artist = currentSong?.artist ?: "儿童英语歌曲",
                onBackClick = onNavigateToLibrary,
                onSettingsClick = onNavigateToSettings
            )
            
            // 主内容区域
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.weight(0.5f))
                
                // 唱片封面和唱针
                DiscWithTonearm(
                    coverPath = currentSong?.coverPath,
                    isPlaying = playbackState.isPlaying,
                    discRotation = if (playbackState.isPlaying) discRotation else currentRotation,
                    tonearmRotation = tonearmRotation,
                    onClick = { /* 可以添加点击切换歌词 */ }
                )
                
                Spacer(modifier = Modifier.weight(0.3f))
                
                // 歌曲信息和收藏
                SongInfoRow(
                    title = currentSong?.title ?: "还没有播放歌曲",
                    isFavorite = currentSong?.isFavorite == true,
                    onFavoriteClick = { viewModel.toggleFavorite() }
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // 进度条
                NeteaseProgressBar(
                    progress = playbackState.progress,
                    currentPosition = playbackState.currentPosition,
                    duration = playbackState.duration,
                    onSeek = { viewModel.seekToProgress(it) }
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 播放控制
                PlaybackControls(
                    isPlaying = playbackState.isPlaying,
                    repeatMode = playbackState.repeatMode,
                    isShuffleEnabled = playbackState.isShuffleEnabled,
                    onPlayPauseClick = { viewModel.togglePlayPause() },
                    onPreviousClick = { viewModel.seekToPrevious() },
                    onNextClick = { viewModel.seekToNext() },
                    onRepeatClick = { viewModel.toggleRepeatMode() },
                    onShuffleClick = { viewModel.toggleShuffle() },
                    onPlaylistClick = onNavigateToLibrary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 底部功能栏
                BottomActionBar(
                    sleepTimerRemaining = sleepTimerRemaining,
                    onTimerClick = { viewModel.showSleepTimerDialog() }
                )
                
                Spacer(modifier = Modifier.weight(0.2f))
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

/**
 * 播放器背景 - 模糊的封面图
 */
@Composable
private fun PlayerBackground(
    coverPath: String?
) {
    val context = LocalContext.current
    
    Box(modifier = Modifier.fillMaxSize()) {
        // 深色底色
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(NeteaseDark)
        )
        
        // 模糊的封面图
        if (coverPath != null) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(File(coverPath))
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(radius = 100.dp),
                contentScale = ContentScale.Crop,
                alpha = 0.3f
            )
        }
        
        // 渐变遮罩
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            NeteaseDark.copy(alpha = 0.7f),
                            NeteaseDark
                        )
                    )
                )
        )
    }
}

/**
 * 顶部导航栏
 */
@Composable
private fun PlayerTopBar(
    songTitle: String,
    artist: String,
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 返回/歌曲库按钮
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.Default.QueueMusic,
                contentDescription = "歌曲库",
                tint = NeteaseTextPrimary,
                modifier = Modifier.size(28.dp)
            )
        }
        
        // 歌曲信息
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = songTitle,
                color = NeteaseTextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = artist,
                color = NeteaseTextSecondary,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        // 设置按钮
        IconButton(onClick = onSettingsClick) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "设置",
                tint = NeteaseTextPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * 唱片和唱针组合 - 仿网易云音乐风格
 * 参考：唱针在唱片右上方，旋转支点在唱针顶部
 */
@Composable
private fun DiscWithTonearm(
    coverPath: String?,
    isPlaying: Boolean,
    discRotation: Float,
    tonearmRotation: Float,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(360.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        // 唱片 - 稍微往下和往左偏移，给唱针留空间
        DiscCover(
            coverPath = coverPath,
            rotation = discRotation,
            onClick = onClick,
            modifier = Modifier
                .padding(top = 60.dp)
                .offset(x = (-15).dp)
        )
        
        // 唱针 - 位于唱片右上方
        Tonearm(
            rotation = tonearmRotation,
            modifier = Modifier
                .offset(x = 90.dp, y = 0.dp)
        )
    }
}

/**
 * 唱针组件 - 仿网易云音乐风格
 * 包含：底座（支点）、唱臂、唱头、唱针尖
 * 旋转支点在底座中心
 */
@Composable
private fun Tonearm(
    rotation: Float,
    modifier: Modifier = Modifier
) {
    // 唱针各部分尺寸（参考网易云比例）
    val baseSize = 36.dp      // 底座直径
    val armWidth = 6.dp       // 唱臂宽度
    val armLength = 140.dp    // 唱臂长度
    val headWidth = 16.dp     // 唱头宽度
    val headHeight = 28.dp    // 唱头高度
    val needleWidth = 3.dp    // 唱针尖宽度
    val needleHeight = 10.dp  // 唱针尖高度
    
    Box(
        modifier = modifier
            .graphicsLayer {
                // 旋转支点在底座中心（顶部中间位置）
                rotationZ = rotation
                transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0.06f)
            }
            .width(baseSize)
            .height(armLength + headHeight + needleHeight + 20.dp)
    ) {
        // 1. 底座外圈 - 金属质感
        Box(
            modifier = Modifier
                .size(baseSize)
                .align(Alignment.TopCenter)
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF6A6A6A),
                            Color(0xFF4A4A4A),
                            Color(0xFF3A3A3A)
                        )
                    )
                )
        )
        
        // 2. 底座内圈 - 高光效果
        Box(
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.TopCenter)
                .offset(y = 6.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF8A8A8A),
                            Color(0xFF5A5A5A)
                        )
                    )
                )
        )
        
        // 3. 底座中心点
        Box(
            modifier = Modifier
                .size(10.dp)
                .align(Alignment.TopCenter)
                .offset(y = 13.dp)
                .clip(CircleShape)
                .background(Color(0xFF3A3A3A))
        )
        
        // 4. 唱臂 - 金属长条，带渐变和阴影
        Box(
            modifier = Modifier
                .width(armWidth)
                .height(armLength)
                .align(Alignment.TopCenter)
                .offset(y = baseSize - 8.dp)
                .shadow(4.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF3A3A3A),
                            Color(0xFF6A6A6A),
                            Color(0xFF8A8A8A),
                            Color(0xFF6A6A6A),
                            Color(0xFF3A3A3A)
                        )
                    )
                )
        )
        
        // 5. 唱臂与唱头连接处 - 小方块
        Box(
            modifier = Modifier
                .width(10.dp)
                .height(12.dp)
                .align(Alignment.TopCenter)
                .offset(y = baseSize + armLength - 16.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF5A5A5A),
                            Color(0xFF4A4A4A)
                        )
                    )
                )
        )
        
        // 6. 唱头 - 梯形效果（用两个叠加的矩形模拟）
        Box(
            modifier = Modifier
                .width(headWidth)
                .height(headHeight)
                .align(Alignment.TopCenter)
                .offset(y = baseSize + armLength - 8.dp)
                .shadow(2.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF5A5A5A),
                            Color(0xFF3A3A3A),
                            Color(0xFF2A2A2A)
                        )
                    )
                )
        )
        
        // 7. 唱针尖 - 红色，网易云特色
        Box(
            modifier = Modifier
                .width(needleWidth)
                .height(needleHeight)
                .align(Alignment.TopCenter)
                .offset(y = baseSize + armLength + headHeight - 12.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            NeteaseRed,
                            NeteaseRed.copy(alpha = 0.7f)
                        )
                    )
                )
        )
    }
}

/**
 * 唱片封面
 */
@Composable
private fun DiscCover(
    coverPath: String?,
    rotation: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val discSize = 280.dp
    val coverSize = 180.dp
    
    Box(
        modifier = modifier
            .size(discSize)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // 唱片底盘（黑色圆盘）
        Box(
            modifier = Modifier
                .size(discSize)
                .rotate(rotation)
                .shadow(16.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF2A2A2A),
                            Color(0xFF1A1A1A),
                            Color(0xFF0A0A0A)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // 唱片纹路 - 简化版
            Box(
                modifier = Modifier
                    .size((coverSize.value + 40).dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.02f))
            )
            Box(
                modifier = Modifier
                    .size((coverSize.value + 60).dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.015f))
            )
            Box(
                modifier = Modifier
                    .size((coverSize.value + 80).dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.01f))
            )
            
            // 封面图片
            Box(
                modifier = Modifier
                    .size(coverSize)
                    .clip(CircleShape)
                    .background(NeteaseGray),
                contentAlignment = Alignment.Center
            ) {
                if (coverPath != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(File(coverPath))
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // 默认封面
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(NeteaseRed.copy(alpha = 0.8f), NeteaseRed.copy(alpha = 0.4f))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(80.dp)
                        )
                    }
                }
            }
            
            // 中心圆点
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF3A3A3A))
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .align(Alignment.Center)
                        .clip(CircleShape)
                        .background(Color(0xFF5A5A5A))
                )
            }
        }
        
    }
}

/**
 * 歌曲信息行
 */
@Composable
private fun SongInfoRow(
    title: String,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.width(48.dp))
        
        Text(
            text = title,
            color = NeteaseTextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        
        IconButton(onClick = onFavoriteClick) {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = if (isFavorite) "取消收藏" else "收藏",
                tint = if (isFavorite) NeteaseRed else NeteaseTextSecondary,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

/**
 * 网易云风格进度条
 */
@Composable
private fun NeteaseProgressBar(
    progress: Float,
    currentPosition: Long,
    duration: Long,
    onSeek: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // 进度条
        Slider(
            value = progress,
            onValueChange = onSeek,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = NeteaseRed,
                activeTrackColor = NeteaseRed,
                inactiveTrackColor = NeteaseProgressBg
            )
        )
        
        // 时间显示
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatTime(currentPosition),
                color = NeteaseTextSecondary,
                fontSize = 12.sp
            )
            Text(
                text = formatTime(duration),
                color = NeteaseTextSecondary,
                fontSize = 12.sp
            )
        }
    }
}

/**
 * 播放控制按钮
 */
@Composable
private fun PlaybackControls(
    isPlaying: Boolean,
    repeatMode: RepeatMode,
    isShuffleEnabled: Boolean,
    onPlayPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onRepeatClick: () -> Unit,
    onShuffleClick: () -> Unit,
    onPlaylistClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // 播放模式
        IconButton(onClick = onRepeatClick) {
            Icon(
                imageVector = when (repeatMode) {
                    RepeatMode.ONE -> Icons.Default.RepeatOne
                    RepeatMode.ALL -> Icons.Default.Repeat
                    else -> Icons.Default.Repeat
                },
                contentDescription = "循环模式",
                tint = if (repeatMode != RepeatMode.OFF) NeteaseRed else NeteaseTextSecondary,
                modifier = Modifier.size(24.dp)
            )
        }
        
        // 上一首
        IconButton(
            onClick = onPreviousClick,
            modifier = Modifier.size(56.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SkipPrevious,
                contentDescription = "上一首",
                tint = NeteaseTextPrimary,
                modifier = Modifier.size(36.dp)
            )
        }
        
        // 播放/暂停
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(NeteaseRed)
                .clickable(onClick = onPlayPauseClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "暂停" else "播放",
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }
        
        // 下一首
        IconButton(
            onClick = onNextClick,
            modifier = Modifier.size(56.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SkipNext,
                contentDescription = "下一首",
                tint = NeteaseTextPrimary,
                modifier = Modifier.size(36.dp)
            )
        }
        
        // 播放列表
        IconButton(onClick = onPlaylistClick) {
            Icon(
                imageVector = Icons.Default.QueueMusic,
                contentDescription = "播放列表",
                tint = NeteaseTextSecondary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * 底部功能栏
 */
@Composable
private fun BottomActionBar(
    sleepTimerRemaining: Long?,
    onTimerClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 定时器
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable(onClick = onTimerClick)
        ) {
            Icon(
                imageVector = Icons.Default.Timer,
                contentDescription = "定时关闭",
                tint = if (sleepTimerRemaining != null && sleepTimerRemaining > 0) NeteaseRed else NeteaseTextSecondary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (sleepTimerRemaining != null && sleepTimerRemaining > 0) {
                    formatTime(sleepTimerRemaining)
                } else {
                    "定时"
                },
                color = if (sleepTimerRemaining != null && sleepTimerRemaining > 0) NeteaseRed else NeteaseTextSecondary,
                fontSize = 10.sp
            )
        }
    }
}

/**
 * 格式化时间
 */
private fun formatTime(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / 1000 / 60) % 60
    return String.format("%02d:%02d", minutes, seconds)
}
