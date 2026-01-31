package com.kidsenglishsongs.player.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.kidsenglishsongs.player.ui.theme.KidsEnglishSongsTheme
import org.junit.Rule
import org.junit.Test

class SleepTimerTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun sleepTimerDialog_displaysTitle() {
        composeTestRule.setContent {
            KidsEnglishSongsTheme {
                SleepTimerDialog(
                    onDismiss = {},
                    onTimerSet = {}
                )
            }
        }

        composeTestRule.onNodeWithText("睡眠定时器").assertExists()
    }

    @Test
    fun sleepTimerDialog_displaysAllTimerOptions() {
        composeTestRule.setContent {
            KidsEnglishSongsTheme {
                SleepTimerDialog(
                    onDismiss = {},
                    onTimerSet = {}
                )
            }
        }

        composeTestRule.onNodeWithText("5 分钟").assertExists()
        composeTestRule.onNodeWithText("10 分钟").assertExists()
        composeTestRule.onNodeWithText("15 分钟").assertExists()
        composeTestRule.onNodeWithText("30 分钟").assertExists()
        composeTestRule.onNodeWithText("45 分钟").assertExists()
        composeTestRule.onNodeWithText("1 小时").assertExists()
    }

    @Test
    fun sleepTimerDialog_triggersOnTimerSet_whenOptionClicked() {
        var selectedMinutes = 0

        composeTestRule.setContent {
            KidsEnglishSongsTheme {
                SleepTimerDialog(
                    onDismiss = {},
                    onTimerSet = { selectedMinutes = it }
                )
            }
        }

        composeTestRule.onNodeWithText("30 分钟").performClick()
        assert(selectedMinutes == 30)
    }

    @Test
    fun sleepTimerDialog_triggersOnDismiss_whenCloseClicked() {
        var dismissed = false

        composeTestRule.setContent {
            KidsEnglishSongsTheme {
                SleepTimerDialog(
                    onDismiss = { dismissed = true },
                    onTimerSet = {}
                )
            }
        }

        composeTestRule.onNodeWithText("关闭").performClick()
        assert(dismissed)
    }

    @Test
    fun sleepTimerDialog_showsRemainingTime_whenTimerActive() {
        composeTestRule.setContent {
            KidsEnglishSongsTheme {
                SleepTimerDialog(
                    onDismiss = {},
                    onTimerSet = {},
                    currentTimerRemaining = 120000L // 2 minutes
                )
            }
        }

        composeTestRule.onNodeWithText("剩余时间: 2:00").assertExists()
        composeTestRule.onNodeWithText("取消定时器").assertExists()
    }

    @Test
    fun sleepTimerDialog_cancelTimer_whenCancelClicked() {
        var cancelled = false

        composeTestRule.setContent {
            KidsEnglishSongsTheme {
                SleepTimerDialog(
                    onDismiss = {},
                    onTimerSet = { if (it == 0) cancelled = true },
                    currentTimerRemaining = 120000L
                )
            }
        }

        composeTestRule.onNodeWithText("取消定时器").performClick()
        assert(cancelled)
    }

    @Test
    fun sleepTimerIndicator_displaysCorrectTime() {
        composeTestRule.setContent {
            KidsEnglishSongsTheme {
                SleepTimerIndicator(
                    remainingMillis = 150000L, // 2:30
                    onClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("2:30").assertExists()
    }

    @Test
    fun sleepTimerIndicator_triggersOnClick() {
        var clicked = false

        composeTestRule.setContent {
            KidsEnglishSongsTheme {
                SleepTimerIndicator(
                    remainingMillis = 60000L,
                    onClick = { clicked = true }
                )
            }
        }

        composeTestRule.onNodeWithText("1:00").performClick()
        assert(clicked)
    }
}

class PlayerComponentsUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun playbackProgressBar_displaysDuration() {
        composeTestRule.setContent {
            KidsEnglishSongsTheme {
                PlaybackProgressBar(
                    progress = 0.5f,
                    currentPosition = 90000L, // 1:30
                    duration = 180000L, // 3:00
                    onSeek = {}
                )
            }
        }

        composeTestRule.onNodeWithText("1:30").assertExists()
        composeTestRule.onNodeWithText("3:00").assertExists()
    }

    @Test
    fun emptyStateView_displaysTitle() {
        composeTestRule.setContent {
            KidsEnglishSongsTheme {
                EmptyStateView(
                    icon = {
                        androidx.compose.material3.Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.MusicOff,
                            contentDescription = null
                        )
                    },
                    title = "还没有歌曲",
                    subtitle = "点击右下角按钮导入歌曲"
                )
            }
        }

        composeTestRule.onNodeWithText("还没有歌曲").assertExists()
        composeTestRule.onNodeWithText("点击右下角按钮导入歌曲").assertExists()
    }

    @Test
    fun loadingIndicator_displaysMessage() {
        composeTestRule.setContent {
            KidsEnglishSongsTheme {
                LoadingIndicator(message = "加载中...")
            }
        }

        composeTestRule.onNodeWithText("加载中...").assertExists()
    }
}
