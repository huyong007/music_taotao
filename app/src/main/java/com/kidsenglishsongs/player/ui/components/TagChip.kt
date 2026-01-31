package com.kidsenglishsongs.player.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kidsenglishsongs.player.data.entity.TagEntity
import com.kidsenglishsongs.player.ui.theme.*

/**
 * 标签芯片组件
 */
@Composable
fun TagChip(
    tag: TagEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    showDeleteButton: Boolean = false,
    onDeleteClick: (() -> Unit)? = null
) {
    val tagColor = try {
        Color(android.graphics.Color.parseColor(tag.color))
    } catch (e: Exception) {
        Primary
    }
    
    val backgroundColor = if (isSelected) tagColor else tagColor.copy(alpha = 0.15f)
    val contentColor = if (isSelected) OnPrimary else tagColor
    
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "#${tag.name}",
            style = MaterialTheme.typography.labelLarge,
            color = contentColor
        )
        
        if (showDeleteButton && onDeleteClick != null) {
            Spacer(modifier = Modifier.width(4.dp))
            
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "删除标签",
                modifier = Modifier
                    .size(16.dp)
                    .clickable(onClick = onDeleteClick),
                tint = contentColor
            )
        }
    }
}

/**
 * 可选择的标签芯片
 */
@Composable
fun SelectableTagChip(
    tag: TagEntity,
    isSelected: Boolean,
    onSelectionChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    TagChip(
        tag = tag,
        onClick = { onSelectionChange(!isSelected) },
        modifier = modifier,
        isSelected = isSelected
    )
}

/**
 * 新建标签按钮
 */
@Composable
fun AddTagChip(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "+ 添加标签",
            style = MaterialTheme.typography.labelLarge,
            color = OnSurfaceVariant
        )
    }
}

/**
 * 标签颜色选择器
 */
@Composable
fun TagColorPicker(
    selectedColor: String,
    onColorSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TagColors.forEach { color ->
            val colorHex = String.format("#%06X", 0xFFFFFF and color.hashCode())
            val isSelected = selectedColor.equals(colorHex, ignoreCase = true)
            
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color)
                    .clickable { onColorSelected(colorHex) }
                    .then(
                        if (isSelected) {
                            Modifier.background(
                                color = Color.White.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(8.dp)
                            )
                        } else Modifier
                    )
            )
        }
    }
}
