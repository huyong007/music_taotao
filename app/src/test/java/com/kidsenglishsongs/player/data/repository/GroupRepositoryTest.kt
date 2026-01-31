package com.kidsenglishsongs.player.data.repository

import com.kidsenglishsongs.player.data.dao.GroupDao
import com.kidsenglishsongs.player.data.entity.GroupEntity
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class GroupRepositoryTest {

    @Mock
    private lateinit var groupDao: GroupDao

    private lateinit var groupRepository: GroupRepository

    private val testGroup = GroupEntity(
        id = "group-1",
        name = "儿歌",
        sortOrder = 0,
        createdAt = System.currentTimeMillis()
    )

    private val testGroups = listOf(
        testGroup,
        GroupEntity(
            id = "group-2",
            name = "字母歌",
            sortOrder = 1,
            createdAt = System.currentTimeMillis()
        )
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        groupRepository = GroupRepository(groupDao)
    }

    @Test
    fun `getAllGroups returns all groups from dao`() = runTest {
        `when`(groupDao.getAllGroups()).thenReturn(flowOf(testGroups))

        val result = groupRepository.getAllGroups().first()

        assertEquals(2, result.size)
        assertEquals("儿歌", result[0].name)
        verify(groupDao).getAllGroups()
    }

    @Test
    fun `getGroupById returns correct group`() = runTest {
        `when`(groupDao.getGroupById("group-1")).thenReturn(testGroup)

        val result = groupRepository.getGroupById("group-1")

        assertNotNull(result)
        assertEquals("儿歌", result?.name)
    }

    @Test
    fun `insertGroup calls dao insert`() = runTest {
        groupRepository.insertGroup(testGroup)

        verify(groupDao).insertGroup(testGroup)
    }

    @Test
    fun `updateGroup calls dao update`() = runTest {
        groupRepository.updateGroup(testGroup)

        verify(groupDao).updateGroup(testGroup)
    }

    @Test
    fun `deleteGroup calls dao delete`() = runTest {
        groupRepository.deleteGroup(testGroup)

        verify(groupDao).deleteGroup(testGroup)
    }

    @Test
    fun `getGroupCount returns correct count`() = runTest {
        `when`(groupDao.getGroupCount()).thenReturn(3)

        val count = groupRepository.getGroupCount()

        assertEquals(3, count)
    }

    @Test
    fun `getNextSortOrder returns max plus one`() = runTest {
        `when`(groupDao.getMaxSortOrder()).thenReturn(5)

        val nextOrder = groupRepository.getNextSortOrder()

        assertEquals(6, nextOrder)
    }

    @Test
    fun `getNextSortOrder returns 0 when no groups exist`() = runTest {
        `when`(groupDao.getMaxSortOrder()).thenReturn(null)

        val nextOrder = groupRepository.getNextSortOrder()

        assertEquals(0, nextOrder)
    }
}
