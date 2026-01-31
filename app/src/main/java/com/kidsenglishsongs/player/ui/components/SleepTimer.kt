package com.kidsenglishsongs.player.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TimerOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.kidsenglishsongs.player.ui.theme.*

/**
 * 睡眠定时器对话框
 */
@Composable
fun SleepTimerDialog(
    onDismiss: () -> Unit,
    onTimerSet: (minutes: Int) -> Unit,
    currentTimerRemaining: Long? = null
) {
    val timerOptions = listOf(
        5 to "5 分钟",
        10 to "10 分钟",
        15 to "15 分钟",
        30 to "30 分钟",
        45 to "45 分钟",
        60 to "1 小时"
    )
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "睡眠定时器",
                    style = MaterialTheme.typography.headlineSmall,
                    color = OnSurface
                )
                
                if (currentTimerRemaining != null && currentTimerRemaining > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val minutes = (currentTimerRemaining / 1000 / 60).toInt()
                    val seconds = ((currentTimerRemaining / 1000) % 60).toInt()
                    
                    Text(
                        text = "剩余时间: ${minutes}:${String.format("%02d", seconds)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Secondary
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 取消定时器按钮
                    Button(
                        onClick = { onTimerSet(0) },
                        colors = ButtonDefaults.buttonColors(containerColor = Error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.TimerOff,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("取消定时器")
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Divider()
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "选择定时时长",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 定时选项
                timerOptions.chunked(2).forEach { rowOptions ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowOptions.forEach { (minutes, label) ->
                            TimerOptionButton(
                                label = label,
                                onClick = { onTimerSet(minutes) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // 如果是奇数个，填充空白
                        if (rowOptions.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 取消按钮
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("关闭")
                }
            }
        }
    }
}

@Composable
private fun TimerOptionButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(PrimaryLight.copy(alpha = 0.3f))
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = PrimaryDark
        )
    }
}

/**
 * 睡眠定时器指示器 - 显示在播放界面
 */
@Composable
fun SleepTimerIndicator(
    remainingMillis: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val minutes = (remainingMillis / 1000 / 60).toInt()
    val seconds = ((remainingMillis / 1000) % 60).toInt()
    
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Secondary.copy(alpha = 0.2f))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Timer,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = SecondaryDark
        )
        
        Spacer(modifier = Modifier.width(6.dp))
        
        Text(
            text = "${minutes}:${String.format("%02d", seconds)}",
            style = MaterialTheme.typography.labelMedium,
            color = SecondaryDark
        )
    }
}
