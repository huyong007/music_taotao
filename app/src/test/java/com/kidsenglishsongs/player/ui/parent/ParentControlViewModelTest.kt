package com.kidsenglishsongs.player.ui.parent

import com.kidsenglishsongs.player.data.entity.GroupEntity
import com.kidsenglishsongs.player.data.entity.SongEntity
import com.kidsenglishsongs.player.data.entity.TagEntity
import com.kidsenglishsongs.player.data.repository.GroupRepository
import com.kidsenglishsongs.player.data.repository.SongRepository
import com.kidsenglishsongs.player.data.repository.TagRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

@OptIn(ExperimentalCoroutinesApi::class)
class ParentControlViewModelTest {

    @Mock
    private lateinit var songRepository: SongRepository

    @Mock
    private lateinit var groupRepository: GroupRepository

    @Mock
    private lateinit var tagRepository: TagRepository

    private lateinit var viewModel: ParentControlViewModel

    private val testDispatcher = StandardTestDispatcher()

    private val testSong = SongEntity(
        id = "test-id",
        title = "Test Song",
        filePath = "/path/to/song.mp3",
        duration = 180000L,
        playCount = 5,
        createdAt = System.currentTimeMillis()
    )

    private val testGroup = GroupEntity(
        id = "group-1",
        name = "儿歌",
        sortOrder = 0,
        createdAt = System.currentTimeMillis()
    )

    private val testTag = TagEntity(
        id = "tag-1",
        name = "快节奏",
        color = "#4FC3F7"
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        `when`(songRepository.getAllSongs()).thenReturn(flowOf(listOf(testSong)))
        `when`(groupRepository.getAllGroups()).thenReturn(flowOf(listOf(testGroup)))
        `when`(tagRepository.getAllTags()).thenReturn(flowOf(listOf(testTag)))
        `when`(songRepository.getSongCount()).thenReturn(1)
        `when`(songRepository.getTotalDuration()).thenReturn(180000L)
        `when`(songRepository.getTotalPlayCount()).thenReturn(5)

        viewModel = ParentControlViewModel(songRepository, groupRepository, tagRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `showCreateGroupDialog sets dialog visibility to true`() {
        viewModel.showCreateGroupDialog()

        assertTrue(viewModel.showCreateGroupDialog.value)
    }

    @Test
    fun `hideCreateGroupDialog sets dialog visibility to false`() {
        viewModel.showCreateGroupDialog()
        viewModel.hideCreateGroupDialog()

        assertFalse(viewModel.showCreateGroupDialog.value)
    }

    @Test
    fun `createGroup creates group and hides dialog`() = runTest {
        `when`(groupRepository.getNextSortOrder()).thenReturn(0)

        viewModel.showCreateGroupDialog()
        viewModel.createGroup("新分组")

        testDispatcher.scheduler.advanceUntilIdle()

        verify(groupRepository).insertGroup(any())
        assertFalse(viewModel.showCreateGroupDialog.value)
    }

    @Test
    fun `showCreateTagDialog sets dialog visibility to true`() {
        viewModel.showCreateTagDialog()

        assertTrue(viewModel.showCreateTagDialog.value)
    }

    @Test
    fun `hideCreateTagDialog sets dialog visibility to false`() {
        viewModel.showCreateTagDialog()
        viewModel.hideCreateTagDialog()

        assertFalse(viewModel.showCreateTagDialog.value)
    }

    @Test
    fun `createTag creates tag and hides dialog`() = runTest {
        viewModel.showCreateTagDialog()
        viewModel.createTag("新标签", "#FF0000")

        testDispatcher.scheduler.advanceUntilIdle()

        verify(tagRepository).insertTag(any())
        assertFalse(viewModel.showCreateTagDialog.value)
    }

    @Test
    fun `showDeleteConfirmDialog sets song to delete`() {
        viewModel.showDeleteConfirmDialog(testSong)

        assertEquals(testSong, viewModel.songToDelete.value)
    }

    @Test
    fun `hideDeleteConfirmDialog clears song to delete`() {
        viewModel.showDeleteConfirmDialog(testSong)
        viewModel.hideDeleteConfirmDialog()

        assertNull(viewModel.songToDelete.value)
    }

    @Test
    fun `deleteSong deletes song and hides dialog`() = runTest {
        viewModel.showDeleteConfirmDialog(testSong)
        viewModel.deleteSong(testSong)

        testDispatcher.scheduler.advanceUntilIdle()

        verify(songRepository).deleteSong(testSong)
        assertNull(viewModel.songToDelete.value)
    }

    @Test
    fun `statistics is loaded on init`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val stats = viewModel.statistics.value
        assertEquals(1, stats.songCount)
        assertEquals(180000L, stats.totalDuration)
        assertEquals(5, stats.totalPlayCount)
    }
}
