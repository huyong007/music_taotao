package com.kidsenglishsongs.player.data.repository

import com.kidsenglishsongs.player.data.dao.PlayHistoryDao
import com.kidsenglishsongs.player.data.dao.SongDao
import com.kidsenglishsongs.player.data.entity.SongEntity
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class SongRepositoryTest {

    private lateinit var songDao: SongDao
    private lateinit var playHistoryDao: PlayHistoryDao
    private lateinit var songRepository: SongRepository

    private val testSong = SongEntity(
        id = "test-id-1",
        title = "Test Song",
        filePath = "/path/to/song.mp3",
        duration = 180000L,
        createdAt = System.currentTimeMillis()
    )

    private val testSongs = listOf(
        testSong,
        SongEntity(
            id = "test-id-2",
            title = "Another Song",
            filePath = "/path/to/another.mp3",
            duration = 240000L,
            createdAt = System.currentTimeMillis()
        )
    )

    @Before
    fun setup() {
        songDao = mock()
        playHistoryDao = mock()
        songRepository = SongRepository(songDao, playHistoryDao)
    }

    @Test
    fun `getAllSongs returns all songs from dao`() = runTest {
        whenever(songDao.getAllSongs()).thenReturn(flowOf(testSongs))

        val result = songRepository.getAllSongs().first()

        assertEquals(2, result.size)
        assertEquals("Test Song", result[0].title)
        verify(songDao).getAllSongs()
    }

    @Test
    fun `getSongById returns correct song`() = runTest {
        whenever(songDao.getSongById("test-id-1")).thenReturn(testSong)

        val result = songRepository.getSongById("test-id-1")

        assertNotNull(result)
        assertEquals("Test Song", result?.title)
        verify(songDao).getSongById("test-id-1")
    }

    @Test
    fun `getSongById returns null for non-existent id`() = runTest {
        whenever(songDao.getSongById("non-existent")).thenReturn(null)

        val result = songRepository.getSongById("non-existent")

        assertNull(result)
    }

    @Test
    fun `insertSong calls dao insert`() = runTest {
        songRepository.insertSong(testSong)

        verify(songDao).insertSong(testSong)
    }

    @Test
    fun `insertSongs calls dao insertSongs`() = runTest {
        songRepository.insertSongs(testSongs)

        verify(songDao).insertSongs(testSongs)
    }

    @Test
    fun `deleteSong calls dao delete`() = runTest {
        songRepository.deleteSong(testSong)

        verify(songDao).deleteSong(testSong)
    }

    @Test
    fun `toggleFavorite toggles favorite status`() = runTest {
        val songNotFavorite = testSong.copy(isFavorite = false)
        whenever(songDao.getSongById("test-id-1")).thenReturn(songNotFavorite)

        songRepository.toggleFavorite("test-id-1")

        verify(songDao).updateFavorite("test-id-1", true)
    }

    @Test
    fun `toggleFavorite unfavorites when already favorite`() = runTest {
        val songFavorite = testSong.copy(isFavorite = true)
        whenever(songDao.getSongById("test-id-1")).thenReturn(songFavorite)

        songRepository.toggleFavorite("test-id-1")

        verify(songDao).updateFavorite("test-id-1", false)
    }

    @Test
    fun `getFavoriteSongs returns only favorite songs`() = runTest {
        val favoriteSongs = listOf(testSong.copy(isFavorite = true))
        whenever(songDao.getFavoriteSongs()).thenReturn(flowOf(favoriteSongs))

        val result = songRepository.getFavoriteSongs().first()

        assertEquals(1, result.size)
        assertTrue(result[0].isFavorite)
    }

    @Test
    fun `searchSongs returns matching songs`() = runTest {
        whenever(songDao.searchSongs("Test")).thenReturn(flowOf(listOf(testSong)))

        val result = songRepository.searchSongs("Test").first()

        assertEquals(1, result.size)
        assertTrue(result[0].title.contains("Test"))
    }

    @Test
    fun `getSongsByGroup returns songs in group`() = runTest {
        val songsInGroup = listOf(testSong.copy(groupId = "group-1"))
        whenever(songDao.getSongsByGroup("group-1")).thenReturn(flowOf(songsInGroup))

        val result = songRepository.getSongsByGroup("group-1").first()

        assertEquals(1, result.size)
        assertEquals("group-1", result[0].groupId)
    }

    @Test
    fun `recordPlay increments play count and records history`() = runTest {
        songRepository.recordPlay("test-id-1", 60000L)

        verify(songDao).incrementPlayCount(eq("test-id-1"), any())
        verify(playHistoryDao).insertPlayHistory(any())
    }

    @Test
    fun `updateCover updates song cover path`() = runTest {
        songRepository.updateCover("test-id-1", "/new/cover.jpg")

        verify(songDao).updateCover("test-id-1", "/new/cover.jpg")
    }

    @Test
    fun `getSongCount returns correct count`() = runTest {
        whenever(songDao.getSongCount()).thenReturn(5)

        val count = songRepository.getSongCount()

        assertEquals(5, count)
    }

    @Test
    fun `getTotalDuration returns sum of all durations`() = runTest {
        whenever(songDao.getTotalDuration()).thenReturn(420000L)

        val duration = songRepository.getTotalDuration()

        assertEquals(420000L, duration)
    }

    @Test
    fun `getTotalPlayCount returns sum of all play counts`() = runTest {
        whenever(songDao.getTotalPlayCount()).thenReturn(15)

        val playCount = songRepository.getTotalPlayCount()

        assertEquals(15, playCount)
    }

    @Test
    fun `getRecentlyPlayedSongs returns songs ordered by last played`() = runTest {
        val recentSongs = listOf(testSong.copy(lastPlayedAt = System.currentTimeMillis()))
        whenever(songDao.getRecentlyPlayedSongs(5)).thenReturn(flowOf(recentSongs))

        val result = songRepository.getRecentlyPlayedSongs(5).first()

        assertEquals(1, result.size)
        assertNotNull(result[0].lastPlayedAt)
    }

    @Test
    fun `getMostPlayedSongs returns songs ordered by play count`() = runTest {
        val popularSongs = listOf(testSong.copy(playCount = 100))
        whenever(songDao.getMostPlayedSongs(5)).thenReturn(flowOf(popularSongs))

        val result = songRepository.getMostPlayedSongs(5).first()

        assertEquals(1, result.size)
        assertEquals(100, result[0].playCount)
    }
}
