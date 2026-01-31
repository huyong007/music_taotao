package com.kidsenglishsongs.player.data.entity

import org.junit.Assert.*
import org.junit.Test

class EntityTest {

    @Test
    fun `SongEntity has correct default values`() {
        val song = SongEntity(
            id = "test-id",
            title = "Test",
            filePath = "/path",
            duration = 1000L,
            createdAt = System.currentTimeMillis()
        )

        assertEquals(0, song.playCount)
        assertFalse(song.isFavorite)
        assertNull(song.coverPath)
        assertNull(song.lyricsPath)
        assertNull(song.groupId)
        assertNull(song.lastPlayedAt)
    }

    @Test
    fun `SongEntity copy works correctly`() {
        val song = SongEntity(
            id = "test-id",
            title = "Test",
            filePath = "/path",
            duration = 1000L,
            createdAt = 1000L
        )

        val copied = song.copy(
            title = "New Title",
            isFavorite = true
        )

        assertEquals("test-id", copied.id)
        assertEquals("New Title", copied.title)
        assertTrue(copied.isFavorite)
    }

    @Test
    fun `GroupEntity has correct default values`() {
        val group = GroupEntity(
            id = "group-id",
            name = "Test Group",
            sortOrder = 0,
            createdAt = System.currentTimeMillis()
        )

        assertNull(group.coverPath)
    }

    @Test
    fun `TagEntity stores color correctly`() {
        val tag = TagEntity(
            id = "tag-id",
            name = "Test Tag",
            color = "#FF5500"
        )

        assertEquals("#FF5500", tag.color)
    }

    @Test
    fun `PlaylistEntity has correct structure`() {
        val playlist = PlaylistEntity(
            id = "playlist-id",
            name = "My Playlist",
            createdAt = 1000L
        )

        assertEquals("playlist-id", playlist.id)
        assertEquals("My Playlist", playlist.name)
        assertEquals(1000L, playlist.createdAt)
    }

    @Test
    fun `PlaylistSongCrossRef has correct structure`() {
        val crossRef = PlaylistSongCrossRef(
            playlistId = "playlist-1",
            songId = "song-1",
            sortOrder = 0
        )

        assertEquals("playlist-1", crossRef.playlistId)
        assertEquals("song-1", crossRef.songId)
        assertEquals(0, crossRef.sortOrder)
    }

    @Test
    fun `SongTagCrossRef has correct structure`() {
        val crossRef = SongTagCrossRef(
            songId = "song-1",
            tagId = "tag-1"
        )

        assertEquals("song-1", crossRef.songId)
        assertEquals("tag-1", crossRef.tagId)
    }

    @Test
    fun `PlayHistoryEntity has correct structure`() {
        val history = PlayHistoryEntity(
            id = "history-1",
            songId = "song-1",
            playedAt = 1000L,
            playDuration = 60000L
        )

        assertEquals("history-1", history.id)
        assertEquals("song-1", history.songId)
        assertEquals(1000L, history.playedAt)
        assertEquals(60000L, history.playDuration)
    }
}
