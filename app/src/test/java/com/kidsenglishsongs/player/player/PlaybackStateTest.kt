package com.kidsenglishsongs.player.player

import org.junit.Assert.*
import org.junit.Test

class PlaybackStateTest {

    @Test
    fun `default PlaybackState has correct initial values`() {
        val state = PlaybackState()

        assertFalse(state.isPlaying)
        assertNull(state.currentSongId)
        assertEquals("", state.currentSongTitle)
        assertNull(state.currentSongCover)
        assertEquals(0L, state.duration)
        assertEquals(0L, state.currentPosition)
        assertEquals(RepeatMode.OFF, state.repeatMode)
        assertFalse(state.isShuffleEnabled)
        assertEquals(1.0f, state.playbackSpeed)
        assertFalse(state.isBuffering)
        assertNull(state.error)
    }

    @Test
    fun `progress is calculated correctly`() {
        val state = PlaybackState(
            duration = 100000L,
            currentPosition = 50000L
        )

        assertEquals(0.5f, state.progress, 0.001f)
    }

    @Test
    fun `progress is 0 when duration is 0`() {
        val state = PlaybackState(
            duration = 0L,
            currentPosition = 50000L
        )

        assertEquals(0f, state.progress)
    }

    @Test
    fun `progress is 0 when both are 0`() {
        val state = PlaybackState(
            duration = 0L,
            currentPosition = 0L
        )

        assertEquals(0f, state.progress)
    }

    @Test
    fun `progress at start is 0`() {
        val state = PlaybackState(
            duration = 100000L,
            currentPosition = 0L
        )

        assertEquals(0f, state.progress)
    }

    @Test
    fun `progress at end is 1`() {
        val state = PlaybackState(
            duration = 100000L,
            currentPosition = 100000L
        )

        assertEquals(1f, state.progress)
    }

    @Test
    fun `copy creates new state with modified values`() {
        val original = PlaybackState()
        val modified = original.copy(
            isPlaying = true,
            currentSongId = "song-1",
            currentSongTitle = "Test Song"
        )

        assertFalse(original.isPlaying)
        assertTrue(modified.isPlaying)
        assertEquals("song-1", modified.currentSongId)
        assertEquals("Test Song", modified.currentSongTitle)
    }
}

class RepeatModeTest {

    @Test
    fun `RepeatMode has three values`() {
        val modes = RepeatMode.values()

        assertEquals(3, modes.size)
        assertTrue(modes.contains(RepeatMode.OFF))
        assertTrue(modes.contains(RepeatMode.ONE))
        assertTrue(modes.contains(RepeatMode.ALL))
    }
}
