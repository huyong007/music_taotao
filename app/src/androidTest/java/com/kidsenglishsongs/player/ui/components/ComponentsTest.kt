package com.kidsenglishsongs.player.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.kidsenglishsongs.player.data.entity.SongEntity
import com.kidsenglishsongs.player.data.entity.TagEntity
import com.kidsenglishsongs.player.ui.theme.KidsEnglishSongsTheme
import org.junit.Rule
import org.junit.Test

class SongCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testSong = SongEntity(
        id = "test-id",
        title = "Test Song Title",
        filePath = "/path/to/song.mp3",
        duration = 180000L, // 3 minutes
        playCount = 5,
        isFavorite = false,
        createdAt = System.currentTimeMillis()
    )

    @Test
    fun songCard_displaysSongTitle() {
        composeTestRule.setContent {
            KidsEnglishSongsTheme {
                SongCard(
                    song = testSong,
                    onClick = {},
                    onFavoriteClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Test Song Title").assertExists()
    }

    @Test
    fun songCard_displaysPlayCount() {
        composeTestRule.setContent {
            KidsEnglishSongsTheme {
                SongCard(
                    song = testSong,
                    onClick = {},
                    onFavoriteClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("播放 5 次", substring = true).assertExists()
    }

    @Test
    fun songCard_displaysDuration() {
        composeTestRule.setContent {
            KidsEnglishSongsTheme {
                SongCard(
                    song = testSong,
                    onClick = {},
                    onFavoriteClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("3:00").assertExists()
    }

    @Test
    fun songCard_triggersOnClick() {
        var clicked = false

        composeTestRule.setContent {
            KidsEnglishSongsTheme {
                SongCard(
                    song = testSong,
                    onClick = { clicked = true },
                    onFavoriteClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Test Song Title").performClick()
        assert(clicked)
    }

    @Test
    fun songCard_triggersFavoriteClick() {
        var favoriteClicked = false

        composeTestRule.setContent {
            KidsEnglishSongsTheme {
                SongCard(
                    song = testSong,
                    onClick = {},
                    onFavoriteClick = { favoriteClicked = true }
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("收藏").performClick()
        assert(favoriteClicked)
    }

    @Test
    fun songCard_showsFavoriteIconWhenFavorite() {
        val favoriteSong = testSong.copy(isFavorite = true)

        composeTestRule.setContent {
            KidsEnglishSongsTheme {
                SongCard(
                    song = favoriteSong,
                    onClick = {},
                    onFavoriteClick = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("取消收藏").assertExists()
    }
}

class TagChipTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testTag = TagEntity(
        id = "tag-1",
        name = "快节奏",
        color = "#4FC3F7"
    )

    @Test
    fun tagChip_displaysTagName() {
        composeTestRule.setContent {
            KidsEnglishSongsTheme {
                TagChip(
                    tag = testTag,
                    onClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("#快节奏").assertExists()
    }

    @Test
    fun tagChip_triggersOnClick() {
        var clicked = false

        composeTestRule.setContent {
            KidsEnglishSongsTheme {
                TagChip(
                    tag = testTag,
                    onClick = { clicked = true }
                )
            }
        }

        composeTestRule.onNodeWithText("#快节奏").performClick()
        assert(clicked)
    }

    @Test
    fun selectableTagChip_togglesSelection() {
        var isSelected = false

        composeTestRule.setContent {
            KidsEnglishSongsTheme {
                SelectableTagChip(
                    tag = testTag,
                    isSelected = isSelected,
                    onSelectionChange = { isSelected = it }
                )
            }
        }

        composeTestRule.onNodeWithText("#快节奏").performClick()
        assert(isSelected)
    }

    @Test
    fun addTagChip_displaysCorrectText() {
        composeTestRule.setContent {
            KidsEnglishSongsTheme {
                AddTagChip(onClick = {})
            }
        }

        composeTestRule.onNodeWithText("+ 添加标签").assertExists()
    }
}

class LargeSongCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testSong = SongEntity(
        id = "test-id",
        title = "Test Song",
        filePath = "/path/to/song.mp3",
        duration = 180000L,
        createdAt = System.currentTimeMillis()
    )

    @Test
    fun largeSongCard_displaysSongTitle() {
        composeTestRule.setContent {
            KidsEnglishSongsTheme {
                LargeSongCard(
                    song = testSong,
                    onClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Test Song").assertExists()
    }

    @Test
    fun largeSongCard_triggersOnClick() {
        var clicked = false

        composeTestRule.setContent {
            KidsEnglishSongsTheme {
                LargeSongCard(
                    song = testSong,
                    onClick = { clicked = true }
                )
            }
        }

        composeTestRule.onNodeWithText("Test Song").performClick()
        assert(clicked)
    }
}
