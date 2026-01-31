package com.kidsenglishsongs.player.ui.navigation

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import com.kidsenglishsongs.player.ui.theme.KidsEnglishSongsTheme
import org.junit.Rule
import org.junit.Test

class NavigationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun appNavigation_startsOnPlayerScreen() {
        composeTestRule.setContent {
            KidsEnglishSongsTheme {
                AppNavigation()
            }
        }

        // Player screen should show "选择歌曲" button
        composeTestRule.onNodeWithText("选择歌曲").assertExists()
    }

    @Test
    fun routes_hasCorrectValues() {
        assertEquals("player", Routes.PLAYER)
        assertEquals("library", Routes.LIBRARY)
        assertEquals("settings", Routes.SETTINGS)
        assertEquals("parent_control", Routes.PARENT_CONTROL)
        assertEquals("playlist", Routes.PLAYLIST)
    }

    private fun assertEquals(expected: String, actual: String) {
        assert(expected == actual) { "Expected $expected but was $actual" }
    }
}
