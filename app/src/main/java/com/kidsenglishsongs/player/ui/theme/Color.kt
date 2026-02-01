package com.kidsenglishsongs.player.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================
// 网易云音乐风格 - 深色主题配色
// ============================================

// 主色调 - 网易云红
val Primary = Color(0xFFEC4141)
val PrimaryLight = Color(0xFFFF6B6B)
val PrimaryDark = Color(0xFFC62828)

// 辅色 - 深灰色
val Secondary = Color(0xFF2A2A2A)
val SecondaryLight = Color(0xFF3A3A3A)
val SecondaryDark = Color(0xFF1A1A1A)

// 强调色 - 网易云红
val Accent = Color(0xFFEC4141)
val AccentLight = Color(0xFFFF6B6B)
val AccentDark = Color(0xFFC62828)

// 背景色 - 深色
val Background = Color(0xFF1A1A1A)
val Surface = Color(0xFF2A2A2A)
val SurfaceVariant = Color(0xFF3A3A3A)

// 错误色
val Error = Color(0xFFEF5350)
val ErrorLight = Color(0xFFFF867C)
val ErrorDark = Color(0xFFB61827)

// 警告色
val Warning = Color(0xFFFF9800)
val WarningLight = Color(0xFFFFCC80)
val WarningDark = Color(0xFFF57C00)

// 成功色
val Success = Color(0xFF4CAF50)
val SuccessLight = Color(0xFFA5D6A7)
val SuccessDark = Color(0xFF388E3C)

// 文本色 - 深色主题
val OnPrimary = Color.White
val OnSecondary = Color.White
val OnBackground = Color.White
val OnSurface = Color.White
val OnSurfaceVariant = Color(0xFFB3B3B3)
val OnError = Color.White

// 收藏按钮颜色
val FavoriteRed = Color(0xFFEC4141)
val FavoriteRedLight = Color(0xFFFF6B6B)
val FavoriteGray = Color(0xFF6A6A6A)

// 标签预设颜色 - 网易云风格
val TagColors = listOf(
    Color(0xFFEC4141),  // 网易云红
    Color(0xFFFF6B6B),  // 浅红
    Color(0xFFFF9800),  // 橙色
    Color(0xFFFFB74D),  // 浅橙
    Color(0xFF4CAF50),  // 绿色
    Color(0xFF81C784),  // 浅绿
    Color(0xFF42A5F5),  // 蓝色
    Color(0xFF90CAF9),  // 浅蓝
    Color(0xFFAB47BC),  // 紫色
    Color(0xFFCE93D8)   // 浅紫
)

// 渐变色
val GradientPrimary = listOf(Primary, PrimaryLight)
val GradientSecondary = listOf(Secondary, SecondaryLight)
val GradientAccent = listOf(Accent, AccentLight)
