package com.kidsenglishsongs.player.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kidsenglishsongs.player.ui.theme.*

/**
 * 超大播放按钮 - 专为儿童设计
 */
@Composable
fun BigPlayButton(
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp
) {
    val scale by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 1.05f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "playButtonScale"
    )
    
    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(Primary, PrimaryDark)
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = true, color = OnPrimary),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = if (isPlaying) "暂停" else "播放",
            modifier = Modifier.size(size * 0.5f),
            tint = OnPrimary
        )
    }
}

/**
 * 圆形控制按钮 - 用于上一首、下一首等
 */
@Composable
fun CircleControlButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    enabled: Boolean = true,
    isPrimary: Boolean = false
) {
    val backgroundColor = when {
        !enabled -> SurfaceVariant
        isPrimary -> Primary
        else -> Surface
    }
    
    val iconColor = when {
        !enabled -> OnSurfaceVariant.copy(alpha = 0.5f)
        isPrimary -> OnPrimary
        else -> OnSurface
    }
    
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = true),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(size * 0.5f),
            tint = iconColor
        )
    }
}

/**
 * 收藏按钮 - 大爱心
 */
@Composable
fun BigFavoriteButton(
    isFavorite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 72.dp
) {
    val scale by animateFloatAsState(
        targetValue = if (isFavorite) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "favoriteButtonScale"
    )
    
    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .clip(CircleShape)
            .background(if (isFavorite) FavoriteRed.copy(alpha = 0.1f) else SurfaceVariant)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = true, color = FavoriteRed),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = if (isFavorite) "取消收藏" else "收藏",
            modifier = Modifier.size(size * 0.5f),
            tint = if (isFavorite) FavoriteRed else FavoriteGray
        )
    }
}

/**
 * 循环模式按钮
 */
@Composable
fun RepeatModeButton(
    repeatMode: com.kidsenglishsongs.player.player.RepeatMode,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    val (icon, tint) = when (repeatMode) {
        com.kidsenglishsongs.player.player.RepeatMode.OFF -> Icons.Default.Repeat to OnSurfaceVariant
        com.kidsenglishsongs.player.player.RepeatMode.ALL -> Icons.Default.Repeat to Primary
        com.kidsenglishsongs.player.player.RepeatMode.ONE -> Icons.Default.RepeatOne to Primary
    }
    
    IconButton(
        onClick = onClick,
        modifier = modifier.size(size)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "循环模式",
            tint = tint,
            modifier = Modifier.size(size * 0.6f)
        )
    }
}

/**
 * 随机播放按钮
 */
@Composable
fun ShuffleButton(
    isEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(size)
    ) {
        Icon(
            imageVector = Icons.Default.Shuffle,
            contentDescription = "随机播放",
            tint = if (isEnabled) Primary else OnSurfaceVariant,
            modifier = Modifier.size(size * 0.6f)
        )
    }
}
