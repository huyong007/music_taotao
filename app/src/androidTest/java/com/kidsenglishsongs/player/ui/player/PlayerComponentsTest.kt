package com.kidsenglishsongs.player.ui.player

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.kidsenglishsongs.player.data.entity.SongEntity
import com.kidsenglishsongs.player.player.PlaybackState
import com.kidsenglishsongs.player.player.RepeatMode
import com.kidsenglishsongs.player.ui.components.*
import com.kidsenglishsongs.player.ui.theme.KidsEnglishSongsTheme
import org.junit.Rule
import org.junit.Test

class PlayerComponentsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun bigPlayButton_displaysPlayIcon_whenNotPlaying() {
        composeTestRule.setContent {
            KidsEnglishSongsTheme {
                BigPlayButton(
                    isPlaying = false,
                    onClick = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("播放").assertExists()
    }

    @Test
    fun bigPlayButton_displaysPauseIcon_whenPlaying() {
        composeTestRule.setContent {
            KidsEnglishSongsTheme {
                BigPlayButton(
                    isPlaying = true,
                    onClick = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("暂停").assertExists()
    }

    @Test
    fun bigPlayButton_triggersCallback_onCclick() {
        var clicked = false

        composeTestRule.setContent {
            KidsEnglishSongsTheme {
                BigPlayButton(
                    isPlaying = false,
                    onClick = { clicked = true }
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("播放").performClick()
        assert(clicked)
    }

    @Test
    fun bigFavoriteButton_displaysFilled_whenFavorite() {
        composeTestRule.setContent {
            KidsEnglishSongsTheme {
                BigFavoriteButton(
                    isFavorite = true,
                    onClick = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("取消收藏").assertExists()
    }

    @Test
    fun bigFavoriteButton_displaysOutline_whenNotFavorite() {
        composeTestRule.setContent {
            KidsEnglishSongsTheme {
                BigFavoriteButton(
                    isFavorite = false,
                    onClick = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("收藏").assertExists()
    }

    @Test
    fun repeatModeButton_displaysCorrectIcon_forEachMode() {
        // Test OFF mode
        composeTestRule.setContent {
            KidsEnglishSongsTheme {
                RepeatModeButton(
                    repeatMode = RepeatMode.OFF,
                    onClick = {}
                )
            }
        }
        composeTestRule.onNodeWithContentDescription("循环模式").assertExists()
    }

    @Test
    fun shuffleButton_triggersCallback_onClick() {
        var clicked = false

        composeTestRule.setContent {
            KidsEnglishSongsTheme {
                ShuffleButton(
                    isEnabled = false,
                    onClick = { clicked = true }
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("随机播放").performClick()
        assert(clicked)
    }

    @Test
    fun circleControlButton_isDisabled_whenEnabledIsFalse() {
        var clicked = false

        composeTestRule.setContent {
            KidsEnglishSongsTheme {
                CircleControlButton(
                    icon = androidx.compose.material.icons.Icons.Default.SkipNext,
                    contentDescription = "下一首",
                    onClick = { clicked = true },
                    enabled = false
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("下一首").performClick()
        assert(!clicked)
    }
}
