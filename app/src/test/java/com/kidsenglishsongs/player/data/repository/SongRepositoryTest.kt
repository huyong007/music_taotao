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
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class SongRepositoryTest {

    @Mock
    private lateinit var songDao: SongDao

    @Mock
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
        MockitoAnnotations.openMocks(this)
        songRepository = SongRepository(songDao, playHistoryDao)
    }

    @Test
    fun `getAllSongs returns all songs from dao`() = runTest {
        `when`(songDao.getAllSongs()).thenReturn(flowOf(testSongs))

        val result = songRepository.getAllSongs().first()

        assertEquals(2, result.size)
        assertEquals("Test Song", result[0].title)
        verify(songDao).getAllSongs()
    }

    @Test
    fun `getSongById returns correct song`() = runTest {
        `when`(songDao.getSongById("test-id-1")).thenReturn(testSong)

        val result = songRepository.getSongById("test-id-1")

        assertNotNull(result)
        assertEquals("Test Song", result?.title)
        verify(songDao).getSongById("test-id-1")
    }

    @Test
    fun `getSongById returns null for non-existent id`() = runTest {
        `when`(songDao.getSongById("non-existent")).thenReturn(null)

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
        `when`(songDao.getSongById("test-id-1")).thenReturn(songNotFavorite)

        songRepository.toggleFavorite("test-id-1")

        verify(songDao).updateFavorite("test-id-1", true)
    }

    @Test
    fun `toggleFavorite unfavorites when already favorite`() = runTest {
        val songFavorite = testSong.copy(isFavorite = true)
        `when`(songDao.getSongById("test-id-1")).thenReturn(songFavorite)

        songRepository.toggleFavorite("test-id-1")

        verify(songDao).updateFavorite("test-id-1", false)
    }

    @Test
    fun `getFavoriteSongs returns only favorite songs`() = runTest {
        val favoriteSongs = listOf(testSong.copy(isFavorite = true))
        `when`(songDao.getFavoriteSongs()).thenReturn(flowOf(favoriteSongs))

        val result = songRepository.getFavoriteSongs().first()

        assertEquals(1, result.size)
        assertTrue(result[0].isFavorite)
    }

    @Test
    fun `searchSongs returns matching songs`() = runTest {
        `when`(songDao.searchSongs("Test")).thenReturn(flowOf(listOf(testSong)))

        val result = songRepository.searchSongs("Test").first()

        assertEquals(1, result.size)
        assertTrue(result[0].title.contains("Test"))
    }

    @Test
    fun `getSongsByGroup returns songs in group`() = runTest {
        val songsInGroup = listOf(testSong.copy(groupId = "group-1"))
        `when`(songDao.getSongsByGroup("group-1")).thenReturn(flowOf(songsInGroup))

        val result = songRepository.getSongsByGroup("group-1").first()

        assertEquals(1, result.size)
        assertEquals("group-1", result[0].groupId)
    }

    @Test
    fun `recordPlay increments play count and records history`() = runTest {
        songRepository.recordPlay("test-id-1", 60000L)

        verify(songDao).incrementPlayCount(eq("test-id-1"), anyLong())
        verify(playHistoryDao).insertPlayHistory(any())
    }

    @Test
    fun `updateCover updates song cover path`() = runTest {
        songRepository.updateCover("test-id-1", "/new/cover.jpg")

        verify(songDao).updateCover("test-id-1", "/new/cover.jpg")
    }

    @Test
    fun `getSongCount returns correct count`() = runTest {
        `when`(songDao.getSongCount()).thenReturn(5)

        val count = songRepository.getSongCount()

        assertEquals(5, count)
    }

    @Test
    fun `getTotalDuration returns sum of all durations`() = runTest {
        `when`(songDao.getTotalDuration()).thenReturn(600000L)

        val duration = songRepository.getTotalDuration()

        assertEquals(600000L, duration)
    }
}
