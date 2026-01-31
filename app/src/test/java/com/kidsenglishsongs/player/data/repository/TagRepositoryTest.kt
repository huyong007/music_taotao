package com.kidsenglishsongs.player.data.repository

import com.kidsenglishsongs.player.data.dao.TagDao
import com.kidsenglishsongs.player.data.entity.TagEntity
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class TagRepositoryTest {

    @Mock
    private lateinit var tagDao: TagDao

    private lateinit var tagRepository: TagRepository

    private val testTag = TagEntity(
        id = "tag-1",
        name = "快节奏",
        color = "#4FC3F7"
    )

    private val testTags = listOf(
        testTag,
        TagEntity(
            id = "tag-2",
            name = "睡前",
            color = "#81C784"
        )
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        tagRepository = TagRepository(tagDao)
    }

    @Test
    fun `getAllTags returns all tags from dao`() = runTest {
        `when`(tagDao.getAllTags()).thenReturn(flowOf(testTags))

        val result = tagRepository.getAllTags().first()

        assertEquals(2, result.size)
        assertEquals("快节奏", result[0].name)
    }

    @Test
    fun `getTagById returns correct tag`() = runTest {
        `when`(tagDao.getTagById("tag-1")).thenReturn(testTag)

        val result = tagRepository.getTagById("tag-1")

        assertNotNull(result)
        assertEquals("快节奏", result?.name)
        assertEquals("#4FC3F7", result?.color)
    }

    @Test
    fun `getTagsForSong returns tags associated with song`() = runTest {
        `when`(tagDao.getTagsForSong("song-1")).thenReturn(flowOf(testTags))

        val result = tagRepository.getTagsForSong("song-1").first()

        assertEquals(2, result.size)
    }

    @Test
    fun `insertTag calls dao insert`() = runTest {
        tagRepository.insertTag(testTag)

        verify(tagDao).insertTag(testTag)
    }

    @Test
    fun `updateTag calls dao update`() = runTest {
        tagRepository.updateTag(testTag)

        verify(tagDao).updateTag(testTag)
    }

    @Test
    fun `deleteTag calls dao delete`() = runTest {
        tagRepository.deleteTag(testTag)

        verify(tagDao).deleteTag(testTag)
    }

    @Test
    fun `getTagCount returns correct count`() = runTest {
        `when`(tagDao.getTagCount()).thenReturn(5)

        val count = tagRepository.getTagCount()

        assertEquals(5, count)
    }
}
