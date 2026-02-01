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
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class ParentControlViewModelTest {

    private lateinit var songRepository: SongRepository
    private lateinit var groupRepository: GroupRepository
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
        Dispatchers.setMain(testDispatcher)

        songRepository = mock {
            on { getAllSongs() } doReturn flowOf(listOf(testSong))
            onBlocking { getSongCount() } doReturn 1
            onBlocking { getTotalDuration() } doReturn 180000L
            onBlocking { getTotalPlayCount() } doReturn 5
        }

        groupRepository = mock {
            on { getAllGroups() } doReturn flowOf(listOf(testGroup))
        }

        tagRepository = mock {
            on { getAllTags() } doReturn flowOf(listOf(testTag))
        }

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
        whenever(groupRepository.getNextSortOrder()).thenReturn(0)

        viewModel.showCreateGroupDialog()
        viewModel.createGroup("新分组")

        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.showCreateGroupDialog.value)
        verify(groupRepository).insertGroup(any())
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
        viewModel.createTag("新标签", "#FF5500")

        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.showCreateTagDialog.value)
        verify(tagRepository).insertTag(any())
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
    fun `deleteSong deletes the song and hides dialog`() = runTest {
        viewModel.showDeleteConfirmDialog(testSong)
        viewModel.deleteSong(testSong)

        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(viewModel.songToDelete.value)
        verify(songRepository).deleteSong(testSong)
    }

    @Test
    fun `statistics is loaded on init`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val stats = viewModel.statistics.value
        assertNotNull(stats)
    }
}
