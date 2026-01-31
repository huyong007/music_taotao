package com.kidsenglishsongs.player.data.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kidsenglishsongs.player.data.dao.*
import com.kidsenglishsongs.player.data.entity.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {

    private lateinit var database: AppDatabase
    private lateinit var songDao: SongDao
    private lateinit var groupDao: GroupDao
    private lateinit var tagDao: TagDao
    private lateinit var playlistDao: PlaylistDao
    private lateinit var playHistoryDao: PlayHistoryDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        songDao = database.songDao()
        groupDao = database.groupDao()
        tagDao = database.tagDao()
        playlistDao = database.playlistDao()
        playHistoryDao = database.playHistoryDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        database.close()
    }

    // Song DAO Tests
    @Test
    fun insertAndGetSong() = runBlocking {
        val song = SongEntity(
            id = "song-1",
            title = "Test Song",
            filePath = "/path/to/song.mp3",
            duration = 180000L,
            createdAt = System.currentTimeMillis()
        )

        songDao.insertSong(song)
        val retrieved = songDao.getSongById("song-1")

        assertNotNull(retrieved)
        assertEquals("Test Song", retrieved?.title)
    }

    @Test
    fun getAllSongs() = runBlocking {
        val songs = listOf(
            SongEntity("1", "Song A", "/a.mp3", 100000, createdAt = 1000),
            SongEntity("2", "Song B", "/b.mp3", 200000, createdAt = 2000)
        )
        songDao.insertSongs(songs)

        val allSongs = songDao.getAllSongs().first()

        assertEquals(2, allSongs.size)
    }

    @Test
    fun deleteSong() = runBlocking {
        val song = SongEntity("1", "Test", "/test.mp3", 100000, createdAt = 1000)
        songDao.insertSong(song)

        songDao.deleteSong(song)
        val retrieved = songDao.getSongById("1")

        assertNull(retrieved)
    }

    @Test
    fun updateFavorite() = runBlocking {
        val song = SongEntity("1", "Test", "/test.mp3", 100000, isFavorite = false, createdAt = 1000)
        songDao.insertSong(song)

        songDao.updateFavorite("1", true)
        val updated = songDao.getSongById("1")

        assertTrue(updated?.isFavorite == true)
    }

    @Test
    fun getFavoriteSongs() = runBlocking {
        songDao.insertSongs(listOf(
            SongEntity("1", "Song A", "/a.mp3", 100000, isFavorite = true, createdAt = 1000),
            SongEntity("2", "Song B", "/b.mp3", 200000, isFavorite = false, createdAt = 2000),
            SongEntity("3", "Song C", "/c.mp3", 300000, isFavorite = true, createdAt = 3000)
        ))

        val favorites = songDao.getFavoriteSongs().first()

        assertEquals(2, favorites.size)
        assertTrue(favorites.all { it.isFavorite })
    }

    @Test
    fun searchSongs() = runBlocking {
        songDao.insertSongs(listOf(
            SongEntity("1", "ABC Song", "/a.mp3", 100000, createdAt = 1000),
            SongEntity("2", "Twinkle Star", "/b.mp3", 200000, createdAt = 2000),
            SongEntity("3", "ABC Alphabet", "/c.mp3", 300000, createdAt = 3000)
        ))

        val results = songDao.searchSongs("ABC").first()

        assertEquals(2, results.size)
    }

    @Test
    fun incrementPlayCount() = runBlocking {
        val song = SongEntity("1", "Test", "/test.mp3", 100000, playCount = 0, createdAt = 1000)
        songDao.insertSong(song)

        songDao.incrementPlayCount("1")
        songDao.incrementPlayCount("1")
        val updated = songDao.getSongById("1")

        assertEquals(2, updated?.playCount)
    }

    // Group DAO Tests
    @Test
    fun insertAndGetGroup() = runBlocking {
        val group = GroupEntity("g1", "儿歌", null, 0, System.currentTimeMillis())

        groupDao.insertGroup(group)
        val retrieved = groupDao.getGroupById("g1")

        assertNotNull(retrieved)
        assertEquals("儿歌", retrieved?.name)
    }

    @Test
    fun getAllGroups() = runBlocking {
        groupDao.insertGroup(GroupEntity("g1", "儿歌", null, 0, 1000))
        groupDao.insertGroup(GroupEntity("g2", "字母", null, 1, 2000))

        val allGroups = groupDao.getAllGroups().first()

        assertEquals(2, allGroups.size)
        // Should be sorted by sortOrder
        assertEquals("儿歌", allGroups[0].name)
    }

    @Test
    fun getMaxSortOrder() = runBlocking {
        groupDao.insertGroup(GroupEntity("g1", "儿歌", null, 0, 1000))
        groupDao.insertGroup(GroupEntity("g2", "字母", null, 5, 2000))
        groupDao.insertGroup(GroupEntity("g3", "数字", null, 3, 3000))

        val maxOrder = groupDao.getMaxSortOrder()

        assertEquals(5, maxOrder)
    }

    // Tag DAO Tests
    @Test
    fun insertAndGetTag() = runBlocking {
        val tag = TagEntity("t1", "快节奏", "#FF0000")

        tagDao.insertTag(tag)
        val retrieved = tagDao.getTagById("t1")

        assertNotNull(retrieved)
        assertEquals("快节奏", retrieved?.name)
        assertEquals("#FF0000", retrieved?.color)
    }

    @Test
    fun getAllTags() = runBlocking {
        tagDao.insertTag(TagEntity("t1", "快节奏", "#FF0000"))
        tagDao.insertTag(TagEntity("t2", "睡前", "#00FF00"))

        val allTags = tagDao.getAllTags().first()

        assertEquals(2, allTags.size)
    }

    // Playlist DAO Tests
    @Test
    fun insertAndGetPlaylist() = runBlocking {
        val playlist = PlaylistEntity("p1", "我的列表", System.currentTimeMillis())

        playlistDao.insertPlaylist(playlist)
        val retrieved = playlistDao.getPlaylistById("p1")

        assertNotNull(retrieved)
        assertEquals("我的列表", retrieved?.name)
    }

    @Test
    fun addSongToPlaylist() = runBlocking {
        // Create song and playlist
        songDao.insertSong(SongEntity("s1", "Song", "/s.mp3", 100000, createdAt = 1000))
        playlistDao.insertPlaylist(PlaylistEntity("p1", "Playlist", 1000))

        // Add song to playlist
        playlistDao.insertPlaylistSongCrossRef(PlaylistSongCrossRef("p1", "s1", 0))

        val songsInPlaylist = playlistDao.getSongsInPlaylist("p1").first()

        assertEquals(1, songsInPlaylist.size)
        assertEquals("Song", songsInPlaylist[0].title)
    }

    @Test
    fun removeSongFromPlaylist() = runBlocking {
        songDao.insertSong(SongEntity("s1", "Song", "/s.mp3", 100000, createdAt = 1000))
        playlistDao.insertPlaylist(PlaylistEntity("p1", "Playlist", 1000))
        playlistDao.insertPlaylistSongCrossRef(PlaylistSongCrossRef("p1", "s1", 0))

        playlistDao.removeSongFromPlaylist("p1", "s1")

        val songsInPlaylist = playlistDao.getSongsInPlaylist("p1").first()
        assertTrue(songsInPlaylist.isEmpty())
    }

    // Play History DAO Tests
    @Test
    fun insertPlayHistory() = runBlocking {
        val history = PlayHistoryEntity("h1", "s1", System.currentTimeMillis(), 60000)

        playHistoryDao.insertPlayHistory(history)
        val allHistory = playHistoryDao.getAllPlayHistory().first()

        assertEquals(1, allHistory.size)
    }

    @Test
    fun getTotalPlayDuration() = runBlocking {
        playHistoryDao.insertPlayHistory(PlayHistoryEntity("h1", "s1", 1000, 60000))
        playHistoryDao.insertPlayHistory(PlayHistoryEntity("h2", "s2", 2000, 90000))

        val totalDuration = playHistoryDao.getTotalPlayDuration()

        assertEquals(150000L, totalDuration)
    }

    @Test
    fun clearAllPlayHistory() = runBlocking {
        playHistoryDao.insertPlayHistory(PlayHistoryEntity("h1", "s1", 1000, 60000))
        playHistoryDao.insertPlayHistory(PlayHistoryEntity("h2", "s2", 2000, 90000))

        playHistoryDao.clearAllPlayHistory()
        val allHistory = playHistoryDao.getAllPlayHistory().first()

        assertTrue(allHistory.isEmpty())
    }

    // Song-Tag relationship tests
    @Test
    fun addTagToSong() = runBlocking {
        songDao.insertSong(SongEntity("s1", "Song", "/s.mp3", 100000, createdAt = 1000))
        tagDao.insertTag(TagEntity("t1", "Tag", "#FF0000"))

        songDao.insertSongTagCrossRef(SongTagCrossRef("s1", "t1"))

        val tags = tagDao.getTagsForSong("s1").first()
        assertEquals(1, tags.size)
        assertEquals("Tag", tags[0].name)
    }

    @Test
    fun getSongsByTag() = runBlocking {
        songDao.insertSong(SongEntity("s1", "Song 1", "/s1.mp3", 100000, createdAt = 1000))
        songDao.insertSong(SongEntity("s2", "Song 2", "/s2.mp3", 200000, createdAt = 2000))
        tagDao.insertTag(TagEntity("t1", "Tag", "#FF0000"))

        songDao.insertSongTagCrossRef(SongTagCrossRef("s1", "t1"))

        val songs = songDao.getSongsByTag("t1").first()
        assertEquals(1, songs.size)
        assertEquals("Song 1", songs[0].title)
    }
}
