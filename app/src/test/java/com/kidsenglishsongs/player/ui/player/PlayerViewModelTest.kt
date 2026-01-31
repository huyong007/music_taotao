package com.kidsenglishsongs.player.ui.player

import com.kidsenglishsongs.player.data.entity.SongEntity
import com.kidsenglishsongs.player.data.repository.SongRepository
import com.kidsenglishsongs.player.player.PlaybackState
import com.kidsenglishsongs.player.player.RepeatMode
import com.kidsenglishsongs.player.player.controller.PlayerController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerViewModelTest {

    @Mock
    private lateinit var playerController: PlayerController

    @Mock
    private lateinit var songRepository: SongRepository

    private lateinit var viewModel: PlayerViewModel

    private val testDispatcher = StandardTestDispatcher()

    private val testSong = SongEntity(
        id = "test-id",
        title = "Test Song",
        filePath = "/path/to/song.mp3",
        duration = 180000L,
        isFavorite = false,
        createdAt = System.currentTimeMillis()
    )

    private val playbackStateFlow = MutableStateFlow(PlaybackState())
    private val playlistFlow = MutableStateFlow<List<SongEntity>>(emptyList())
    private val sleepTimerFlow = MutableStateFlow<Long?>(null)

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        `when`(playerController.playbackState).thenReturn(playbackStateFlow)
        `when`(playerController.currentPlaylist).thenReturn(playlistFlow)
        `when`(playerController.sleepTimerRemaining).thenReturn(sleepTimerFlow)

        viewModel = PlayerViewModel(playerController, songRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `play calls playerController play`() {
        viewModel.play()

        verify(playerController).play()
    }

    @Test
    fun `pause calls playerController pause`() {
        viewModel.pause()

        verify(playerController).pause()
    }

    @Test
    fun `togglePlayPause calls playerController togglePlayPause`() {
        viewModel.togglePlayPause()

        verify(playerController).togglePlayPause()
    }

    @Test
    fun `seekTo calls playerController seekTo`() {
        viewModel.seekTo(5000L)

        verify(playerController).seekTo(5000L)
    }

    @Test
    fun `seekToProgress calculates position and seeks`() {
        playbackStateFlow.value = PlaybackState(duration = 100000L)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.seekToProgress(0.5f)

        verify(playerController).seekTo(50000L)
    }

    @Test
    fun `seekToNext calls playerController seekToNext`() {
        viewModel.seekToNext()

        verify(playerController).seekToNext()
    }

    @Test
    fun `seekToPrevious calls playerController seekToPrevious`() {
        viewModel.seekToPrevious()

        verify(playerController).seekToPrevious()
    }

    @Test
    fun `toggleRepeatMode calls playerController toggleRepeatMode`() {
        viewModel.toggleRepeatMode()

        verify(playerController).toggleRepeatMode()
    }

    @Test
    fun `setRepeatMode calls playerController setRepeatMode`() {
        viewModel.setRepeatMode(RepeatMode.ONE)

        verify(playerController).setRepeatMode(RepeatMode.ONE)
    }

    @Test
    fun `toggleShuffle calls playerController toggleShuffle`() {
        viewModel.toggleShuffle()

        verify(playerController).toggleShuffle()
    }

    @Test
    fun `showSleepTimerDialog sets dialog visibility to true`() {
        viewModel.showSleepTimerDialog()

        assertTrue(viewModel.showSleepTimerDialog.value)
    }

    @Test
    fun `hideSleepTimerDialog sets dialog visibility to false`() {
        viewModel.showSleepTimerDialog()
        viewModel.hideSleepTimerDialog()

        assertFalse(viewModel.showSleepTimerDialog.value)
    }

    @Test
    fun `setSleepTimer calls playerController and hides dialog`() {
        viewModel.showSleepTimerDialog()
        viewModel.setSleepTimer(30)

        verify(playerController).setSleepTimer(30)
        assertFalse(viewModel.showSleepTimerDialog.value)
    }

    @Test
    fun `cancelSleepTimer calls playerController cancelSleepTimer`() {
        viewModel.cancelSleepTimer()

        verify(playerController).cancelSleepTimer()
    }

    @Test
    fun `playSong calls playerController playSong`() {
        viewModel.playSong(testSong)

        verify(playerController).playSong(testSong)
    }

    @Test
    fun `setPlaylist calls playerController setPlaylist`() {
        val songs = listOf(testSong)
        viewModel.setPlaylist(songs, 0)

        verify(playerController).setPlaylist(songs, 0)
    }

    @Test
    fun `addToQueue calls playerController addToQueue`() {
        viewModel.addToQueue(testSong)

        verify(playerController).addToQueue(testSong)
    }

    @Test
    fun `playbackState reflects playerController state`() = runTest {
        val newState = PlaybackState(isPlaying = true, currentSongId = "test-id")
        playbackStateFlow.value = newState

        assertEquals(newState, viewModel.playbackState.value)
    }
}
